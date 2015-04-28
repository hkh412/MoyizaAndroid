package com.hkh.myz.fragments;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.hkh.myz.MainActivity;
import com.hkh.myz.PagerActivity;
import com.hkh.myz.R;
import com.hkh.myz.adapter.GridViewAdapter;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;
import com.hkh.myz.service.QueryGalleryListService;
import com.hkh.myz.util.ParseUtil;
import com.hkh.myz.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class GalleryFragment extends SearchableFragment 
	implements OnItemClickListener {
	
	Context mContext;
	ArrayList<DataHashMap> data = null;
	private boolean dataLoaded = false;
	private int currentPage=1;
	private boolean mergeData = false;
	private PullToRefreshGridView boardList;
	private GridViewAdapter boardAdapter;
	
	/**
	 * list 혹은 tile 구분 
	 * (모이자 PC버전에 파싱로직이 다른 화면을 같은 GalleryFragment로 파싱하기 위함에 있다)
	 */
	String type = null;
	
	/**
	 * 게시판 ID
	 */
	String boardId = null;
	
	/**
	 * 게시글 상위 URL
	 */
	String bbsUrl = null;
	
	String bbsId = null;
	
	String boardName = null;
	
	public GalleryFragment() {
		data = new ArrayList<DataHashMap>();
		type = "tile";
	}
	
	public GalleryFragment(String menuType) {
		data = new ArrayList<DataHashMap>();
		type = menuType.split("[|]")[1];
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
		boardList = (PullToRefreshGridView) rootView.findViewById(R.id.refreshGridView);
		
		boardList.setOnRefreshListener(new OnRefreshListener2<GridView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				mergeData = false;
				currentPage = 1;
				if (searchMode) {
					querySearchData();
				} else {
					queryData(bbsUrl);
				}
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				currentPage++;
				mergeData = true;
				if (searchMode) {
					querySearchData(currentPage);
				} else {
					queryData(currentPage);
				}
			}
		});
		boardList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				boardList.setCurrentMode(Mode.PULL_FROM_END);
				boardList.setRefreshing(true);
			}
		});
		//스크롤시 이미지뷰 퍼포먼스 조절
		boardList.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
		boardList.setOnItemClickListener(this);
		
		// 저장된 menu type 있으면 불러온다
		if (savedInstanceState != null && type == null) {
			type = savedInstanceState.getString("menuType");
		}
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (dataLoaded) {
			boardList.setAdapter(boardAdapter);
			boardAdapter.notifyDataSetChanged();
			return;
		}
		// url: http://bbs.moyiza.com/index.php?mid=story_photo
		Bundle bundle = getArguments();
		bbsUrl = Links.DESKTOP_BBS+bundle.getString("url");
		bbsId = Util.getValueFromUrl(bbsUrl, "mid");
		
		boardAdapter = new GridViewAdapter(mContext, R.layout.gallery_row_grid, data);
		boardList.setAdapter(boardAdapter);
		
		if (searchMode) {
			querySearchData();
		} else {
			queryData(bbsUrl);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("menuType", type);
	}
	
	public void queryData(int page) {
		String pagingUrl = bbsUrl+"&page="+String.valueOf(page);
		mergeData = true;
		queryData(pagingUrl);
	}
	
	public void queryData(String url) {
		((PagerActivity)mContext).showLoadingIndicator();
		QueryGalleryListService service = new QueryGalleryListService(mContext);
		service.setService(url, this, "onQueryResult");
		service.request();
	}
	
	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
		((PagerActivity)mContext).hideLoadingIndicator();
		boardList.onRefreshComplete();
		boardList.setCurrentMode(Mode.PULL_FROM_START);
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
		
		Element result = Jsoup.parse(html).body();
		dataLoaded = true;
		ArrayList<DataHashMap> newList = new ArrayList<DataHashMap>();
		if (type.equals("list")) {
			// list
			Elements rows = result.select("table.mbbs_table tr.photo_list");
			newList = ParseUtil.parseGalleryElementsByList(rows);
		} else {
			// tile
			Elements rows = result.select("div.mbbs_album_list div.mbbs_album_one");
			newList = ParseUtil.parseGalleryElementsByTile(rows);
		}
		if (!mergeData) {
			data.clear();
		}
		data.addAll(newList);
		boardAdapter.notifyDataSetChanged();
		
        // 좌측메뉴 최초 한번 로딩
        if (getActivity() instanceof MainActivity) {
        	((MainActivity) getActivity()).openDrawerList();
        }
	}
	
	@Override
	public void querySearchData() {
	}
	
	public void querySearchData(int page) {
	}
	
	public void querySearchOnData(int divPage) {
	}
	
	@Override
	public void querySearchData(String url) {
	}
	
	public void onSearchResult(String url, String html, AjaxStatus ajaxStatus) {
		boardList.onRefreshComplete();
		boardList.setCurrentMode(Mode.PULL_FROM_START);
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
		dataLoaded = true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DataHashMap map = data.get(position);
		String link = map.get("link");
		
		BoardDetailFragment detailFragment = new BoardDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString("link", link);
		bundle.putString("authorImg", "N/A");
		bundle.putString("postNo", map.get("postNo"));
		bundle.putString("bbs_id", bbsId);
		
		detailFragment.setArguments(bundle);
		((PagerActivity)mContext).setDetailFragment(detailFragment);
	}
}
