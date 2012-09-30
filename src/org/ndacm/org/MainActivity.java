package org.ndacm.org;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final String TAG = "MainActivity";

	private Camera mCamera;
	private CameraPreview mPreview;
	private boolean focusCall = false;
	private MainActivity activity = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create our Preview view and set it as the content of our
		// activity.
		mPreview = new CameraPreview(this);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		// Add a listener to the Capture button
		preview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				Log.i(TAG, "Touch, calling takePicture");
				if (focusCall) {
					mCamera.autoFocus(mAutofocus);
				} else {
					mCamera.takePicture(null, null, mPicture);
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkCameraHardware(this)) {
			// Create an instance of Camera
			mCamera = getCameraInstance();
		}
		mPreview.updateCamera(mCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance

			// get Camera parameters
			Camera.Parameters params = c.getParameters();

			List<String> focusModes = params.getSupportedFocusModes();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH
					&& focusModes
							.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				// set Camera parameters
				c.setParameters(params);
				focusCall = true;
				Log.i(TAG, "Focus mode set to Continuous Picture");
			} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				// set Camera parameters
				c.setParameters(params);
				focusCall = true;
				Log.i(TAG, "Focus mode set to Auto");
			} else if (focusModes
					.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				// set Camera parameters
				c.setParameters(params);
				focusCall = false;
				Log.i(TAG, "Focus mode set to Infinity");
			}
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}

		return c; // returns null if camera is unavailable
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "ACMCamera");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				new UploadFilesTask().execute(pictureFile);
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
			mPreview.updateCamera(mCamera);
		}
	};

	private AutoFocusCallback mAutofocus = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			camera.takePicture(null, null, mPicture);
		}
	};

	/** A basic Camera preview class */
	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context) {
			super(context);
			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			if (mCamera == null) {
				return;
			}
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// Take care of releasing the Camera preview in your
			// activity.
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}

		public void updateCamera(Camera camera) {
			this.mCamera = camera;
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}
	}

	 private class UploadFilesTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... params) {
			Connector.UploadPicture(activity,params[0]);
			return null;
		}
		 
		 
	 }
	
}
