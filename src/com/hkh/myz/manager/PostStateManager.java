package com.hkh.myz.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 기능1: 방문한 게시글 저장기능. 2차원 Set으로 관리
 * [메뉴Set<게시글Set<게시글ID String>>]
 * 
 * 기능2: 즐겨찾기 메뉴 관리기능
 * @author hkh
 *
 */
public class PostStateManager {

	private static String TAG = PostStateManager.class.getSimpleName();
	private static Context mContext;
	private static PostStateManager instance = null;
	private HashMap<String, ArrayList<String>> pageState = null;
	private List<Integer> favoriteList = null;
	private SharedPreferences spf = null;
	
	private PostStateManager() {
		pageState = new HashMap<String, ArrayList<String>>();
		favoriteList = new ArrayList<Integer>();
		spf = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		restorePostState();
	}
	public static PostStateManager getInstance(Context context) {
		if (instance == null) {
			mContext = context;
			instance = new PostStateManager();
		}
		return instance;
	}
	
	/**
	 * 메뉴별 방문한 페이지 조회 [기능1]
	 * @param menuName
	 * @return
	 */
	public ArrayList<String> getVisitedList(String menuName) {
		ArrayList<String> list = pageState.get("key_"+menuName);
		if (list == null) {
			list = new ArrayList<String>();
			pageState.put("key_"+menuName, list);
		}
		return list;
	}
	
	/**
	 * SharedPreference 로 방문한 게시글 restore [기능1]
	 */
	private void restorePostState() {
		// restore key set first
		Set<String> keySet = spf.getStringSet("page_keyset", null);
		if (keySet != null) {
			for (Iterator<String> iterator=keySet.iterator();iterator.hasNext();) {
				String key = iterator.next();
				Set<String> set = spf.getStringSet(key, null);
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(set);
				pageState.put(key, list);
			}
		}
	}
	
	/**
	 * SharedPreference 에 방문한 게시글 저장 [기능1] - 앱이 pause상태로 진입시 호출됨
	 */
	public void savePostState() {
		Set<String> keySet = pageState.keySet();
		// key set 저장
		spf.edit().putStringSet("page_keyset", keySet).commit();
		
		// 메뉴별 set저장
		for (Iterator<String> iterator=keySet.iterator();iterator.hasNext();) {
			String key = iterator.next();
			ArrayList<String> list = pageState.get(key);
			putSet2SharedPreference(key, list);
		}
	}
	
	private void putSet2SharedPreference(String key, ArrayList<String> list) {
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		spf.edit().putStringSet(key, set).commit();
	}
	
	/**
	 * 저장된 즐겨찾기 메뉴 생성 [기능2]
	 * @param parentNodes (좌측메뉴 Group)
	 * @param childNodes (좌측하위메뉴)
	 */
	public List<Integer> restoreFavoriteMenu() {
		Set<String> menuSet = spf.getStringSet("menu_favorite", null);
		if (menuSet == null) {
			return null;
		}
		favoriteList.clear();
		for (String uid : menuSet) {
			try {
				favoriteList.add(Integer.parseInt(uid));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return favoriteList;
	}
	
	public List<Integer> getFavoriteList() {
		return favoriteList;
	}
	
	/**
	 * 메뉴 UID를 임시저장목록에 추가
	 * 추가성공하면 true, 중복된 추가이면 false
	 * @param uid
	 * @return
	 */
	public boolean addFavoriteMenu(int uid) {
		if (!favoriteList.contains(uid)) {
			favoriteList.add(uid);
			return true;
		}
		return false;
	}
	
	/**
	 * 메뉴 UID를 임시저장목록에서 삭제
	 * @param uid
	 * @return
	 */
	public boolean removeFavoriteMenu(int uid) {
		if (favoriteList.contains(uid)) {
			favoriteList.remove((Integer)uid);
			return true;
		}
		return false;
	}
	
	/**
	 * 즐겨찾기 메뉴저장 [기능2] - 앱이 pause상태로 진입시 호출됨
	 */
	public void saveFavoriteMenu() {
		Set<String> menuSet = new HashSet<String>();
		for (Integer uid : favoriteList) {
			menuSet.add(String.valueOf(uid));
		}
		spf.edit().putStringSet("menu_favorite", menuSet).commit();
	}
}
