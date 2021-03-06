package com.bulgogi.bricks.detector;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.util.*;

import android.util.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.model.*;
import com.googlecode.javacpp.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PlateDetector {
    private final String TAG = PlateDetector.class.getSimpleName();

    private final boolean DEBUG = true;
    static final int mSmallAreaThreshold = 10000;
    static final int mLargeAreaThreshold = 130000;

    private boolean mIsEnabled;
    private CvMemStorage mStorage;
    private CvScalar mStartHSV;
    private CvScalar mEndHSV;
    private boolean mIsLargePlate;

    public PlateDetector(CvScalar startHSV, CvScalar endHSV) {
        mIsEnabled = false;
        mIsLargePlate = true;
        mStorage = CvMemStorage.create();
        mStartHSV = startHSV;
        mEndHSV = endHSV;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public boolean isLargePlate() {
        return mIsLargePlate;
    }

    public void process(final IplImage src, IplImage preprocessed, IplImage processed) {
        IplImage threshed = OpenCV.getThresholdedImageHSV(src, mStartHSV, mEndHSV, true);
        cvCvtColor(threshed, preprocessed, CV_GRAY2RGBA);

        CvSeq contours = new CvSeq();
        cvFindContours(threshed, mStorage, contours, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

        mIsEnabled = false;
        while (contours != null && !contours.isNull()) {
            double area = Math.abs(cvContourArea(contours, CV_WHOLE_SEQ, 0));
            if (area > mSmallAreaThreshold) {
                CvSeq points = cvApproxPoly(contours, Loader.sizeof(CvContour.class), mStorage, CV_POLY_APPROX_DP, cvContourPerimeter(contours) * 0.05, 0);
                if (points.total() == 4) {
                    if (area >= mLargeAreaThreshold) {
                        mIsLargePlate = true;
                    } else {
                        mIsLargePlate = false;
                    }
                    mIsEnabled = true;

                    cvDrawContours(preprocessed, points, new CvScalar(41, 128, 185, 255), new CvScalar(41, 128, 185, 255), 1, CV_FILLED, 8);
                    CvSeq rect = rectify(points);
                    perspectiveTransformAndWarp(src, processed, rect);

                    for (int i = 0; i < points.total(); i++) {
                        CvPoint point = new CvPoint(cvGetSeqElem(points, i));
                        cvDrawCircle(preprocessed, point, 20, new CvScalar(255, 255, 255, 255), -1, 8, 0);
                    }
                }

                if (DEBUG) {
                    Log.d(TAG, "points.total: " + points.total());
                }
            }
            Log.d(TAG, "area: " + area);

            contours = contours.h_next();
        }

        threshed.release();
    }

    private CvSeq rectify(CvSeq approx) {
        if (true) {
            for (int i = 0; i < approx.total(); i++) {
                Log.d(TAG, "approx[" + i + "] " + (new CvPoint(cvGetSeqElem(approx, i)).x() + ", " + (new CvPoint(cvGetSeqElem(approx, i)).y())));
            }
        }

        ArrayList<CvPoint> points = new ArrayList<CvPoint>() {
            {
                add(new CvPoint());
                add(new CvPoint());
                add(new CvPoint());
                add(new CvPoint());
            }
        };

        for (CvPoint point : points) {
            cvSeqPopFront(approx, point);
        }

        Comparator<CvPoint> cvPointComparator = new Comparator<CvPoint>() {
            @Override
            public int compare(CvPoint lhs, CvPoint rhs) {
                return ((Integer) (lhs.x() + lhs.y())).compareTo(rhs.x() + rhs.y());
            }
        };

        CvPoint topLeft = Collections.min(points, cvPointComparator);
        CvPoint bottomRight = Collections.max(points, cvPointComparator);

        points.remove(topLeft);
        points.remove(bottomRight);

        CvPoint p0 = (CvPoint) points.get(0);
        CvPoint p1 = (CvPoint) points.get(1);
        CvPoint topRight = p0.x() > p1.x() ? p0 : p1;
        CvPoint bottomLeft = p0.x() < p1.x() ? p0 : p1;

        cvSeqPush(approx, topLeft);
        cvSeqPush(approx, topRight);
        cvSeqPush(approx, bottomLeft);
        cvSeqPush(approx, bottomRight);

        for (int i = 0; i < approx.total(); i++) {
            Log.d(TAG, "rect[" + i + "] " + (new CvPoint(cvGetSeqElem(approx, i)).x() + ", " + (new CvPoint(cvGetSeqElem(approx, i)).y())));
        }
        return approx;
    }

    private IplImage perspectiveTransformAndWarp(final IplImage image, IplImage perspective, final CvSeq rect) {
        CvPoint2D32f src = new CvPoint2D32f(4);
        CvPoint2D32f dest = new CvPoint2D32f(4);

        for (int i = 0; i < rect.total(); i++) {
            CvPoint point = new CvPoint(cvGetSeqElem(rect, i));
            src.position(i).put(point);
        }

        if (mIsLargePlate) {
            dest.position(0).put(new CvPoint(0, 0));
            dest.position(1).put(new CvPoint(Constant.LARGE_GRID_SIZE, 0));
            dest.position(2).put(new CvPoint(0, Constant.LARGE_GRID_SIZE));
            dest.position(3).put(new CvPoint(Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE));
        } else {
            dest.position(0).put(new CvPoint(0, 0));
            dest.position(1).put(new CvPoint(Constant.SMALL_GRID_SIZE, 0));
            dest.position(2).put(new CvPoint(0, Constant.SMALL_GRID_SIZE));
            dest.position(3).put(new CvPoint(Constant.SMALL_GRID_SIZE, Constant.SMALL_GRID_SIZE));
        }


        CvMat srcMatrix = cvCreateMat(4, 2, CV_32FC1);
        CvMat destMatrix = cvCreateMat(4, 2, CV_32FC1);

        for (int i = 0; i < 4; i++) {
            double[] point = src.position(i).get();
            srcMatrix.put(point);
        }

        for (int i = 0; i < 4; i++) {
            double[] point = dest.position(i).get();
            destMatrix.put(point);
        }

        Log.i(TAG, "Source Matrix\n" + srcMatrix.toString() + "\nDest Matrix\n" + destMatrix.toString());

        CvMat homographyMatrix = cvCreateMat(3, 3, CV_32FC1);
        opencv_calib3d.cvFindHomography(srcMatrix, destMatrix, homographyMatrix);
        cvWarpPerspective(image, perspective, homographyMatrix);

        Log.i(TAG, "Homography Matrix\n" + homographyMatrix);

        return perspective;
    }
}
