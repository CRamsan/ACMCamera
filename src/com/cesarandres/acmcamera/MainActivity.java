package com.cesarandres.acmcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final String TAG = "MainActivity";

	private Camera mCamera;
	private CameraPreview mPreview;
	private boolean focusCall = false;
	private MainActivity activity = this;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create our Preview view and set it as the content of our
		// activity.
		mPreview = new CameraPreview(this);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		((Button) findViewById(R.id.buttonShutter)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					// get an image from the camera
					if (focusCall)
					{
						mCamera.autoFocus(mAutofocus);
					}
					else
					{
						mCamera.takePicture(null, null, mPicture);
					}
				}
			});	

		((ToggleButton) findViewById(R.id.toggleButtonFLash)).setEnabled(false);
		((ToggleButton) findViewById(R.id.toggleButtonFLash)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,
											 boolean isChecked)
				{
					Camera.Parameters params = mCamera.getParameters();
					List<String> flashModes = params
						.getSupportedFlashModes();
					if (flashModes == null)
					{
						return;
					}		

					if (isChecked)
					{

						if (flashModes
							.contains(Camera.Parameters.FLASH_MODE_AUTO))
						{
							params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
							// set Camera parameters
						}
						else if (flashModes
								 .contains(Camera.Parameters.FLASH_MODE_ON))
						{
							params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
							// set Camera parameters
						}
					}
					else
					{
						if (flashModes
							.contains(Camera.Parameters.FLASH_MODE_OFF))
						{
							params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							// set Camera parameters
							focusCall = true;
						}
					}

					mCamera.setParameters(params);
				}
			});


		((Button) findViewById(R.id.buttonSettings)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					startActivity(new Intent(activity, SettingsActivity.class));
				}
			});

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (checkCameraHardware(this))
		{
			// Create an instance of Camera
			mCamera = getCameraInstance();
			Camera.Parameters params = mCamera.getParameters();		
			List<String> flashModes = params.getSupportedFlashModes();
			if (flashModes == null)
			{
				((ToggleButton) findViewById(R.id.toggleButtonFLash)).setEnabled(false);			
			}
			else
			{
				((ToggleButton) findViewById(R.id.toggleButtonFLash)).setEnabled(true);
			}
		}
		mPreview.updateCamera(mCamera);		
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseCamera()
	{
		if (mCamera != null)
		{
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA))
		{
			// this device has a camera
			return true;
		}
		else
		{
			// no camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance()
	{
		Camera c = null;
		try
		{
			c = Camera.open(); // attempt to get a Camera instance

			// get Camera parameters
			Camera.Parameters params = c.getParameters();

			List<String> focusModes = params.getSupportedFocusModes();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH
				&& focusModes
				.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
			{
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				// set Camera parameters
				focusCall = true;
			}
			else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
			{
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				// set Camera parameters
				focusCall = true;
			}
			else if (focusModes
					 .contains(Camera.Parameters.FOCUS_MODE_INFINITY))
			{
				// Autofocus mode is supported

				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				// set Camera parameters
				focusCall = false;
			}

			List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
			Camera.Size bestSize = null;
			for (Camera.Size size : pictureSizes)
			{
				if (bestSize == null)
				{
					bestSize = size;
				}
				else
				{
					if (bestSize.width <= size.width
						&& bestSize.height <= size.height)
					{
						bestSize = size;
					}
				}
			}
			params.setPictureSize(bestSize.width, bestSize.height);

			c.setParameters(params);

		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
		}

		return c; // returns null if camera is unavailable
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type)
	{
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type)
	{
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
			Environment.getExternalStorageDirectory(), "ACMCamera"
			+ File.separator + "WaitingToUpload");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists())
		{
			if (!mediaStorageDir.mkdirs())
			{
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
			.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
								 + "IMG_" + timeStamp + ".jpg");
		}
		else
		{
			return null;
		}

		return mediaFile;
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null)
			{
				return;
			}

			try
			{
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();

				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

				if (((ToggleButton) findViewById(R.id.toggleButtonAutoUpload))
					.isChecked())
				{
					if (networkInfo != null && networkInfo.isConnected())
					{
						new UploadFilesTask().execute(pictureFile);
					}
					else
					{
						Toast.makeText(activity, "No network connection",
									   Toast.LENGTH_SHORT).show();
					}
				}
			}
			catch (FileNotFoundException e)
			{
			}
			catch (IOException e)
			{
			}
			mPreview.updateCamera(mCamera);
		}				
	};

	private AutoFocusCallback mAutofocus = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera)
		{
			camera.takePicture(null, null, mPicture);
		}
	};

	/** A basic Camera preview class */
	public class CameraPreview extends SurfaceView implements
	SurfaceHolder.Callback
	{
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context)
		{
			super(context);
			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			if (mCamera == null)
			{
				return;
			}
			try
			{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			}
			catch (IOException e)
			{
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			// Take care of releasing the Camera preview in your
			// activity.
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
								   int h)
		{
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null)
			{
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try
			{
				mCamera.stopPreview();
			}
			catch (Exception e)
			{
				// ignore: tried to stop a non-existent preview
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try
			{
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			}
			catch (Exception e)
			{
			}
		}

		public void updateCamera(Camera camera)
		{
			this.mCamera = camera;
			try
			{
				mCamera.stopPreview();
			}
			catch (Exception e)
			{
				// ignore: tried to stop a non-existent preview
			}

			// start preview with new settings
			try
			{
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			}
			catch (Exception e)
			{
			}
		}
	}

	private class UploadFilesTask extends AsyncTask<File, Void, Void>
	{

		private boolean taskCompleted = false;

		@Override
		protected void onPreExecute()
		{
			Toast.makeText(activity, "Upload task started", Toast.LENGTH_SHORT)
				.show();
		}

		@Override
		protected Void doInBackground(File... params)
		{
			File fileToUpload = params[0];
			if (Connector.UploadPicture(getApplicationContext(), fileToUpload))
			{
				File mediaStorageCompleted = new File(
					Environment.getExternalStorageDirectory(), "ACMCamera");
				// Create the storage directory if it does not exist
				if (!mediaStorageCompleted.exists())
				{
					if (!mediaStorageCompleted.mkdirs())
					{
						return null;
					}
				}				
				fileToUpload.renameTo(new File(mediaStorageCompleted, fileToUpload.getName()));
				taskCompleted = true;
			}			
			return null;
		}

		@Override
		protected void onPostExecute(Void none)
		{
			if (taskCompleted)
			{
				Toast.makeText(activity, "Upload task completed",
							   Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(activity, "Upload task failed",
							   Toast.LENGTH_SHORT).show();
			}
		}

	}

}
