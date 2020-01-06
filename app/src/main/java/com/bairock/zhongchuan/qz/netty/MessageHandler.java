package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.google.gson.Gson;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<MessageRoot<?>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRoot<?> msg) {
        if(msg.getType() == MessageRootType.HEART){
            Location location = (Location) msg.getData();
            Log.e("MessageHandler", location.getLat() + "," + location.getLng());
        }
        Gson gson = new Gson();
        String json = gson.toJson(msg);
        Log.e("MessageHandler", json);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
