package com.hkh.myz.adapter;

import com.hkh.myz.fragments.EmptyFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * 리스트 / 상세 Fragment 슬라이딩
 * @author Administrator
 *
 */
public class ListDetailPagerAdapter extends FragmentStatePagerAdapter{

	private Fragment listFragment = null;
	private Fragment detailFragment = new EmptyFragment();
	private FragmentManager mFragmentManager = null;
	public ListDetailPagerAdapter(FragmentManager fm, Fragment listFragment) {
		super(fm);
		this.listFragment = listFragment;
		this.mFragmentManager = fm;
	}

	@Override
	public Fragment getItem(int position) {
		if (position == 0) {
			return listFragment;
		} else if (position == 1) {
			return detailFragment;
		} else {
			return null;
		}
	}
	
	@Override
    public int getItemPosition(Object object)
    {
		return POSITION_NONE;
    }
	
	public void setDetailFragment(Fragment detailFragment) {
		mFragmentManager.beginTransaction().remove(this.detailFragment).commit();
		this.detailFragment = detailFragment;
        notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return 2;
	}
}
