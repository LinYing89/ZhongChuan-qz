package com.bairock.zhongchuan.qz.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import java.net.InetSocketAddress;

public class ClientBase {

    private String username;
    private Location location;// 位置信息
    private String ip;
    private Marker marker;
//    private InetSocketAddress inetSocketAddress;

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
        this.ip = ip;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

//    public InetSocketAddress getInetSocketAddress() {
//        return inetSocketAddress;
//    }
//
//    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
//        this.inetSocketAddress = inetSocketAddress;
//    }
}
