package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.utils.Util;

public class UdpMessageHelper {

    public static final byte HEART = 0x01;

    public static byte[] createBytes(UdpMessage udpMessage){
        byte[] bytes = new byte[6 + udpMessage.getData().length];
        //成员编号
        bytes[0] = (byte) (udpMessage.getMemberNumber() >> 8 & 0xff);
        bytes[1] = (byte) (udpMessage.getMemberNumber() & 0xff);
        //功能码
        bytes[2] = udpMessage.getFactionCode();
        //数据长度
        bytes[4] = (byte) (udpMessage.getDataLength() >> 8 & 0xff);
        bytes[5] = (byte) (udpMessage.getDataLength() & 0xff);
        //
        System.arraycopy(udpMessage.getData(), 0, bytes, 6, udpMessage.getData().length);
        return bytes;
    }

    public static UdpMessage createHeart(String number, String ip, Location location){
        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setMemberNumber(Short.parseShort(number));
        udpMessage.setFactionCode(HEART);
        udpMessage.setDataLength((short)12);
        byte[] data = new byte[12];
        if(null != ip) {
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                data[0] = (byte) Integer.parseInt(ips[0]);
                data[1] = (byte) Integer.parseInt(ips[1]);
                data[2] = (byte) Integer.parseInt(ips[2]);
                data[3] = (byte) Integer.parseInt(ips[3]);
            }
        }
        int lng = (int)(location.getLng() * 10000000);
        int lat = (int)(location.getLat() * 10000000);
        byte[] byteLng = intToBytes(lng);
        byte[] byteLat = intToBytes(lat);
        System.arraycopy(byteLng, 0, data, 4, byteLng.length);
        System.arraycopy(byteLat, 0, data, 8, byteLng.length);
        udpMessage.setData(data);
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
