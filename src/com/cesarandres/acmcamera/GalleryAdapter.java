package com.cesarandres.acmcamera;

import java.util.ArrayList;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GalleryAdapter extends BaseAdapter {

	private ArrayList<Picture> pictures;
	private LayoutInflater mInflater;
	private String apiURL;
	private PrettyTime p;

	public GalleryAdapter(Context context, ArrayList<Picture> pictures) {
		this.mInflater = LayoutInflater.from(context);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String baseUrl = settings.getString(
				context.getResources().getString(R.string.keyUploadURL), "");
		this.pictures = pictures;
		this.apiURL = baseUrl + "/uploads/";
		this.p = new PrettyTime();
	}

	@Override
	public int getCount() {
		return this.pictures.size();
	}

	@Override
	public Picture getItem(int position) {
		return this.pictures.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.textViewName);
			holder.date = (TextView) convertView
					.findViewById(R.id.textViewDate);
			holder.image = (NetworkImageView) convertView
					.findViewById(R.id.networImageView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String uploadTime = p.format(new Date(Long.parseLong(getItem(position)
				.getUploaded()) * 1000l));
		holder.name.setText(getItem(position).getContributor());
		holder.date.setText(uploadTime);
		holder.image.setImageUrl(apiURL + getItem(position).getUuid(),
				GalleryActivity.mImageLoader);
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView date;
		NetworkImageView image;
	}
}