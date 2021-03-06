package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
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
import com.bairock.zhongchuan.qz.netty.VoiceBroadcaster;
import com.bairock.zhongchuan.qz.recorderlib.RecordManager;
import com.bairock.zhongchuan.qz.recorderlib.recorder.RecordConfig;
import com.bairock.zhongchuan.qz.recorderlib.recorder.RecordHelper;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordDataListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordFftDataListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordResultListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordStateListener;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.widght.AudioView;

import java.io.File;

public class VoiceUploadActivity extends AppCompatActivity {

    private static String TAG = "VoiceUploadActivity";
    private Context context;
    private ImageView imgHangUp;
    private TextView txtMessage;
    private AudioView audioView;
    private Chronometer chronometer;
    private AskBroadcastReceiver receiver;
    private String ip;
    private RecordManager recordManager = RecordManager.getInstance();

    private SendUdpThread sendUdpThread;
    private boolean upload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_upload);
        context = this.getBaseContext();
        App.getInstance2().addActivity(this);

        ip = UserUtil.findMainServerIp();
        if(ip == null){
            Toast.makeText(this, "信息处理设备不在线, 正在本地录制...", Toast.LENGTH_SHORT).show();
//            finish();
        }

        findViews();
        setOnListener();
        initRecord();
        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VOICE_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        // 是否请求信息处理设备, 如果试主动上传则需要请求, 如果是被动上传(信息处理设备主动请求)则不需要请求
        boolean askMainServer = getIntent().getBooleanExtra("askMainServer", true);
        if(askMainServer) {
            sendUdpThread = new SendUdpThread(UdpMessageHelper.createVoiceCallMainServerAsk(UserUtil.user.getUsername()), ip);
            sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
                @Override
                public void onNoAnswer() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VoiceUploadActivity.this, "信息处理设备无应答", Toast.LENGTH_SHORT).show();
//                        finish();
                        }
                    });

                }
            });
            sendUdpThread.start();
        }else{
            upload = true;
        }
        startVoice();
//        MessageBroadcaster.sendIp(UdpMessageHelper.createVoiceCallMainServerAsk(UserUtil.user.getUsername()), ip);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != ip) {
            MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), ip);
        }
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null != recordManager){
            recordManager.stop();
            recordManager.setRecordDataListener(null);
        }

        if(null != sendUdpThread){
            sendUdpThread.interrupt();
        }
    }

    private void findViews() {
        txtMessage = findViewById(R.id.txtMessage);
        chronometer = findViewById(R.id.chronometer);
        imgHangUp = findViewById(R.id.imgHangUp);
        audioView = findViewById(R.id.audioView);
        audioView.setStyle(AudioView.ShowStyle.getStyle("STYLE_ALL"), audioView.getDownStyle());
        audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle("STYLE_ALL"));
    }

    private void setOnListener() {
        imgHangUp.setOnClickListener(onClickListener);
    }

    private void initRecord() {
        recordManager.init(App.getInstance2(), false);
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
//        String recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
//                Environment.getExternalStorageDirectory().getAbsolutePath());
        String recordDir = FileUtil.getPolicePath() + "voice" + File.separator;
        recordManager.changeRecordDir(recordDir);
        initRecordEvent();
        Logger.e(TAG, recordManager.getRecordConfig().toString());
    }

    private void initRecordEvent() {
        recordManager.setRecordStateListener(new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                Logger.i(TAG, "onStateChange %s", state.name());

                switch (state) {
                    case PAUSE:
                        Logger.i(TAG, "暂停中", state.name());
                        break;
                    case IDLE:
                        Logger.i(TAG, "空闲中", state.name());
                        break;
                    case RECORDING:
                        Logger.i(TAG, "录音中", state.name());
                        break;
                    case STOP:
                        Logger.i(TAG, "停止", state.name());
                        break;
                    case FINISH:
                        Logger.i(TAG, "录音结束", state.name());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String error) {
                Logger.i(TAG, "onError %s", error);
            }
        });
        recordManager.setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
            }
        });
        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                Toast.makeText(context, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
        recordManager.setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                audioView.setWaveData(data);
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imgHangUp :
//                    MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), ip);
                    finish();
                    break;
            }
        }
    };

    private void startVoice(){
        txtMessage.setText("正在上传语音");
        chronometer.start();
        recordManager.start();
        recordManager.setRecordDataListener(recordDataListener);
    }

    private RecordDataListener recordDataListener = new RecordDataListener(){

        @Override
        public void onData(byte[] data) {
//            Log.e("Main", data.length + "?");
            if(upload) {
                VoiceBroadcaster.send(data, ip);
            }
        }
    };

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SendUdpThread.answered = true;
            txtMessage.setText("");
            if(null != sendUdpThread) {
                sendUdpThread.interrupt();
            }
            String result = intent.getStringExtra("result");
            if(result.equals("0")){
                //接受
//                startVoice();
                upload = true;
            }else if(result.equals("1")){
                //拒绝1/挂断2
                Toast.makeText(VoiceUploadActivity.this, "信息处理设备拒接", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }
    }

}
