package com.cesarandres.acmcamera;

import android.os.*;
import android.preference.*;
import android.preference.Preference.*;
import android.widget.*;
import java.io.*;

public class SettingsActivity extends PreferenceActivity
{

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

		private boolean taskCompleted = false;

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
						fileToUpload.renameTo(new File(mediaStorageCompleted, fileToUpload.getName()));
					}			
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void none)
		{
			if (taskCompleted)
			{
				Toast.makeText(getApplicationContext(), "Upload task completed",
							   Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Upload task failed",
							   Toast.LENGTH_SHORT).show();
			}
			findPreference(getResources().getString(R.string.keyUploadNow)).setEnabled(true);
		}

	}

}
