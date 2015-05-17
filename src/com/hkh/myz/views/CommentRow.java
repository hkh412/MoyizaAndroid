package com.hkh.myz.views;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hkh.myz.R;
import com.hkh.myz.config.HTMLAttrs;
import com.hkh.myz.config.HTMLTags;
import com.hkh.myz.config.Links;

public class CommentRow extends LinearLayout {

	private static String TAG = CommentRow.class.getSimpleName();
	Context mContext;
	TextView tvCmtMemo;
	TextView tvCmtAuthor;
	TextView tvCmtDate;
	ImageView imgReplyMark;
	ImageView imgAuthor;
	View viewDummy;
	
	public CommentRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.layout_comment_row, this, true);
		tvCmtMemo = (TextView)view.findViewById(R.id.tv_cmt_memo);
		tvCmtAuthor = (TextView)view.findViewById(R.id.tv_cmt_author);
		tvCmtDate = (TextView)view.findViewById(R.id.tv_cmt_date);
		imgReplyMark = (ImageView)view.findViewById(R.id.img_reply_mark);
//		viewDummy = (View)view.findViewById(R.id.view_dummy_indent);
		imgAuthor = (ImageView)view.findViewById(R.id.img_cmt_author);
	}

	public void setReplyIntent(boolean fullWidth) {
		if (fullWidth) {
			imgReplyMark.setVisibility(View.GONE);
//			viewDummy.setVisibility(View.GONE);
		} else {
			imgReplyMark.setVisibility(View.VISIBLE);
//			viewDummy.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setCommentMemo(String memo) {
		// <a> 태그처리
		Element element = Jsoup.parse(memo).body();
		final String href = element.select(HTMLTags.A).attr(HTMLAttrs.HREF);
		if (href != null && href.length()>0) {
			tvCmtMemo.setText(Html.fromHtml(memo));
			tvCmtMemo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});
		} else {
			tvCmtMemo.setText(Html.fromHtml(memo));
		}
	}
	
	public void setCommentAuthor(String author) {
		tvCmtAuthor.setText(author);
	}
	
	public void setCommentDate(String date) {
		tvCmtDate.setText(date);
	}
	
	public void setCommentImg(String authorImg) {
		if (authorImg != null && authorImg.length()>0) {
			AQuery aq = new AQuery(mContext);
			aq.id(imgAuthor).image(Links.MOBILE_BBS+authorImg, true, true);
		} else {
			imgAuthor.setImageResource(R.drawable.ic_social_person);
		}
	}
}
