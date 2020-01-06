package com.bairock.zhongchuan.qz.bean;

import com.easemob.exceptions.EaseMobException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ZCChatManager {

    private static ZCChatManager instance = new ZCChatManager();

    private List<ZCConversation> conversations = new ArrayList<>();

    public static synchronized ZCChatManager getInstance() {
        return instance;
    }

    public ZCConversation getConversation(String username){
        for (ZCConversation con: conversations) {
            if(con.getUsername().equals(username)){
                return con;
            }
        }
        return null;
    }

    public void addConversation(ZCConversation conversation){
        if (!conversations.contains(conversation)) {
            conversations.add(conversation);
        }
    }

    public void removeConversation(ZCConversation conversation){
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
