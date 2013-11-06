package com.bulgogi.bricks.model;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.graphics.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Pattern {
	static Bitmap mProcessedBitmap;
	static IplImage mProcessedImage;
	
	public Pattern() {
		mProcessedBitmap = Bitmap.createBitmap(Constant.GRID_SIZE, Constant.GRID_SIZE, Bitmap.Config.ARGB_8888);
		mProcessedImage = IplImage.create(Constant.GRID_SIZE, Constant.GRID_SIZE, IPL_DEPTH_8U, 4);
	}
	
	public void cleanup() {
		cvReleaseImage(mProcessedImage);
	}
	
	public Bitmap getProcessedBitmap() {
		return mProcessedBitmap;
	}
	
	public IplImage getProcessedImage() {
		return mProcessedImage;
	}
	
	public void iplImageToBitmap() {
		OpenCV.IplImageToBitmap(mProcessedImage, mProcessedBitmap);
	}
}
