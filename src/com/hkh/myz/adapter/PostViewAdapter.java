package com.hkh.myz.adapter;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.hkh.myz.R;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.CommentData;
import com.hkh.myz.data.HeaderData;
import com.hkh.myz.data.PostItem;
import com.hkh.myz.data.Session;
import com.hkh.myz.events.OnScaleImageViewListener;
import com.hkh.myz.manager.SessionManager;
import com.hkh.myz.views.CommentRow;
import com.hkh.myz.views.CustomAudioView;
import com.hkh.myz.views.CustomImageView;
import com.hkh.myz.views.CustomVideoView;

public class PostViewAdapter extends ArrayAdapter<PostItem> {

	private static String TAG = PostViewAdapter.class.getSimpleName();
	Context mContext;
	int resourceId;
	AQuery aq;
	OnScaleImageViewListener mScaleImageListener;

	public PostViewAdapter(Context context, int resource, List<PostItem> objects) {
		super(context, resource, objects);
		this.resourceId = resource;
		this.mContext = context;
		aq = new AQuery(mContext);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		
		if (view == null) {
			view = ((Activity) mContext).getLayoutInflater().inflate(resourceId, parent, false);
			holder = new ViewHolder();
			holder.textView = (TextView) view.findViewById(R.id.textview);
			holder.tvSuggestLogin = (TextView) view.findViewById(R.id.tv_suggest_login);
			holder.layoutCmtCnt = (LinearLayout) view.findViewById(R.id.layout_cmtcnt);
			holder.commentRow = (CommentRow) view.findViewById(R.id.comment_row);
			holder.headerView = (LinearLayout) view.findViewById(R.id.layout_header);
			holder.webView = (WebView) view.findViewById(R.id.webview);
			holder.videoView = (CustomVideoView) view.findViewById(R.id.videoview);
			holder.audioView = (CustomAudioView) view.findViewById(R.id.audioview);
			holder.imageView = (CustomImageView) view.findViewById(R.id.imageview);
			holder.imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v instanceof CustomImageView && mScaleImageListener != null) {
						String imageUrl = ((CustomImageView)v).getImageUrl();
						mScaleImageListener.onScaleImageView(imageUrl);
					}
				}
			});			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		holder.imageView.setVisibility(View.GONE);
		holder.videoView.setVisibility(View.GONE);
		holder.audioView.setVisibility(View.GONE);
		holder.textView.setVisibility(View.GONE);
		holder.tvSuggestLogin.setVisibility(View.GONE);
		holder.layoutCmtCnt.setVisibility(View.GONE);
		holder.commentRow.setVisibility(View.GONE);
		holder.headerView.setVisibility(View.GONE);
		holder.webView.setVisibility(View.GONE);
		
		PostItem item = this.getItem(position);
		
		switch (item.type) {
		case PostItem.TYPE_HEADER_VIEW:
			holder.headerView.setVisibility(View.VISIBLE);
			ImageView imgAuthor = (ImageView) holder.headerView.findViewById(R.id.img_cont_author);
			TextView tvAuthor = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_author);
			TextView tvTitle = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_title);
			TextView tvDate = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_date);
			TextView tvViewCnt = (TextView) holder.headerView.findViewById(R.id.tv_head_viewcnt);
			
			HeaderData headerData = item.headerData;
			tvAuthor.setText(headerData.authorNm);
			tvTitle.setText(headerData.title);
			tvDate.setText(headerData.date);
			String viewCntText = mContext.getString(R.string.title_view_count);
			tvViewCnt.setText(String.format(viewCntText, headerData.viewCnt));
			
			if (headerData.authorImg != null && headerData.authorImg.equals("N/A")) {
				/**
				 * 저자 프로필 사진 숨김처리 (GalleryFragment은 PC버전을 파싱하기 때문에 모바일 프로필사진을 가져올 수 없다)
				 * authorImg 값을 "N/A"로 하드코딩함 GalleryFragment Line:219 참조
				 */
				imgAuthor.setVisibility(View.GONE);
			} else if (headerData.authorImg != null && headerData.authorImg.length()>0){
				aq.id(imgAuthor)
				.image(Links.MOBILE_BBS+headerData.authorImg, true, true);
			} else {
				imgAuthor.setImageResource(R.drawable.ic_social_person);
			}
			break;
			
		case PostItem.TYPE_TEXT_VIEW:
			holder.textView.setVisibility(View.VISIBLE);	
			holder.textView.setText(Html.fromHtml(item.contText));
			holder.textView.setOnClickListener(null);
			break;
			
		case PostItem.TYPE_IMAGE_NO_LINK:
			holder.imageView.setVisibility(View.VISIBLE);
			holder.imageView.loadImage(item.imageUrl);
			break;
			
		case PostItem.TYPE_IMAGE_WITH_LINK:
			holder.imageView.setVisibility(View.VISIBLE);
			holder.imageView.loadImage(item.imageLink);
			final String imgLink = item.hyperLink;
			holder.imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgLink));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(mContext, 
								mContext.getString(R.string.message_link_invalid), Toast.LENGTH_SHORT).show();
						Log.e(TAG, e.getMessage());
					}
				}
			});
			break;
			
		case PostItem.TYPE_HREF_LINK:
			holder.textView.setVisibility(View.VISIBLE);
			holder.textView.setText(Html.fromHtml(item.contText));
			final String link = item.hyperLink;
			holder.textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});
			break;
			
		case PostItem.TYPE_COMMENT_INFO:
			holder.layoutCmtCnt.setVisibility(View.VISIBLE);
			// 총 댓글 수량 문구
			TextView tvCmtCnt = (TextView) holder.layoutCmtCnt.findViewById(R.id.tv_detail_cmtcnt);
			tvCmtCnt.setText(Html.fromHtml(item.cmtCntText));
			Session session = SessionManager.getInstance().getSession();
			if (session == null) {
				holder.tvSuggestLogin.setVisibility(View.VISIBLE);
			} else {
				holder.tvSuggestLogin.setVisibility(View.GONE);
			}
			break;
			
		case PostItem.TYPE_COMMENT_VIEW:
			holder.commentRow.setVisibility(View.VISIBLE);
			CommentData cmtData = item.cmtData;
			holder.commentRow.setCommentAuthor(cmtData.authorNm);
			holder.commentRow.setCommentImg(cmtData.authorImg);
			holder.commentRow.setCommentDate(cmtData.cmtDate);
			holder.commentRow.setCommentMemo(cmtData.cmtMemo);
			holder.commentRow.setReplyIntent(cmtData.isReply);
			
			// 글 작성자 코멘트 여부
			boolean isAuthorCmt = item.cmtData.authorNm.equals(item.authorNm);
			int cmtBgColor = isAuthorCmt ? 
					R.color.comment_bg_highlight_wh : R.color.comment_bg_normal_wh;
			holder.commentRow.setBackgroundResource(cmtBgColor);
			break;
			
		case PostItem.TYPE_WEB_VIEW:
			holder.webView.setVisibility(View.VISIBLE);
			holder.webView.loadData(item.element.toString(),"text/html; charset=UTF-8", null);
			break;
			
		case PostItem.TYPE_VIDEO_VIEW:
			holder.videoView.setVisibility(View.VISIBLE);
			holder.videoView.loadThumbnailImage(item.thumbnailLink);
			holder.videoView.setVideoLink(item.videoLink);
			break;
			
		case PostItem.TYPE_AUDIO_VIEW:
			holder.audioView.setVisibility(View.VISIBLE);
			holder.audioView.setAudioLink(item.audioLink);
			break;
			
		default:
			break;
		}
		return view;
	}
	
	static class ViewHolder {
		LinearLayout headerView;
		CustomAudioView audioView;
		CustomVideoView videoView;
		CustomImageView imageView;
		TextView textView;
		TextView tvSuggestLogin;
		WebView webView;
		LinearLayout layoutCmtCnt;
		CommentRow commentRow;
	}
	
	public void setOnScaleImageViewListener(OnScaleImageViewListener listener) {
		mScaleImageListener = listener;
	}
}
