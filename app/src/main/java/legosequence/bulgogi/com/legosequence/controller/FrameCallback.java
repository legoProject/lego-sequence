package legosequence.bulgogi.com.legosequence.controller;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseArray;

import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;

import de.greenrobot.event.EventBus;
import legosequence.bulgogi.com.legosequence.config.Constant;
import legosequence.bulgogi.com.legosequence.cv.OpenCV;
import legosequence.bulgogi.com.legosequence.detector.PatternDetector;
import legosequence.bulgogi.com.legosequence.detector.PlateDetector;
import legosequence.bulgogi.com.legosequence.event.Events;
import legosequence.bulgogi.com.legosequence.model.Pattern;
import legosequence.bulgogi.com.legosequence.model.Plate;
import legosequence.bulgogi.com.legosequence.model.Sequence;
import legosequence.bulgogi.com.legosequence.sound.InstrumentType;
import legosequence.bulgogi.com.legosequence.utils.Alarm;
import legosequence.bulgogi.com.legosequence.utils.OnAlarmListener;
import legosequence.bulgogi.com.legosequence.view.OverlayView;


public class FrameCallback implements Camera.PreviewCallback {
    private final String TAG = FrameCallback.class.getSimpleName();

    private final CvScalar HSV_BLUE_MIN = new CvScalar(100, 100, 100, 0);
    private final CvScalar HSV_BLUE_MAX = new CvScalar(120, 255, 255, 0);
    private final CvScalar HSV_GREEN_MIN = new CvScalar(60, 30, 30, 0);
    private final CvScalar HSV_GREEN_MAX = new CvScalar(90, 255, 255, 0);
    private final CvScalar HSV_CYAN_MIN = new CvScalar(90, 100, 100, 0);
    private final CvScalar HSV_CYAN_MAX = new CvScalar(100, 255, 255, 0);

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
        mSequences.put(Constant.SEQUENCE_TYPE.CYAN.ordinal(),
                new Sequence(new PlateDetector(HSV_CYAN_MIN, HSV_CYAN_MAX), new PatternDetector(HSV_CYAN_MIN, HSV_CYAN_MAX)));
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

                    if (mCurrentSequence.getPlateDetector().isLargePlate()) {
                        Log.e(TAG, "LARGE PLATE! " + i);
                        mCurrentSequence.getPatternDetector().recreate(Constant.LARGE_CELL_SIZE);
                        mPlate.recreate(width, height, Constant.LARGE_GRID_SIZE);
                        mPattern.recreate(Constant.LARGE_GRID_SIZE);
                    } else {
                        Log.e(TAG, "SMALL PLATE! " + i);
                        mCurrentSequence.getPatternDetector().recreate(Constant.SMALL_CELL_SIZE);
                        mPlate.recreate(width, height, Constant.SMALL_GRID_SIZE);
                        mPattern.recreate(Constant.SMALL_GRID_SIZE);
                    }

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

                    EventBus.getDefault().post(Events.SoundRelease.eventOf());
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
        Events.SoundSwitching event = null;
        switch (index) {
            case 0:
                event = Events.SoundSwitching.eventOf(InstrumentType.TONE);
                break;
            case 1:
                event = Events.SoundSwitching.eventOf(InstrumentType.DRUM);
                break;
            case 2:
                event = Events.SoundSwitching.eventOf(InstrumentType.MIX);
                break;
            default:
                event = Events.SoundSwitching.eventOf(InstrumentType.TONE);
                break;
        }
        EventBus.getDefault().post(event);
    }
}
