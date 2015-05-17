package com.hkh.myz.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
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
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hkh.myz.MainActivity;
import com.hkh.myz.PagerActivity;
import com.hkh.myz.R;
import com.hkh.myz.adapter.BoardAdapter;
import com.hkh.myz.config.Config;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;
import com.hkh.myz.manager.CookieManager;
import com.hkh.myz.manager.PostStateManager;
import com.hkh.myz.service.QueryListPagingService;
import com.hkh.myz.service.QueryListService;
import com.hkh.myz.util.ParseUtil;
import com.hkh.myz.util.Util;

public class BoardFragment extends SearchableFragment 
	implements OnItemClickListener {
	
	private static String TAG = BoardFragment.class.getSimpleName();
	Context mContext;
	AQuery aq;
	
	PullToRefreshListView bbsList;
	ArrayList<DataHashMap> data = null;
	BoardAdapter bbsAdapter = null;
	boolean dataLoaded = false;
	
	/**
	 * 게시판 조회하는 url
	 */
	String bbsUrl = null;
	
	/**
	 * 게시판 ID
	 */
	String bbsId = null;
	
	/**
	 * paging number, List Paging 조회시 사용
	 */
	int page = 1;
	
	/**
	 * empty string 혹은 "location"
	 * 지역정보 타입의 메뉴일 때 지역정보 나타냄
	 */
	String type = null;
	
	
	ArrayList<String> visitedList = null;
	
	public BoardFragment() {
		data = new ArrayList<DataHashMap>();
		type = "";
	}
	
	public BoardFragment(String menuType) {
		data = new ArrayList<DataHashMap>();
		type = menuType;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		aq = new AQuery(mContext);
		
		View rootView = inflater.inflate(R.layout.fragment_board, container, false);
		bbsList = (PullToRefreshListView) rootView.findViewById(R.id.list_board);
		bbsList.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				if (searchMode) {
					querySearchData();
				} else {
					queryData(bbsUrl);
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				queryPagingData();
			}
		});
		bbsList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				bbsList.setCurrentMode(Mode.PULL_FROM_END);
				bbsList.setRefreshing(true);
			}
		});
		bbsList.setOnItemClickListener(this);
		
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
			bbsList.setAdapter(bbsAdapter);
			return;
		}
		
		Bundle bundle = getArguments();
		bbsUrl = Links.MOBILE_BBS+bundle.getString("url");
		bbsId = Util.getValueFromUrl(bbsUrl, "bbs_id");
		
		/**
		 * 방문한 페이지 저장
		 */
		PostStateManager psmgr = PostStateManager.getInstance(mContext);
		visitedList = psmgr.getVisitedList(bbsUrl);
		
		bbsAdapter = new BoardAdapter(mContext, R.layout.list_item_board, data, visitedList);
		bbsList.setAdapter(bbsAdapter);
		if (type != null && type.equals("location")) {
			bbsAdapter.showLocationText();
		}
		
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
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DataHashMap map = data.get(position-1);
		String link = map.get("link");
		String authorImg = map.get("authorImg");
		String postNo = map.get("postNo");
		
		BoardDetailFragment detailFragment = new BoardDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString("link", link);
		bundle.putString("authorImg", authorImg);
		bundle.putString("postNo", postNo);
		bundle.putString("bbs_id", bbsId);
		
		// 방문한 post id 저장
		Util.addUniqueItem2LimitedSize(visitedList, postNo,
				Config.LIST_LIMIT);
		
		detailFragment.setArguments(bundle);
		((PagerActivity)mContext).setDetailFragment(detailFragment);
	}
	
	@Override
	public void queryData(String url) {
		((PagerActivity)mContext).showLoadingIndicator();
		QueryListService service = new QueryListService(mContext);
		service.setService(url, this, "onQueryResult");
		service.request();
	}
	
	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
		((PagerActivity)mContext).hideLoadingIndicator();
		bbsList.onRefreshComplete();
		bbsList.setCurrentMode(Mode.PULL_FROM_START);
		
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
		page = 2;
		
		// set cookies
		if (!CookieManager.getInstance().hasCookies()) {
			List<Cookie> cookies = ajaxStatus.getCookies();
			CookieManager.getInstance().setCookies(cookies);
		}
		
		dataLoaded = true;
		Element body = Jsoup.parse(html).body();
        Elements elements = body.select("div.article");
        ArrayList<DataHashMap> newList = ParseUtil.parseBoardElements(elements);	
//        if (newList.size() > 0) {
//        	lastPostNo = newList.get(newList.size()-1).get("postNo");
//        }
        data.clear();
        data.addAll(newList);
        bbsAdapter.notifyDataSetChanged();
        
        // 좌측메뉴 최초 한번 로딩
        if (getActivity() instanceof MainActivity) {
        	((MainActivity) getActivity()).openDrawerList();
        }
	}

	/**
	 * List Paging 데이터 조회하는 함수
	 * @param url
	 */
	public void queryPagingData() {
		((PagerActivity)mContext).showLoadingIndicator();
		QueryListPagingService service = new QueryListPagingService(mContext);
		service.setService("", this, "onQueryPagingResult");
		service.setFormParams(bbsId, "", page, bbsUrl);
		service.request();
	}
	
	/**
	 * List Paging 데이터 조회 callback 함수
	 * @param url
	 * @param json
	 * @param ajaxStatus
	 */
	public void onQueryPagingResult(String url, String json, AjaxStatus ajaxStatus) {
		((PagerActivity)mContext).hideLoadingIndicator();
		bbsList.onRefreshComplete();
		bbsList.setCurrentMode(Mode.PULL_FROM_START);
		
		if (json == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
		page++;
		
		ArrayList<DataHashMap> newList = ParseUtil.parseBoardJson(json);
//        if (newList.size() > 0) {
//        	lastPostNo = newList.get(newList.size()-1).get("postNo");
//        }
        data.addAll(newList);
        bbsAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void querySearchData() {
		String url = bbsUrl+"&sf=0&stx="+searchQuery;
		querySearchData(url);
	}

	@Override
	public void querySearchData(int page) {
		String url = bbsUrl+"&sf=0&stx="+searchQuery+"&page="+page;
		querySearchData(url);
	}

	@Override
	public void querySearchData(String url) {
		((PagerActivity)mContext).showLoadingIndicator();
		AjaxCallback<String> cb = new AjaxCallback<String>();           
		cb.url(url).type(String.class).weakHandler(this, "onQueryResult");
		aq.ajax(cb);
	}
}
