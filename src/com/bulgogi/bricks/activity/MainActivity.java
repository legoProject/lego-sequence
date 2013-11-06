package com.bulgogi.bricks.activity;

import android.app.*;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.bulgogi.bricks.*;
import com.bulgogi.bricks.camera.*;

public class MainActivity extends Activity {
	private Preview mPreview;
	private Camera mCamera;
	
    int mNumberOfCameras;
    int mCameraCurrentlyLocked;
    
    // The first rear facing camera
    int mDefaultCameraId;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        FrameLayout container = new FrameLayout(this);
        ResultsView resultsView = new ResultsView(this);
        mPreview = new Preview(this, resultsView);
        container.addView(mPreview);
        container.addView(resultsView);
        setContentView(container);
        
        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();
        
        // Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				mNumberOfCameras = i;
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        mCameraCurrentlyLocked = mDefaultCameraId;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ac_main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.switch_camera:
            // check for availability of multiple cameras
            if (mNumberOfCameras == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.camera_alert)).setNeutralButton("Close", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

            // OK, we have multiple cameras.
            // Release this camera -> cameraCurrentlyLocked
            if (mCamera != null) {
                mCamera.stopPreview();
                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }

            // Acquire the next camera and request Preview to reconfigure parameters.
			mCamera = Camera.open((mCameraCurrentlyLocked + 1) % mNumberOfCameras);
			mCameraCurrentlyLocked = (mCameraCurrentlyLocked + 1) % mNumberOfCameras;
			mPreview.switchCamera(mCamera);

            // Start the preview
            mCamera.startPreview();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
