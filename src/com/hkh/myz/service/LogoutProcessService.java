package com.hkh.myz.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import android.content.Context;

import com.androidquery.AQuery;
import com.hkh.myz.config.Consts;
import com.hkh.myz.data.Session;
import com.hkh.myz.manager.SessionManager;

public class LogoutProcessService extends ServiceBase {

	public LogoutProcessService(Context context) {
		super(context);
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(AQuery.POST_ENTITY, entity);
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", "http://m.moyiza.com/member/my_info.php");
        
        setHeaders(headers);
        setParams(params);
	}
	
	@Override
	public void request() {
		super.request();
	}
	
}
