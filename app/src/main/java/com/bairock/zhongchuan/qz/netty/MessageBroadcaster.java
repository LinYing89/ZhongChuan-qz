package com.bairock.zhongchuan.qz.netty;

import com.bairock.zhongchuan.qz.bean.MessageRoot;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

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
                .handler( new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new MessageEncoder(new InetSocketAddress("255.255.255.255", 9999)));
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageHandler());
                    }
                } )
                .localAddress(new InetSocketAddress(9999));
    }
    public Channel bind() {
        channel =  bootstrap.bind().syncUninterruptibly().channel();
        return channel;
    }

    public static void send(MessageRoot message){
        if(null != channel) {
            channel.writeAndFlush(message);
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }
}
