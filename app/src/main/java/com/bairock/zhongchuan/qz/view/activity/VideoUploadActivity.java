package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.library.common.UdpControlInterface;
import com.library.live.Publish;
import com.library.live.stream.UdpSend;
import com.library.live.vd.VDEncoder;
import com.library.live.view.PublishView;

import java.io.File;

public class VideoUploadActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Publish publish;
    private ImageView imgHangUp;
    private TextView txtMessage;
    private String ip;
    private AskBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);
        ip = UserUtil.findMainServerIp();
        if(ip == null){
            Toast.makeText(this, "对方不在线", Toast.LENGTH_SHORT).show();
            finish();
        }
        findViews();

        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        MessageBroadcaster.sendIp(UdpMessageHelper.createVideoCallMainServerAsk(UserUtil.user.getUsername()), ip);
    }

    private void findViews(){
        txtMessage = findViewById(R.id.txtMessage);
        imgHangUp = findViewById(R.id.imgHangUp);
        chronometer = findViewById(R.id.chronometer);
        initPushView();

        imgHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), ip);
                finish();
            }
        });
    }

    private void initPushView(){
        publish = new Publish.Buider(this, (PublishView) findViewById(R.id.publishView))
                .setPushMode(new UdpSend(Util.getLocalIp(), 10009))
                .setFrameRate(15)//帧率
                .setVideoCode(VDEncoder.H264)//编码方式
                .setIsPreview(true)//是否需要显示预览(如需后台推流最好设置false，如果设置false则构建Buider可以调用单参数方法Publish.Buider(context))
                .setPublishBitrate(600 * 1024)//推流采样率
                .setCollectionBitrate(600 * 1024)//采集采样率
                .setCollectionBitrateVC(64 * 1024)//音频采集采样率
                .setPublishBitrateVC(24 * 1024)//音频推流采样率
                .setPublishSize(480, 320)//推流分辨率，不要高于预览分辨率
                .setPreviewSize(480, 320)//预览分辨率，决定截屏、录制文件的分辨率
                .setRotate(false)//是否为前置摄像头,默认后置
                .setScreenshotsMode(Publish.CONVERSION)//截屏模式
                .setCenterScaleType(true)
//                .setVideoDirPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "VideoLive")//录制路径,当前为默认路径
                .setPictureDirPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "VideoPicture")//拍照路径,当前为默认路径
                .setVideoDirPath(FileUtil.getPoliceVideoPath())//录制路径,当前为默认路径
                .setUdpControl(new UdpControlInterface() {
                    @Override
                    public byte[] Control(byte[] bytes, int offset, int length) {//bytes为udp包数据,offset为起始位,length为长度
                        //返回自定义后udp包数据,不要做耗时操作。如果调用了此方法不要将原数组返回
                        H264Broadcaster.send(bytes, ip);
                        return new byte[0];
                    }
                })
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != publish) {
            publish.stopRecode();//停止录制
            publish.stop();
            publish.destroy();
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

        publish.start();
    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();

            String result = intent.getStringExtra("result");
            if(result.equals("0")){
                //接受
                startVideo();
            }else if(result.equals("1")){
                //拒绝1/挂断2
                Toast.makeText(VideoUploadActivity.this, "对方忙", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
