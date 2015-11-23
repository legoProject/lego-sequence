package legosequence.bulgogi.com.legosequence.display;

import android.app.Activity;
import android.util.DisplayMetrics;

public class DisplayInfo {

    private Activity mParent = null;
    private DisplayMetrics metrics = null;
    private int mWidth = 0;
    private int mHeight = 0;


    public DisplayInfo(Activity parent) {
        mParent = parent;
        metrics = new DisplayMetrics();
        mParent.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
    }

    public int getDeviceWidth() {
        return mWidth;
    }

    public int getDeviceHeight() {
        return mHeight;
    }
}
