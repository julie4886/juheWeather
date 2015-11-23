package com.juhe.juheweather.adapter;

import java.util.List;

import com.juhe.juheweather.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListViewAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> list;

	public CityListViewAdapter(Context mContext, List<String> list) {
		super();
		this.mContext = mContext;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		
		if (convertView == null) {
			rowView = LayoutInflater.from(mContext).inflate(
					R.layout.activity_city_item, null);
		
		} else {
			rowView = convertView;
		}

		TextView tv_city = (TextView) rowView.findViewById(R.id.tv);
		tv_city.setText(getItem(position));
		return rowView;
	}

}
