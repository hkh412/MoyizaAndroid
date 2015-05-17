package com.hkh.myz.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hkh.myz.R;
import com.hkh.myz.data.MenuData;
import com.hkh.myz.manager.PostStateManager;

/**
 * Expandable Drawer ListView Adapter
 * @author hkh
 */
public class ExpandableDrawerListAdapter extends BaseExpandableListAdapter {
	
	private static String TAG = ExpandableDrawerListAdapter.class.getSimpleName();
	Context mContext;
	List<MenuData> parentNodes;
	List<ArrayList<MenuData>> childNodes;
	LayoutInflater inflater;
	PostStateManager psm = null;
	
	public ExpandableDrawerListAdapter(Context context, 
			List<MenuData> parents, List<ArrayList<MenuData>> children) {
		mContext = context;
		this.parentNodes = parents;
		this.childNodes = children;
		inflater = ((Activity)mContext).getLayoutInflater();
		psm = PostStateManager.getInstance(mContext);
	}
	
	@Override
	public View getChildView(final int groupPosition, int childPosition, boolean isLastChild,
			View convertView, final ViewGroup parent) {
		View view = convertView;
		/**
		 * 메뉴명
		 */
		TextView textView = null;
		ToggleButton tgbFavorite = null;
		if (view == null) {
			view = inflater.inflate(R.layout.drawer_child_item, null);
		}
		MenuData menuData = getChild(groupPosition, childPosition);
		textView = (TextView) view.findViewById(R.id.list_header);
		tgbFavorite = (ToggleButton) view.findViewById(R.id.tgb_favorite_child);
		tgbFavorite.setTag(menuData);
		// 즐겨찾기 버튼상태 동기화
		List<Integer> favoriteList = psm.getFavoriteList();
		if (favoriteList.contains(menuData.getUid())) {
			tgbFavorite.setChecked(true);
		} else {
			tgbFavorite.setChecked(false);
		}
		textView.setText(getChild(groupPosition, childPosition).getName());
		
		tgbFavorite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MenuData menuData = (MenuData) buttonView.getTag();
				int uid = menuData.getUid();
				// 즐겨찾기 추가
				if (isChecked) {
					// shared preference 저장목록에 추가
					boolean success = psm.addFavoriteMenu(uid);
					if (!success) {
						// 이미 추가된 메뉴, 추가실패
						return;
					}
					
					Toast.makeText(mContext,
							menuData.getName()+mContext.getString(R.string.message_favorite_added),
							Toast.LENGTH_SHORT).show();
					
					// shared preference 저장
					psm.saveFavoriteMenu();
					// UI update
					ArrayList<MenuData> favorites = childNodes.get(0);
					ArrayList<MenuData> fromGroup = childNodes.get(groupPosition);
					for (MenuData menu : fromGroup) {
						if (menu.getUid()==uid) {
							favorites.add(menu);
						}
					}
					
				} else {
					// 즐겨찾기 취소
					boolean success = psm.removeFavoriteMenu(uid);
					if (!success) {
						// 존재하지 않는 메뉴, 삭제실패
						return;
					}
					
					// UI update
					ArrayList<MenuData> favorites = childNodes.get(0);
					ArrayList<MenuData> fromGroup = childNodes.get(groupPosition);
					MenuData removeMenu = null;
					for (MenuData menu : fromGroup) {
						if (menu.getUid()==uid) {
							removeMenu = menu;
						}
					}
					favorites.remove(removeMenu);
				}
				notifyDataSetChanged();
			}
		});
		return view;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = convertView;
		
		/**
		 *  +/- group indicator
		 */
		ToggleButton tgbExpandable = null;
		/**
		 * 메뉴명
		 */
		TextView textView = null;
		if (view == null) {
			view = inflater.inflate(R.layout.drawer_parent_item, null);
		}
		tgbExpandable = (ToggleButton) view.findViewById(R.id.tgb_expandable);
		textView = (TextView) view.findViewById(R.id.tv_parent_menu_nm);
		tgbExpandable.setChecked(isExpanded);
		
		/**
		 * 메뉴명
		 */
		int size = getChildrenCount(groupPosition);
		textView.setText(parentNodes.get(groupPosition).getName()+" ("+size+")");
		
		boolean blackTheme = mContext.getApplicationInfo().theme == R.style.Theme_Black;
		/**
		 * background color 지정
		 */
		int favoriteResId = blackTheme ? 
				R.drawable.selector_menu_favorite_bg_bl : R.drawable.selector_menu_favorite_bg_wh;
		
		int parentResId = blackTheme ? 
				R.drawable.selector_menu_parent_bg_bl : R.drawable.selector_menu_parent_bg_wh;
		
		if (groupPosition==0) {
			// 즐겨찾기 메뉴
			view.setBackgroundResource(favoriteResId);
		} else {
			// 기타메뉴
			view.setBackgroundResource(parentResId);
		}
		
		return view;
	}

	@Override
	public MenuData getChild(int groupPosition, int childPosition) {
		return childNodes.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childNodes.get(groupPosition).size();
	}

	@Override
	public MenuData getGroup(int groupPosition) {
		return parentNodes.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return parentNodes.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
