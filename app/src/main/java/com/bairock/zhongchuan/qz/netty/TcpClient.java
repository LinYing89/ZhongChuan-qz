package com.bairock.zhongchuan.qz.netty;

import android.util.Log;

import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.User;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class TcpClient {

    private Bootstrap b;

    private boolean linking;
    private TcpClientHandler tcpClientHandler;
    private ClientBase user;

    public TcpClient(ClientBase user) {
        this.user = user;
        tcpClientHandler = new TcpClientHandler(this);
        init();
    }

    public ClientBase getUser() {
        return user;
    }

    public void setUser(ClientBase user) {
        this.user = user;
    }

    private void init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        b = new Bootstrap(); // (1)
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline ph = ch.pipeline();
//                ph.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8));
                // 以("\n")为结尾分割的 解码器
                ph.addLast("framer", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                // 解码和编码，应和客户端一致
                ph.addLast("decoder", new StringDecoder(StandardCharsets.UTF_8));
                ph.addLast("encoder", new StringEncoder(StandardCharsets.UTF_8));

                ph.addLast(new IdleStateHandler(-1, 30, -1, TimeUnit.SECONDS)); // 1
                ph.addLast(tcpClientHandler);
            }
        });
    }

    public boolean isLinked() {
        return null != tcpClientHandler && null != tcpClientHandler.channel && tcpClientHandler.channel.isActive();
    }

    public void link() {
        if (linking || isLinked()) {
            return;
        }
        if(user.getInetSocketAddress() == null){
            return;
        }
        linking = true;
        tcpClientHandler = new TcpClientHandler(this);
        String serverName = user.getInetSocketAddress().getHostString();
        Log.e("TcpClient", "link to " + serverName);
//        int port = user.getInetSocketAddress().getPort();
        try {
            // Start the client.
            ChannelFuture channelFuture = b.connect(serverName, 8888); // (5)
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture();
            Log.e("TcpClient", "linked success " + serverName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        linking = false;
    }

    public void linkSync() {
        if (linking) {
            return;
        }
        if(user.getInetSocketAddress() == null){
            return;
        }
        linking = true;
        String serverName = user.getInetSocketAddress().getHostString();
        int port = user.getInetSocketAddress().getPort();
        try {
            // Start the client.
            ChannelFuture channelFuture = b.connect(serverName, port); // (5)
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture().sync();
            Log.e("TcpClient", "linked success " + serverName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        linking = false;
    }

    public void send(String msg) {
//        if(!isLinked()){
//            linkSync();
//        }
        Log.e("TcpClient", user.getUsername() + " linked = " + isLinked());
        if (isLinked()) {
            tcpClientHandler.send(msg);
        }
    }
}
