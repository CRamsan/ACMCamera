package com.cesarandres.acmcamera;

import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class GalleryActivity extends Activity {

	private static Volley volley = new Volley();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
	}
}
