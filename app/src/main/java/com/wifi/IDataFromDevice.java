package com.wifi;

public interface IDataFromDevice {
	public void OnStreamInfo(int nWidth, int nHeigh);
	public void OnStream(int eAVCodecID, byte[] bytAVData, int nDataSize);
	
	public void OnMsg(Object o, int nCmdType, byte[] pData, int nDataSize);

	public void OnH264(byte[] bytAVData, int nDataSize);
}
