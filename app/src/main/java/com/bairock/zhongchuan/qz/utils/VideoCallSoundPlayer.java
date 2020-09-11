package com.bairock.zhongchuan.qz.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.bairock.zhongchuan.qz.R;

import java.util.HashMap;

public class VideoCallSoundPlayer {

    private static HashMap<Integer,Integer> soundId = new HashMap<>();
    private static SoundPool mSoundPool;
    private static int streamId = -1;

    public static void init(Context context){
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        builder.setAudioAttributes(attrBuilder.build());
        mSoundPool = builder.build();
        soundId.put(1, mSoundPool.load(context, R.raw.call, 1));
    }

    public static void play(){
        streamId = mSoundPool.play(soundId.get(1), 1, 1, 0,-1, 1);
    }

    public static void stop(){
        if(streamId != -1) {
            mSoundPool.stop(streamId);
            streamId = -1;
        }
    }

    public static void release(){
        mSoundPool.release();
    }
}
