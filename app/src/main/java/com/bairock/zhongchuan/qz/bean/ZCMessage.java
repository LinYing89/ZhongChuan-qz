package com.bairock.zhongchuan.qz.bean;

/**
 * 短消息会话中的一个消息
 */
public class ZCMessage {

    //消息内容, 如果是文件, 则为文件路径
    private String content;
    private boolean unread;
    //消息类型
    private ZCMessageType messageType;

    private ZCMessageDirect direct;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public ZCMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ZCMessageType messageType) {
        this.messageType = messageType;
    }

    public ZCMessageDirect getDirect() {
        return direct;
    }

    public void setDirect(ZCMessageDirect direct) {
        this.direct = direct;
    }
}
