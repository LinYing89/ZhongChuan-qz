package com.bairock.zhongchuan.qz.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 短消息会话
 */
public class ZCConversation {

    private Long id;

    private List<MessageRoot<ZCMessage>> messages;

    //未读条数
    private int unreadCount = 0;
    private String username;

    public ZCConversation(String username) {
        this.username = username;
        if (this.messages == null) {
            this.messages = Collections.synchronizedList(new ArrayList<MessageRoot<ZCMessage>>());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MessageRoot<ZCMessage>> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageRoot<ZCMessage>> messages) {
        this.messages = messages;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void unreadCountPlus(){
        unreadCount++;
    }

    public void unreadCountSub(){
        if(unreadCount > 0) {
            unreadCount--;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addMessage(MessageRoot<ZCMessage> message) {
//        if (this.messages.size() > 0) {
//            MessageRoot messageLast = this.messages.get(this.messages.size() - 1);
//            if (message.getMsgId() != null && messageLast.getMsgId() != null && message.getMsgId().equals(messageLast.getMsgId())) {
//                return;
//            }
//        }

        boolean haved = false;

//        for (MessageRoot zcMessage : messages) {
//            if (zcMessage.getMsgId().equals(message.getMsgId())) {
//                haved = true;
//                break;
//            }
//        }

        if (!haved) {
            this.messages.add(message);
            if (message.getData().getDirect() == ZCMessageDirect.RECEIVE) {
                ++this.unreadCount;
            }
        }
    }

    public int getMsgCount() {
        return messages.size();
    }

    public MessageRoot<ZCMessage> getLastMessage() {
        return messages.size() == 0 ? null : messages.get(messages.size() - 1);
    }

    public MessageRoot<ZCMessage> getMessage(int index) {
        if(index >= 0 && index < messages.size()) {
            return messages.get(index);
        }else {
            return null;
        }
    }
}
