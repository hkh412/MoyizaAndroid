package com.hkh.myz.fragments;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class SearchableFragment extends Fragment {
	
	protected String searchQuery = null;
	// 일반모드 검색모드 구분
	protected boolean searchMode = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		Bundle bundle = getArguments();
		if (bundle != null) {
			searchQuery = bundle.getString(SearchManager.QUERY);
		}
		
		// 일반모드, 검색모드 구분
		if (searchQuery != null) {
			searchMode = true;
		} else {
			searchMode = false;
		}
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 조회 호출
	 */
	abstract public void queryData(String url);
	
	/**
	 * 제목검색 (처음검색)
	 */
	abstract public void querySearchData();
	
	/**
	 * 제목검색 paging
	 * @param url
	 * @param page
	 */
	abstract public void querySearchData(int page);
	
	/**
	 * 검색 호출
	 * @param url
	 */
	abstract public void querySearchData(String url);
}
