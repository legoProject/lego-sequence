package com.bulgogi.bricks.activity;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bulgogi.bricks.R;
import com.bulgogi.bricks.controller.FrameCallback;
import com.bulgogi.bricks.event.Events;
import com.bulgogi.bricks.model.Pattern;
import com.bulgogi.bricks.model.Plate;
import com.bulgogi.bricks.sound.MixToneMatrix;
import com.bulgogi.bricks.sound.ToneMatrix;
import com.bulgogi.bricks.view.OverlayView;
import com.bulgogi.bricks.view.Preview;
import com.kai.gdxexample.GdxExample;

import de.greenrobot.event.EventBus;


public class MainActivity extends AndroidApplication {
	private Camera mCamera;
	private FrameCallback mFrameCb;
	private Preview mPreview;
	private Pattern mPattern;
	private Plate mPlate;
	private ToneMatrix mToneMatrix;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		initModel();
		
		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = false;
		
		initialize(new GdxExample(), cfg);
		// Create a FrameLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		FrameLayout container = new FrameLayout(this);
		OverlayView overlayView = new OverlayView(this);
		mFrameCb = new FrameCallback(overlayView, mPlate, mPattern);
		mPreview = new Preview(this, mFrameCb);
		container.addView(mPreview);
		container.addView(overlayView);
		setContentView(container);
		
		//mToneMatrix = new SequencialToneMatrix(this);
		mToneMatrix = new MixToneMatrix(this);
		EventBus.getDefault().register(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the back-facing camera.
		mCamera = Camera.open();
		mPreview.setCamera(mCamera);
		
		//mToneMatrix.prepareToneMatrix(this);
		mToneMatrix.loadSound();
        mToneMatrix.playToneMatrix();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		mToneMatrix.releaseToneMatrix();
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
	    	Log.i("MainActivity","onEventMainThread : " + patterns);
	    	
	    	//test
	    	boolean[][] testGrid = {
	    			{true,false,false,true},
	    			{true,false,false,false},
	    			{false,true,true,false},
	    			{true,false,false,true}
	    	};
	    	mToneMatrix.setGrid(patterns.getPatterns());
	    	//mToneMatrix.setGrid(testGrid);
    }
}
