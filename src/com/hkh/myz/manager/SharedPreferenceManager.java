package com.hkh.myz.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
	private static SharedPreferenceManager instance = null;
	Context mContext;
	
	private SharedPreferenceManager(Context context) {
		mContext = context;
	}
	
	public static SharedPreferenceManager getInstance(Context context) {
		if (instance == null) {
			instance = new SharedPreferenceManager(context);
		}
		return instance;
	}
	
	public void putString(String key, String value) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		sharedPreferences.edit().putString(key, value).commit();
	}
	
	public void putInt(String key, int value) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		sharedPreferences.edit().putInt(key, value).commit();
	}
	
	public void putBoolean(String key, boolean value) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		sharedPreferences.edit().putBoolean(key, value).commit();
	}
	
	public String getString(String key) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, null);
	}
	
	public int getInt(String key) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		return sharedPreferences.getInt(key, -1);
	}
	
	public boolean getBoolean(String key) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(key, false);
	}
	
}
