package com.cesarandres.acmcamera;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

public class PreviewActivity extends Activity {

	private static RequestQueue volley;
	public static ImageLoader mImageLoader;
	private String uuid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		volley = Volley.newRequestQueue(this);
		mImageLoader = new ImageLoader(volley, new BitmapLruCache());
		uuid = getIntent().getExtras().getString("uuid");
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String urlToUpload = settings.getString(
				this.getResources().getString(R.string.keyUploadURL), "");

		((NetworkImageView) findViewById(R.id.imageViewPreview)).setImageUrl(
				urlToUpload + "/uploads/original/" + uuid, mImageLoader);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
