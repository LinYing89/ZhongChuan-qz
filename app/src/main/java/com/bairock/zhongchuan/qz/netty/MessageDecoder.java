package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.utils.UserUtil;
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
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String strMsg = new String(bytes);

        JsonObject jsonObject = JsonParser.parseString(strMsg).getAsJsonObject();
        String strType = jsonObject.get("type").getAsString();
        Gson gson = new Gson();
        switch (strType) {
            case "HEART": {
                Type type = new TypeToken<MessageRoot<Location>>() {
                }.getType();
                MessageRoot<Location> zcMessage = gson.fromJson(strMsg, type);
                UserUtil.setHeartInfo(msg.sender(), zcMessage);
                out.add(zcMessage);
                break;
            }
            case "CHAT": {
                Type type = new TypeToken<MessageRoot<ZCMessage>>() {
                }.getType();
                MessageRoot<ZCMessage> zcMessage = gson.fromJson(strMsg, type);
                out.add(zcMessage);
                break;
            }
            case "VIDEO": {
                Type type = new TypeToken<MessageRoot<byte[]>>() {
                }.getType();
                MessageRoot<byte[]> zcMessage = gson.fromJson(strMsg, type);
                out.add(zcMessage);
                break;
            }
            default:
                out.add(null);
                break;
        }
        Log.e("MessageDecoder", msg.sender().toString());
    }
}
