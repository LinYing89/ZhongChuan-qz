package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.VoiceBroadcaster;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.library.common.UdpControlInterface;
import com.library.talk.Listen;
import com.library.talk.Speak;
import com.library.talk.stream.ListenRecive;
import com.library.talk.stream.SpeakSend;

import java.util.Arrays;

public class VoiceCallActivity extends AppCompatActivity {

    private static String TAG = "VideoCallActivity";

    private TextView txtTo;
    private Chronometer chronometer;
    private ImageView imgMute;
    private ImageView imgHangUp;
    private ImageView imgSpeaker;

    private String ip;

    private Speak speak;
    public static Listen listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        findViews();

        String name = getIntent().getStringExtra(Constants.NAME);
        txtTo.setText(name);
        ip = UserUtil.findIpByUsername(name);
        if(null == ip){
            Logger.e(TAG, "ip is null");
            finish();
        }
//        ip = "192.168.1.6";
        Logger.e(TAG, "ip:" + ip);

        setListener();

        speak = new Speak.Buider()
                .setPushMode(new SpeakSend(ip, 5555))
                .setPublishBitrate(24 * 1024)//音频推流采样率
//                .setVoiceDirPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "VideoTalk")//录制路径,当前为默认路径
                .setUdpControl(new UdpControlInterface() {
                    @Override
                    public byte[] Control(byte[] bytes, int offset, int length) {
                        VoiceBroadcaster.send(bytes, ip);
                        return new byte[0];
//                        return Arrays.copyOf(bytes, length);
                    }
                })
                .build();
        speak.start();

        listen = new Listen.Buider()
                .setPullMode(new ListenRecive())
                .setMultiple(1)//音频调节，倍数限制为1-8倍。1为原声,放大后可能导致爆音。
                .build();
        listen.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        publishHe.stopRecode();//停止录制
        if(null != speak) {
            speak.stop();
            speak.destroy();
        }

        if(null != listen) {
            listen.stop();
            listen.destroy();
        }

        if(null != chronometer) {
            chronometer.stop();
        }
    }

    private void findViews(){
        txtTo = findViewById(R.id.txtTo);
        chronometer = findViewById(R.id.chronometer);
        chronometer.start();
        imgMute = findViewById(R.id.imgMute);
        imgHangUp = findViewById(R.id.imgHangUp);
        imgSpeaker = findViewById(R.id.imgSpeaker);
    }

    private void setListener(){
        imgMute.setOnClickListener(onClickListener);
        imgHangUp.setOnClickListener(onClickListener);
        imgSpeaker.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgMute:
                    break;
                case R.id.imgHangUp:
                    finish();
                    break;
                case R.id.imgSpeaker:
                    break;
            }
        }
    };
}
