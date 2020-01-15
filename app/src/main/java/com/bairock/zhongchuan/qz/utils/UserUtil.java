package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.User;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    public static User user;

    public static List<User> users = new ArrayList<>();

    public static void addUser(User user){
        if(null == user || user.getNumber() == null){
            return;
        }
        for(User user1 : users){
            if(user1.getNumber().equals(user.getNumber())){
                return;
            }
        }
        users.add(user);
    }

    public static void initUsers(){
        user = new User();
        user.setNumber("8080");
        users.add(user);
        for(int i = 1; i < 6; i++) {
            User user = new User();
            user.setNumber("808" + i);
            user.setUserName("808" + i);
            users.add(user);
        }
    }

    public static User findUserByUsername(String username){
        for(User user : users){
            if(user.getNumber().equals(username)){
                return user;
            }
        }
        return null;
    }

    public static InetSocketAddress findInetSocketAddressByUsername(String username){
        for(User user : users){
            if(user.getNumber().equals(username)){
                return user.getInetSocketAddress();
            }
        }
        return null;
    }

    public static void setHeartInfo(InetSocketAddress inetSocketAddress, MessageRoot<Location> messageRoot){
        for(User user : users){
            if(user.getNumber().equals(messageRoot.getFrom())){
                user.setInetSocketAddress(inetSocketAddress);
                user.setLocation(messageRoot.getData());
            }
        }
    }

}
