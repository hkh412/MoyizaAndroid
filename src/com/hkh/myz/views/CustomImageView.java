package com.hkh.myz.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.hkh.myz.R;

public class CustomImageView extends FrameLayout {

	Context mContext;
	ImageView mainImage;
	String imageUrl;
	
	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.layout_imageview, this, true);
		mainImage = (ImageView) view.findViewById(R.id.imageview_holder);
	}
	
	public void loadImage(String imageUrl) {
		this.imageUrl = imageUrl;
		final AQuery aq = new AQuery(mContext);
		// targetWidth set to 300 for down sampling
		// https://code.google.com/p/android-query/wiki/ImageLoading
		aq.id(mainImage)
			.image(imageUrl, true, true, 300, R.drawable.ic_empty, null, AQuery.FADE_IN, AQuery.RATIO_PRESERVE)
			.progress(R.id.imageview_progress);
	}
	
	public String getImageUrl() {
		return this.imageUrl;
	}
}
