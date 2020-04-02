package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.netty.VoiceBroadcaster;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.library.common.UdpControlInterface;
import com.library.talk.Listen;
import com.library.talk.Speak;
import com.library.talk.stream.ListenRecive;
import com.library.talk.stream.SpeakSend;

import java.util.Arrays;

public class VoiceCallActivity extends AppCompatActivity {

    private static String TAG = "VideoCallActivity";

    private TextView txtTo;
    private TextView txtMessage;
    private Chronometer chronometer;
    private ImageView imgMute;
    private ImageView imgHangUp;
    private ImageView imgSpeaker;
    private ImageView imgHangUp2;
    private ImageView imgOk;

    private LinearLayout layoutAsk;
    private LinearLayout layoutAns;

    private String name;
    private String ip;

    private Speak speak;
    public static Listen listen;
    private AskBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        name = getIntent().getStringExtra(Constants.NAME);
        String voiceType = getIntent().getStringExtra(Constants.VOICE_TYPE);
        findViews();

        if(voiceType.equals(Constants.VOICE_ASK)){
            // 主动发起请求, 等待对方应答界面
            layoutAsk.setVisibility(View.VISIBLE);
            layoutAns.setVisibility(View.GONE);
            MessageBroadcaster.send(UdpMessageHelper.createVoiceCallAns(UserUtil.user.getUsername()), name);
        }else {
            // 被动接听界面
            txtMessage.setText("");
            layoutAsk.setVisibility(View.GONE);
            layoutAns.setVisibility(View.VISIBLE);
        }

        txtTo.setText(name);
        ip = UserUtil.findIpByUsername(name);
        if(null == ip){
            Logger.e(TAG, "ip is null");
            finish();
        }
//        ip = "192.168.1.6";
        Logger.e(TAG, "ip:" + ip);

        setListener();

        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VOICE_ASK_ACTION);
        // 设置广播的优先级别
//        intentFilter.setPriority(5);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        publishHe.stopRecode();//停止录制
        // 注销广播
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null != speak) {
            speak.stop();
            speak.destroy();
        }

        if(null != listen) {
            listen.stop();
            listen.destroy();
            listen = null;
        }

        if(null != chronometer) {
            chronometer.stop();
        }
    }

    private void findViews(){
        txtTo = findViewById(R.id.txtTo);
        txtMessage = findViewById(R.id.txtMessage);
        chronometer = findViewById(R.id.chronometer);
        imgMute = findViewById(R.id.imgMute);
        imgHangUp = findViewById(R.id.imgHangUp);
        imgSpeaker = findViewById(R.id.imgSpeaker);
        imgHangUp2 = findViewById(R.id.imgHangUp2);
        imgOk = findViewById(R.id.imgOk);
        layoutAsk = findViewById(R.id.layoutAsk);
        layoutAns = findViewById(R.id.layoutAns);
    }

    private void setListener(){
        imgMute.setOnClickListener(onClickListener);
        imgHangUp.setOnClickListener(onClickListener);
        imgSpeaker.setOnClickListener(onClickListener);
        imgHangUp2.setOnClickListener(onClickListener);
        imgOk.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgMute:
                    break;
                case R.id.imgHangUp:
                case R.id.imgHangUp2:
                    MessageBroadcaster.send(UdpMessageHelper.createVoiceCallAsk(UserUtil.user.getUsername(), 1), name);
                    finish();
                    break;
                case R.id.imgSpeaker:
                    break;
                case R.id.imgOk:
                    MessageBroadcaster.send(UdpMessageHelper.createVoiceCallAsk(UserUtil.user.getUsername(), 0), name);
                    layoutAns.setVisibility(View.GONE);
                    layoutAsk.setVisibility(View.VISIBLE);
                    startVoice();
                    break;
            }
        }
    };

    private void startVoice(){
        txtMessage.setText("");
        chronometer.start();
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

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();

            String result = intent.getStringExtra("result");
            if(result.equals("0")){
                //接受
                startVoice();
            }else if(result.equals("1")){
                //拒绝1/挂断2
                Toast.makeText(VoiceCallActivity.this, "对方忙", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
