package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.library.common.UdpControlInterface;
import com.library.common.WriteFileCallback;
import com.library.live.Publish;
import com.library.live.stream.UdpRecive;
import com.library.live.stream.UdpSend;
import com.library.live.stream.VideoCallback;
import com.library.live.vd.VDEncoder;
import com.library.live.view.PublishView;

import java.io.File;

public class VideoUploadActivity extends AppCompatActivity {

    private Chronometer chronometer;
    public Publish publish;
    private ImageView imgHangUp;
    private TextView txtMessage;
    private String mainServerIp;
    private AskBroadcastReceiver receiver;

    private SendUdpThread sendUdpThread;
    private boolean upload = false;

    private UdpRecive myUdpReceiver = new UdpRecive();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);
        App.getInstance2().addActivity(this);
        mainServerIp = UserUtil.findMainServerIp();
        if(mainServerIp == null){
            Toast.makeText(this, "信息处理设备不在线", Toast.LENGTH_SHORT).show();
//            finish();
        }

//        mainServerIp = "192.168.137.1";

        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        findViews();

        startVideo();
        myUdpReceiver.setVideoCallback(new VideoCallback() {
            @Override
            public void videoCallback(byte[] bytes) {
                if(null != mainServerIp) {
                    H264Broadcaster.send(bytes, mainServerIp);
                }
            }
        });

        // 是否请求信息处理设备, 如果试主动上传则需要请求, 如果是被动上传(信息处理设备主动请求)则不需要请求
        boolean askMainServer = getIntent().getBooleanExtra("askMainServer", true);
        if(askMainServer) {
            if(null != mainServerIp) {
                txtMessage.setText("正在请求信息处理设备...");
                sendUdpThread = new SendUdpThread(UdpMessageHelper.createVideoCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
                sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
                    @Override
                    public void onNoAnswer() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoUploadActivity.this, "信息处理设备无应答", Toast.LENGTH_SHORT).show();
//                        finish();
                            }
                        });

                    }
                });
                sendUdpThread.start();
            }
        }else{
            upload = true;
            myUdpReceiver.startRevice();
            publish.start();
        }
//        MessageBroadcaster.sendIp(UdpMessageHelper.createVideoCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
    }


    private void findViews(){
        txtMessage = findViewById(R.id.txtMessage);
        imgHangUp = findViewById(R.id.imgHangUp);
        chronometer = findViewById(R.id.chronometer);
        initPushView();

        imgHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), mainServerIp);
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
                .setVideoFileName(FileUtil.getPoliceFileName())
                .setUdpControl(new UdpControlInterface() {
                    @Override
                    public byte[] Control(byte[] bytes, int offset, int length) {//bytes为udp包数据,offset为起始位,length为长度
                        //返回自定义后udp包数据,不要做耗时操作。如果调用了此方法不要将原数组返回
                        if(upload) {
                            myUdpReceiver.write(bytes);
//                            byte[] bytes1 = new byte[length];
//                            System.arraycopy(bytes, 0, bytes1, 0, length);
//                            if(bytes[0] == (byte)0x01) {
//                                byte[] data = Arrays.copyOfRange(bytes, 13, ByteUtil.byte_to_short(bytes[11], bytes[12]) + 13);
//                                H264Broadcaster.send(data, mainServerIp);
//                            }
                        }
                        return new byte[0];
                    }
                })
                .build();

        publish.setWriteFileCallback(new WriteFileCallback() {
            @Override
            public void success(String s) {
                Log.e("VideoUploadAct", s);
            }

            @Override
            public void failure(String s) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != publish) {
            publish.stopRecode();//停止录制
            publish.stop();
            publish.destroy();
        }
        if(null != mainServerIp) {
            MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), mainServerIp);
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

    private void startVideo(){
        txtMessage.setText("");
        chronometer.start();
        publish.startRecode();
//        if(upload) {
//            publish.start();
//        }
    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            txtMessage.setText("");
            SendUdpThread.answered = true;
            if(null != sendUdpThread) {
                sendUdpThread.interrupt();
            }
            String result = intent.getStringExtra("result");
            if (result.equals("0")) {
                //接受
                upload = true;
                myUdpReceiver.startRevice();
                publish.start();
//                startVideo();
            } else if (result.equals("1")) {
                //拒绝1/挂断2
                Toast.makeText(VideoUploadActivity.this, "信息处理设备拒绝请求", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
