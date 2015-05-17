package com.hkh.myz.manager;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.hkh.myz.R;
import com.hkh.myz.config.Consts;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.Session;
import com.hkh.myz.events.OnJavascriptEventListener;
import com.hkh.myz.fragments.LoginDialogFragment;
import com.hkh.myz.fragments.LoginDialogFragment.LoginListener;
import com.hkh.myz.service.LoginFormService;
import com.hkh.myz.service.LoginProcessService;
import com.hkh.myz.service.LogoutProcessService;
import com.hkh.myz.service.ServiceBase;

public class LoginManager implements OnJavascriptEventListener {
	private static LoginManager instance = null;
	private LoginListener mLoginListener = null;
	private LoginDialogFragment loginFragment = null;
	
	private Context mContext = null;
	private String username = null;
	private String password = null;
	
	boolean doingLogin = false;
	boolean doingLogout = false;
	
	private LoginManager() {
	}
	
	public static LoginManager getInstance() {
		if (instance == null) {
			instance = new LoginManager();
		}
		return instance;
	}

	public void setLoginListener(LoginListener loginListener) {
		this.mLoginListener = loginListener;
	}
	
	public void setLoginListener(LoginDialogFragment loginFragment, LoginListener loginListener) {
		this.loginFragment = loginFragment;
		this.mLoginListener = loginListener;
	}
	
	/**
	 * 모이자 로그인 함수
	 * @param context
	 * @param username
	 * @param password
	 * @param listener
	 */
	public void doLogin(Context context, String username, String password) {
		this.mContext = context;
		this.username = username;
		this.password = password;
		
		if (doingLogin) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_login_is_processing), Toast.LENGTH_SHORT).show();
			return;
		}
		doingLogin = true;
		
		LoginFormService service = new LoginFormService(context);
		service.setService(Links.LOGIN_FORM_URL, this, "onLoginFormResult");
		service.request();
	}
	
	public void onLoginFormResult(String url, String result, AjaxStatus status) {
		// set cookies
		if (!CookieManager.getInstance().hasCookies()) {
			List<Cookie> cookies = status.getCookies();
			CookieManager.getInstance().setCookies(cookies);
		}
		
		String errorMsg = "";
		if (result == null) {
			mLoginListener.onLoginFailure(errorMsg);
			return;
		}
        Document document = Jsoup.parse(result);
        Element body = document.body();
        Elements scripts = body.select("script, jscript");
        String mencoder = "";
        String tfrom = "";
        try {
        	if (scripts.size()>0) {
        		mencoder = scripts.get(scripts.size()-1).html();
        		int index = mencoder.indexOf(mContext.getString(R.string.script_remove));
        		if (index>0) {
        			mencoder = mencoder.substring(0, index);
        		}
        	}
        	tfrom = body.select("#login_form input[name=tfrom]").val();
        } catch (Exception e) {};
		
		LoginProcessService service = new LoginProcessService(mContext);
		// this calls onEvalComplete method
		service.setOnJavascriptEventListener(this);
		service.setService(Links.LOGIN_URL, this, "onLoginResult");
		service.setAccount(username, password);
		service.setTFromCode(tfrom);
		service.setJavaScript(mencoder);
	}
	
	/**
	 * 로그인 처리 callback
	 * @param url
	 * @param result
	 * @param status
	 */
	public void onLoginResult(String url, String result, AjaxStatus status) {
		doingLogin = false;
		
		String errorMsg = "";
		if (result == null) {
			mLoginListener.onLoginFailure(errorMsg);
			return;
		}
		try {
			JSONObject json = new JSONObject(result);
			if (json.has("suc") && json.getInt("suc")==1) {
				setSessionFromCookie(status);
				mLoginListener.onLoginComplete(loginFragment);
				return;
				
			} else {
				errorMsg = json.getString("msg");
			}
		} catch (JSONException e) {
			errorMsg = mContext.getString(R.string.message_login_fail);
		}
		mLoginListener.onLoginFailure(errorMsg);
	}
	
	private void setSessionFromCookie(AjaxStatus status) {
		if (status.getCode() == 200) {
			List<Cookie> cookies = status.getCookies();
			String passport = null;
			String sessionId = null;
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(Consts.C_PASSPORT)) {
					passport = cookie.getValue();
					
				} else if (cookie.getName().equals(Consts.M_SESSION_ID)) {
					sessionId = cookie.getValue();
					
				}
			}
			
			if (passport != null) {
				Session session = new Session(passport, sessionId);
				SessionManager.getInstance().setSession(session);
			}
		}
	}
	
	/**
	 * 모이자 로그아웃 함수
	 */
	public void doLogout(Context context) {
		if (doingLogout) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_logout_is_processing), Toast.LENGTH_SHORT).show();
			return;
		}
		doingLogout = true;
		
		LogoutProcessService service = new LogoutProcessService(context);
		service.setService(Links.LOGOUT_URL, this, "onLogoutResult");
		service.request();
	}
	
	/**
	 * 로그아웃 callback
	 * @param url
	 * @param result
	 * @param status
	 */
	public void onLogoutResult(String url, String result, AjaxStatus status) {
		doingLogout = false;
		
		String errorMsg = "";
		if (result == null) {
			mLoginListener.onLogoutFailure(errorMsg);
			return;
		}
		try {
			JSONObject json = new JSONObject(result);
			if (json.has("suc") && json.getInt("suc")==1) {
				setSessionFromCookie(status);
				mLoginListener.onLogoutComplete(loginFragment);
				return;
				
			} else {
				errorMsg = json.getString("msg");
			}
		} catch (Exception e) {
		}
		mLoginListener.onLogoutFailure(errorMsg);
	}

	@Override
	public void onEvalComplete(ServiceBase service) {
		service.request();
	}
}
