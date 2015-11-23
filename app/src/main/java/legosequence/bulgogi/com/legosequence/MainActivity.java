package legosequence.bulgogi.com.legosequence;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.util.List;

import de.greenrobot.event.EventBus;
import legosequence.bulgogi.com.legosequence.controller.FrameCallback;
import legosequence.bulgogi.com.legosequence.event.Events;
import legosequence.bulgogi.com.legosequence.model.Pattern;
import legosequence.bulgogi.com.legosequence.model.Plate;
import legosequence.bulgogi.com.legosequence.sound.InstrumentType;
import legosequence.bulgogi.com.legosequence.sound.MatrixManager;
import legosequence.bulgogi.com.legosequence.sound.MixToneMatrix;
import legosequence.bulgogi.com.legosequence.sound.SequencialToneMatrix;
import legosequence.bulgogi.com.legosequence.sound.ToneMatrix;
import legosequence.bulgogi.com.legosequence.view.OverlayView;
import legosequence.bulgogi.com.legosequence.view.Preview;

public class MainActivity extends AndroidApplication {

    private final String TAG = MainActivity.class.getSimpleName();

    private Camera mCamera;
    private FrameCallback mFrameCb;
    private Preview mPreview;
    private Pattern mPattern;
    private Plate mPlate;
    private ToneMatrix mToneMatrix;
    private PowerManager.WakeLock mWakeLock;

    private InstrumentType mCurrentInstrumentType = InstrumentType.TONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initModel();

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGLSurfaceView20API18 = false;

        initialize(new MatrixManager(), cfg);

        // Create a FrameLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        FrameLayout container = new FrameLayout(this);
        OverlayView overlayView = new OverlayView(this);
        mFrameCb = new FrameCallback(overlayView, mPlate, mPattern);
        mPreview = new Preview(this, mFrameCb);
        container.addView(mPreview);
        container.addView(overlayView);
        setContentView(container);

        mToneMatrix = new SequencialToneMatrix();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        keepScreenOn();

        // Open the default i.e. the back-facing camera.
        mCamera = Camera.open();
        mPreview.setCamera(mCamera);
        if (mToneMatrix != null) {
            mToneMatrix.loadSound(InstrumentType.TONE);
            mToneMatrix.playToneMatrix();
        }
    }

    @Override
    protected void onPause() {
        if (mToneMatrix != null) {
            mToneMatrix.releaseToneMatrix();
        }

        releaseScreenOn();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseModel();
        mFrameCb.cleanup();
    }

    private void initModel() {
        Camera camera = Camera.open();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        List<Camera.Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size size = Preview.getOptimalPreviewSize(supportedPreviewSizes, dm.widthPixels, dm.heightPixels);

        mPlate = new Plate(size.width, size.height);
        mPattern = new Pattern();

        camera.release();
    }

    private void releaseModel() {
        mPlate.cleanup();
        mPattern.cleanup();
    }

    private void keepScreenOn() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    private void releaseScreenOn() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void onEventMainThread(Events.PatternDetect patterns) {

        boolean[][] paterns = patterns.getPatterns();
        if (mCurrentInstrumentType == InstrumentType.MIX && paterns[0].length > 4) {
            return;
        }

        mToneMatrix.changeInputGrid(patterns.getPatterns());
    }

    public void onEventMainThread(Events.SoundSwitching type) {
        mCurrentInstrumentType = type.getType();
        switchInstrument(type);
    }

    public void onEventMainThread(Events.SoundRelease release) {
        if (mToneMatrix != null) {
            mToneMatrix.releaseToneMatrix();
            mToneMatrix = null;
        }
    }

    public void switchInstrument(Events.SoundSwitching type) {
        if (mToneMatrix != null)
            mToneMatrix.releaseToneMatrix();

        if (type.getType() == InstrumentType.MIX) {
            mToneMatrix = new MixToneMatrix();
        } else {
            mToneMatrix = new SequencialToneMatrix();
        }

        mToneMatrix.loadSound(type.getType());
        mToneMatrix.playToneMatrix();
    }
}
