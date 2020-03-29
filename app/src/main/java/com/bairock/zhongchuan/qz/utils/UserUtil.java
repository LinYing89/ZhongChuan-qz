package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageSource;
import com.bairock.zhongchuan.qz.bean.SoundRecorder;
import com.bairock.zhongchuan.qz.bean.Telescope;
import com.bairock.zhongchuan.qz.bean.UnmannedAerialVehicle;
import com.bairock.zhongchuan.qz.bean.User;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    public static User user;

    public static List<User> users = new ArrayList<>();

    public static List<Telescope> telescopes = new ArrayList<>();
    public static List<UnmannedAerialVehicle> unmannedAerialVehicles = new ArrayList<>();
    public static List<SoundRecorder> soundRecorders = new ArrayList<>();

    public static List<ClientBase> findClientBases(){
        List<ClientBase> clientBases = new ArrayList<>();
        clientBases.addAll(users);
        clientBases.addAll(telescopes);
        clientBases.addAll(unmannedAerialVehicles);
        clientBases.addAll(soundRecorders);
        return clientBases;
    }


    public static void addClientBase(ClientBase clientBase){
        if(clientBase instanceof User){
            addUser((User) clientBase);
        }else if(clientBase instanceof UnmannedAerialVehicle){
            addUnmannedAerialVehicle((UnmannedAerialVehicle) clientBase);
        }else if(clientBase instanceof Telescope) {
            addTelescope((Telescope) clientBase);
        }else if(clientBase instanceof SoundRecorder){
            addSoundRecorder((SoundRecorder) clientBase);
        }
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

    public static void addTelescope(Telescope telescope){
        if(null == telescope || telescope.getUsername() == null){
            return;
        }
        telescopes.add(telescope);
    }

    public static void addUnmannedAerialVehicle(UnmannedAerialVehicle unmannedAerialVehicle){
        if(null == unmannedAerialVehicle || unmannedAerialVehicle.getUsername() == null){
            return;
        }
        unmannedAerialVehicles.add(unmannedAerialVehicle);
    }

    public static void addSoundRecorder(SoundRecorder soundRecorder){
        if(null == soundRecorder || soundRecorder.getUsername() == null){
            return;
        }
        soundRecorders.add(soundRecorder);
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

        for(int i = 1; i < 3; i++) {
            SoundRecorder soundRecorder = new SoundRecorder();
            soundRecorder.setUsername("608" + i);
            soundRecorders.add(soundRecorder);
            TcpClientUtil.add(soundRecorder);
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

    public static String findIpByUsername(String username){
        for(User user : users){
            if(user.getUsername().equals(username)){
                return user.getIp();
            }
        }
        for(ClientBase user : telescopes){
            if(user.getUsername().equals(username)){
                return user.getIp();
            }
        }
        for(ClientBase user : unmannedAerialVehicles){
            if(user.getUsername().equals(username)){
                return user.getIp();
            }
        }
        for(ClientBase user : soundRecorders){
            if(user.getUsername().equals(username)){
                return user.getIp();
            }
        }
        return null;
    }

    public static InetSocketAddress findInetSocketAddressByUsername(String username){
        InetSocketAddress inetSocketAddress = null;
        for(User user : users){
            if(user.getUsername().equals(username)){
                inetSocketAddress = new InetSocketAddress(user.getIp(), 10000);
                break;
            }
        }
        if(null == inetSocketAddress){
            for(ClientBase user : telescopes){
                if(user.getUsername().equals(username)){
                    inetSocketAddress = new InetSocketAddress(user.getIp(), 10000);
                    break;
                }
            }
        }
        if(null == inetSocketAddress){
            for(ClientBase user : unmannedAerialVehicles){
                if(user.getUsername().equals(username)){
                    inetSocketAddress = new InetSocketAddress(user.getIp(), 10000);
                    break;
                }
            }
        }
        if(null == inetSocketAddress){
            for(ClientBase user : soundRecorders){
                if(user.getUsername().equals(username)){
                    inetSocketAddress = new InetSocketAddress(user.getIp(), 10000);
                    break;
                }
            }
        }
        return inetSocketAddress;
    }

//    public static void setHeartInfo(InetSocketAddress inetSocketAddress, MessageRoot<Location> messageRoot){
//        if(messageRoot.getSource() == MessageSource.PHONE) {
//            for (User user : users) {
//                if (user.getUsername().equals(messageRoot.getFrom())) {
//                    user.setInetSocketAddress(inetSocketAddress);
//                    user.setLocation(messageRoot.getData());
//                    TcpClientUtil.tryLink(user);
//                }
//            }
//        }else if(messageRoot.getSource() == MessageSource.UAV){
//            for (ClientBase user : unmannedAerialVehicles) {
//                if (user.getUsername().equals(messageRoot.getFrom())) {
//                    user.setInetSocketAddress(inetSocketAddress);
//                    user.setLocation(messageRoot.getData());
//                    TcpClientUtil.tryLink(user);
//                }
//            }
//        }else {
//            for (ClientBase user : telescopes) {
//                if (user.getUsername().equals(messageRoot.getFrom())) {
//                    user.setInetSocketAddress(inetSocketAddress);
//                    user.setLocation(messageRoot.getData());
//                    TcpClientUtil.tryLink(user);
//                }
//            }
//        }
//    }

    public static void setHeartInfo(String memberNumber, String ip, Location location){

        ClientBase clientBase = null;
        for (User user : users) {
            if (user.getUsername().equals(memberNumber)) {
                clientBase = user;
                break;
            }
        }
        if(null == clientBase){
            for (ClientBase user : unmannedAerialVehicles) {
                if (user.getUsername().equals(memberNumber)) {
                    clientBase = user;
                    break;
                }
            }
        }
        if(null == clientBase) {
            for (ClientBase user : telescopes) {
                if (user.getUsername().equals(memberNumber)) {
                    clientBase = user;
                    break;
                }
            }
        }
        if(null == clientBase) {
            for (ClientBase user : soundRecorders) {
                if (user.getUsername().equals(memberNumber)) {
                    clientBase = user;
                    break;
                }
            }
        }

        if(null != clientBase){
            clientBase.setIp(ip);
            clientBase.setLocation(location);
            if(clientBase instanceof User) {
                TcpClientUtil.tryLink(clientBase);
            }
        }
    }

}
