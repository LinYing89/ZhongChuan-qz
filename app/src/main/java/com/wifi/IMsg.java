package com.wifi;

public interface IMsg {
	public int OnCallbackMsg(int nCmdType, byte[] pData, int nDataSize, int nUserData);
}
