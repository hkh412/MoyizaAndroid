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
import com.hkh.myz.util.Util;

import android.content.Context;

/**
 * 게시판 paging 데이터 조회하는 서비스
 * @author hkh
 *
 */
public class QueryListPagingService extends ServiceBase {

	public QueryListPagingService(Context context) {
		super(context);
	}

	@Override
	public void setService(String url, Object handler, String handlerName) {
		super.setService(Links.LIST_PAGING_URL, handler, handlerName);
	}
	
	public void setFormParams(String bbsId, String category, int page, String regionUrl) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("bbs_id", bbsId));                         
        pairs.add(new BasicNameValuePair("category", category));    
        pairs.add(new BasicNameValuePair("mb_no", ""));    
        pairs.add(new BasicNameValuePair("page", String.valueOf(page)));
        String country = Util.getValueFromUrl(regionUrl, "country");
        String province = Util.getValueFromUrl(regionUrl, "province");
        String city = Util.getValueFromUrl(regionUrl, "city");
        if (country != null) {
        	pairs.add(new BasicNameValuePair("country", country));
        }
        if (province != null) {
        	pairs.add(new BasicNameValuePair("province", province));
        }
        if (city != null) {
        	pairs.add(new BasicNameValuePair("city", city));
        }
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        setParams(params);
        setHeaders(bbsId);
	}

	/**
	 * http request headers
	 * @param headers
	 */
	private void setHeaders(String bbsId) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", "http://m.moyiza.com/list.php?bbs_id="+bbsId);
		super.setHeaders(headers);
	}
}
