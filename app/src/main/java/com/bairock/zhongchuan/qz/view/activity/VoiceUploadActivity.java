package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.recorderlib.RecordManager;
import com.bairock.zhongchuan.qz.recorderlib.recorder.RecordConfig;
import com.bairock.zhongchuan.qz.recorderlib.recorder.RecordHelper;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordDataListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordFftDataListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordResultListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.bairock.zhongchuan.qz.recorderlib.recorder.listener.RecordStateListener;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.widght.AudioView;

import java.io.File;
import java.util.Locale;

public class VoiceUploadActivity extends AppCompatActivity {

    private static String TAG = "VoiceUploadActivity";
    private Context context;
    private Button btnStart;
    private AudioView audioView;
    private Chronometer chronometer;

    private boolean isStart = false;
    final RecordManager recordManager = RecordManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_upload);
        context = this.getBaseContext();
        findViews();
        setOnListener();
        initRecord();
        chronometer.start();
    }

    private void findViews() {
        btnStart = findViewById(R.id.btnStart);
        audioView = findViewById(R.id.audioView);
        audioView.setStyle(AudioView.ShowStyle.getStyle("STYLE_ALL"), audioView.getDownStyle());
        audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle("STYLE_ALL"));
    }

    private void setOnListener() {
        btnStart.setOnClickListener(onClickListener);
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
                case R.id.btnStart :
                    doPlay();
                    break;
            }
        }
    };

    private void doStop() {
        recordManager.stop();
        recordManager.setRecordDataListener(null);
        btnStart.setText("开始上报");
        isStart = false;
    }

    private void doPlay() {
        if (isStart) {
            doStop();
        } else {
            btnStart.setText("结束上报");
            recordManager.start();
            recordManager.setRecordDataListener(recordDataListener);
            isStart = true;
        }
//		if (isStart) {
//			recordManager.pause();
//			btnStart.setText("结束上报");
//			isPause = true;
//			isStart = false;
//		} else {
//			recordManager.start();
//			recordManager.setRecordDataListener(recordDataListener);
//			isPause = false;
//			isStart = true;
//		}
    }

    private RecordDataListener recordDataListener = new RecordDataListener(){

        @Override
        public void onData(byte[] data) {
            Log.e("Main", data.length + "?");
        }
    };

}
