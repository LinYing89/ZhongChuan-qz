package com.bairock.zhongchuan.qz.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.VoiceMessageBody;

import java.io.File;

import static com.bairock.zhongchuan.qz.bean.ZCMessageDirect.RECEIVE;
import static com.bairock.zhongchuan.qz.bean.ZCMessageDirect.SEND;

public class VoicePlayClickListener implements View.OnClickListener {

    private MessageRoot<ZCMessage> messageRoot;
    private ZCMessage message;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;

    public VoicePlayClickListener(MessageRoot<ZCMessage> messageRoot, ImageView v,
                                  ImageView iv_read_status, BaseAdapter adapter, Activity activity) {
        this.messageRoot = messageRoot;
        message = messageRoot.getData();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = activity;
    }

    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (message.getDirect() == RECEIVE) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        ((ChatActivity) activity).playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        ((ChatActivity) activity).playMsgId = messageRoot.getMsgId();
        AudioManager audioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // TODO Auto-generated method stub
                            mediaPlayer.release();
                            mediaPlayer = null;
                            stopPlayVoice(); // stop animation
                        }

                    });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();

            // 如果是接收的消息
            if (message.getDirect() == RECEIVE) {
//                if (!messageRoot.isListened() && iv_read_status != null
//                        && iv_read_status.getVisibility() == View.VISIBLE) {
//                    // 隐藏自己未播放这条语音消息的标志
//                    iv_read_status.setVisibility(View.INVISIBLE);
//                    EMChatManager.getInstance().setMessageListened(message);
//                }
            }

        } catch (Exception e) {
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (message.getDirect() == RECEIVE) {
            voiceIconView.setImageResource(R.drawable.voice_from_icon);
        } else {
            voiceIconView.setImageResource(R.drawable.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {
        String st = activity.getResources().getString(
                R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (((ChatActivity) activity).playMsgId != null
                    && ((ChatActivity) activity).playMsgId.equals(messageRoot
                    .getMsgId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }

        if (message.getDirect() == SEND) {
            // for sent msg, we will try to play the voice file directly
            playVoice(message.getContent());
        } else {
            File file = new File(message.getContent());
            if (file.exists() && file.isFile())
                playVoice(message.getContent());
            else
                System.err.println("file not exist");

        }
    }
}