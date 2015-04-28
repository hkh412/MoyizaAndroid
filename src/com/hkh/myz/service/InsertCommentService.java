package com.hkh.myz.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.androidquery.AQuery;
import com.hkh.myz.config.Links;

import android.content.Context;

public class InsertCommentService extends ServiceBase {

	public InsertCommentService(Context context) {
		super(context);
	}
	
	@Override
	public void setService(String url, Object handler, String handlerName) {
		super.setService(Links.SEND_COMMENT_URL, handler, handlerName);
	}
	
	public void setComment(String refererUrl, String comment, String bbsId, String postNo) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("mode", "write"));                         
        pairs.add(new BasicNameValuePair("content", comment));                         
        pairs.add(new BasicNameValuePair("bbs_id", bbsId));    
        pairs.add(new BasicNameValuePair("ano", postNo));    
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", refererUrl);
		
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        setHeaders(headers);
        setParams(params);
	}

}
