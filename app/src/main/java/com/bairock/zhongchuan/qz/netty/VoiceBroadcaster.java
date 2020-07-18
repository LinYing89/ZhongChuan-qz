package com.bairock.zhongchuan.qz.netty;

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

/**
 * 音频流接受, 端口10002
 */
public class VoiceBroadcaster {

    private static VoiceBroadcaster ins = new VoiceBroadcaster();

    public static final int PORT = 10001;

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    public static Channel channel;

    public static VoiceBroadcaster getIns(){
        return ins;
    }

    private VoiceBroadcaster() {
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
                        pipeline.addLast(new VoiceDecoder());
                    }
                } )
                .localAddress(new InetSocketAddress(PORT));
    }
    public Channel bind() {
        channel =  bootstrap.bind(PORT).syncUninterruptibly().channel();
        return channel;
    }

    public static void send(byte[] bytes, String ip){
        if(null != channel) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, PORT);
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                    inetSocketAddress));
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }
}
