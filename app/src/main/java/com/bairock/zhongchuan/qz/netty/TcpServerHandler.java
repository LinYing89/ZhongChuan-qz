package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.util.Log;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String str = (String)msg;
        Log.e("TcpServerHandler", str);
        Gson gson = new Gson();
        Type type = new TypeToken<MessageRoot<ZCMessage>>() {}.getType();
        MessageRoot<ZCMessage> messageRoot = gson.fromJson(str, type);
        if(messageRoot.getType() == MessageRootType.CHAT){
            if(messageRoot.getTo().equals(UserUtil.user.getUsername())) {
                messageRoot.getData().setDirect(ZCMessageDirect.RECEIVE);
                ConversationUtil.addReceivedMessage(messageRoot);

                Intent i = new Intent(ConversationUtil.CHAT_ACTION);
                i.putExtra("from", messageRoot.getFrom());
                App.getInstance().sendOrderedBroadcast(i, ConversationUtil.CHAT_BROADCAST_PERMISSION);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {  // 2
            IdleStateEvent event = (IdleStateEvent) evt;
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
            System.out.println( ctx.channel().remoteAddress()+"超时类型：" + type);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
