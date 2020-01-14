package com.bairock.zhongchuan.qz.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.bairock.zhongchuan.qz.enums.UserStatus;

import java.net.InetSocketAddress;

public class User {
	private String id;//
	private String password;
	private String number;
	private String userName;// 用户名
	private String headUrl;// 头像保存路径
	private Location location;// 位置信息
	private String ip;
	private UserStatus userStatus = UserStatus.ONLINE;
	private Marker marker;

	private InetSocketAddress inetSocketAddress;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
}
