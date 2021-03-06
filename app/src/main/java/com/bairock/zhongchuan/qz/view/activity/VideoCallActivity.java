package com.bairock.zhongchuan.qz.view.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.utils.VideoCallSoundPlayer;
import com.library.common.UdpControlInterface;
import com.library.live.Player;
import com.library.live.Publish;
import com.library.live.stream.UdpRecive;
import com.library.live.stream.UdpSend;
import com.library.live.vd.VDDecoder;
import com.library.live.vd.VDEncoder;
import com.library.live.view.PlayerView;
import com.library.live.view.PublishView;

public class VideoCallActivity extends AppCompatActivity {

    private static String TAG = "VideoCallActivity";

    private Chronometer chronometer;
    public static Player player;
    private Publish publishMe;
//    private TextView txtTo;
    private ImageView imgMute;
    private ImageView imgHangUp;
    private ImageView imgSpeaker;
    private ImageView imgHangUp2;
    private ImageView imgOk;

    private LinearLayout layoutAsk;
    private LinearLayout layoutAns;

    public static String name = "";
    private String ip;
    private boolean muted;

    private AskBroadcastReceiver receiver;

    private SendUdpThread sendUdpThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        App.getInstance2().addActivity(this);
        name = getIntent().getStringExtra(Constants.NAME);
        String videoType = getIntent().getStringExtra(Constants.VIDEO_TYPE);
        findViews();

        VideoCallSoundPlayer.play();

        ip = UserUtil.findIpByUsername(name);
        if(ip == null || ip.isEmpty()){
            Toast.makeText(VideoCallActivity.this, "对方不在线", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(videoType.equals(Constants.VIDEO_ASK)){
            // 主动发起请求, 等待对方应答界面
            layoutAsk.setVisibility(View.VISIBLE);
            layoutAns.setVisibility(View.GONE);
            sendUdpThread = new SendUdpThread(UdpMessageHelper.createVideoCallAsk(UserUtil.user.getUsername()), ip);
            sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
                @Override
                public void onNoAnswer() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoCallActivity.this, "对方无应答", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                }
            });
            sendUdpThread.start();
//            MessageBroadcaster.send(UdpMessageHelper.createVideoCallAsk(UserUtil.user.getUsername()), name);
        }else {
            // 被动接听界面
            layoutAsk.setVisibility(View.GONE);
            layoutAns.setVisibility(View.VISIBLE);
        }

//        txtTo.setText(name);
        ip = UserUtil.findIpByUsername(name);
        if(null == ip){
            Logger.e(TAG, "ip is null");
            Toast.makeText(this, "对方不在线", Toast.LENGTH_SHORT).show();
            finish();
        }
        Logger.e(TAG, "ip:" + ip);

        setListener();

        init();
        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VIDEO_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

//        startVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageBroadcaster.send(UdpMessageHelper.createVideoCallAns(UserUtil.user.getUsername(), 1), name);
        name = "";
//        publishHe.stopRecode();//停止录制
        // 注销广播
        VideoCallSoundPlayer.stop();
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null != publishMe) {
            publishMe.stop();
            publishMe.destroy();
        }

        if(null != player) {
            player.stop();
            player.destroy();
            player = null;
        }

        if(null != chronometer) {
            chronometer.stop();
        }

        if(null != sendUdpThread){
            sendUdpThread.interrupt();
        }

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_UNMUTE, AudioManager.STREAM_MUSIC, 0);
    }

    private void findViews(){
//        txtTo = findViewById(R.id.txtTo);
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
    private void init(){
        publishMe = new Publish.Buider(this, (PublishView) findViewById(R.id.publishViewMe))
//                .setPushMode(new UdpSend(ip, H264Broadcaster.PORT))
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
                .setRotate(true)//是否为前置摄像头,默认后置
                .setScreenshotsMode(Publish.CONVERSION)//截屏模式
                .setCenterScaleType(true)
//                .setVideoDirPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "VideoLive")//录制路径,当前为默认路径
//                .setPictureDirPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "VideoPicture")//拍照路径,当前为默认路径
                .setVideoDirPath(FileUtil.getPoliceVideoPath())//录制路径,当前为默认路径
                .setUdpControl(new UdpControlInterface() {
                    @Override
                    public byte[] Control(byte[] bytes, int offset, int length) {//bytes为udp包数据,offset为起始位,length为长度
                        //返回自定义后udp包数据,不要做耗时操作。如果调用了此方法不要将原数组返回
//                            player.write(bytes);
                        byte[] bytes1 = new byte[length];
                        System.arraycopy(bytes, 0, bytes1, 0, length);
                        H264Broadcaster.send(bytes1, ip);
//                        H264bRoadcaster.send(bytes, "192.168.1.6");
                        return new byte[0];
                    }
                })
                .build();
//        publishMe.start();
        player = new Player.Buider((PlayerView) findViewById(R.id.player))
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgMute:
                    if(muted){
                        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_UNMUTE, AudioManager.STREAM_MUSIC, 0);
                        muted = false;
                        imgMute.setImageResource(R.drawable.icon_mute_normal);
                    }else {
                        muted = true;
                        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        //静音 并显示UI  第三个参数 flags=1
                        mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.STREAM_MUSIC, 0);
                        imgMute.setImageResource(R.drawable.icon_mute_on);
                    }
                    break;
                case R.id.imgHangUp:
                case R.id.imgHangUp2:
//                    MessageBroadcaster.send(UdpMessageHelper.createVideoCallAns(UserUtil.user.getUsername(), 1), name);
                    finish();
                    break;
                case R.id.imgSpeaker:
                    publishMe.rotate();
                    break;
                case R.id.imgOk:
                    VideoCallSoundPlayer.stop();
                    sendUdpThread = new SendUdpThread(UdpMessageHelper.createVideoCallAns(UserUtil.user.getUsername(), 0), ip);
                    sendUdpThread.start();
//                    MessageBroadcaster.send(UdpMessageHelper.createVideoCallAns(UserUtil.user.getUsername(), 0), name);
                    layoutAns.setVisibility(View.GONE);
                    layoutAsk.setVisibility(View.VISIBLE);
                    startVideo();
                    break;
            }
        }
    };

    private void startVideo(){
        chronometer.start();
        publishMe.start();

    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            VideoCallSoundPlayer.stop();
            SendUdpThread.answered = true;
            if(null != sendUdpThread) {
                sendUdpThread.interrupt();
            }
            String result = intent.getStringExtra("result");
            if(result.equals("0")){
                //接受
                startVideo();
            }else if(result.equals("1")){
                //拒绝1/挂断2
                Toast.makeText(VideoCallActivity.this, "对方忙", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
