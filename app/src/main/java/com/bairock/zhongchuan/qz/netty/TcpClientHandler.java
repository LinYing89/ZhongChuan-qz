package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.util.Log;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.adapter.MessageAdapter;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class TcpClientHandler extends ChannelInboundHandlerAdapter {

    Channel channel;

    private TcpClient tcpClient;

    public TcpClientHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String str = (String) msg;
        Log.e("TcpClientHandler", str);
        Gson gson = new Gson();
        Type type = new TypeToken<MessageRoot<ZCMessage>>() {}.getType();
        MessageRoot<ZCMessage> messageRoot = gson.fromJson(str, type);
        if(messageRoot.getType() == MessageRootType.CHAT){
            if(messageRoot.getTo().equals(UserUtil.user.getUsername())) {
                messageRoot.getData().setDirect(ZCMessageDirect.RECEIVE);
                ZCMessage message = messageRoot.getData();
                if(message.getMessageType() == ZCMessageType.IMAGE){
                    String flePath =  message.getContent();
                    String appPath = App.getInstance().getFilesDir().getAbsolutePath();
                    String newPath = appPath + MessageAdapter.IMAGE_DIR + new Date().getTime() + flePath.substring(flePath.lastIndexOf("."));
                    FileUtil.readBin2Image(message.getStream(), newPath);
                    message.setStream(null);
                }
                ConversationUtil.addReceivedMessage(messageRoot);

                Intent i = new Intent(ConversationUtil.CHAT_ACTION);
                i.putExtra("from", messageRoot.getFrom());
                App.getInstance().sendOrderedBroadcast(i, ConversationUtil.CHAT_BROADCAST_PERMISSION);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
         ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {  // 2
            IdleStateEvent event = (IdleStateEvent) evt;
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
//                sendHeart();
                System.out.println( ctx.channel().remoteAddress() +" send heart ");
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    public void send(String msg) {
        msg = msg + System.getProperty("line.separator");
        try {
            if (null != channel) {
                channel.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
