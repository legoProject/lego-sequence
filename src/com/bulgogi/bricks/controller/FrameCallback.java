package com.bulgogi.bricks.controller;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.graphics.*;
import android.hardware.Camera;

import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.detector.*;
import com.bulgogi.bricks.model.*;
import com.bulgogi.bricks.view.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FrameCallback implements Camera.PreviewCallback {
	private final CvScalar HSV_BLUE_MIN = new CvScalar(100, 100, 100, 0);
	private final CvScalar HSV_BLUE_MAX = new CvScalar(120, 255, 255, 0);
	
	private OverlayView mOverlayView;
	private Bitmap mFrameBitmap;
	private IplImage mFrameImage;
	private Plate mPlate;
	private Pattern mPattern;
	private PlateDetector mPlateDetector;
	private PatternDetector mPatternDetector;
	
	public FrameCallback(OverlayView overlayView, Plate plate, Pattern pattern) {
		mOverlayView = overlayView;
		mPlate = plate;
		mPattern = pattern;
		mPlateDetector = new PlateDetector(HSV_BLUE_MIN, HSV_BLUE_MAX);
		mPatternDetector = new PatternDetector(HSV_BLUE_MIN, HSV_BLUE_MAX);
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
		int[] argb = new int[width * height];
		OpenCV.decodeYUV420SP(argb, data, width, height);
		
		mFrameBitmap.setPixels(argb, 0, width, 0, 0, width, height);
		OpenCV.BitmapToIplImage(mFrameBitmap, mFrameImage);
		
		mPlateDetector.process(mFrameImage, mPlate.getPreprocessedImage(), mPlate.getProcessedImage());
		mPlate.iplImageToBitmap();
		
		mPatternDetector.process(mPlate.getProcessedImage(), mPattern.getProcessedImage());
		mPattern.iplImageToBitmap();
	}
	
	private void update() {
		mOverlayView.update(mPlate.getPreprocessedBimtap(), mPlate.getProcessedBitmap(), mPattern.getProcessedBitmap());		
	}
}
