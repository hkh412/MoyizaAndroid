package com.hkh.myz.manager;

import com.hkh.myz.data.Session;

public class SessionManager {
	
	private static SessionManager instance = null;
	private Session session = null;
	
	private SessionManager() {
	}
	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public boolean hasSession() {
		return this.session != null;
	}
}
