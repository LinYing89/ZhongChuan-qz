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
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.library.live.Player;
import com.library.live.stream.UdpRecive;
import com.library.live.vd.VDDecoder;
import com.library.live.view.PlayerView;
import com.library.talk.Listen;
import com.library.talk.stream.ListenRecive;

public class VoiceUploadThirdActivity extends AppCompatActivity {

    private Chronometer chronometer;
    public static Listen listen;
    private ImageView imgHangUp;
    private TextView txtMessage;
    private String mainServerIp;
    // 第三方设备ip
    private String thirdIp;
    private AskBroadcastReceiver receiver;

    private SendUdpThread sendUdpThread;
    private boolean upload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_upload_third);
        App.getInstance2().addActivity(this);
        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VOICE_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        findViews();

        thirdIp = UserUtil.findSoundRecorderIp();
        if (thirdIp == null) {
            Toast.makeText(this, "便携式录音设备不在线", Toast.LENGTH_SHORT).show();
            finish();
        }else {

            mainServerIp = UserUtil.findMainServerIp();
            if (mainServerIp == null) {
                Toast.makeText(this, "信息处理设备不在线", Toast.LENGTH_SHORT).show();
//            finish();
            }
        }

        txtMessage.setText("正在请求便携式录音设备...");
        sendUdpThread = new SendUdpThread(UdpMessageHelper.createVoiceCallThirdAsk(UserUtil.user.getUsername()), thirdIp);
        sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
            @Override
            public void onNoAnswer() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VoiceUploadThirdActivity.this, "便携式录音设备无应答", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            }
        });
        sendUdpThread.start();
//        MessageBroadcaster.sendIp(UdpMessageHelper.createVoiceCallThirdAsk(UserUtil.user.getUsername()), thirdIp);

    }

    private void findViews() {
        txtMessage = findViewById(R.id.txtMessage);
        imgHangUp = findViewById(R.id.imgHangUp);
        chronometer = findViewById(R.id.chronometer);
        initListen();

        imgHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBroadcaster.sendIp(UdpMessageHelper.createCallThirdStopAsk(UserUtil.user.getUsername()), mainServerIp);
                MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), mainServerIp);
                finish();
            }
        });
    }

    private void initListen() {
        listen = new Listen.Buider()
                .setPullMode(new ListenRecive())
                .setMultiple(1)//音频调节，倍数限制为1-8倍。1为原声,放大后可能导致爆音。
                .build();
        listen.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != listen) {
            listen.stop();
            listen.destroy();
            listen = null;
        }
        chronometer.stop();
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(null != sendUdpThread){
            sendUdpThread.interrupt();
        }
    }

    private void startVoice(){
        txtMessage.setText("");
        chronometer.start();

//        player.start();
    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            SendUdpThread.answered = true;
            if(null != sendUdpThread) {
                sendUdpThread.interrupt();
            }

            String result = intent.getStringExtra("result");
            String source = intent.getStringExtra("source");
            if(source.equals("third")){
                if (result.equals("0")) {
                    //接受
                    txtMessage.setText("正在请求信息处理设备...");
                    sendUdpThread = new SendUdpThread(UdpMessageHelper.createVoiceCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
                    sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
                        @Override
                        public void onNoAnswer() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "信息处理设备无应答", Toast.LENGTH_SHORT).show();
//                                    finish();
                                }
                            });
                        }
                    });
                    sendUdpThread.start();
                    startVoice();
//                    MessageBroadcaster.sendIp(UdpMessageHelper.createVoiceCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
                } else if (result.equals("1")) {
                    //拒绝1/挂断2
                    Toast.makeText(VoiceUploadThirdActivity.this, "第三方设备拒绝请求", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else {
                if (result.equals("0")) {
                    //接受
                    upload = true;
//                    startVoice();
                } else if (result.equals("1")) {
                    //拒绝1/挂断2
                    Toast.makeText(VoiceUploadThirdActivity.this, "信息处理设备拒绝请求", Toast.LENGTH_SHORT).show();
//                    finish();
                }
            }
        }
    }
}
