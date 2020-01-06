package com.bairock.zhongchuan.qz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.view.activity.LoginActivity;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		// initBaiduPush();
		// initData();
		int RunCount = Utils.getIntValue(this, "RUN_COUNT");
		if (RunCount == 0) {
			// TODO 引导页面
		} else {
			Utils.putIntValue(this, "RUN_COUNT", RunCount++);
		}
		Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
				Constants.LoginState);
		if (isLogin) {
			// Intent intent = new Intent(this, UpdateService.class);
			// startService(intent);
			getLogin();
		} else {
			mHandler.sendEmptyMessage(0);
		}
	}

	private void getLogin() {
		String name = Utils.getValue(this, Constants.User_ID);
		String pwd = Utils.getValue(this, Constants.PWD);
		if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name))
			getChatserive(name, pwd);
		else {
			Utils.RemoveValue(SplashActivity.this, Constants.LoginState);
			mHandler.sendEmptyMessageDelayed(0, 600);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
					Constants.LoginState);
			Intent intent = new Intent();
			if (isLogin) {
				intent.setClass(SplashActivity.this, MainActivity.class);
			} else {
				intent.setClass(SplashActivity.this, LoginActivity.class);
			}
			startActivity(intent);
			overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
			finish();
		}
	};

	private void getChatserive(final String userName, final String password) {
		EMChatManager.getInstance().login(userName, password, new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						runOnUiThread(new Runnable() {
							public void run() {
								// TODO 保存用户信息
								Utils.putBooleanValue(SplashActivity.this,
										Constants.LoginState, true);
								Utils.putValue(SplashActivity.this,
										Constants.User_ID, userName);
								Utils.putValue(SplashActivity.this,
										Constants.PWD, password);

								Log.e("Token", EMChatManager.getInstance()
										.getAccessToken());
								Log.d("main", "登陆聊天服务器成功！");
								// 加载群组和会话
								EMGroupManager.getInstance().loadAllGroups();
								EMChatManager.getInstance()
										.loadAllConversations();
								mHandler.sendEmptyMessage(0);
							}
						});
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.d("main", "登陆聊天服务器失败！");
					}
				});
	}

}
