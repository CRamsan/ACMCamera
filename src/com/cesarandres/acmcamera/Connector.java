package com.cesarandres.acmcamera;

import android.content.*;
import android.preference.*;
import android.util.*;
import java.io.*;
import java.net.*;

public class Connector
{

	public static boolean UploadPicture(Context context, File file)
	{
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		try
		{
			FileInputStream fileInputStream = new FileInputStream(file);

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String urlToUpload = settings.getString(context.getResources().getString(R.string.keyUploadURL), "");
			URL url = new URL(urlToUpload);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
										  "multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
				.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
							+ file.getAbsolutePath() + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0)
			{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
									+ lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();

			Log.i("Connector", serverResponseCode + ": " + serverResponseMessage);
			
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
}
