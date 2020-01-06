package com.bairock.zhongchuan.qz.bean;

public class MessageRoot<T> {

    private String msgId;
    private MessageRootType type;
    private String from;
    private String to;
    private Long time;
    private T data;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MessageRootType getType() {
        return type;
    }

    public void setType(MessageRootType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
