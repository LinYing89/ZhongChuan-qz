package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.activity.VideoCallActivity;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class H264Decoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
//        ByteBuf data = msg.content(); // 获取对DatagramPacket 中的数据（ByteBuf）的引用
//        String strMsg = data.toString();
        ByteBuf byteBuf = msg.copy().content();
        if (byteBuf.readableBytes() < 6) {
            return;
        }
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        VideoCallActivity.player.write(req);
//        Log.e("H264Decoder", Util.bytesToHexString(req));
    }
}
