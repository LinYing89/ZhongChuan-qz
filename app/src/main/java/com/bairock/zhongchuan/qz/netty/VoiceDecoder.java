package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.bean.User;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.activity.VideoCallActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceCallActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceUploadThirdActivity;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
        byteBuf.release();
        if(null != VoiceCallActivity.listen){
            VoiceCallActivity.listen.write(req);
        }else if (null != VoiceUploadThirdActivity.listen){
            VoiceUploadThirdActivity.listen.write(req);
            // todo 传给后台
        }else{
            UdpMessage udpMessage = UdpMessageHelper.createVoiceCallAns(UserUtil.user.getUsername(), 1);
            byte[] bytes = UdpMessageHelper.createBytes(udpMessage);
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                    msg.sender()));
        }
//        Log.e("H264Decoder", Util.bytesToHexString(req));
    }
}
