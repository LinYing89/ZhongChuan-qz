package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.User;

import java.util.ArrayList;
import java.util.List;

public class UserUtil {

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
        for(int i = 0; i < 6; i++) {
            User user = new User();
            user.setNumber("808" + i);
            user.setUserName("808" + i);
            users.add(user);
        }
    }

}
