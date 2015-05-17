package com.hkh.myz.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.res.AssetManager;

import com.androidquery.AQuery;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.hkh.myz.events.OnJavascriptEventListener;

public class LoginProcessService extends ServiceBase implements JsCallback {

	Context mContext = null;
	String script = null;
	
	String username = null;
	String password = null;
	String tfrom = null;
	String tcode = null;
	
	OnJavascriptEventListener mListener = null;
	
	public LoginProcessService(Context context) {
		super(context);
		mContext = context;
	}
	
	public void setAccount(String username, String password) {
		this.username = username;
		this.password = password;
//		RequestFileService service = new RequestFileService(mContext);
//		service.setHeaders();
//		service.setService("http://m.moyiza.com/common/js/mencoder.min.php?ver=1412031758", this, "onRequestFileResult");
//		service.request();
	}
	
	public void setJavaScript(final String script) {
		this.script = script;
		evalScript(script, tfrom, this);
	}
	
	public void setTFromCode(String tfrom) {
		this.tfrom = tfrom;
	}
	
	public void setOnJavascriptEventListener(OnJavascriptEventListener listener) {
		this.mListener = listener;
	}
	
//	public void onRequestFileResult(String url, String result, AjaxStatus status) {
//		if (result == null) {
//			result = getScriptFromAssets();
//		}
//		evalScript(result, username);
//	}
	
	private String getScriptFromAssets() {
		String js="";
		try {
			String line = null;
			AssetManager assetManager = mContext.getAssets();
			BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open("mencoder.min.js")));
			while ((line=br.readLine())!= null) {
				js += line;
			}
		} catch (Exception e) {
		}
		return js;
	}
	
	private void evalScript(String js, String param, JsCallback callback) {
		JsEvaluator jsEvaluator = new JsEvaluator(mContext);
		jsEvaluator.evaluate(js);
		jsEvaluator.callFunction(js, this, "mencoder2", param);
	}

	@Override
	public void onResult(String result) {
		if (tcode == null) {
			tcode = result;
			evalScript(this.script, this.username, this);
			return;
		}
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("user_id", username));                         
        pairs.add(new BasicNameValuePair("password", password));    
        pairs.add(new BasicNameValuePair("tcode1", tcode));
        pairs.add(new BasicNameValuePair("code", result));
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", "http://m.moyiza.com/login.php");
		
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        setParams(params);
        setHeaders(headers);
        
        if (mListener != null) {
        	mListener.onEvalComplete(this);
        }
	}

}
