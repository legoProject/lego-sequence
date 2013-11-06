package com.bulgogi.bricks.activity;

import java.util.*;

import android.app.*;
import android.hardware.*;
import android.hardware.Camera.Size;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.bulgogi.bricks.*;
import com.bulgogi.bricks.controller.*;
import com.bulgogi.bricks.detector.*;
import com.bulgogi.bricks.model.*;
import com.bulgogi.bricks.view.*;

public class MainActivity extends Activity {
	private Camera mCamera;
	private Preview mPreview;
	private Pattern mPattern;
	private Plate mPlate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		initModel();
		
		// Create a FrameLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		FrameLayout container = new FrameLayout(this);
		OverlayView overlayView = new OverlayView(this);
		mPreview = new Preview(this, new FrameCallback(overlayView, mPlate, mPattern));
		container.addView(mPreview);
		container.addView(overlayView);
		setContentView(container);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the back-facing camera.
		mCamera = Camera.open();
		mPreview.setCamera(mCamera);
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
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		releaseModel();
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
		if (mPlate != null) {
			mPlate.cleanup();
		}
		
		if (mPattern != null) {
			mPattern.cleanup();
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
}
