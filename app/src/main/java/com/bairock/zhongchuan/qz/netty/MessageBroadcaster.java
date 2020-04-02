package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.google.gson.Gson;

import org.jivesoftware.smack.util.StringUtils;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

public class MessageBroadcaster {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public static Channel channel;

    public MessageBroadcaster() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 2048)
                .option(ChannelOption.SO_SNDBUF, 1024 * 2048)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 2048))
                .handler( new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new MessageEncoder(new InetSocketAddress("255.255.255.255", 10000)));
                        pipeline.addLast(new MessageDecoder());
//                        pipeline.addLast(new MessageHandler());
                    }
                } )
                .localAddress(new InetSocketAddress(10000));
    }
    public Channel bind() {
        channel =  bootstrap.bind(10000).syncUninterruptibly().channel();
        return channel;
    }

//    public static void send(MessageRoot message){
//        if(null != channel) {
//            String to = message.getTo();
//            if(!to.equals("0")){
//                InetSocketAddress inetSocketAddress = UserUtil.findInetSocketAddressByUsername(to);
//                Log.e("MessageBroadcaster", "send to " + inetSocketAddress);
//                if(null == inetSocketAddress){
//                    return;
//                }
//                Gson gson = new Gson();
//                String json = gson.toJson(message);
//
//                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8),
//                        inetSocketAddress));
//            }else {
//                channel.writeAndFlush(message);
//            }
//        }
//    }

//    public static void send(MessageRoot message, String to){
//        if(null != channel) {
////            String to = message.getTo();
//            if(!to.equals("0")){
//                InetSocketAddress inetSocketAddress = UserUtil.findInetSocketAddressByUsername(to);
//                Log.e("MessageBroadcaster", "send to " + inetSocketAddress);
//                if(null == inetSocketAddress){
//                    return;
//                }
//                Gson gson = new Gson();
//                String json = gson.toJson(message);
//
//                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8),
//                        inetSocketAddress));
//            }else {
//                channel.writeAndFlush(message);
//            }
//        }
//    }

    public static void send(UdpMessage udpMessage, String to){
        if(null != channel) {
            byte[] bytes = UdpMessageHelper.createBytes(udpMessage);
            if(null != to && !to.isEmpty() && !to.equals("0")){
                InetSocketAddress inetSocketAddress = UserUtil.findInetSocketAddressByUsername(to);
                Log.e("MessageBroadcaster", "send to " + inetSocketAddress);
                if(null == inetSocketAddress){
                    return;
                }
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                        inetSocketAddress));
            }else {
                channel.writeAndFlush(bytes);
            }
        }
    }

    public static void sendIp(UdpMessage udpMessage, String ip, int port){
        if(null != channel) {
            byte[] bytes = UdpMessageHelper.createBytes(udpMessage);
            if(null == ip || ip.isEmpty()) {
                channel.writeAndFlush(bytes);
            }else{
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
            Log.e("MessageBroadcaster", "send to " + inetSocketAddress);
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                    inetSocketAddress));
            }
        }
    }

    public static void sendBroadcast(UdpMessage udpMessage){
        if(null != channel) {
            byte[] bytes = UdpMessageHelper.createBytes(udpMessage);
            channel.writeAndFlush(bytes);
//            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
//                    new InetSocketAddress("192.168.1.6", 10000)));
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }
}
