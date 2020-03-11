package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
//        ByteBuf data = msg.content(); // 获取对DatagramPacket 中的数据（ByteBuf）的引用
//        String strMsg = data.toString();
        ByteBuf byteBuf = msg.copy().content();
        if(byteBuf.readableBytes() < 6){
            return;
        }
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);

        int memberNumber = Util.bytesToInt(new byte[]{req[0], req[1]});
        byte factionCode = req[2];
        int length = Util.bytesToInt(new byte[]{req[4], req[5]});
        byte[] data = new byte[length];
        if(length > 0){
            System.arraycopy(req, 6, data, 0, length);
        }
        switch (factionCode){
            case UdpMessageHelper.HEART:
                int ip1 = data[0] & 0xff;
                int ip2 = data[1] & 0xff;
                int ip3 = data[2] & 0xff;
                int ip4 = data[3] & 0xff;
                String ip = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                int lng = UdpMessageHelper.bytesToInt(new byte[]{data[4], data[5], data[6], data[7]});
                int lat = UdpMessageHelper.bytesToInt(new byte[]{data[8], data[9], data[10], data[11]});
                double dlng = lng / 10000000d;
                double dlat = lat / 10000000d;
                UserUtil.setHeartInfo(String.valueOf(memberNumber), ip, new Location(dlng, dlat));
                break;
            default: break;
        }
        out.add(byteBuf);

//        String strMsg = new String(bytes);
//
//        JsonObject jsonObject = JsonParser.parseString(strMsg).getAsJsonObject();
//        String strType = jsonObject.get("type").getAsString();
//        Gson gson = new Gson();
//        switch (strType) {
//            case "HEART": {
//                Type type = new TypeToken<MessageRoot<Location>>() {
//                }.getType();
//                MessageRoot<Location> zcMessage = gson.fromJson(strMsg, type);
////                UserUtil.setHeartInfo(msg.sender(), zcMessage);
//                out.add(zcMessage);
////                Log.e("MessageDecoder", msg.sender().toString());
//                break;
//            }
//            case "CHAT": {
//                Type type = new TypeToken<MessageRoot<ZCMessage>>() {
//                }.getType();
//                MessageRoot<ZCMessage> zcMessage = gson.fromJson(strMsg, type);
//                out.add(zcMessage);
//                break;
//            }
//            case "VIDEO": {
////                Type type = new TypeToken<MessageRoot<byte[]>>() {
////                }.getType();
////                MessageRoot<byte[]> zcMessage = gson.fromJson(strMsg, type);
////                out.add(zcMessage);
////                break;
//            }
//            default:
//                out.add(null);
//                break;
//        }

    }
}
