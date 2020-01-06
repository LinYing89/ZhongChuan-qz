package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;

import java.util.Date;
import java.util.UUID;

public class HeartThread extends Thread {

    @Override
    public void run() {
        while (!interrupted()){
//            MessageRoot<Location> messageRoot = new MessageRoot<>();
            MessageRoot<byte[]> messageRoot = new MessageRoot<>();
            messageRoot.setFrom("8090");
            messageRoot.setTo("0");
//            messageRoot.setType(MessageRootType.HEART);
            messageRoot.setType(MessageRootType.VIDEO);
            messageRoot.setMsgId(UUID.randomUUID().toString());
            messageRoot.setTime(new Date().getTime());

//            Location location = new Location(119.23117988388064, 34.60552436039501);
//            messageRoot.setData(location);
            messageRoot.setData(new byte[]{0, 1, 0, 1});
//            ZCMessage message = new ZCMessage();
//            message.setContent("test");
//            message.setMessageType(ZCMessageType.TXT);

            MessageBroadcaster.send(messageRoot);
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
