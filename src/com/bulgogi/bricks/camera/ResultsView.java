package com.bulgogi.bricks.camera;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.content.*;
import android.graphics.*;
import android.hardware.Camera;
import android.util.*;
import android.view.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.detector.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ResultsView extends View implements Camera.PreviewCallback {
	private final String TAG = ResultsView.class.getSimpleName();

	static class Plate {
		static Bitmap mPreProcessedBitmap;
		static IplImage mPreProcessedImage;
	    
		static Bitmap mProcessedBitmap;
		static IplImage mProcessedImage;
		
		static void init(int width, int height) {
			Plate.mPreProcessedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Plate.mPreProcessedImage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
			
			Plate.mProcessedBitmap = Bitmap.createBitmap(Constant.GRID_SIZE, Constant.GRID_SIZE, Bitmap.Config.ARGB_8888);
			Plate.mProcessedImage = IplImage.create(Constant.GRID_SIZE, Constant.GRID_SIZE, IPL_DEPTH_8U, 4);
		}
	}
	
	static class Pattern {
		static Bitmap mProcessedBitmap;
		static IplImage mProcessedImage;
		
		static void init(int width, int height) {
			Pattern.mProcessedBitmap = Bitmap.createBitmap(Constant.GRID_SIZE, Constant.GRID_SIZE, Bitmap.Config.ARGB_8888);
			Pattern.mProcessedImage = IplImage.create(Constant.GRID_SIZE, Constant.GRID_SIZE, IPL_DEPTH_8U, 4);
		}
	}
	
	private PlateDetector mPlateDetector;
	private PatternDetector mPatternDetector;
	
	private Bitmap mSourceBitmap;
	private IplImage mSourceImage;
	
    private int mWidth;
	private int mHeight;
	
	private Paint mPaint;
	
	public ResultsView(Context context) {
		super(context);
		
		mPlateDetector = new PlateDetector(new CvScalar(100, 100, 100, 0), new CvScalar(120, 255, 255, 0));
		mPatternDetector = new PatternDetector(new CvScalar(100, 100, 100, 0), new CvScalar(120, 255, 255, 0));
		mPaint = new Paint();
	}

	@Override
	public void onPreviewFrame(final byte[] data, final Camera camera) {
		try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (mWidth != size.width || mHeight != size.height) {
    				cleanup();
    				init(size.width, size.height);
    			}
            
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        		e.printStackTrace();
        }		
	}

	private void init(int width, int height) {
		mWidth = width;
		mHeight = height;
		
		mSourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		mSourceImage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		
		Plate.init(width, height);
		Pattern.init(width, height);
	}
	
	private void cleanup() {
		if (mSourceImage != null) {
			cvReleaseImage(mSourceImage);
		}
		
		if (Plate.mPreProcessedImage != null) {
			cvReleaseImage(Plate.mPreProcessedImage);
		}
		
		if (Plate.mProcessedImage != null) {
			cvReleaseImage(Plate.mProcessedImage);
		}
		
		if (Pattern.mProcessedImage != null) {
			cvReleaseImage(Pattern.mProcessedImage);
		}
	}
	
	private void processImage(final byte[] data, int width, int height) {
		int[] abgr = new int[width * height];
		OpenCV.decodeYUV420SP(abgr, data, width, height);
		mSourceBitmap.setPixels(abgr, 0, width, 0, 0, width, height);
		
		OpenCV.BitmapToIplImage(mSourceBitmap, mSourceImage);
		
		mPlateDetector.process(mSourceImage, Plate.mPreProcessedImage, Plate.mProcessedImage);
		OpenCV.IplImageToBitmap(Plate.mPreProcessedImage, Plate.mPreProcessedBitmap);
		OpenCV.IplImageToBitmap(Plate.mProcessedImage, Plate.mProcessedBitmap);
		
		mPatternDetector.process(Plate.mProcessedImage, Pattern.mProcessedImage);
		OpenCV.IplImageToBitmap(Pattern.mProcessedImage, Pattern.mProcessedBitmap);
		
		postInvalidate();
	}

    @Override
	protected void onDraw(Canvas canvas) {
		if (Plate.mPreProcessedBitmap != null) {
			canvas.drawBitmap(Plate.mPreProcessedBitmap, 
					new Rect(0, 0, mWidth, mHeight), 
					new RectF(0, 0, 320, 240), mPaint);
		}
		
		if (Plate.mProcessedBitmap != null) {
			canvas.drawBitmap(Plate.mProcessedBitmap, 
					new Rect(0, 0, Constant.GRID_SIZE, Constant.GRID_SIZE), 
					new RectF(getWidth() - Constant.GRID_SIZE, 0, getWidth(), Constant.GRID_SIZE), mPaint);
		}

		if (Pattern.mProcessedBitmap != null) {
			canvas.drawBitmap(Pattern.mProcessedBitmap, 
					new Rect(0, 0, Constant.GRID_SIZE, Constant.GRID_SIZE), 
					new RectF(0, getHeight() - Constant.GRID_SIZE, Constant.GRID_SIZE, getHeight()), mPaint);
		}
		
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(20);
		String s = "Tone Metrix - 24K";
		float textWidth = mPaint.measureText(s);
		canvas.drawText(s, (getWidth() - textWidth) / 2, 20, mPaint);
	}
}
