package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;

import java.util.Date;
import java.util.UUID;

public class HeartThread extends Thread {

    @Override
    public void run() {
        while (!interrupted()) {
            chatTest();
            heartTest();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void heartTest() {
        MessageRoot<Location> messageRoot = new MessageRoot<>();
        messageRoot.setFrom("8080");
        messageRoot.setTo("0");
        messageRoot.setType(MessageRootType.HEART);
        messageRoot.setMsgId(UUID.randomUUID().toString());
        messageRoot.setTime(new Date().getTime());

        double lng = 0;
        if(i == 0){
            lng = 119.25745697692036;
        }else {
            lng = 119.25945697692036;
        }
        Location location = new Location(lng, 34.73371279664106);
        messageRoot.setData(location);
//            messageRoot.setData(new byte[]{0, 1, 0, 1});
        messageRoot.setData(location);
        MessageBroadcaster.send(messageRoot);
    }

    private int i = 0;

    private void chatTest() {
        sendSendMessageTest();
        if (i == 0) {
            i = 1;
//            sendSendMessageTest();
        } else {
            i = 0;
//            sendReceiveMessageTest();
        }
    }

    private void sendSendMessageTest() {
        MessageRoot<ZCMessage> messageRoot = new MessageRoot<>();
        messageRoot.setFrom("8080");
        messageRoot.setTo("8080");
        messageRoot.setType(MessageRootType.CHAT);
        messageRoot.setMsgId(UUID.randomUUID().toString());
        messageRoot.setTime(new Date().getTime());
        ZCMessage message = new ZCMessage();
        message.setMessageType(ZCMessageType.TXT);
        message.setContent("test send");
        messageRoot.setData(message);
        TcpClientUtil.send(messageRoot);
//        MessageBroadcaster.send(messageRoot);
        ConversationUtil.addSendMessage(messageRoot);
    }

    private void sendReceiveMessageTest() {
        MessageRoot<ZCMessage> messageRoot = new MessageRoot<>();
        messageRoot.setFrom("8080");
        messageRoot.setTo("8080");
        messageRoot.setType(MessageRootType.CHAT);
        messageRoot.setMsgId(UUID.randomUUID().toString());
        messageRoot.setTime(new Date().getTime());
        ZCMessage message = new ZCMessage();
        message.setMessageType(ZCMessageType.TXT);
        message.setContent("test receive");
        messageRoot.setData(message);
        MessageBroadcaster.send(messageRoot);
    }
}
