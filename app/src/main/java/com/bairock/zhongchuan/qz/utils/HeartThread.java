package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.MessageSource;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;

import java.util.Date;
import java.util.UUID;

public class HeartThread extends Thread {

    @Override
    public void run() {
        while (!interrupted()) {
//            chatTest();
//            sendHeart();
//            heartTest();
            UserUtil.sendMyHeart();
            FileUtil.saveLocation(UserUtil.MY_LOCATION.getLat(), UserUtil.MY_LOCATION.getLng());
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHeart(){
        double lng = 0;
        if(i == 0){
            lng = 119.25745697692036;
        }else {
            lng = 119.25945697692036;
        }
        Location location = new Location(lng, 34.73371279664106);
        MessageBroadcaster.sendBroadcast(UdpMessageHelper.createHeart(UserUtil.user.getUsername(), Util.getLocalIp(), location));

//        for(ClientBase clientBase : UserUtil.clientBases){
//            MessageRoot<Location> messageRoot = new MessageRoot<>();
//            messageRoot.setFrom(clientBase.getUsername());
//            messageRoot.setTo("0");
//            messageRoot.setType(MessageRootType.HEART);
//            messageRoot.setMsgId(UUID.randomUUID().toString());
//            messageRoot.setTime(new Date().getTime());
//
//            double lng = 0;
//            if(i == 0){
//                lng = 119.25745697692036;
//            }else {
//                lng = 119.25945697692036;
//            }
//            Location location = new Location(lng, 34.73371279664106);
//            messageRoot.setData(location);
//            MessageBroadcaster.sendBroadcast(UdpMessageHelper.createHeart(clientBase.getUsername(), Util.getLocalIp(), location));
//            try {
//                sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
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
        MessageBroadcaster.sendBroadcast(UdpMessageHelper.createHeart("8080", Util.getLocalIp(), location));
//        MessageBroadcaster.send(messageRoot);

        MessageRoot<Location> messageRoot1 = new MessageRoot<>();
        messageRoot1.setFrom("9081");
        messageRoot1.setTo("0");
        messageRoot1.setSource(MessageSource.TELESCOPE);
        messageRoot1.setType(MessageRootType.HEART);
        messageRoot1.setMsgId(UUID.randomUUID().toString());
        messageRoot1.setTime(new Date().getTime());
        double lng1 = 0;
        if(i == 0){
            lng1 = 119.25745697692036;
        }else {
            lng1 = 119.25995697692036;
        }
        Location location1 = new Location(lng1, 34.73371279664106);
        messageRoot1.setData(location1);
        MessageBroadcaster.sendBroadcast(UdpMessageHelper.createHeart("9081", Util.getLocalIp(), location1));
//        MessageBroadcaster.send(messageRoot1);

        MessageRoot<Location> messageRoot2 = new MessageRoot<>();
        messageRoot2.setFrom("7081");
        messageRoot2.setTo("0");
        messageRoot2.setSource(MessageSource.UAV);
        messageRoot2.setType(MessageRootType.HEART);
        messageRoot2.setMsgId(UUID.randomUUID().toString());
        messageRoot2.setTime(new Date().getTime());

        double lng2 = 0;
        if(i == 0){
            lng2 = 119.25745697692036;
        }else {
            lng2 = 119.25445697692036;
        }
        Location location2 = new Location(lng2, 34.73371279664106);
        messageRoot2.setData(location2);
        MessageBroadcaster.sendBroadcast(UdpMessageHelper.createHeart("7081", Util.getLocalIp(), location2));
//        MessageBroadcaster.send(messageRoot2);
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
//        MessageBroadcaster.send(messageRoot);
    }
}
