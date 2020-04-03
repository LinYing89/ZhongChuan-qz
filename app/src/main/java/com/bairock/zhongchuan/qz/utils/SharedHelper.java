package com.bairock.zhongchuan.qz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bairock.zhongchuan.qz.App;

/**
 *
 * Created by Administrator on 2017/5/19.
 */

public class SharedHelper {
	private final String policeNum = "policeNum";
	private final String policeName = "policeName";
	private final String psd = "psd";

    private final String runOnPowerOn = "runOnPowerOn";

	/**
	 * file name of user
	 */
	private final String user = "user";

	public SharedHelper() {
	}

	private SharedPreferences getSharedFile(String sharedName) {
		return App.getInstance2().getSharedPreferences(sharedName,
				Context.MODE_PRIVATE);
	}

	public void getAll(){
		SharedPreferences shared = getSharedFile(user);
		Config.runOnPowerOn = shared.getBoolean(runOnPowerOn, true);
		Config.username = shared.getString(policeName, "8080");
		Config.password = shared.getString(psd, "");

	}

    public void putRunOnPowerOn(){
        SharedPreferences shared = getSharedFile(user);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(runOnPowerOn, Config.runOnPowerOn);
        editor.apply();
    }

	public void putUser(){
		SharedPreferences shared = getSharedFile(user);
		SharedPreferences.Editor editor = shared.edit();
		editor.putString(policeName, Config.username);
		editor.putString(psd, Config.password);
		editor.apply();
	}
}
