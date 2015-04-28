package com.hkh.myz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.hkh.myz.util.Util;
import com.hkh.myz.views.HackyViewPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 상세뷰에서 이미지 클릭했을 때 실행되는 이미지 확장 Activity
 * @author hkh
 *
 */
public class ScaleImageActivity extends Activity 
	implements OnClickListener, OnPageChangeListener {

	private final String IMAGE_URL_KEY = "image_url_key";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmm",Locale.US);
//	private SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	Context mContext;
	AQuery aq;
	DisplayImageOptions options;
	
	private ArrayList<String> imageUrls;
	private int currentPosition = 0;
	
	private ScaleImageAdapter mPagerAdapter;
	private HackyViewPager mViewPager;
	private TextView tvPosition;
	private ImageButton btnClose;
	private ImageButton btnDownload;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		aq = new AQuery(mContext);
		options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.black)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_empty)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(200)) //이미지 표시할 때 Fade효과
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    //Remove notification bar
	    this.getWindow().setFlags(
	    		WindowManager.LayoutParams.FLAG_FULLSCREEN,
	    		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    
		setContentView(R.layout.activity_scale_image);
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		mViewPager.setOnPageChangeListener(this);
		tvPosition = (TextView) findViewById(R.id.tv_position);
		btnDownload = (ImageButton) findViewById(R.id.img_btn_download);
		btnDownload.setOnClickListener(this);
		btnClose = (ImageButton) findViewById(R.id.img_btn_close);
		btnClose.setOnClickListener(this);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			imageUrls = bundle.getStringArrayList(IMAGE_URL_KEY);
			currentPosition = bundle.getInt("position");
		}
		
		if (imageUrls != null) {
			mPagerAdapter = new ScaleImageAdapter(imageUrls);
			mViewPager.setAdapter(mPagerAdapter);
			tvPosition.setText(getPositionText());
		}
		if (currentPosition >= 0) {
			mViewPager.setCurrentItem(currentPosition);
		}
	}
	
	private String getPositionText() {
		String current = String.valueOf(currentPosition+1);
		String total = String.valueOf(imageUrls.size());
		return current+"/"+total;
	}

	class ScaleImageAdapter extends PagerAdapter {

		private ArrayList<String> imageUrls;
		
		public ScaleImageAdapter(ArrayList<String> imageUrls) {
			super();
			this.imageUrls = imageUrls;
		}
		
		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setMaximumScale(5.0f);
			ImageLoader.getInstance().displayImage(imageUrls.get(position), photoView, options);
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnDownload) {
			ImageLoader.getInstance().loadImage(imageUrls.get(currentPosition), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					long time = System.currentTimeMillis();
					String title = mContext.getString(R.string.app_name)+dateFormat.format(time);
					// Bitmap 이미지 저장
					boolean success = Util.saveImageToExternalStorage(mContext, loadedImage, title);
					
					if (success) {
						Toast.makeText(mContext,
								mContext.getString(R.string.message_photo_saved), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext,
								mContext.getString(R.string.message_photo_cannot_save), Toast.LENGTH_SHORT).show();
					}
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
				}
			});
		} else if (v == btnClose) {
			finish();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
		tvPosition.setText(getPositionText());
	}
}
