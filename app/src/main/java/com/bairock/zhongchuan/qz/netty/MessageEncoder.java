package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.google.gson.Gson;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

public class MessageEncoder extends MessageToMessageEncoder<byte[]> {

    private final InetSocketAddress remoteAddress;

    MessageEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) {
//        Gson gson = new Gson();
//        String json = gson.toJson(msg);
//        byte[] bytes = json.getBytes();
        ByteBuf byteBuf = ctx.alloc().buffer(msg.length);// 分配byteBuf的内存
        byteBuf.writeBytes(msg);
        out.add(new DatagramPacket(byteBuf, remoteAddress));
    }
}
