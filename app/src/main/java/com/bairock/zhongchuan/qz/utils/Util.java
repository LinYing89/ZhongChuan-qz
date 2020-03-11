package com.bairock.zhongchuan.qz.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Util {

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static int bytesToInt(byte[] by) {
        int value = 0;
        for (int i = 0; i < by.length; i++) {
            value = value << 8 | (by[i] & 0xff);
        }
        return value;
    }

    public static String getLocalIp() {
        try {
            Enumeration interfs = NetworkInterface.getNetworkInterfaces();

            while(interfs.hasMoreElements()) {
                NetworkInterface interf = (NetworkInterface)interfs.nextElement();
                Enumeration addres = interf.getInetAddresses();

                while(addres.hasMoreElements()) {
                    InetAddress in = (InetAddress)addres.nextElement();
                    if (in instanceof Inet4Address) {
                        String addr = in.getHostAddress();
                        if (!addr.equals("127.0.0.1")) {
                            return addr;
                        }
                    }
                }
            }
        } catch (SocketException var5) {
            var5.printStackTrace();
        }

        return null;
    }
}
