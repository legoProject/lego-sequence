package com.bulgogi.bricks.controller;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.graphics.*;
import android.hardware.Camera;
import android.util.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.detector.*;
import com.bulgogi.bricks.event.Events.SoundRelease;
import com.bulgogi.bricks.event.Events.SoundSwitching;
import com.bulgogi.bricks.model.*;
import com.bulgogi.bricks.sound.*;
import com.bulgogi.bricks.utils.*;
import com.bulgogi.bricks.view.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import de.greenrobot.event.*;

public class FrameCallback implements Camera.PreviewCallback {
	private final String TAG = FrameCallback.class.getSimpleName();
	
	private final CvScalar HSV_BLUE_MIN = new CvScalar(100, 100, 100, 0);
	private final CvScalar HSV_BLUE_MAX = new CvScalar(120, 255, 255, 0);
	private final CvScalar HSV_GREEN_MIN = new CvScalar(60, 30, 30, 0);
	private final CvScalar HSV_GREEN_MAX = new CvScalar(90, 255, 255, 0);
	
	private OverlayView mOverlayView;
	private Bitmap mFrameBitmap;
	private IplImage mFrameImage;
	private Plate mPlate;
	private Pattern mPattern;
	private SparseArray<Sequence> mSequences;
	private Sequence mPreviousSequence;
	private Sequence mCurrentSequence;
	private Alarm mAlarm = new Alarm();
	
	public FrameCallback(OverlayView overlayView, Plate plate, Pattern pattern) {
		mOverlayView = overlayView;
		mPlate = plate;
		mPattern = pattern;
		
		mSequences = new SparseArray<Sequence>();
		mSequences.put(Constant.SEQUENCE_TYPE.BLUE.ordinal(), 
				new Sequence(new PlateDetector(HSV_BLUE_MIN, HSV_BLUE_MAX), new PatternDetector(HSV_BLUE_MIN, HSV_BLUE_MAX)));
		mSequences.put(Constant.SEQUENCE_TYPE.GREEN.ordinal(), 
				new Sequence(new PlateDetector(HSV_GREEN_MIN, HSV_GREEN_MAX), new PatternDetector(HSV_GREEN_MIN, HSV_GREEN_MAX)));
//		mSequences.put(mSequenceType.CYAN.ordinal(), 
//				new Sequence(new PlateDetector(HSV_GREEN_MIN, HSV_GREEN_MAX), new PatternDetector(HSV_GREEN_MIN, HSV_GREEN_MAX)));
	}
	
	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		try {
            Camera.Size size = camera.getParameters().getPreviewSize();
			if (mFrameBitmap == null) {
				mFrameBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
			}

			if (mFrameImage == null) {
				mFrameImage = IplImage.create(size.width, size.height, IPL_DEPTH_8U, 4);
			}
			
            processImage(data, size.width, size.height);
            update();
            
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
        		e.printStackTrace();
        }				
	}
	
	private void processImage(final byte[] data, int width, int height) {
		Log.v(TAG, "processImage " + width + " x " + height);
		
		boolean mStopped = true;
		int[] argb = new int[width * height];
		OpenCV.decodeYUV420SP(argb, data, width, height);
		
		mFrameBitmap.setPixels(argb, 0, width, 0, 0, width, height);
		OpenCV.BitmapToIplImage(mFrameBitmap, mFrameImage);
		
		for (int i = 0; i < mSequences.size(); i++) {
			Sequence sequence = mSequences.get(i);
			sequence.getPlateDetector().process(mFrameImage, mPlate.getPreprocessedImage(), mPlate.getProcessedImage());
			if (sequence.isEnabled()) {
				mStopped = false;
				
				if (mAlarm.isAlarmPending()) {
					Log.e(TAG, "ALARM CANCEL! " + i);
					mAlarm.cancelAlarm();
					break;
				}
				
				mCurrentSequence = sequence;
				if (mPreviousSequence != mCurrentSequence) {
					mPreviousSequence = mCurrentSequence;
					
					Log.e(TAG, "POST SWITCHED! " + i);
					sendEventPlateSwitched(i);
				}
				
				break;
			}
		}
		
		if (mStopped && mCurrentSequence != null && !mAlarm.isAlarmPending()) {
			Log.e(TAG, "POST STOPPED!");
			mAlarm.setAlarm(3000);
			mAlarm.setOnAlarmListener(new OnAlarmListener() {
				@Override
				public void onAlarm(Alarm alarm) {
					mCurrentSequence = null;
					mPreviousSequence = null;
					
					EventBus.getDefault().post(SoundRelease.eventOf());		
				}
			});
		}
		
		if (mCurrentSequence != null) {
			mPlate.iplImageToBitmap();

			mCurrentSequence.getPatternDetector().process(mPlate.getProcessedImage(), mPattern.getProcessedImage());
			mPattern.iplImageToBitmap();
		}
	}
	
	private void update() {
		mOverlayView.update(mPlate.getPreprocessedBimtap(), mPlate.getProcessedBitmap(), mPattern.getProcessedBitmap());		
	}
	
	public void cleanup() {
		if (mFrameImage != null) {
			mFrameImage.release();
			mFrameImage = null;
		}
	}
	
	private void sendEventPlateSwitched(int index) {
		SoundSwitching event = null;
		switch (index) {
		case 0:
			 event = SoundSwitching.eventOf(InstrumentType.TONE);
			break;
		case 1:
			event = SoundSwitching.eventOf(InstrumentType.DRUM);
			break;
		case 2:
			event = SoundSwitching.eventOf(InstrumentType.MIX);
			break;
		default:
			event = SoundSwitching.eventOf(InstrumentType.TONE);
			break;
		}
		EventBus.getDefault().post(event);
	}
}
