package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.utils.Util;

import java.nio.charset.StandardCharsets;

public class UdpMessageHelper {

    public static final byte HEART = 0x01;
    public static final byte LOGIN_ASK = 0x02;
    public static final byte LOGIN_ANS = 0x03;

    // 手持终端请求第三方开始推送视频流
    public static final byte VIDEO_CALL_THIRD_ASK = 0x11;
    // 第三方回应收到请求
    public static final byte VIDEO_CALL_THIRD_ANS = 0x12;

    // 手持终端请求第三方推送音频流
    public static final byte VOICE_CALL_THIRD_ASK = 0x21;
    // 第三方回应收到请求
    public static final byte VOICE_CALL_THIRD_ANS = 0x22;
    // 手持终端请求第三方停止推送数据流
    public static final byte CALL_THIRD_STOP_ASK = 0x23;
    // 第三方回应收到请求
    public static final byte CALL_THIRD_STOP_ANS = 0x24;

    // 手持终端请求推送视频流给信息处理终端
    public static final byte VIDEO_CALL_MAIN_SERVER_ASK = 0x31;
    // 信息处理终端回应推送视频流
    public static final byte VIDEO_CALL_MAIN_SERVER_ANS = 0x32;

    // 手持终端请求推送音频流给信息处理终端
    public static final byte VOICE_CALL_MAIN_SERVER_ASK = 0x41;
    // 信息处理终端回应推送音频流
    public static final byte VOICE_CALL_MAIN_SERVER_ANS = 0x42;

    // 手持终端请求停止推送音视频数据流到信息处理终端, 包括音视频
    public static final byte CALL_MAIN_SERVER_STOP_ASK = 0x43;
    // 信息处理终端回应收到停止推送请求
    public static final byte CALL_MAIN_SERVER_STOP_ANS = 0x44;

    public static final byte VOICE_CALL_ASK = 0x51;
    public static final byte VOICE_CALL_ANS = 0x52;
    public static final byte VIDEO_CALL_ASK = 0x53;
    public static final byte VIDEO_CALL_ANS = 0x54;
    public static final byte TEXT_MESSAGE = 0x61;
    public static final byte IMAGE_MESSAGE = 0x63;
    // 信息处理设备发送的文本消息
    public static final byte MAIN_SERVER_TEXT_MESSAGE = 0x65;
    public static final byte MAIN_SERVER_TEXT_MESSAGE_ANS = 0x66;
    // 信息处理设备请求手持终端上传视频
    public static final byte MAIN_SERVER_VIDEO_CALL_ASK = 0x67;
    public static final byte MAIN_SERVER_VIDEO_CALL_ANS = 0x68;
    // 信息处理设备请求手持终端上传音频
    public static final byte MAIN_SERVER_VOICE_CALL_ASK = 0x69;
    public static final byte MAIN_SERVER_VOICE_CALL_ANS = 0x70;

    public static byte[] createBytes(UdpMessage udpMessage){
        int length = 6;
        if(udpMessage.getData() != null && udpMessage.getData().length > 0){
            length = 6 + udpMessage.getData().length;
        }
        byte[] bytes = new byte[length];
        //成员编号
        bytes[0] = (byte) (udpMessage.getMemberNumber() >> 8 & 0xff);
        bytes[1] = (byte) (udpMessage.getMemberNumber() & 0xff);
        //功能码
        bytes[2] = udpMessage.getFactionCode();
        bytes[3] = udpMessage.getErrorCode();
        //数据长度
        bytes[4] = (byte) (udpMessage.getDataLength() >> 8 & 0xff);
        bytes[5] = (byte) (udpMessage.getDataLength() & 0xff);
        //
        if(udpMessage.getData() != null && udpMessage.getData().length > 0) {
            System.arraycopy(udpMessage.getData(), 0, bytes, 6, udpMessage.getData().length);
        }
        return bytes;
    }

    public static UdpMessage createHeart(String number, String ip, Location location){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(HEART);
        udpMessage.setDataLength((short)13);
        byte[] data = new byte[13];
        data[0] = 0x01;
        if(null != ip) {
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                data[1] = (byte) Integer.parseInt(ips[0]);
                data[2] = (byte) Integer.parseInt(ips[1]);
                data[3] = (byte) Integer.parseInt(ips[2]);
                data[4] = (byte) Integer.parseInt(ips[3]);
            }
        }
        int lng = (int)(location.getLng() * 10000000);
        int lat = (int)(location.getLat() * 10000000);
        byte[] byteLng = intToBytes(lng);
        byte[] byteLat = intToBytes(lat);
        System.arraycopy(byteLng, 0, data, 5, byteLng.length);
        System.arraycopy(byteLat, 0, data, 9, byteLng.length);
        udpMessage.setData(data);
        return udpMessage;
    }

    public static UdpMessage createLogin(String number, String password){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(LOGIN_ASK);
        char[] cPassword = password.toCharArray();
        byte [] byteNum = intToBytes(Integer.parseInt(number));
        byte[] data = new byte[4 + cPassword.length];
        data[0] = byteNum[0];
        data[1] = byteNum[1];
        data[2] = byteNum[2];
        data[3] = byteNum[3];
        byte[] bPassword = new byte[cPassword.length];
        for(int i = 0; i < cPassword.length; i++){
            bPassword[i] = (byte) cPassword[i];
        }
        System.arraycopy(bPassword, 0, data, 4, bPassword.length);
        udpMessage.setDataLength((short) data.length);
        udpMessage.setData(data);
        return udpMessage;
    }

    //创建语音呼叫回应命令
    public static UdpMessage createVoiceCallAns(String number, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VOICE_CALL_ANS);
        udpMessage.setErrorCode((byte) errorCode);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    //创建语音呼叫请求命令
    public static UdpMessage createVoiceCallAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VOICE_CALL_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    //创建视频呼叫回应命令
    public static UdpMessage createVideoCallAns(String number, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VIDEO_CALL_ANS);
        udpMessage.setErrorCode((byte) errorCode);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    //创建视频呼叫请求命令
    public static UdpMessage createVideoCallAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VIDEO_CALL_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建推送视频流到信息处理终端命令
    public static UdpMessage createVideoCallMainServerAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VIDEO_CALL_MAIN_SERVER_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建推送音频流到信息处理终端命令
    public static UdpMessage createVoiceCallMainServerAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VOICE_CALL_MAIN_SERVER_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建停止推送数据流到信息处理终端命令
    public static UdpMessage createCallMainServerStopAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(CALL_MAIN_SERVER_STOP_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建请求第三方推送视频流命令
    public static UdpMessage createVideoCallThirdAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VIDEO_CALL_THIRD_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建请求第三方推送音频流命令
    public static UdpMessage createVoiceCallThirdAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(VOICE_CALL_THIRD_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    // 创建请求第三方停止推送数据流
    public static UdpMessage createCallThirdStopAsk(String number){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(CALL_THIRD_STOP_ASK);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    public static UdpMessage createTextMessage(String number, String message){
        byte[] by = message.getBytes(StandardCharsets.UTF_8);
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(TEXT_MESSAGE);
        udpMessage.setDataLength((short) by.length);
        udpMessage.setData(by);
        return udpMessage;
    }

    public static UdpMessage createImageMessage(String number, byte[] imageBytes){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(IMAGE_MESSAGE);
        udpMessage.setDataLength((short) imageBytes.length);
        udpMessage.setData(imageBytes);
        return udpMessage;
    }

    /**
     * 创建图片消息
     * @param number 发放编号
     * @param imageBytes 图片部分字节
     * @param errorCode 错误码, 如果为1表示后面还有数据, 如果为0表示数据已发完
     * @return 消息
     */
    public static UdpMessage createSubImageMessage1(String number, byte[] imageBytes, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(IMAGE_MESSAGE);
        udpMessage.setErrorCode((byte)errorCode);
        udpMessage.setDataLength((short) imageBytes.length);
        udpMessage.setData(imageBytes);
        return udpMessage;
    }

    //创建视频呼叫回应命令
    public static UdpMessage createMainServerTextMessageAns(String number, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(MAIN_SERVER_TEXT_MESSAGE_ANS);
        udpMessage.setErrorCode((byte) errorCode);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    public static UdpMessage createMainServerVideoCallAns(String number, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(MAIN_SERVER_VIDEO_CALL_ANS);
        udpMessage.setErrorCode((byte) errorCode);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    public static UdpMessage createMainServerVoiceCallAns(String number, int errorCode){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(MAIN_SERVER_VOICE_CALL_ANS);
        udpMessage.setErrorCode((byte) errorCode);
        udpMessage.setDataLength((short) 0);
        return udpMessage;
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 将short数值转换为占二个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。
     */
    public static byte[] shortToBytes(short value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static short bytesToShort(byte[] bytes) {
        return (short) (bytes[0] * 16 + bytes[1]);
    }

    public static int bytesToInt(byte[] bytes) {
        return bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
    }

    public static void main(String[] args) {
        String[] strings = "a.b.c".split("\\.");
        UdpMessage udpMessage = createHeart("8000", "192.168.1.100", new Location(119.25745697692036, 34.73371279664106));
        byte[] bytes = createBytes(udpMessage);
        String str = Util.bytesToHexString(bytes);
        System.out.println(str);
    }
}
