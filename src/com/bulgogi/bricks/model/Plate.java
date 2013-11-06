package com.bulgogi.bricks.model;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.graphics.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Plate {
	private Bitmap mPreprocessedBitmap;
	private Bitmap mProcessedBitmap;
	
	private IplImage mPreprocessedImage;
	private IplImage mProcessedImage;

	public Plate(int width, int height) {
		mPreprocessedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		mProcessedBitmap = Bitmap.createBitmap(Constant.GRID_SIZE, Constant.GRID_SIZE, Bitmap.Config.ARGB_8888);
		
		mPreprocessedImage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		mProcessedImage = IplImage.create(Constant.GRID_SIZE, Constant.GRID_SIZE, IPL_DEPTH_8U, 4);
		
		cvSet(mPreprocessedImage, cvScalar(255, 0, 0, 255));
		cvSet(mProcessedImage, cvScalar(255, 0, 0, 255));
	}
	
	public void cleanup() {
		cvReleaseImage(mPreprocessedImage);
		cvReleaseImage(mProcessedImage);
	}
	
	public Bitmap getPreprocessedBimtap() {
		return mPreprocessedBitmap;
	}
	
	public Bitmap getProcessedBitmap() {
		return mProcessedBitmap;
	}
	
	public IplImage getPreprocessedImage() {
		return mPreprocessedImage;
	}
	
	public IplImage getProcessedImage() {
		return mProcessedImage;
	}
	
	public void iplImageToBitmap() {
		OpenCV.IplImageToBitmap(mPreprocessedImage, mPreprocessedBitmap);
		OpenCV.IplImageToBitmap(mProcessedImage, mProcessedBitmap);
	}
}