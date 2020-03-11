package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.H264bRoadcaster;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
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
    private Button btnOff;
    private TextView txtTo;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        findViews();

        String name = getIntent().getStringExtra(Constants.NAME);
        txtTo.setText(name);
        ip = UserUtil.findIpByUsername(name);
        if(null == ip){
            Logger.e(TAG, "ip is null");
            finish();
        }
        Logger.e(TAG, "ip:" + ip);
    }

    private void findViews(){
        btnOff = findViewById(R.id.btnOff);
        txtTo = findViewById(R.id.txtTo);
        chronometer = findViewById(R.id.chronometer);
        chronometer.start();

        publishMe = new Publish.Buider(this, (PublishView) findViewById(R.id.publishViewMe))
                .setPushMode(new UdpSend(ip, 6666))
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
//                        if(null != player){
//                            player.write(bytes);
//                        }
//                        H264bRoadcaster.send(bytes, ip);
                        H264bRoadcaster.send(bytes, "192.168.1.6");
                        return new byte[0];
                    }
                })
                .build();

        publishMe.start();

        player = new Player.Buider((PlayerView) findViewById(R.id.player))
//                .setPullMode(new UdpRecive(10001))
                .setPullMode(new UdpRecive())
                .setVideoCode(VDDecoder.H264)//设置解码方式
                .setMultiple(1)//音频调节，倍数限制为1-8倍。1为原声,放大后可能导致爆音。
                .setUdpControl(new UdpControlInterface() {
                    @Override
                    public byte[] Control(byte[] bytes, int offset, int length) {//bytes为接收到的原始数据
                        Logger.e(TAG, "BYTES LENGTH: " + bytes.length);
                        return bytes;
                    }
                })
                .build();
        player.start();

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        publishHe.stopRecode();//停止录制
        if(null != publishMe) {
            publishMe.stop();
            publishMe.destroy();
        }

        if(null != player) {
            player.stop();
            player.destroy();
        }

        if(null != chronometer) {
            chronometer.stop();
        }
    }
}
