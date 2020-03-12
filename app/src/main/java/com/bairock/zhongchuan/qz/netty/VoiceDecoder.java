package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.view.activity.VideoCallActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceCallActivity;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class VoiceDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
//        ByteBuf data = msg.content(); // 获取对DatagramPacket 中的数据（ByteBuf）的引用
//        String strMsg = data.toString();
        ByteBuf byteBuf = msg.copy().content();
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        VoiceCallActivity.listen.write(req);
//        Log.e("H264Decoder", Util.bytesToHexString(req));
    }
}