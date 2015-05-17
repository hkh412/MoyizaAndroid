package com.hkh.myz.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hkh.myz.R;
import com.hkh.myz.data.DataHashMap;
import com.hkh.myz.views.CustomImageView;

public class GridViewAdapter extends ArrayAdapter<DataHashMap>{

	private Context cxt;
	private int resourceId;
	
	public GridViewAdapter(Context context, int resourceId, ArrayList<DataHashMap> data) {
		super(context, resourceId, data);
		this.cxt = context;
		this.resourceId = resourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = ((Activity) cxt).getLayoutInflater().inflate(resourceId, parent, false);
			holder = new ViewHolder();
			holder.imageView = (CustomImageView) view.findViewById(R.id.image);
			holder.txtTitle = (TextView) view.findViewById(R.id.title);
			holder.txtAuthor = (TextView) view.findViewById(R.id.autor);
			holder.txtDate = (TextView) view.findViewById(R.id.date);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		DataHashMap map = getItem(position);
		String title = map.get("title");
		String authorNm = map.get("authorNm");
		String date = map.get("date");
		String imgUrl = map.get("imgUrl");
		
		holder.txtTitle.setText(title);
		holder.txtAuthor.setText(authorNm);
		holder.txtDate.setText(date);
		holder.imageView.loadImage(imgUrl);
		
		return view;
	}
	
	static class ViewHolder {
		CustomImageView imageView;
		TextView txtTitle;
		TextView txtAuthor;
		TextView txtDate;
	}
}
