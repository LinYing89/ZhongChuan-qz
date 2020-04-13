package com.bairock.zhongchuan.qz.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.MainActivity;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.common.DES;
import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.BaseActivity;

import java.lang.ref.WeakReference;

//登陆
public class LoginActivity extends BaseActivity implements OnClickListener {
	private TextView txt_title;
	private ImageView img_back;
	private Button btn_login;
	private EditText et_usertel, et_password;
	private boolean loging = false;
	private ProgressDialog loginDialog;

	public static MyHandler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_login);
		super.onCreate(savedInstanceState);
		handler = new MyHandler(this);
		MessageBroadcaster messageBroadcaster = new MessageBroadcaster();
		messageBroadcaster.bind();
	}

	@Override
	protected void initControl() {
		txt_title = findViewById(R.id.txt_title);
		txt_title.setText("登陆");
		img_back = findViewById(R.id.img_back);
		img_back.setVisibility(View.VISIBLE);
		btn_login = findViewById(R.id.btn_login);
		et_usertel = findViewById(R.id.et_usertel);
		et_password = findViewById(R.id.et_password);
		et_usertel.setText("8080");
		et_password.setText("8080");
	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {
	}

	@Override
	protected void setListener() {
		img_back.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		et_usertel.addTextChangedListener(new TextChange());
		et_password.addTextChangedListener(new TextChange());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			Utils.finish(LoginActivity.this);
			break;
		case R.id.btn_login:
			showDialog();
			getLogin();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						int i = 0;
						while (i < 10) {
							Thread.sleep(1000);
							if (!loging) {
								//登录已返回
								return;
							}
							i++;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//登录超时
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(LoginActivity.this, "登录超时", Toast.LENGTH_SHORT).show();
							closeDialog();
						}
					});
				}
			}).start();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loging = false;
		if(null != loginDialog){
			if(loginDialog.isShowing()){
				loginDialog.dismiss();
			}
			loginDialog = null;
		}
		handler = null;
	}

	private void getLogin() {
		String userName = et_usertel.getText().toString().trim();
		String password = et_password.getText().toString().trim();
		getLogin(userName, password);
	}

	private void getLogin(final String userName, final String password) {
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
			UserUtil.user.setUsername(userName);
			loging = true;
			MessageBroadcaster.sendBroadcast(UdpMessageHelper.createLogin(userName, password));

//			Intent intent = new Intent(LoginActivity.this,
//					MainActivity.class);
//			startActivity(intent);
//			overridePendingTransition(R.anim.push_up_in,
//					R.anim.push_up_out);
//			finish();
		} else {
			Utils.showLongToast(LoginActivity.this, "请填写账号或密码！");
		}
	}

	public void showDialog(){
		loginDialog = new ProgressDialog(this);
		loginDialog.setTitle("正在登录");
//		loginDialog.setIcon(R.mipmap.ic_launcher_round);
		loginDialog.setMessage("请稍等...");
		loginDialog.setCancelable(false);
		loginDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		loginDialog.show();
	}

	public void closeDialog(){
		loginDialog.dismiss();
	}

	// EditText监听器
	class TextChange implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence cs, int start, int before,
				int count) {
			boolean Sign2 = et_usertel.getText().length() > 0;
			boolean Sign3 = et_password.getText().length() > 1;
			if (Sign2 & Sign3) {
				btn_login.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_bg_green));
				btn_login.setEnabled(true);
			} else {
				btn_login.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_enable_green));
				btn_login.setTextColor(0xFFD0EFC6);
				btn_login.setEnabled(false);
			}
		}
	}

	public static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		MyHandler(LoginActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final LoginActivity theActivity = mActivity.get();
			if(!theActivity.loging){
				return;
			}
			theActivity.closeDialog();
			theActivity.loging = false;
			switch (msg.what) {
				case 1:
					//登录失败
					Toast.makeText(theActivity, "登录失败", Toast.LENGTH_SHORT).show();
					break;
				case 0:
					//登录成功
					Toast.makeText(theActivity, "登录成功", Toast.LENGTH_SHORT).show();
					UserUtil.user.setUsername(theActivity.et_usertel.getText().toString());
					Intent intent = new Intent(theActivity, MainActivity.class);
					theActivity.startActivity(intent);
					theActivity.finish();
					break;
			}

		}
	}

}
