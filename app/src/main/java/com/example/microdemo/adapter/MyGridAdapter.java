package com.example.microdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import io.rong.app.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyGridAdapter extends BaseAdapter {
	private String[] files;

	private LayoutInflater mLayoutInflater;

	public MyGridAdapter(String[] files, Context context) {
		this.files = files;
		mLayoutInflater = LayoutInflater.from(context);
	}

	
	public String[] getAllUrls(){
		return files;
	}
	@Override
	public int getCount() {
		return files == null ? 0 : files.length;
	}

	@Override
	public String getItem(int position) {
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyGridViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new MyGridViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.gridview_item,
					parent, false);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.album_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (MyGridViewHolder) convertView.getTag();
		}
		String url = getItem(position);

		viewHolder.imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//imageBrower(position,bean.urls);
			}
		});
		
		ImageLoader.getInstance().displayImage(url, viewHolder.imageView);
		

		return convertView;
	}

	private static class MyGridViewHolder {
		ImageView imageView;
	}
}