package com.hkh.myz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.cookie.Cookie;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.hkh.myz.config.Consts;
import com.hkh.myz.data.Session;
import com.hkh.myz.manager.CookieManager;
import com.hkh.myz.manager.SessionManager;

public abstract class ServiceBase {

	private static final String MOBILE_USER_AGENT = 
			"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us)"+
			"AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5";
	
	Context mContext;
	AjaxCallback<String> cb;
	
	public ServiceBase(Context context) {
		mContext = context;
		cb = new AjaxCallback<String>();
		setCookies(CookieManager.getInstance().getCookies());
	}
	
	public <T> void attachSession() {
		Session session = SessionManager.getInstance().getSession();
		if (session != null) {
			Map<String, String> cookies = new HashMap<String, String>();
			cookies.put(Consts.C_PASSPORT, session.getPassPort());
			cookies.put(Consts.M_SESSION_ID, session.getSessionId());
			cb.cookies(cookies);
		}
	}
	
	/**
	 * 서비스 설정
	 * @param url 
	 * @param handler - callback 메서드를 포함한 클래스 인스턴스
	 * @param handlerName - callback 메서드명
	 */
	public void setService(String url, Object handler, String handlerName) {
		cb.url(url).type(String.class).weakHandler(handler, handlerName);
	}
	
	/**
	 * http post body params
	 * @param params
	 */
	public void setParams(Map<String, Object> params) {
		cb.params(params);
	}
	
	/**
	 * http request headers
	 * @param headers
	 */
	public void setHeaders(Map<String, String> headers) {
		if (headers != null) {
			cb.headers(headers);
		}
	}
	
	/**
	 * http set request cookies
	 * @param cookies
	 */
	public void setCookies(List<Cookie> cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cb.cookie(cookie.getName(), cookie.getValue());
			}
		}
	}
	
	/**
	 * http request 실행
	 */
	public void request() {
		if (cb != null) {
			AQuery aq = new AQuery(mContext);
			attachSession();
			cb.header("User-Agent", MOBILE_USER_AGENT);
			aq.ajax(cb);
		}
	};
}
