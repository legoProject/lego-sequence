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
    private int mCellSize = Constant.LARGE_CELL_SIZE;

    public PatternDetector(CvScalar startHSV, CvScalar endHSV) {
        mStartHSV = startHSV;
        mEndHSV = endHSV;
        mPattern = new boolean[mCellSize][mCellSize];
    }

    public void process(final IplImage src, IplImage processed) {
        boolean blur = false;
        if (mCellSize == Constant.LARGE_CELL_SIZE) {
            blur = true;
        }

        IplImage threshed = OpenCV.getThresholdedImageHSV(src, mStartHSV, mEndHSV, blur);
        CvScalar avgColor = cvAvg(threshed, null);
        Log.e(TAG, "avgColor: " + avgColor.val(0));
        if (avgColor.val(0) < 80) {
            Log.e(TAG, "NOISE!! " + avgColor.val(0));
            return;
        }

        for (int y = 0; y < mCellSize; y++) {
            for (int x = 0; x < mCellSize; x++) {
                int color = pickColor(threshed, x, y);
                putArray(x, y, color);
            }
        }

        postPatternEvent();
        dumpArray();

        cvCvtColor(threshed, processed, CV_GRAY2RGBA);
        drawGrid(processed);
    }

    private void putArray(int x, int y, int color) {
        mPattern[x][y] = color > BRICK_THRESHOLD ? false : true;
    }

    public void recreate(int cellSize) {
        mCellSize = cellSize;
        mPattern = new boolean[cellSize][cellSize];
    }

    private void drawGrid(IplImage src) {
        // Draw horizontal lines
        for (int i = 0; i < mCellSize; i++) {
            CvPoint pt1 = new CvPoint(i * mCellSize, 0);
            CvPoint pt2 = new CvPoint(i * mCellSize, mCellSize * mCellSize - 1);
            cvDrawLine(src, pt1, pt2, cvScalar(22, 160, 133, 255), 1, 8, 0);
        }

        // Draw vertical lines
        for (int i = 0; i < mCellSize; i++) {
            CvPoint pt1 = new CvPoint(0, i * mCellSize);
            CvPoint pt2 = new CvPoint(mCellSize * mCellSize - 1, i * mCellSize);
            cvDrawLine(src, pt1, pt2, cvScalar(22, 160, 133, 255), 1, 8, 0);
        }
    }

    private int pickColor(final IplImage src, int x, int y) {
        CvScalar color = new CvScalar();

        CvRect rect = new CvRect(x * mCellSize, y * mCellSize, mCellSize, mCellSize);
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
        for (int y = 0; y < mCellSize; y++) {
            for (int x = 0; x < mCellSize; x++) {
                log += mPattern[x][y] == true ? "1 " : "0 ";
            }

            log += "\n";
        }
        log += "********************************\n";
        Log.w(TAG, log);
    }

    private boolean[][] getPatternForSingleNote(boolean[][] pattern) {
        int cellSize = mCellSize - 2;
        boolean[][] patternWithoutOutline = new boolean[cellSize][cellSize];

        for (int y = 1; y < mCellSize - 1; y++) {
            for (int x = 1; x < mCellSize - 1; x++) {
                patternWithoutOutline[x - 1][y - 1] = pattern[x][y];
            }
        }

        return patternWithoutOutline;
    }

    private boolean[][] getPatternForMix(boolean[][] pattern) {
        int cellSize = mCellSize - 4;
        boolean[][] patternWithoutOutline = new boolean[cellSize][cellSize];

        for (int y = 2; y < mCellSize - 2; y++) {
            for (int x = 2; x < mCellSize - 2; x++) {
                patternWithoutOutline[x - 2][y - 2] = pattern[x][y];
            }
        }

        return patternWithoutOutline;
    }

    private void postPatternEvent() {
        if (mCellSize == Constant.LARGE_CELL_SIZE) {
            EventBus.getDefault().post(Events.PatternDetect.eventOf(getPatternForSingleNote(mPattern)));
        } else {
            EventBus.getDefault().post(Events.PatternDetect.eventOf(getPatternForMix(mPattern)));
        }
    }
}
