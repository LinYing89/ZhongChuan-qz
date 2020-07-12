package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.utils.Config;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.SharedHelper;

import java.lang.ref.WeakReference;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchRunOnPowerOn;
    private TextView txtMap;
    private Button btnLogout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        App.getInstance2().addActivity(this);
        findViews();
        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != progressDialog && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void findViews(){
        switchRunOnPowerOn = findViewById(R.id.switchRunOnPowerOn);
        txtMap = findViewById(R.id.txtMap);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setListener(){
        switchRunOnPowerOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedHelper sharedHelper = new SharedHelper();
                boolean isRun = switchRunOnPowerOn.isChecked();
                if(Config.runOnPowerOn != isRun){
                    Config.runOnPowerOn = isRun;
                    sharedHelper.putRunOnPowerOn();
                }
            }
        });
        txtMap.setOnClickListener(onClickListener);
        btnLogout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.txtMap:
                    startActivity(new Intent(SettingsActivity.this, OfflineMapActivity.class));
                    break;
                case R.id.btnLogout:
                    progressDialog = new ProgressDialog(SettingsActivity.this);
                    progressDialog.setMessage("正在注销，请稍后......");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    LoginOutTask toMainTask = new LoginOutTask(SettingsActivity.this);
                    toMainTask.execute();
                    break;
            }
        }
    };

    private static class LoginOutTask extends AsyncTask<Void, Void, Boolean> {
        WeakReference<SettingsActivity> mActivity;
        LoginOutTask(SettingsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SettingsActivity theAct = mActivity.get();
            try {
                Config.username = "";
                Config.password = "";
                SharedHelper sharedHelper = new SharedHelper();
                sharedHelper.putUser();
                Intent i3 = new Intent(ConversationUtil.LOGOUT_ACTION);
                i3.putExtra("exit", 0);
                App.getInstance().sendOrderedBroadcast(i3, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                SettingsActivity theAct = mActivity.get();
                //theAct.startActivity(new Intent(theAct,LoginActivity.class));
                theAct.finish();
                //杀死整个进程
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
