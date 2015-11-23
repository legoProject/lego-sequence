package legosequence.bulgogi.com.legosequence.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import legosequence.bulgogi.com.legosequence.config.Constant;

public class OverlayView extends View {
    private final String TAG = OverlayView.class.getSimpleName();

    private Paint mPaint;
    private Bitmap mPlatePreprocessed;
    private Bitmap mPlateProcessed;
    private Bitmap mPatternProcessed;

    public OverlayView(Context context) {
        super(context);

        mPaint = new Paint();
    }

    public void update(Bitmap platePreprocessed, Bitmap plateProcessed, Bitmap patternProcessed) {
        mPlatePreprocessed = platePreprocessed;
        mPlateProcessed = plateProcessed;
        mPatternProcessed = patternProcessed;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPlatePreprocessed != null) {
            canvas.drawBitmap(mPlatePreprocessed,
                    new Rect(0, 0, mPlatePreprocessed.getWidth(), mPlatePreprocessed.getHeight()),
                    new RectF(0, 0, Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE), mPaint);
        }

        if (mPlateProcessed != null) {
            canvas.drawBitmap(mPlateProcessed,
                    new Rect(0, 0, mPlateProcessed.getWidth(), mPlateProcessed.getHeight()),
                    new RectF(getWidth() - Constant.LARGE_GRID_SIZE, 0, getWidth(), Constant.LARGE_GRID_SIZE), mPaint);
        }

        if (mPatternProcessed != null) {
            canvas.drawBitmap(mPatternProcessed,
                    new Rect(0, 0, mPatternProcessed.getWidth(), mPatternProcessed.getHeight()),
                    new RectF(0, getHeight() - Constant.LARGE_GRID_SIZE, Constant.LARGE_GRID_SIZE, getHeight()), mPaint);
        }

        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(20);
        String s = "Lego Metrix - 24K";
        float textWidth = mPaint.measureText(s);
        canvas.drawText(s, (getWidth() - textWidth) / 2, 20, mPaint);
    }
}
