package com.hkh.myz.views;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hkh.myz.R;
import com.hkh.myz.manager.PlayerStudio;

public class CustomAudioView extends LinearLayout
	implements OnPreparedListener, OnCompletionListener, OnSeekBarChangeListener {

	private static String TAG = CustomAudioView.class.getSimpleName();
	Context mContext;
	
	/**
	 * ToggleButton 체크되면 playing 상태
	 */
	ToggleButton tgbPlayPause;
	
	SeekBar seekBar;
	
	TextView tvDuration;
	
	MediaPlayer mediaPlayer;
	
	String audioLink = "";
	
	/**
	 * SeekBar setProgress 가능 여부
	 */
	boolean seekBarUpdatable = true;
	
	/**
	 * updateProgress Timer 호출 여부
	 */
	boolean mUpdateProgress = false;
	private Runnable mUpdateRunnable = new Runnable() {
	    @Override
	    public void run() {
	    	updateProgress();
	        if (mUpdateProgress) {
	            postDelayed(mUpdateRunnable, 1000);
	        }
	    }
	};
	
	public CustomAudioView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.layout_audioview, this, true);
		tgbPlayPause = (ToggleButton) view.findViewById(R.id.tgb_play_pause);
		seekBar = (SeekBar) view.findViewById(R.id.seek_bar_audio);
		tvDuration = (TextView) view.findViewById(R.id.music_duration);
		
		tgbPlayPause.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					boolean isPrepared = PlayerStudio.getInstance().isAudioPrepared();
					if (isPrepared) {
						mediaPlayer.start();
						playAudio();
					}
				} else {
					mediaPlayer.pause();
					pauseAudio();
				}
			}
		});
		seekBar.setOnSeekBarChangeListener(this);
	}
	
	private void playAudio() {
		tgbPlayPause.setChecked(true);
		postDelayed(mUpdateRunnable, 1000);
		mUpdateProgress = true;
		PlayerStudio.getInstance().setAudioPlaying(true);
	}
	
	private void pauseAudio() {
		tgbPlayPause.setChecked(false);
		mUpdateProgress = false;
		PlayerStudio.getInstance().setAudioPlaying(false);
	}
	
	@Override
	protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    
	    mUpdateProgress = false;
	    if (mediaPlayer != null) {
	    	mediaPlayer.release();
	    	mediaPlayer = null;
	    	PlayerStudio.getInstance().setMediaPlayer(null);
	    	PlayerStudio.getInstance().setDatasourceSet(false);
	    	PlayerStudio.getInstance().setAudioPrepared(false);
	    	PlayerStudio.getInstance().setAudioPlaying(false);
	    }
	}

	public void setAudioLink(String audioLink) {
		// restore MediaPlayer
		mediaPlayer = PlayerStudio.getInstance().getMediaPlayer();
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		if (PlayerStudio.getInstance().isAudioPlaying()) {
			playAudio();
			
		} else if (PlayerStudio.getInstance().isAudioPrepared()) {
			postDelayed(mUpdateRunnable, 1000);
			
		}
		
		if (PlayerStudio.getInstance().isDatasourceSet()) {
			return;
		}
		PlayerStudio.getInstance().setDatasourceSet(true);
		this.audioLink = audioLink;
		
		// pull mp3 music data
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", "mcheck=ok");
		try {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(mContext, Uri.parse(audioLink), headers);
			Log.d(TAG, "CustomAudioView#setDataSource");
		} catch (Exception e) {
			e.printStackTrace();
			PlayerStudio.getInstance().setDatasourceSet(false);
		}
		mediaPlayer.prepareAsync();
	}

	/**
	 * audio ready 상태
	 */
	@Override
	public void onPrepared(MediaPlayer mp) {
		PlayerStudio.getInstance().setAudioPrepared(true);
		int duration = mp.getDuration() / 1000;
		seekBar.setMax(duration);
		mediaPlayer.start();
		playAudio();
	}
	
	/**
	 * SeekBar, Duration Text 업데이트
	 */
	private void updateProgress() {
		if (mediaPlayer != null) {
    		int current = mediaPlayer.getCurrentPosition() / 1000;
    		if (seekBarUpdatable) {
    			seekBar.setMax(mediaPlayer.getDuration()/1000);
    			seekBar.setProgress(current);
    			updateDurationText(current, seekBar.getMax());
    		}
    	}
	}

	private void updateDurationText(int current, int total) {
		String timeFormat = "%d:%02d / %d:%02d";
		int currentMin = current / 60;
		int currentSec = current - currentMin * 60;
		int totalMin = total / 60;
		int totalSec = total - totalMin * 60;
		String timeText = String.format(timeFormat, currentMin, currentSec, totalMin, totalSec);
		tvDuration.setText(timeText);
	}
	
	/**
	 * 재생 완료
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		mUpdateProgress = false;
		tgbPlayPause.setChecked(false);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			updateDurationText(progress, seekBar.getMax());
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		seekBarUpdatable = false;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		seekBarUpdatable = true;
		if(mediaPlayer != null){
			Log.i(TAG, "onProgressChanged");
			seekBar.setMax(seekBar.getMax());
			mediaPlayer.seekTo(seekBar.getProgress() * 1000);
		}
	}
}
