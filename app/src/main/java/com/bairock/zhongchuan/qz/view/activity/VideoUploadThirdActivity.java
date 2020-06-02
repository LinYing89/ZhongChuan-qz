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

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.fragment.FragmentVideoUpload;
import com.library.live.Player;
import com.library.live.stream.UdpRecive;
import com.library.live.vd.VDDecoder;
import com.library.live.view.PlayerView;

public class VideoUploadThirdActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Player player;
    private ImageView imgHangUp;
    private TextView txtMessage;
    private String mainServerIp;
    // 第三方设备ip
    private String thirdIp;
    private AskBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload_third);
        mainServerIp = UserUtil.findMainServerIp();
        if (mainServerIp == null) {
            Toast.makeText(this, "信息处理终端不在线", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        findViews();

        String source = getIntent().getStringExtra("source");
        if (source.equals(FragmentVideoUpload.SOURCE_TELESCOPE)) {
            thirdIp = UserUtil.findTelescopeIp();
            if (thirdIp == null) {
                Toast.makeText(this, "摄像望远镜不在线", Toast.LENGTH_SHORT).show();
                finish();
            }
            txtMessage.setText("正在请求摄像望远镜...");
            MessageBroadcaster.sendIp(UdpMessageHelper.createVideoCallThirdAsk(UserUtil.user.getUsername()), thirdIp);
        } else if (source.equals(FragmentVideoUpload.SOURCE_DRONE)) {
            thirdIp = UserUtil.findDroneIp();
            if (thirdIp == null) {
                Toast.makeText(this, "无人机不在线", Toast.LENGTH_SHORT).show();
                finish();
            }
            txtMessage.setText("正在请求无人机...");
            MessageBroadcaster.sendIp(UdpMessageHelper.createVideoCallThirdAsk(UserUtil.user.getUsername()), thirdIp);
        }

    }

    private void findViews() {
        txtMessage = findViewById(R.id.txtMessage);
        imgHangUp = findViewById(R.id.imgHangUp);
        chronometer = findViewById(R.id.chronometer);
        initPlayView();

        imgHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBroadcaster.sendIp(UdpMessageHelper.createCallThirdStopAsk(UserUtil.user.getUsername()), mainServerIp);
                MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), mainServerIp);
                finish();
            }
        });
    }

    private void initPlayView() {
        player = new Player.Buider((PlayerView) findViewById(R.id.playerView))
//                .setPullMode(new UdpRecive(10002))
                .setPullMode(new UdpRecive())
                .setVideoCode(VDDecoder.H264)//设置解码方式
                .setMultiple(0)//音频调节，倍数限制为1-8倍。1为原声,放大后可能导致爆音。
                .setSize(480, 320)
//                .setUdpControl(new UdpControlInterface() {
//                    @Override
//                    public byte[] Control(byte[] bytes, int offset, int length) {//bytes为接收到的原始数据
//                        Logger.e(TAG, "BYTES LENGTH: " + bytes.length);
//                        return bytes;
//                    }
//                })
                .build();
        player.start();
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != player) {
            player.stop();
            player.destroy();
            player = null;
        }
        chronometer.stop();
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVideo(){
        txtMessage.setText("");
        chronometer.start();

//        player.start();
    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            String result = intent.getStringExtra("result");
            String source = intent.getStringExtra("source");
            if(source.equals("third")){
                if (result.equals("0")) {
                    //接受
                    txtMessage.setText("正在请求信息处理终端...");
                    MessageBroadcaster.sendIp(UdpMessageHelper.createVideoCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
                } else if (result.equals("1")) {
                    //拒绝1/挂断2
                    Toast.makeText(VideoUploadThirdActivity.this, "第三方设备拒绝请求", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else {
                if (result.equals("0")) {
                    //接受
                    startVideo();
                } else if (result.equals("1")) {
                    //拒绝1/挂断2
                    Toast.makeText(VideoUploadThirdActivity.this, "信息处理终端拒绝请求", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
