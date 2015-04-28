package com.hkh.myz;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hkh.myz.MoyizaApplication.TrackerName;
import com.hkh.myz.adapter.ActionBarDropDownAdapter;
import com.hkh.myz.adapter.ExpandableDrawerListAdapter;
import com.hkh.myz.adapter.ListDetailPagerAdapter;
import com.hkh.myz.config.Config;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;
import com.hkh.myz.data.MenuData;
import com.hkh.myz.data.MoyizaRegion;
import com.hkh.myz.data.Session;
import com.hkh.myz.fragments.BoardFragment;
import com.hkh.myz.fragments.GalleryFragment;
import com.hkh.myz.fragments.LoginDialogFragment;
import com.hkh.myz.fragments.LoginDialogFragment.LoginListener;
import com.hkh.myz.manager.LoginManager;
import com.hkh.myz.manager.PostStateManager;
import com.hkh.myz.manager.SessionManager;
import com.hkh.myz.manager.SharedPreferenceManager;
import com.hkh.myz.service.QueryPersonalInfoService;
import com.hkh.myz.util.ParseUtil;
import com.hkh.myz.util.Util;

/**
 * App: Moyiza.com
 * @author hkh
 *
 */
public class MainActivity extends PagerActivity 
	implements OnChildClickListener, OnClickListener, LoginListener, OnNavigationListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	Context mContext;
	
	/**
	 * 좌측 메뉴 open / close 인벤트 핸들러
	 */
	ActionBarDrawerToggle mDrawerToggle;
	
	/**
	 * 좌측 메뉴 layout
	 */
	DrawerLayout mDrawerLayout;
	
	/**
	 * 좌측 메뉴 ListView
	 */
	ExpandableListView mDrawerList;
	
	/**
	 * 좌측 메뉴 ListView Adapter 클래스
	 */
	ExpandableDrawerListAdapter mDrawerListAdapter;
	
	ActionBarDropDownAdapter mActionBarListAdapter;
	
	ActionBar mActionBar;
	
	/**
	 * 왼쪽 슬라이드 메뉴관련
	 */
	LinearLayout mLeftDrawer;
	
	/**
	 * 메뉴 데이터 (상위, 하위 포함) 
	 */
	ArrayList<MenuData> menuList = new ArrayList<MenuData>();
	
	/**
	 * 좌측 상위 메뉴 데이터
	 */
	ArrayList<MenuData> parentNodes = new ArrayList<MenuData>();
	
	/**
	 * 메뉴하위 게시글 목록
	 */
	ArrayList<ArrayList<MenuData>> childNodes = new ArrayList<ArrayList<MenuData>>();
	
	ArrayList<MoyizaRegion> regionList = new ArrayList<MoyizaRegion>();
	
	/**
	 * 좌측메뉴 최초 열림
	 */
	boolean initialMenuOpen = false;
	
	MenuData currentMenuData;
	
	FrameLayout mLayoutHead;		// 클릭 이벤트 발생용 Layout
	FrameLayout mLayoutProfile;		// 사용자 정보 Layout
	TextView tvLogin;				// 로그인 텍스트
	TextView tvNickname;			// 사용자 닉네임
	TextView tvLevel;				// 사용자 등급
	TextView tvPoint;				// 사용자 포인트
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);
		
		mLoadingLayout = (LinearLayout) findViewById(R.id.layout_indicator);
		
		// 메뉴 데이터 로딩
		menuList = Util.loadMenuData(mContext, parentNodes, childNodes);
		
		// 지역정보 로딩
		regionList = Util.loadRegionData(mContext);
		
		// 즐겨찾기 데이터 추가
		restoreFavoriteMenu(menuList);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				hideSoftKeyboard();
				invalidateOptionsMenu();
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        
		mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
		
		/**
		 * 로그인 사용자정보 영역 컴포넌트
		 */
		mLayoutProfile = (FrameLayout) findViewById(R.id.layout_profile);
		tvLogin = (TextView) findViewById(R.id.tv_login_text);
		tvNickname = (TextView) findViewById(R.id.tv_profile_nickname);
		tvLevel = (TextView) findViewById(R.id.tv_profile_level);
		tvPoint = (TextView) findViewById(R.id.tv_profile_point);
		mLayoutHead = (FrameLayout) findViewById(R.id.layout_head);
		mLayoutHead.setOnClickListener(this);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(this);
		
		mDrawerList = (ExpandableListView) findViewById(R.id.drawer_list);
		mDrawerListAdapter = new ExpandableDrawerListAdapter(mContext, parentNodes, childNodes);
		mDrawerList.setAdapter(mDrawerListAdapter);
		
		/**
		 * 좌측 메뉴 child node 클릭 listener 설정
		 */
		mDrawerList.setOnChildClickListener(this);
		
		// 즐겨찾기 메뉴는 기본으로 Expand 상태
		mDrawerList.expandGroup(0);
		
		setActionBar();
		setAdView();
		sendAnalytics();
//		checkAutoLogin();
		
		int lastUid = SharedPreferenceManager.getInstance(mContext).getInt("last-menu-uid");
		MenuData lastMenuData = null;
		if (lastUid < 0) {
			// 저장된 메뉴 없음, 도움요청
			lastMenuData = Util.getMatchedUrlDataByUid(menuList, 1);
		} else {
			lastMenuData = Util.getMatchedUrlDataByUid(menuList, lastUid);
		}
		loadFragment(lastMenuData);
		currentMenuData = lastMenuData;
		
	}

	/**
	 * 액션바 관련 설정
	 */
	private void setActionBar() {
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mActionBarListAdapter = new ActionBarDropDownAdapter(mContext,
				android.R.layout.simple_spinner_dropdown_item, regionList);
		mActionBar.setListNavigationCallbacks(mActionBarListAdapter, this);
	}
	
	@Override
	public void setAdView() {
		if (!Config.AdEnable) {
			return;
		}
		mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new ToastAdListener(this));

        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID); 
        Log.d(TAG, "DeviceId: "+deviceId);
        
        AdRequest.Builder builder = new AdRequest.Builder();
        if (Config.ADVIEW_TEST) {
        	builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        	.addTestDevice(deviceId);
        }
        adRequest = builder.build();
        mAdView.loadAd(adRequest);
	}
	
	/**
	 * 통계데이터 전송
	 */
	public void sendAnalytics() {
        Tracker t = ((MoyizaApplication) getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		if (item.getItemId() == R.id.action_write) {
			// 글쓰기
			boolean blackTheme = mContext.getApplicationInfo().theme == R.style.Theme_Black;
			int theme = blackTheme ? AlertDialog.THEME_DEVICE_DEFAULT_DARK : AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext, theme);
			builder.setTitle("글쓰기 알림")
				.setMessage(mContext.getString(R.string.message_write_post_alert))
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Links.MOBILE_WEB_LOGIN));
						mContext.startActivity(intent);
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void loadFragment(MenuData menuData) {
		// 액션바 타이틀 변경
		mActionBar.setTitle(menuData.getName());
		
		Fragment fragment = getFragmentByMenuType(menuData.getType());
		if (menuData.getType().equals("location")) {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			mActionBar.setSelectedNavigationItem(0);
		} else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		setFragmentArguments(fragment, menuData.getUrl());
	}
	
	private void setFragmentArguments(Fragment listFragment, String url) {
		Bundle bundle = new Bundle();
		bundle.putString("url", url);
		listFragment.setArguments(bundle);
		
		mListDetailAdapter = null;
		mListDetailAdapter = new ListDetailPagerAdapter(getSupportFragmentManager(), listFragment);
		mViewPager.setAdapter(mListDetailAdapter);
		mViewPager.setOffscreenPageLimit(mListDetailAdapter.getCount());
	}

	@Override
	public void onShowBackButtonToast() {
		Toast.makeText(mContext, mContext.getString(R.string.message_back_button_toast),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		
		mDrawerLayout.closeDrawer(mLeftDrawer);
		mBackBtnClickCount = 0;
		mViewPager.setAdapter(null);
		
		// 선택한 메뉴 인덱스 저장
		MenuData menuData = mDrawerListAdapter.getChild(groupPosition, childPosition);
		// 선택한 메뉴 저장
		SharedPreferenceManager.getInstance(mContext).putInt("last-menu-uid", menuData.getUid());
		loadFragment(menuData);
		currentMenuData = menuData;
		
		return true;
	}
	
	/**
	 * 상단 액션바 지역선택 처리
	 */
	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		hideSoftKeyboard();
		MoyizaRegion region = mActionBarListAdapter.getItem(position);
		mDrawerLayout.closeDrawer(mLeftDrawer);
		
		Fragment fragment = getFragmentByMenuType(currentMenuData.getType());
		String url = currentMenuData.getUrl();
		url = url+"&"+region.getParam();
		
		setFragmentArguments(fragment, url);
		return false;
	}
	
	/**
	 * 저장된 즐겨찾기 불러오기
	 * @param menuList
	 */
	private void restoreFavoriteMenu(List<MenuData> menuList) {
		// restore favorite menu
		PostStateManager psm = PostStateManager.getInstance(mContext);
		List<Integer> favoriteList = psm.restoreFavoriteMenu();
		// 저장된 favoriteList가 있으면 즐겨찾기메뉴에 추가
		if (favoriteList != null) {
			ArrayList<MenuData> favorites = childNodes.get(0);
			for (MenuData menuData : menuList) {
				if (favoriteList.contains(menuData.getUid())) {
					favorites.add(menuData);
				}
			}
		}
	}
	
	/**
	 * 처음 게시글목록 로딩완료후 side menu 열림
	 */
	public void openDrawerList() {
		if (!initialMenuOpen) {
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							mDrawerLayout.openDrawer(mLeftDrawer);
						}
					});
				}
			};
			Timer timer = new Timer();
			timer.schedule(timerTask, 300);
			initialMenuOpen = true;
		}
	}
	
	/**
	 * 좌측 사용자 정보 설정
	 */
	private void setProfile() {
		Session session = SessionManager.getInstance().getSession();
		if (session != null) {
			tvLogin.setVisibility(View.GONE);
			mLayoutProfile.setVisibility(View.VISIBLE);
			DataHashMap map = session.getPersonalInfo();
			tvNickname.setText(map.get("nick-name"));
			tvLevel.setText(map.get("user-level"));
			tvPoint.setText(map.get("user-point"));
			
		} else {
			tvLogin.setVisibility(View.VISIBLE);
			mLayoutProfile.setVisibility(View.GONE);
		}
		mLayoutHead.refreshDrawableState();
	}
	
	/**
	 * 자동 로그인 설정여부를 체크하고 자동 로그인 수행
	 */
	private void checkAutoLogin() {
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		boolean isAutoLogin = spm.getBoolean("auto-login");
		if (isAutoLogin) {
			String username = spm.getString("username");
			String password = spm.getString("password");
			if (username == null || password == null) {
				Log.e(TAG, "자동로그인이 체크되었지만 저장된 계정에 문제가 발생함.");
			} else {
				LoginManager.getInstance().setLoginListener(this);
				LoginManager.getInstance().doLogin(mContext, username, password);
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		if (mLayoutHead == view) {
			mDrawerLayout.closeDrawer(mLeftDrawer);
			
			// 로그인
			LoginDialogFragment dialogFragment = new LoginDialogFragment();
			dialogFragment.setLoginListener(this);
	        dialogFragment.show(getSupportFragmentManager(), "LoginDialogFragment");
		}
	}

	@Override
	public void onLoginComplete(LoginDialogFragment loginFragment) {
		// 로그인 성공처리
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		
		// 자동로그인 설정
		spm.putBoolean("auto-login", true);
		Toast.makeText(mContext, 
				mContext.getString(R.string.message_login_success), Toast.LENGTH_SHORT).show();
		
		// 개인정보 조회
		QueryPersonalInfoService service = new QueryPersonalInfoService(mContext);
		service.setService(Links.PERSONAL_INFO_URL, this, "onPersonalInfoResult");
		service.request();
		
		// dimiss login dialog
		if (loginFragment != null) {
			loginFragment.dismiss();
		}
		invalidateOptionsMenu();
	}

	@Override
	public void onLoginFailure(String message) {
		Log.i(TAG, "onLoginFailure: "+message);
		Toast.makeText(mContext,
				message, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onLogoutComplete(LoginDialogFragment loginFragment) {
		// session 지움
		SessionManager.getInstance().setSession(null);
		// 자동로그인  false
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		spm.putBoolean("auto-login", false);
		
		// setProfile 호출
		setProfile();
		refreshWritePermission();
		
		// Toast
		Toast.makeText(mContext,
				mContext.getString(R.string.message_logout_success), Toast.LENGTH_SHORT).show();
		
		// dimiss login dialog
		if (loginFragment != null) {
			loginFragment.dismiss();
		}
		invalidateOptionsMenu();
	}

	@Override
	public void onLogoutFailure(String message) {
	}
	
	/**
	 * 로그인 성공후 개인정보 조회
	 * @param url
	 * @param html
	 * @param status
	 */
	public void onPersonalInfoResult(String url, String html, AjaxStatus status) {
		if (html == null) {
			// 로그인 TextView 대신 개인정보조회 실패 메시지, 클릭하면 개인정보 재조회??
		}
		Element body = Jsoup.parse(html).body();
		DataHashMap map = ParseUtil.parsePersonalInfo(body);
		// Session 사용자정보 입력
		Session session = SessionManager.getInstance().getSession();
		session.setPersonalInfo(map);
		
		// 사용자 페이지 업데이트 (쓴글, 댓글, 포인트, 등급, 이미지 등)
		setProfile();
		refreshWritePermission();
	}
	
	private Fragment getFragmentByMenuType(String menuType) {
		Fragment fragment = null;
		if (menuType.contains("gallery")) {
			fragment = new GalleryFragment(menuType);
		} else {
			// Location 타입일 경우 지역정보 보임
			fragment = new BoardFragment(menuType);
		}
		return fragment;
	}
}
