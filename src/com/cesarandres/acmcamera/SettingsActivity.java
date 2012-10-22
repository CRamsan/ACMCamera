package com.cesarandres.acmcamera;

import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.*;
import android.widget.*;
import java.io.*;

public class SettingsActivity extends PreferenceActivity
{
	private static final int TASK_COMPLETED= 1;
	private static final int TASK_FAILED_SOME_UPLOADED = 3;
	private static final int TASK_FAILED_NOTHING_UPLOADED = 5;
	private static final int TASK_NOT_STARTED = 6;
	private static final int TASK_NO_CONNECTION = 7;

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

		Preference myPref = findPreference(getResources().getString(R.string.keyUploadNow));

		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference)
				{
					new UploadFilesTask().execute();
					return true;
				}
			});	

	}   


	private class UploadFilesTask extends AsyncTask<Void, Void, Void>
	{

		private int taskCompleted = TASK_FAILED_NOTHING_UPLOADED;
		private int uploadCounter = 0;
		@Override
		protected void onPreExecute()
		{
			findPreference(getResources().getString(R.string.keyUploadNow)).setEnabled(false);
			Toast.makeText(getApplicationContext(), "Upload task started", Toast.LENGTH_SHORT)
				.show();
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), "ACMCamera"
				+ File.separator + "WaitingToUpload");

			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo == null || !networkInfo.isConnected())
			{
				taskCompleted = TASK_NO_CONNECTION;
				return null;
			}

			if (mediaStorageDir.exists() && mediaStorageDir.isDirectory() && mediaStorageDir.listFiles().length > 0)
			{
				for (int i = 0; i < mediaStorageDir.listFiles().length; i++)
				{
					File fileToUpload = mediaStorageDir.listFiles()[i];
					if (Connector.UploadPicture(getApplicationContext(), fileToUpload))
					{
						File mediaStorageCompleted = new File(
							Environment.getExternalStorageDirectory(), "ACMCamera");
						// Create the storage directory if it does not exist
						if (!mediaStorageCompleted.exists())
						{
							if (!mediaStorageCompleted.mkdirs())
							{
								break;
							}
						}
						uploadCounter++;
						taskCompleted = TASK_FAILED_SOME_UPLOADED;
						fileToUpload.renameTo(new File(mediaStorageCompleted, fileToUpload.getName()));
					}			
				}
				taskCompleted = TASK_COMPLETED;
			}
			else
			{
				taskCompleted = TASK_NOT_STARTED;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void none)
		{
			switch (taskCompleted)
			{
				case TASK_COMPLETED:
					Toast.makeText(getApplicationContext(), "Upload task completed, "  + uploadCounter  + " pictures uploaded",
								   Toast.LENGTH_SHORT).show();
					break;
				case TASK_FAILED_SOME_UPLOADED:
					Toast.makeText(getApplicationContext(), "Upload task failed, " + uploadCounter  + " pictures uploaded",
								   Toast.LENGTH_SHORT).show();
					break;
				case TASK_NO_CONNECTION:
					Toast.makeText(getApplicationContext(), "No connection",
								   Toast.LENGTH_SHORT).show();
					break;
				case TASK_NOT_STARTED:
					Toast.makeText(getApplicationContext(), "Nothing to upload",
								   Toast.LENGTH_SHORT).show();
					break;
				case TASK_FAILED_NOTHING_UPLOADED:
					Toast.makeText(getApplicationContext(), "Upload task failed",
								   Toast.LENGTH_SHORT).show();
					break;
			}		
			
			findPreference(getResources().getString(R.string.keyUploadNow)).setEnabled(true);
		}

	}

}
