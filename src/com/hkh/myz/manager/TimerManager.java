package com.hkh.myz.manager;

import java.util.Timer;
import java.util.TimerTask;


public class TimerManager {

	private static TimerManager instance = null;
	private Timer mTimer;
	private CountDownTask mTimerTask;
	
	private TimerManager() {
	}
	
	public static TimerManager getInstance() {
		if (instance == null) {
			instance = new TimerManager();
		}
		return instance;
	}
	
	public void setCountDown(int seconds) {
		mTimerTask = new CountDownTask(seconds);
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, 0, 1000);
	}
	
	/**
	 * 댓글쓰기 Timer reset 여부
	 * true 이면 글쓰기 가능, false 불가능
	 * @return
	 */
	public boolean isTimerReset() {
		if (mTimerTask == null) {
			return true;
		}
		return mTimerTask.getRemainSeconds() <= 0;
	}
	
	static class CountDownTask extends TimerTask {
		int remainSeconds = 0;
		public CountDownTask(int seconds) {
			this.remainSeconds = seconds;
		}
		
		public int getRemainSeconds() {
			return this.remainSeconds;
		}
		
		@Override
		public void run() {
			remainSeconds = remainSeconds-1;
			if (remainSeconds<0)
				remainSeconds = 0;
		}
	}
}
