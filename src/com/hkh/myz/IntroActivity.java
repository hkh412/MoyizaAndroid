package com.hkh.myz;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.hkh.myz.R;
import com.hkh.myz.config.Config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Window;

public class IntroActivity extends Activity {

	InterstitialAd interstitial;
	Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		setIntroAdView();
	}

	private void setIntroAdView() {
		if (!Config.AdInterstitialEnable) {
			Intent intent = new Intent(mContext, MainActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		// Create ad request.
	    AdRequest adRequest = new AdRequest.Builder().build();

	    // Begin loading your interstitial.
		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId(mContext.getString(R.string.ad_unit_interstitial_id));
		interstitial.loadAd(adRequest);
		
		String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID); 
		AdRequest.Builder builder = new AdRequest.Builder();
        if (Config.ADVIEW_TEST) {
        	builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        	.addTestDevice(deviceId);
        }
        adRequest = builder.build();
//        mAdView.loadAd(adRequest);
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {

			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				displayInterstitial();
			}

			@Override
			public void onAdClosed() {
				super.onAdClosed();
				Intent intent = new Intent(mContext, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	public void displayInterstitial() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}
	
}
