package com.hkh.myz.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hkh.myz.R;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;

public class BoardAdapter extends ArrayAdapter<DataHashMap> {

	Context mContext;
	AQuery aq;
	int resourceId;
	List<DataHashMap> data;
	
	// 지역정보 표시여부
	boolean showLocation = false;
	ArrayList<String> visitedList = null;
	
	public BoardAdapter(Context context, int resource, List<DataHashMap> data,
			ArrayList<String> visitedList) {
		super(context, resource, data);
		
		this.resourceId = resource;
		this.mContext = context;
		this.data = data;
		this.visitedList = visitedList;
		aq = new AQuery(mContext);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		BoardHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(resourceId, parent, false);
			
			holder = new BoardHolder();
			holder.imgAuthor = (ImageView) row.findViewById(R.id.img_author);
			holder.txtTitle = (TextView) row.findViewById(R.id.tv_board_title);
			holder.txtContent = (TextView) row.findViewById(R.id.tv_board_content);
			holder.txtAuthor = (TextView) row.findViewById(R.id.tv_board_author);
			holder.txtDate = (TextView) row.findViewById(R.id.tv_board_date);
			holder.txtCmtCnt = (TextView) row.findViewById(R.id.tv_cmt_count);
			holder.txtRegion = (TextView) row.findViewById(R.id.tv_region);
			holder.txtViewCnt = (TextView) row.findViewById(R.id.tv_board_viewcount);
			holder.viewMask = (View) row.findViewById(R.id.view_read_mask);
			row.setTag(holder);
			
		} else {
			holder = (BoardHolder)row.getTag();
		}
		
		DataHashMap map = data.get(position);
		String postNo = map.get("postNo");
		String title = map.get("title");
		String content = map.get("content");
		String authorNm = map.get("authorNm");
		String authorImg = map.get("authorImg");
		String date = map.get("date");
		String cmtCnt = map.get("cmtCnt");
		cmtCnt = cmtCnt == null ? "0" : cmtCnt;
		String viewCnt = map.get("viewCnt");
		String viewCntText = mContext.getString(R.string.title_view_count);
		viewCntText = String.format(viewCntText, viewCnt);
		String region = map.get("region");
		if (region.length() > 3) {
			region = region.substring(0, 2);
		}
		
		if (authorImg != null && authorImg.length()>0) {
			aq.id(holder.imgAuthor)
			  .image(Links.MOBILE_BBS+authorImg, true, true, 0, R.drawable.ic_social_person);
		} else {
			holder.imgAuthor.setImageResource(R.drawable.ic_social_person);
		}
		
		holder.txtTitle.setText(title);
		holder.txtContent.setText(content);
		if (title.length() <= 15) {
			holder.txtContent.setVisibility(View.VISIBLE);
		} else {
			holder.txtContent.setVisibility(View.GONE);
		}
		holder.txtAuthor.setText(authorNm);
		holder.txtDate.setText(date);
		holder.txtCmtCnt.setText(cmtCnt);
		holder.txtViewCnt.setText(viewCntText);
		holder.txtRegion.setText(region);
		
		// 지역정보 표시
		int visibility = showLocation ? View.VISIBLE : View.GONE;
		holder.txtRegion.setVisibility(visibility);
		
		// 댓글수 색상 표시
		try {
			int count = Integer.parseInt(cmtCnt);
			if (count < 5) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_grey);
			} else if (count >= 10 && count < 20) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_blue);
			} else if (count >= 20) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_orange);
			}
		} catch (Exception e) {
			holder.txtCmtCnt.setText("0");
			holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_grey);
		}
		
		visibility = visitedList.contains(postNo) ? View.VISIBLE : View.GONE;
		holder.viewMask.setVisibility(visibility);
		
		return row;
	}

	static class BoardHolder
	{
		ImageView imgAuthor;
		TextView txtTitle;
		TextView txtContent;
		TextView txtAuthor;
		TextView txtDate;
		TextView txtCmtCnt;
		TextView txtRegion;		// 지역
		TextView txtViewCnt;
		View viewMask;
	}
	
	public void showLocationText() {
		this.showLocation = true;
	}
}
