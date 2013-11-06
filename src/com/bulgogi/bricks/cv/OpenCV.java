package com.bulgogi.bricks.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import android.graphics.*;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class OpenCV {
	static public void IplImageToBitmap(final IplImage image, final Bitmap bitmap) {
		bitmap.copyPixelsFromBuffer(image.getByteBuffer());
	}
	
	static public void BitmapToIplImage(final Bitmap bitmap, final IplImage image) {
		bitmap.copyPixelsToBuffer(image.getByteBuffer());
	}
	
	static public IplImage getThresholdedImageHSV(IplImage bgra, CvScalar start, CvScalar end, boolean blur) {
		// first convert the image to BGR
		IplImage bgr = cvCreateImage(cvGetSize(bgra), IPL_DEPTH_8U, 3);
		cvCvtColor(bgra, bgr, CV_BGRA2BGR);
		
		// now convert that to HSV
        IplImage hsv = cvCreateImage(cvGetSize(bgra), IPL_DEPTH_8U, 3);
        cvCvtColor(bgr, hsv, CV_BGR2HSV);
        
        // threshold the HSV based on the start and end vectors
        IplImage threshed = cvCreateImage(cvGetSize(bgra), IPL_DEPTH_8U, 1);
        cvInRangeS(hsv, start, end, threshed);

        if (blur) {
	        // smooth out the thresholded image
	        cvSmooth(threshed, threshed, CV_MEDIAN, 13);
        }
        
        // return memory from the images we're done with
        cvReleaseImage(hsv);
        cvReleaseImage(bgr);        
        
		return threshed;
	}
	
	static public void decodeYUV420SP(int[] abgr, byte[] yuv420sp, int width, int height) {
		int frameSize = width * height;
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				int y1192 = 1192 * y;

				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				abgr[yp] = 0xff000000 | ((b << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((r >> 10) & 0xff);
			}
		}		
	}

}
