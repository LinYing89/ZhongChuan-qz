package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.utils.UserUtil;

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

public class H264bRoadcaster {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public static Channel channel;

    public H264bRoadcaster() {
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
//                        pipeline.addLast(new MessageEncoder(new InetSocketAddress("255.255.255.255", 10000)));
                        pipeline.addLast(new H264Decoder());
//                        pipeline.addLast(new MessageHandler());
                    }
                } )
                .localAddress(new InetSocketAddress(10001));
    }
    public Channel bind() {
        channel =  bootstrap.bind(10001).syncUninterruptibly().channel();
        return channel;
    }

    public static void send(byte[] bytes, String ip){
        if(null != channel) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, 10001);
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                    inetSocketAddress));
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }
}
