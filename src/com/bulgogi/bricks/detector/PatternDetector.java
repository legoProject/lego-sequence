package com.bulgogi.bricks.detector;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import android.util.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.event.Events;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import de.greenrobot.event.EventBus;

public class PatternDetector {
	private final String TAG = PatternDetector.class.getSimpleName();
	
	private final boolean DEBUG = false;
	private final int BRICK_THRESHOLD = 120;
	
	private CvScalar mStartHSV;
	private CvScalar mEndHSV;
	private boolean[][] mPattern;
	
	public PatternDetector(CvScalar startHSV, CvScalar endHSV) {
		mStartHSV = startHSV;
		mEndHSV = endHSV;
		mPattern = new boolean[14][14];
	}
	
	public void process(final IplImage src, IplImage processed/*, boolean[][] result*/) {
		IplImage threshed = OpenCV.getThresholdedImageHSV(src, mStartHSV, mEndHSV, true);
		
		for (int y = 1; y < Constant.CELL_SIZE-1; y++) {
			for (int x = 1; x < Constant.CELL_SIZE-1; x++) {
				int color = pickColor(threshed, x, y);
				putArray(x - 1, y - 1, color);
			}
        }
        
//		result = mPattern;
		postPatternEvent();
		dumpArray();
		
        cvCvtColor(threshed, processed, CV_GRAY2RGBA);
        drawGrid(processed);
	}
	
	private void putArray(int x, int y, int color) {
		mPattern[x][y] = color > BRICK_THRESHOLD ? false : true;		
	}
	
	private void drawGrid(IplImage src) {
		// Draw horizontal lines
		for (int i = 0; i < Constant.CELL_SIZE; i++) {
			CvPoint pt1 = new CvPoint(i * Constant.CELL_SIZE, 0);
			CvPoint pt2 = new CvPoint(i * Constant.CELL_SIZE, Constant.CELL_SIZE * Constant.CELL_SIZE - 1);
			cvDrawLine(src, pt1, pt2, cvScalar(22, 160, 133, 255), 1, 8, 0);
		}
		
		// Draw vertical lines
		for (int i = 0; i < Constant.CELL_SIZE; i++) {
			CvPoint pt1 = new CvPoint(0, i * Constant.CELL_SIZE);
			CvPoint pt2 = new CvPoint(Constant.CELL_SIZE * Constant.CELL_SIZE - 1, i * Constant.CELL_SIZE);
			cvDrawLine(src, pt1, pt2, cvScalar(22, 160, 133, 255), 1, 8, 0);
		}
	}
	
	private int pickColor(final IplImage src, int x, int y) {
		CvScalar color = new CvScalar();
		
		CvRect rect = new CvRect(x * Constant.CELL_SIZE, y * Constant.CELL_SIZE, Constant.CELL_SIZE, Constant.CELL_SIZE);
		cvSetImageROI(src, rect);
		color = cvAvg(src, null);
		cvResetImageROI(src);
		
		if (DEBUG) {
			Log.e(TAG, "[" + x + ", " + y + "] " + color.val(0));
		}
		
		return (int) color.val(0);
	}
	
	private void dumpArray() {
		String log = "\n********************************\n";
        for (int y = 1; y < Constant.CELL_SIZE - 1; y++) {
			for (int x = 1; x < Constant.CELL_SIZE - 1; x++) {
				log += mPattern[x-1][y-1] == true ? "1 " : "0 ";
			}
			
			log += "\n";
        }
        log += "********************************\n";
        Log.w(TAG, log);
	}
	
	private void postPatternEvent() {
		EventBus.getDefault().post(Events.PatternDetact.eventOf(mPattern));
	}
}
