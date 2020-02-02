package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.User;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.netty.TcpClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TcpClientUtil {
    public static List<TcpClient> tcpClients = new ArrayList<>();

    public static void add(User user){
        TcpClient tcpClient = new TcpClient(user);
        tcpClients.add(tcpClient);
    }

    private static TcpClient findByUser(User user){
        return findByUsername(user.getUsername());
    }

    private static TcpClient findByUsername(String username){
        for(TcpClient tcpClient : tcpClients){
            if(tcpClient.getUser().getUsername().equals(username)){
                return tcpClient;
            }
        }
        return null;
    }

    public static void tryLink(User user){
        TcpClient tcpClient = findByUser(user);
        if(null != tcpClient){
            if(!tcpClient.isLinked()){
                tcpClient.link();
            }
        }
    }

    public static void send(final MessageRoot<ZCMessage> messageRoot){
        String username = messageRoot.getTo();
        final TcpClient tcpClient = findByUsername(username);
        if(null != tcpClient){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    tcpClient.send(gson.toJson(messageRoot));
                    ZCMessage message = messageRoot.getData();
                    if(message.getMessageType() != ZCMessageType.TXT){
                        message.setStream(null);
                    }
                }
            });
            CommonUtils.executeThread(thread);
        }
    }
}
