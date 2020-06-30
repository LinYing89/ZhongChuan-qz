package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.library.common.WriteFileCallback;
import com.library.live.Publish;
import com.library.live.stream.UdpSend;
import com.library.live.vd.VDEncoder;
import com.library.live.view.PublishView;

import java.io.File;

public class ChatVideoActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Publish publish;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_video);
        findViews();
    }

    private void findViews(){
        btnStart = findViewById(R.id.btnStart);
        chronometer = findViewById(R.id.chronometer);
        chronometer.start();
        publish = new Publish.Buider(this, (PublishView) findViewById(R.id.publishView))
                .setPushMode(new UdpSend("192.168.1.6", 8765))
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
                .build();

        publish.start();

        publish.setWriteFileCallback(new WriteFileCallback() {
            @Override
            public void success(String s) {
                Log.e("ChatVideoAct", s);
                Intent intent = new Intent();
                intent.putExtra("filePath", s);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void failure(String s) {
                finish();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStart.getText().equals("开始录制")){
                    btnStart.setText("结束录制");
                    publish.startRecode();
                }else{
                    btnStart.setText("开始录制");
                    publish.stopRecode();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publish.stopRecode();//停止录制
        publish.stop();
        publish.destroy();
        chronometer.stop();
    }
}
