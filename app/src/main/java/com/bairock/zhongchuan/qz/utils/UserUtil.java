package com.bairock.zhongchuan.qz.utils;

import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.enums.ClientBaseType;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    public static ClientBase user = new ClientBase();

    public static List<ClientBase> clientBases = new ArrayList<>();

//    public static List<User> users = new ArrayList<>();
//
//    public static List<Telescope> telescopes = new ArrayList<>();
//    public static List<UnmannedAerialVehicle> unmannedAerialVehicles = new ArrayList<>();
//    public static List<SoundRecorder> soundRecorders = new ArrayList<>();

    public static List<ClientBase> findClientBases(){
        return clientBases;
//        List<ClientBase> clientBases = new ArrayList<>();
//        clientBases.addAll(users);
//        clientBases.addAll(telescopes);
//        clientBases.addAll(unmannedAerialVehicles);
//        clientBases.addAll(soundRecorders);
//        return clientBases;
    }


    public static void addClientBase(ClientBase clientBase){
        clientBases.add(clientBase);
        if(clientBase.getClientBaseType() == ClientBaseType.PHONE){
            TcpClientUtil.add(clientBase);
        }
//        if(clientBase instanceof User){
//            addUser((User) clientBase);
//        }else if(clientBase instanceof UnmannedAerialVehicle){
//            addUnmannedAerialVehicle((UnmannedAerialVehicle) clientBase);
//        }else if(clientBase instanceof Telescope) {
//            addTelescope((Telescope) clientBase);
//        }else if(clientBase instanceof SoundRecorder){
//            addSoundRecorder((SoundRecorder) clientBase);
//        }
    }
//
//    public static void addUser(User user){
//        if(null == user || user.getUsername() == null){
//            return;
//        }
//        for(User user1 : users){
//            if(user1.getUsername().equals(user.getUsername())){
//                return;
//            }
//        }
//        users.add(user);
//    }

//    public static void addTelescope(Telescope telescope){
//        if(null == telescope || telescope.getUsername() == null){
//            return;
//        }
//        telescopes.add(telescope);
//    }

//    public static void addUnmannedAerialVehicle(UnmannedAerialVehicle unmannedAerialVehicle){
//        if(null == unmannedAerialVehicle || unmannedAerialVehicle.getUsername() == null){
//            return;
//        }
//        unmannedAerialVehicles.add(unmannedAerialVehicle);
//    }

//    public static void addSoundRecorder(SoundRecorder soundRecorder){
//        if(null == soundRecorder || soundRecorder.getUsername() == null){
//            return;
//        }
//        soundRecorders.add(soundRecorder);
//    }

    public static void initUsers(){
        user = new ClientBase();
        user.setUsername("8080");
        user.setClientBaseType(ClientBaseType.PHONE);
        clientBases.add(user);
        TcpClientUtil.add(user);
        for(int i = 1; i < 6; i++) {
            ClientBase user1 = new ClientBase();
            user1.setUsername("808" + i);
            user1.setRealName("808" + i);
            user1.setClientBaseType(ClientBaseType.PHONE);
            clientBases.add(user1);
            TcpClientUtil.add(user1);
        }

        for(int i = 1; i < 3; i++) {
            ClientBase telescope = new ClientBase();
            telescope.setUsername("908" + i);
            telescope.setClientBaseType(ClientBaseType.TELESCOPE);
            clientBases.add(telescope);
//            TcpClientUtil.add(telescope);
        }

        for(int i = 1; i < 3; i++) {
            ClientBase unmannedAerialVehicle = new ClientBase();
            unmannedAerialVehicle.setUsername("708" + i);
            unmannedAerialVehicle.setClientBaseType(ClientBaseType.UAV);
            clientBases.add(unmannedAerialVehicle);
//            TcpClientUtil.add(unmannedAerialVehicle);
        }

        for(int i = 1; i < 3; i++) {
            ClientBase soundRecorder = new ClientBase();
            soundRecorder.setUsername("608" + i);
            soundRecorder.setClientBaseType(ClientBaseType.SOUND_RECORDER);
            clientBases.add(soundRecorder);
//            TcpClientUtil.add(soundRecorder);
        }
        ClientBase mainServer = new ClientBase();
        mainServer.setUsername("1000");
        mainServer.setClientBaseType(ClientBaseType.MAIN_SERVER);
        clientBases.add(mainServer);
    }

    public static List<ClientBase> findPhoneUser(){
        List<ClientBase> phoneUsers = new ArrayList<>();
        for(ClientBase user : clientBases){
            if(user.getClientBaseType() == ClientBaseType.PHONE){
                phoneUsers.add(user);
            }
        }
        return phoneUsers;
    }

    public static ClientBase findUserByUsername(String username){
        for(ClientBase user : clientBases){
            if(user.getClientBaseType() == ClientBaseType.PHONE && user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }

    public static String findIpByUsername(String username){
        for(ClientBase user : clientBases){
            if(user.getUsername().equals(username)){
                return user.getIp();
            }
        }
        return null;
    }

    public static InetSocketAddress findInetSocketAddressByUsername(String username){
        for(ClientBase user : clientBases){
            if(user.getUsername().equals(username)){
                return new InetSocketAddress(user.getIp(), 10001);
            }
        }
        return null;
    }

    public static void setHeartInfo(String memberNumber, String ip, Location location){

        ClientBase clientBase = null;
        for(ClientBase user : clientBases){
            if(user.getUsername().equals(memberNumber)){
                clientBase = user;
                break;
            }
        }

        if(null != clientBase){
            clientBase.setIp(ip);
            clientBase.setLocation(location);
            if(clientBase.getClientBaseType() == ClientBaseType.PHONE) {
                TcpClientUtil.tryLink(clientBase);
            }
        }
    }

}
