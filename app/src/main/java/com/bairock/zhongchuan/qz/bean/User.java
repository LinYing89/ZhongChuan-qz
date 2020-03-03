package com.bairock.zhongchuan.qz.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.bairock.zhongchuan.qz.enums.UserStatus;

import java.net.InetSocketAddress;

public class User extends ClientBase{
	private String id;//
	private String password;
	private String realName;// 用户名
	private String headUrl;// 头像保存路径
	private UserStatus userStatus = UserStatus.ONLINE;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

}
