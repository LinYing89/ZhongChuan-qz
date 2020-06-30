package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessage;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;

public class SendUdpThread extends Thread {

    private OnNoAnswerListener onNoAnswerListener;

    public static boolean answered = false;
    public byte[] bytes;
    private String ip;

    public SendUdpThread(UdpMessage udpMessage, String ip) {
        this.bytes = UdpMessageHelper.createBytes(udpMessage);
        this.ip = ip;
        answered = false;
    }

    public OnNoAnswerListener getOnNoAnswerListener() {
        return onNoAnswerListener;
    }

    public void setOnNoAnswerListener(OnNoAnswerListener onNoAnswerListener) {
        this.onNoAnswerListener = onNoAnswerListener;
    }

    @Override
    public void run() {
        super.run();
        for(int i = 0; i < 3; i++){
            MessageBroadcaster.sendIp(bytes, ip);
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            if(answered){
                return;
            }
        }
        if(null != onNoAnswerListener){
            onNoAnswerListener.onNoAnswer();
        }
    }

    public interface OnNoAnswerListener{
        void onNoAnswer();
    }
}
