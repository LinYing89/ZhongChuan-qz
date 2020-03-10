package com.bairock.zhongchuan.qz.netty;

/**
 * 心跳以及基础通信udp协议格式
 */
public class UdpMessage {

    //成员编号 2 byte
    private short memberNumber;
    //功能码
    private byte factionCode;
    //错误码
    private byte errorCode = 0;
    //数据长度
    private short dataLength;
    //数据
    private byte[] data;

    public short getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(short memberNumber) {
        this.memberNumber = memberNumber;
    }

    public byte getFactionCode() {
        return factionCode;
    }

    public void setFactionCode(byte factionCode) {
        this.factionCode = factionCode;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(byte errorCode) {
        this.errorCode = errorCode;
    }

    public short getDataLength() {
        return dataLength;
    }

    public void setDataLength(short dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
