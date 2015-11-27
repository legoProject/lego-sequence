package legosequence.bulgogi.com.legosequence.model;


import android.graphics.Bitmap;

import org.bytedeco.javacpp.opencv_core.IplImage;

import legosequence.bulgogi.com.legosequence.config.Constant;
import legosequence.bulgogi.com.legosequence.cv.OpenCV;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;

public class Pattern {
    private Bitmap mProcessedBitmap;
    private IplImage mProcessedImage;

    public Pattern() {
        mProcessedBitmap = Bitmap.createBitmap(Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE, Bitmap.Config.ARGB_8888);
        mProcessedImage = IplImage.create(Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE, IPL_DEPTH_8U, 4);
    }

    public void cleanup() {
        mProcessedImage.release();
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

    public void recreate(int gridSize) {
        cleanup();
        mProcessedBitmap = Bitmap.createBitmap(gridSize, gridSize, Bitmap.Config.ARGB_8888);
        mProcessedImage = IplImage.create(gridSize, gridSize, IPL_DEPTH_8U, 4);
    }
}
