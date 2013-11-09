package com.cesarandres.acmcamera;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Connector {

	private static final String lineEnd = "\r\n";
	private static final String twoHyphens = "--";
	private static final String boundary = "*****";

	public static boolean UploadPicture(Context context, File file) {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String urlToUpload = settings.getString(context.getResources()
				.getString(R.string.keyUploadURL), "");
		if ("".equals(urlToUpload)) {
			return false;
		}

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			String name = settings.getString(
					context.getResources().getString(
							R.string.keyContributorsName), "");
			String password = settings.getString(context.getResources()
					.getString(R.string.keyPassword), "");

			URL url = new URL(urlToUpload + "/index.php");
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

			writePostParam(outputStream, "name", name);
			writePostParam(outputStream, "password", password);

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
							+ file.getAbsolutePath() + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
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

			Log.i("Connector", serverResponseCode + ": "
					+ serverResponseMessage);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			Log.d("Connector", response.toString());

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static void writePostParam(DataOutputStream out, String key,
			String value) throws IOException {
		out.writeBytes(twoHyphens + boundary + lineEnd);
		out.writeBytes("Content-Disposition: form-data; name=\"" + key + "\""
				+ lineEnd);
		out.writeBytes(lineEnd);
		out.writeBytes(value);
		out.writeBytes(lineEnd);
	}
}
