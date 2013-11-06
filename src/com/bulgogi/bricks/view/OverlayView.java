package com.bulgogi.bricks.view;

import android.content.*;
import android.graphics.*;
import android.view.*;

import com.bulgogi.bricks.config.*;

public class OverlayView extends View {
	private final String TAG = OverlayView.class.getSimpleName();
	
	private Paint mPaint;
	private Bitmap mPlatePreprocessed;
	private Bitmap mPlateProcessed;
	private Bitmap mPatternProprocessed;
	
	public OverlayView(Context context) {
		super(context);
		
		mPaint = new Paint();
	}

	public void update(Bitmap platePreprocessed, Bitmap plateProcessed, Bitmap patternProcessed) {
		mPlatePreprocessed = platePreprocessed; 
		mPlateProcessed = plateProcessed;
		mPatternProprocessed = patternProcessed;
		postInvalidate();
	}
	
    @Override
	protected void onDraw(Canvas canvas) {
		if (mPlatePreprocessed != null) {
			canvas.drawBitmap(mPlatePreprocessed, 
					new Rect(0, 0, getWidth(), getHeight()), 
					new RectF(0, 0, 320, 240), mPaint);
		}
		
		if (mPlateProcessed != null) {
			canvas.drawBitmap(mPlateProcessed, 
					new Rect(0, 0, Constant.GRID_SIZE, Constant.GRID_SIZE), 
					new RectF(getWidth() - Constant.GRID_SIZE, 0, getWidth(), Constant.GRID_SIZE), mPaint);
		}

		if (mPatternProprocessed != null) {
			canvas.drawBitmap(mPatternProprocessed, 
					new Rect(0, 0, Constant.GRID_SIZE, Constant.GRID_SIZE), 
					new RectF(0, getHeight() - Constant.GRID_SIZE, Constant.GRID_SIZE, getHeight()), mPaint);
		}
		
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(20);
		String s = "Lego Metrix - 24K";
		float textWidth = mPaint.measureText(s);
		canvas.drawText(s, (getWidth() - textWidth) / 2, 20, mPaint);
	}
}
