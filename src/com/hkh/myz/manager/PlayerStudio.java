package com.hkh.myz.manager;

import android.media.MediaPlayer;

public class PlayerStudio {

	private PlayerStudio() {
		
	}
	
	private static MediaPlayer mediaPlayer;
	private static PlayerStudio instance;
	private boolean isDatasourceSet = false;
	
	/**
	 * MediaPlayer ready 상태 여부
	 */
	private boolean isAudioPrepared = false;
	
	/**
	 * MediaPlayer 초기화 여부
	 */
	private boolean isAudioPlaying = false;
	
	public static PlayerStudio getInstance() {
		if (instance == null) {
			instance = new PlayerStudio();
		}
		return instance;
	}
	
	public MediaPlayer getMediaPlayer() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
		return mediaPlayer;
	}
	
	public void setMediaPlayer(MediaPlayer player) {
		mediaPlayer = player;
	}
	
	public boolean isDatasourceSet() {
		return isDatasourceSet;
	}
	
	public void setDatasourceSet(boolean isDatasourceSet) {
		this.isDatasourceSet = isDatasourceSet;
	}
	
	public boolean isAudioPrepared() {
		return isAudioPrepared ;
	}
	public void setAudioPrepared(boolean prepared) {
		this.isAudioPrepared = prepared;
	}
	
	public boolean isAudioPlaying() {
		return isAudioPlaying  ;
	}
	public void setAudioPlaying(boolean playing) {
		this.isAudioPlaying = playing;
	}
}
