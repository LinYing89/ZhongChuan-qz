package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageSource;
import com.bairock.zhongchuan.qz.bean.Telescope;
import com.bairock.zhongchuan.qz.bean.UnmannedAerialVehicle;
import com.bairock.zhongchuan.qz.bean.User;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    public static User user;

    public static List<User> users = new ArrayList<>();

    public static List<Telescope> telescopes = new ArrayList<>();
    public static List<UnmannedAerialVehicle> unmannedAerialVehicles = new ArrayList<>();

    public static List<ClientBase> findClientBases(){
        List<ClientBase> clientBases = new ArrayList<>();
        clientBases.addAll(users);
        clientBases.addAll(telescopes);
        clientBases.addAll(unmannedAerialVehicles);
        return clientBases;
    }

    public static void addUser(User user){
        if(null == user || user.getUsername() == null){
            return;
        }
        for(User user1 : users){
            if(user1.getUsername().equals(user.getUsername())){
                return;
            }
        }
        users.add(user);
    }

    public static void initUsers(){
        user = new User();
        user.setUsername("8080");
        users.add(user);
        TcpClientUtil.add(user);
        for(int i = 1; i < 6; i++) {
            User user1 = new User();
            user1.setUsername("808" + i);
            user1.setRealName("808" + i);
            users.add(user1);
            TcpClientUtil.add(user1);
        }

        for(int i = 1; i < 3; i++) {
            Telescope telescope = new Telescope();
            telescope.setUsername("908" + i);
            telescopes.add(telescope);
            TcpClientUtil.add(telescope);
        }

        for(int i = 1; i < 3; i++) {
            UnmannedAerialVehicle unmannedAerialVehicle = new UnmannedAerialVehicle();
            unmannedAerialVehicle.setUsername("708" + i);
            unmannedAerialVehicles.add(unmannedAerialVehicle);
            TcpClientUtil.add(unmannedAerialVehicle);
        }
    }

    public static User findUserByUsername(String username){
        for(User user : users){
            if(user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }

    public static InetSocketAddress findInetSocketAddressByUsername(String username){
        InetSocketAddress inetSocketAddress = null;
        for(User user : users){
            if(user.getUsername().equals(username)){
                inetSocketAddress = user.getInetSocketAddress();
                break;
            }
        }
        if(null == inetSocketAddress){
            for(ClientBase user : telescopes){
                if(user.getUsername().equals(username)){
                    inetSocketAddress = user.getInetSocketAddress();
                    break;
                }
            }
        }
        if(null == inetSocketAddress){
            for(ClientBase user : unmannedAerialVehicles){
                if(user.getUsername().equals(username)){
                    inetSocketAddress = user.getInetSocketAddress();
                    break;
                }
            }
        }
        return inetSocketAddress;
    }

    public static void setHeartInfo(InetSocketAddress inetSocketAddress, MessageRoot<Location> messageRoot){
        if(messageRoot.getSource() == MessageSource.PHONE) {
            for (User user : users) {
                if (user.getUsername().equals(messageRoot.getFrom())) {
                    user.setInetSocketAddress(inetSocketAddress);
                    user.setLocation(messageRoot.getData());
                    TcpClientUtil.tryLink(user);
                }
            }
        }else if(messageRoot.getSource() == MessageSource.UAV){
            for (ClientBase user : unmannedAerialVehicles) {
                if (user.getUsername().equals(messageRoot.getFrom())) {
                    user.setInetSocketAddress(inetSocketAddress);
                    user.setLocation(messageRoot.getData());
                    TcpClientUtil.tryLink(user);
                }
            }
        }else {
            for (ClientBase user : telescopes) {
                if (user.getUsername().equals(messageRoot.getFrom())) {
                    user.setInetSocketAddress(inetSocketAddress);
                    user.setLocation(messageRoot.getData());
                    TcpClientUtil.tryLink(user);
                }
            }
        }
    }

}
