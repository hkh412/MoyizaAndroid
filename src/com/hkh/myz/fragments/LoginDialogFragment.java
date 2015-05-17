package com.hkh.myz.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hkh.myz.R;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;
import com.hkh.myz.data.Session;
import com.hkh.myz.manager.LoginManager;
import com.hkh.myz.manager.SessionManager;
import com.hkh.myz.manager.SharedPreferenceManager;

public class LoginDialogFragment extends DialogFragment {

	protected static final String TAG = LoginDialogFragment.class.getSimpleName();
	Context mContext;
	LoginListener mListener;
	EditText etUsername;
	EditText etPassword;
	TextView tvNickname;
	TextView tvLevel;
	TextView tvPoint;
	TextView tvMemberJoin;
	boolean hasSession = false;
	
	public interface LoginListener {
		void onLoginComplete(LoginDialogFragment loginFragment);
		void onLoginFailure(String message);
		void onLogoutComplete(LoginDialogFragment loginFragment);
		void onLogoutFailure(String message);
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity();
		
		boolean blackTheme = mContext.getApplicationInfo().theme == R.style.Theme_Black;
		int theme = blackTheme ? AlertDialog.THEME_DEVICE_DEFAULT_DARK : AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext, theme);
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // 로그인 상태 판단 (로그인/로그아웃) 결정
	    View loginView;
	    int textId;
	    Session session = SessionManager.getInstance().getSession();
	    hasSession = session != null;
	    if (hasSession) {
	    	// 로그인 됨 / 로그아웃 대화창 열림
	    	loginView = inflater.inflate(R.layout.dialog_logout, null);
	    	textId = R.string.logout;
	    	tvNickname = (TextView) loginView.findViewById(R.id.tv_profile_nickname);
			tvLevel = (TextView) loginView.findViewById(R.id.tv_profile_level);
			tvPoint = (TextView) loginView.findViewById(R.id.tv_profile_point);

			DataHashMap map = session.getPersonalInfo();
			try {
				tvNickname.setText(map.get("nick-name"));
				tvLevel.setText(map.get("user-level"));
				tvPoint.setText(map.get("user-point"));
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
	    } else {
	    	loginView = inflater.inflate(R.layout.dialog_login, null);
	    	textId = R.string.login;
		    etUsername = (EditText)loginView.findViewById(R.id.edit_username);
		    etPassword = (EditText)loginView.findViewById(R.id.edit_password);
		    tvMemberJoin = (TextView)loginView.findViewById(R.id.tv_member_join);
		    tvMemberJoin.setText(Html.fromHtml(mContext.getString(R.string.text_member_join)));
		    tvMemberJoin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Links.MEMBER_JOIN));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});
		    
		    // 저장된 아이디 비밀번호가 있으면 셋팅한다.
		    SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		    String userName = spm.getString("username");
		    String passWord = spm.getString("password");
		    if (userName != null) {
		    	etUsername.setText(userName);
		    }
		    if (passWord != null) {
		    	etPassword.setText(passWord);
		    }
	    }
	    
	    builder.setView(loginView)
	    .setPositiveButton(textId, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// do nothing here, will be override onStart()
			}
		})
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		LoginDialogFragment.this.getDialog().cancel();
	    	}
	    });
	    return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog)getDialog();
	    if(dialog != null) {
	    	Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
	        positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	if (hasSession) {
                		onLogoutAction();
                	} else {
                		onLoginAction(etUsername.getText(), etPassword.getText());
                	}
                }
            });
	    }
	}

	public void setLoginListener(LoginListener listener) {
		mListener = listener;
		LoginManager.getInstance().setLoginListener(this, mListener);
	}
	/**
	 * 로그인
	 */
	private void onLoginAction(CharSequence username, CharSequence password) {
		String userName = username.toString().trim();
		String passWord = password.toString().trim();
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(mContext);
		manager.putString("username", userName);
		manager.putString("password", passWord);
		LoginManager.getInstance().doLogin(mContext, userName, passWord);
	}
	
	/**
	 * 로그아웃
	 */
	private void onLogoutAction() {
		// Session 지움
		LoginManager.getInstance().doLogout(mContext);
	}
}
