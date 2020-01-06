package com.bairock.zhongchuan.qz.view.activity;

import android.content.Intent;
import android.os.Bundle;
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

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.MainActivity;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.common.DES;
import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.view.BaseActivity;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.juns.health.net.loopj.android.http.RequestParams;

//登陆
public class LoginActivity extends BaseActivity implements OnClickListener {
	private TextView txt_title;
	private ImageView img_back;
	private Button btn_login;
	private EditText et_usertel, et_password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_login);
		super.onCreate(savedInstanceState);
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
		et_usertel.setText("12345");
		et_password.setText("1234578");
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
			getLogin();
			break;
		default:
			break;
		}
	}

	private void getLogin() {
		String userName = et_usertel.getText().toString().trim();
		String password = et_password.getText().toString().trim();
		getLogin(userName, password);
	}

	private void getLogin(final String userName, final String password) {
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
			RequestParams params = new RequestParams();
			params.put("username", userName);
			params.put("password", DES.md5Pwd(password));
			Intent intent = new Intent(LoginActivity.this,
					MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_up_in,
					R.anim.push_up_out);
			finish();
		} else {
			Utils.showLongToast(LoginActivity.this, "请填写账号或密码！");
		}
	}

	private void getChatserive(final String userName, final String password) {
		EMChatManager.getInstance().login(userName, password, new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						runOnUiThread(new Runnable() {
							public void run() {
								Utils.putBooleanValue(LoginActivity.this,
										Constants.LoginState, true);
								Utils.putValue(LoginActivity.this,
										Constants.User_ID, userName);
								Utils.putValue(LoginActivity.this,
										Constants.PWD, password);
								Log.d("main", "登陆聊天服务器成功！");
								// 加载群组和会话
								EMGroupManager.getInstance().loadAllGroups();
								EMChatManager.getInstance()
										.loadAllConversations();
								Intent intent = new Intent(LoginActivity.this,
										MainActivity.class);
								startActivity(intent);
								overridePendingTransition(R.anim.push_up_in,
										R.anim.push_up_out);
								finish();
							}
						});
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.d("main", "登陆聊天服务器失败！");
						runOnUiThread(new Runnable() {
							public void run() {
								Utils.showLongToast(LoginActivity.this, "登陆失败！");
							}
						});
					}
				});
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
			boolean Sign3 = et_password.getText().length() > 4;
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

}
