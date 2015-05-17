package com.hkh.myz.manager;

import java.util.List;

import org.apache.http.cookie.Cookie;

public class CookieManager {

	private List<Cookie> cookies = null;
	private static CookieManager instance = null;
	
	private CookieManager() {
	}
	public static CookieManager getInstance() {
		if (instance == null) {
			instance = new CookieManager();
		}
		return instance;
	}
	
	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}
	
	public List<Cookie> getCookies() {
		return cookies;
	}
	
	public boolean hasCookies() {
		return cookies != null;
	}
	
	public String getCookieSSID() {
		if (!hasCookies())
			return "";
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("114_ssid")) {
				return cookie.getValue();
			}
		}
		return "";
	}
}
