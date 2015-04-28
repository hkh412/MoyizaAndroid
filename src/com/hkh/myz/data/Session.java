package com.hkh.myz.data;

public class Session {

	private String passport = "";
	private String sessionId = "";
	private DataHashMap personalInfo;
	
	public Session(String passport, String sessionId) {
		this.passport = passport;
		this.sessionId = sessionId;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getPassPort() {
		return passport;
	}
	public void setPassPort(String passport) {
		this.passport = passport;
	}
	
	public DataHashMap getPersonalInfo() {
		return personalInfo;
	}
	public void setPersonalInfo(DataHashMap map) {
		personalInfo = map;
	}
}
