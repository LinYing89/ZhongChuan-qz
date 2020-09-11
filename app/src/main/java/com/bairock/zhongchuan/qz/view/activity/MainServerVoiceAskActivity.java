package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.VideoCallSoundPlayer;

import static com.bairock.zhongchuan.qz.view.fragment.FragmentVideoUpload.SOURCE_LOCAL;

public class MainServerVoiceAskActivity extends AppCompatActivity {

    private ImageView imgLocal;
    private ImageView imgHangup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server_voice_ask);
        findViews();
        setListener();

        VideoCallSoundPlayer.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoCallSoundPlayer.stop();
    }

    private void findViews(){
        imgLocal = findViewById(R.id.imgLocal);
        imgHangup = findViewById(R.id.imgHangup);
    }

    private void setListener(){
        imgLocal.setOnClickListener(onClickListener);
        imgHangup.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.imgLocal:
                    Intent intent = new Intent(MainServerVoiceAskActivity.this, VoiceUploadActivity.class);
                    intent.putExtra("source", SOURCE_LOCAL);
                    intent.putExtra("askMainServer", false);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.imgHangup:
                    String ip = UserUtil.findMainServerIp();
                    MessageBroadcaster.sendIp(UdpMessageHelper.createVoiceCallAns(UserUtil.user.getUsername(), 1), ip);
                    finish();
                    break;
            }
        }
    };
}