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
		mProcessedBitmap = Bitmap.createBitmap(Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE, Bitmap.Config.ARGB_8888);
		
		mPreprocessedImage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		mProcessedImage = IplImage.create(Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE, IPL_DEPTH_8U, 4);
	}
	
	public void cleanup() {
		mPreprocessedImage.release();
		mProcessedImage.release();
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
	
	public void recreate(int width, int height, int gridSize) {
		cleanup();
		mPreprocessedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		mProcessedBitmap = Bitmap.createBitmap(gridSize, gridSize, Bitmap.Config.ARGB_8888);
		
		mPreprocessedImage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		mProcessedImage = IplImage.create(gridSize, gridSize, IPL_DEPTH_8U, 4);				
	}
}
