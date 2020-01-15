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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<MessageRoot<?>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRoot<?> msg) {
        if(msg == null || (!msg.getTo().equals("0") && !msg.getTo().equals(UserUtil.user.getNumber()))){
            return;
        }

        if(msg.getType() == MessageRootType.CHAT){
            if(msg.getTo().equals(UserUtil.user.getNumber())) {
                MessageRoot<ZCMessage> messageRoot = (MessageRoot<ZCMessage>) msg;
                messageRoot.getData().setDirect(ZCMessageDirect.RECEIVE);
                ConversationUtil.addReceivedMessage(messageRoot);

                Intent i = new Intent(ConversationUtil.CHAT_ACTION);
                i.putExtra("from", msg.getFrom());
                App.getInstance().sendOrderedBroadcast(i, ConversationUtil.CHAT_BROADCAST_PERMISSION);
            }
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
