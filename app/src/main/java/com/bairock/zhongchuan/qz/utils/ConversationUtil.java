package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ConversationUtil {

    public static String CHAT_ACTION = "com.bairock.zhongchuan.qz.chat";
    public static String CHAT_BROADCAST_PERMISSION = "qz.permission.MY_BROADCAST_PERMISSION";

    public static ZCConversation activeConversation = null;

    public static List<ZCConversation> conversations = Collections.synchronizedList(new ArrayList<ZCConversation>());

    public static void addReceivedMessage(MessageRoot<ZCMessage> messageRoot){
        boolean haved = false;
        for(ZCConversation conversation : conversations){
            if(conversation.getUsername().equals(messageRoot.getFrom())){
                conversation.addMessage(messageRoot);
                haved = true;
                break;
            }
        }
        if(!haved){
            ZCConversation zcConversation = new ZCConversation(messageRoot.getFrom());
            zcConversation.addMessage(messageRoot);
            conversations.add(zcConversation);
        }
    }

    public static void addSendMessage(MessageRoot<ZCMessage> messageRoot){
        boolean haved = false;
        for(ZCConversation conversation : conversations){
            if(conversation.getUsername().equals(messageRoot.getTo())){
                conversation.addMessage(messageRoot);
                haved = true;
                break;
            }
        }
        if(!haved){
            ZCConversation zcConversation = new ZCConversation(messageRoot.getTo());
            zcConversation.addMessage(messageRoot);
            conversations.add(zcConversation);
        }
    }

    public static ZCConversation getConversation(String username){
        for (ZCConversation con: conversations) {
            if(con.getUsername().equals(username)){
                return con;
            }
        }
        return null;
    }

    public static ZCConversation activeConversation(String username){
        for (ZCConversation con: conversations) {
            if(con.getUsername().equals(username)){
                activeConversation = con;
                return con;
            }
        }
        return null;
    }

    public static void removeConversation(ZCConversation conversation){
        conversations.remove(conversation);
    }

    public static MessageRoot<ZCMessage> createSendMessage(ZCMessageType zcMessageType, String from, String to) {
        MessageRoot<ZCMessage> messageRoot = new MessageRoot<>();
        messageRoot.setMsgId(UUID.randomUUID().toString());
        messageRoot.setTime(new Date().getTime());
        messageRoot.setFrom(from);
        messageRoot.setTo(to);
        messageRoot.setType(MessageRootType.CHAT);

        ZCMessage message = new ZCMessage();
        message.setMessageType(zcMessageType);
        message.setDirect(ZCMessageDirect.SEND);
        message.setUnread(false);

        messageRoot.setData(message);
        return messageRoot;
    }
}
