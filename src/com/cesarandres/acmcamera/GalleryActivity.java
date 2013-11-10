package com.cesarandres.acmcamera;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class GalleryActivity extends Activity {

	private static RequestQueue volley;
	public static ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		volley = Volley.newRequestQueue(this);
		mImageLoader = new ImageLoader(volley, new BitmapLruCache());
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.updateContent();
	}

	@Override
	protected void onStop() {
		super.onStop();
		GalleryActivity.volley.cancelAll(this);
	}

	private void updateContent() {

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String baseUrl = settings.getString(
				this.getResources().getString(R.string.keyUploadURL), "");

		URL url;
		try {
			url = new URL(baseUrl + "/db.php");

			Listener<PictureResponse> success = new Response.Listener<PictureResponse>() {
				@Override
				public void onResponse(PictureResponse response) {
					{
						ListView listRoot = (ListView) findViewById(R.id.listViewGallery);
						GalleryAdapter adapter = new GalleryAdapter(
								GalleryActivity.this, response.getPhotos());
						listRoot.setAdapter(adapter);
						listRoot.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								String uuid = ((Picture)arg0.getItemAtPosition(arg2)).getUuid();
								Intent i = new Intent(GalleryActivity.this, PreviewActivity.class);
								i.putExtra("uuid", uuid);
								startActivity(i);
							}
						});
					}
				}
			};

			ErrorListener error = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.equals(new Object());
				}
			};

			GsonRequest<PictureResponse> gsonOject = new GsonRequest<PictureResponse>(
					url.toString(), PictureResponse.class, null, success, error);
			gsonOject.setTag(this);
			GalleryActivity.volley.add(gsonOject);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
