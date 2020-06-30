package com.bairock.zhongchuan.qz.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.bairock.zhongchuan.qz.enums.ClientBaseType;
import com.bairock.zhongchuan.qz.enums.UserStatus;

public class ClientBase {

    private String username;
    private Location location;// 位置信息
    private String ip;
    private Marker marker;

    private String id;//
    private String password;
    private String realName;// 用户名
    private String headUrl;// 头像保存路径
    private ClientBaseType clientBaseType = null;
    private UserStatus userStatus = UserStatus.ONLINE;
//    private InetSocketAddress inetSocketAddress;

    private OnIpChangedListener onIpChangedListener;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        if(null != marker){
            marker.setPosition(new LatLng(location.getLat(), location.getLng()));
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        if(ip == null && this.ip == null){
            return;
        }
        if (this.ip == null || !this.ip.equals(ip)){
            this.ip = ip;
            if (null != onIpChangedListener) {
                onIpChangedListener.onIpChanged();
            }
        }
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public ClientBaseType getClientBaseType() {
        return clientBaseType;
    }

    public void setClientBaseType(ClientBaseType clientBaseType) {
        this.clientBaseType = clientBaseType;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public OnIpChangedListener getOnIpChangedListener() {
        return onIpChangedListener;
    }

    public void setOnIpChangedListener(OnIpChangedListener onIpChangedListener) {
        this.onIpChangedListener = onIpChangedListener;
    }

    public interface OnIpChangedListener{
        void onIpChanged();
    }
}
