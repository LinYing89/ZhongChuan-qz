package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.util.Log;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        if(m.readableBytes() < 6){
            return;
        }
        byte[] req = new byte[m.readableBytes()];
        m.readBytes(req);
        int memberNumber = req[0] << 8 | req[1];
        byte factionCode = req[2];
        int length = req[4] << 8 | req[5];
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
