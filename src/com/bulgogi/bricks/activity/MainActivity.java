package com.bulgogi.bricks.activity;

import java.util.*;

import android.content.*;
import android.hardware.*;
import android.hardware.Camera.Size;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.badlogic.gdx.backends.android.*;
import com.bulgogi.bricks.*;
import com.bulgogi.bricks.controller.*;
import com.bulgogi.bricks.event.*;
import com.bulgogi.bricks.model.*;
import com.bulgogi.bricks.sound.*;
import com.bulgogi.bricks.view.*;

import de.greenrobot.event.*;


public class MainActivity extends AndroidApplication {
	private final String TAG = MainActivity.class.getSimpleName();
	
	private Camera mCamera;
	private FrameCallback mFrameCb;
	private Preview mPreview;
	private Pattern mPattern;
	private Plate mPlate;
	private ToneMatrix mToneMatrix;
	private WakeLock mWakeLock;
	
	private InstrumentType mCurrentInstrumentType = InstrumentType.TONE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initModel();
        
		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = false;
		
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
		
		mToneMatrix.loadSound(InstrumentType.TONE);
        mToneMatrix.playToneMatrix();
	}

	@Override
	protected void onPause() {
		
		//사운드는 미리 해줘야 정상동작 함 
		mToneMatrix.releaseToneMatrix();
		
		releaseScreenOn();
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
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
		
		List<Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ac_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void onEventMainThread(Events.PatternDetect patterns) {
	    	
	    	boolean[][] paterns = patterns.getPatterns();
	    	if (mCurrentInstrumentType == InstrumentType.MIX 
	    			&& paterns[0].length > 4) {
	    		return ;
	    	}
	    	
	    	mToneMatrix.changeInputGrid(patterns.getPatterns());
    }
	
	public void onEventMainThread(Events.SoundSwitching type) {
		mCurrentInstrumentType = type.getType();
		switchInstrument(type);
	}
	
	public void switchInstrument(Events.SoundSwitching type) {
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
