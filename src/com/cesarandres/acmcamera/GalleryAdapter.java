package com.cesarandres.acmcamera;

import java.util.ArrayList;

import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GalleryAdapter extends BaseAdapter {

	private ArrayList<Picture> pictures;
	private LayoutInflater mInflater;

	public GalleryAdapter(Context context, ArrayList<Picture> stats) {
		this.mInflater = LayoutInflater.from(context);
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
			holder.name = (TextView) convertView.findViewById(R.id.);
			holder.image = (NetworkImageView) convertView.findViewById(R.id.);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		

		return convertView;
	}

	static class ViewHolder {
		TextView name;
		NetworkImageView image;
	}
}