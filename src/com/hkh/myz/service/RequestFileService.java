package com.hkh.myz.service;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class RequestFileService extends ServiceBase {

	public RequestFileService(Context context) {
		super(context);
	}
	
	public void setHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", "http://m.moyiza.com/login.php");
		setHeaders(headers);
	}
	
}
