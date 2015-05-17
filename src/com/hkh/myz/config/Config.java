package com.hkh.myz.config;

public class Config {

	/**
	 *  Log 메시지 출력 여부
	 */
	public static boolean LogEnable = false;
	
	/**
	 * 광고뷰 테스트모드
	 */
	public static boolean ADVIEW_TEST = false;
	
	/**
	 * 광고 로딩여부
	 */
	public static boolean AdEnable = true;
	
	public static boolean AdInterstitialEnable = false;
	
	/**
	 * 광고 Toast 여부
	 */
	public static boolean AdToast = false;
	
	/**
	 * 방문한 게시글 저장 리스트의 크기
	 */
	public static int LIST_LIMIT = 100;
	
	/**
	 * AD_BUDDIZ THRESHOLD - MainActivity가 10번 실행된 경우 Adbuddiz 전면광고가 노출된다.
	 */
	public static int AD_THRESHOLD = 5;
}
