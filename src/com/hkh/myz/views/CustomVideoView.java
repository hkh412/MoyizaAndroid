package com.hkh.myz.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.androidquery.AQuery;
import com.hkh.myz.R;

public class CustomVideoView extends FrameLayout {

	private Context mContext;
	private ImageView thumnailImage;
	private ImageButton playButton;
	private String videoUrl;
	
	public CustomVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.layout_videoview, this, true);
		
		thumnailImage = (ImageView) view.findViewById(R.id.video_thumnail);
		if (thumnailImage == null) {
			return;
		}
		thumnailImage.setAdjustViewBounds(true);
		thumnailImage.setScaleType(ScaleType.FIT_CENTER);
		
		playButton = (ImageButton) view.findViewById(R.id.video_playbutton);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (videoUrl != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
					mContext.startActivity(intent);
				}
			}
		});
	}
	
	public void setVideoLink(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	
	public void loadThumbnailImage(String imageUrl) {
		AQuery aq = new AQuery(mContext);
		aq.id(thumnailImage).image(imageUrl, true, true, 0, R.drawable.ic_empty);
	}
}
