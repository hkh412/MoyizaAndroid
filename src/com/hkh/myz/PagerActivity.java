package com.hkh.myz;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hkh.myz.adapter.ListDetailPagerAdapter;
import com.hkh.myz.fragments.BoardDetailFragment;

public abstract class PagerActivity extends ActionBarActivity
	implements OnPageChangeListener {
	
	private static String TAG = PagerActivity.class.getSimpleName();
	Context mContext;
	ViewPager mViewPager;
	ListDetailPagerAdapter mListDetailAdapter;
	LinearLayout mLoadingLayout;
	AdView mAdView;
	AdRequest adRequest;
	
	protected int mBackBtnClickCount = 0;
	
	/**
	 * n 번만큼 back button을 클릭하면 activity 종료함
	 */
	protected int backCountForFinish = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
	}
	
	public void setDetailFragment(Fragment detailFragment) {
		mListDetailAdapter.setDetailFragment(detailFragment);
		mViewPager.setCurrentItem(1);
	}
	
	public void showLoadingIndicator() {
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideLoadingIndicator() {
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 상세화면의 키보드 숨김
	 */
	public void hideSoftKeyboard() {
		if (mViewPager == null) {
			return;
		}
		if (mListDetailAdapter == null) {
			return;
		}
		if (mListDetailAdapter.getCount()<1) {
			return;
		}
		Fragment fragment = mListDetailAdapter.getItem(1);
		if (fragment != null && fragment instanceof BoardDetailFragment) {
			((BoardDetailFragment)fragment).hideSoftKeyboard(mContext);
		}
	}
	
	/**
	 * 로그인 성공시 댓글쓰기 버튼 visibility refresh
	 */
	public void refreshWritePermission() {
		if (mViewPager == null) {
			return;
		}
		if (mListDetailAdapter == null) {
			return;
		}
		if (mListDetailAdapter.getCount()<1) {
			return;
		}
		Fragment fragment = mListDetailAdapter.getItem(1);
		if (fragment != null && fragment instanceof BoardDetailFragment) {
			((BoardDetailFragment)fragment).refreshWriteCommentView();
		}
	}
	
	/**
	 * back 키 기본 동작 overriding
	 */
	@Override
	public void onBackPressed() {
		if (mViewPager != null) {
			if (mViewPager.getCurrentItem() == 1) {		// 게시글 상세
				mViewPager.setCurrentItem(0);
				mBackBtnClickCount = 0;
				return;
			}
			if (mViewPager.getCurrentItem() == 0) {		// 게시글 목록
				onShowBackButtonToast();
				if (mBackBtnClickCount >= backCountForFinish) {
					finish();
				}
				mBackBtnClickCount++;
				return;
			}
		}
		super.onBackPressed();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAdView != null) {
			mAdView.destroy();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdView != null) {
			mAdView.pause();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdView != null) {
			mAdView.resume();
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (position == 0) {
			hideSoftKeyboard();
			showAdView();
		} else if (position == 1) {
//			hideAdView();
		}
	}
	
	public void setAdView() {
	}
	
	public void showAdView() {
		if (mAdView != null) {
			mAdView.setVisibility(View.VISIBLE);
		}
	}

	public void hideAdView() {
		if (mAdView != null) {
			mAdView.setVisibility(View.GONE);
		}
	}
	
	/**
	 * back 버튼 연속클릭시 보여줄 메시지
	 */
	abstract public void onShowBackButtonToast();
}
