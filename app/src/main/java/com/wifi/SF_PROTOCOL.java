package com.wifi;

import com.utility.Convert;

import java.util.Arrays;

public class SF_PROTOCOL {
	public static final int IOCTRL_TYPE_UNKN=0;
	//现场视频音频请求
	public static final int IOCTRL_TYPE_LIVE_START_REQ 	= 1;
	public static final int IOCTRL_TYPE_LIVE_START_RESP = 2;

	//停止播放
	public static final int IOCTRL_TYPE_LIVE_STOP_REQ = 3;
	public static final int IOCTRL_TYPE_LIVE_STOP_RESP = 4;
	
	//获取WiFi参数
	public static final int IOCTRL_TYPE_GET_AP_PARAMETERS_REQ = 9;
	public static final int IOCTRL_TYPE_GET_AP_PARAMETERS_RESP = 10;
	
	//设置WiFi参数
	public static final int IOCTRL_TYPE_SET_AP_PARAMETERS_REQ = 11;
	public static final int IOCTRL_TYPE_SET_AP_PARAMETERS_RESP = 12;

	//获取录像文件列表
	public static final int IOCTRL_TYPE_LIST_RECORDFILES_REQ = 21;
	public static final int IOCTRL_TYPE_LIST_RECORDFILES_RESP = 22;

	//下载录像文件
	public static final int IOCTRL_TYPE_DOWNLOAD_RECORD_FILE_REQ = 23;
	public static final int IOCTRL_TYPE_DOWNLOAD_RECORD_FILE_RESP = 24;

	//回放录像文件
	public static final int IOCTRL_TYPE_RECORD_PLAYCONTROL_REQ = 25;
	public static final int IOCTRL_TYPE_RECORD_PLAYCONTROL_RESP = 26;
	
	//网络控制设备录像
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_START_REQ = 27;
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_START_RESP = 28;
	//检查录像状态
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_CHECK_REQ = 29;
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_CHECK_RESP = 30;
	//停止录像
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_STOP_REQ = 31;
	public static final int IOCTRL_TYPE_NET_TRIGGER_RECORD_STOP_RESP = 32;

	//设备参数设置
	public static final int IOCTRL_TYPE_GET_SYSFWVER_REQ = 39;	//App to Device
	public static final int IOCTRL_TYPE_GET_SYSFWVER_RESP = 40;	//Device to App

	//获取设备时间
	public static final int IOCTRL_TYPE_GET_DEV_TIME_REQ = 41;
	public static final int IOCTRL_TYPE_GET_DEV_TIME_RESP = 42;
	
	//设置设备时间
	public static final int IOCTRL_TYPE_SET_DEV_TIME_REQ = 43;
	public static final int IOCTRL_TYPE_SET_DEV_TIME_RESP = 44;

	//设置设备访问密码
	public static final int IOCTRL_TYPE_CHANGE_DEV_PASSWORD_REQ = 45;
	public static final int IOCTRL_TYPE_CHANGE_DEV_PASSWORD_RESP = 46;

	// 图像参数设置
	public static final int IOCTRL_TYPE_GET_DEV_IMAGE_PARAMETERS_REQ = 51;
	public static final int IOCTRL_TYPE_GET_DEV_IMAGE_PARAMETERS_RESP = 52;
	
	public static final int IOCTRL_TYPE_SET_DEV_IMAGE_PARAMETERS_REQ = 53;
	public static final int IOCTRL_TYPE_SET_DEV_IMAGE_PARAMETERS_RESP = 54;


	// 视频参数设置
	public static final int IOCTRL_TYPE_GET_DEV_VIDEO_PARAMETERS_REQ = 55;
	public static final int IOCTRL_TYPE_GET_DEV_VIDEO_PARAMETERS_RESP = 56;
	
	public static final int IOCTRL_TYPE_SET_DEV_VIDEO_PARAMETERS_REQ = 57;
	public static final int IOCTRL_TYPE_SET_DEV_VIDEO_PARAMETERS_RESP = 58;

	//录像参数设置
	public static final int IOCTRL_TYPE_GET_RECORD_PARAMETERS_REQ = 63;
	public static final int IOCTRL_TYPE_GET_RECORD_PARAMETERS_RESP = 64;
	
	public static final int IOCTRL_TYPE_SET_RECORD_PARAMETERS_REQ = 65;
	public static final int IOCTRL_TYPE_SET_RECORD_PARAMETERS_RESP = 66;	
	

	//验证密码 &  授权
	public static final int IOCTRL_TYPE_AUTHORIZE_REQ = 67; 
	public static final int IOCTRL_TYPE_AUTHORIZE_RESP = 68;

	// SD卡 TF卡 操作
	public static final int IOCTRL_TYPE_SET_DEVICE_SDFORMAT_REQ = 76;
	public static final int IOCTRL_TYPE_SET_DEVICE_SDFORMAT_RESP = 77;
	
	public static final int IOCTRL_TYPE_SDFORMAT_QUERY_REQ = 78;
	public static final int IOCTRL_TYPE_SDFORMAT_QUERY_RESP = 79;
		
	public static final int IOCTRL_TYPE_GET_SD_INFO_REQ = 80;
	public static final int IOCTRL_TYPE_GET_SD_INFO_RESP = 81;   

	//设置设备工作频率 50hz  or 60hz
	public static final int IOCTRL_TYPE_GET_FREQUENCY_INFO_REQ = 86;
	public static final int IOCTRL_TYPE_GET_FREQUENCY_INFO_RESP = 87;

	public static final int IOCTRL_TYPE_SET_FREQUENCY_INFO_REQ = 88;
	public static final int IOCTRL_TYPE_SET_FREQUENCY_INFO_RESP = 89;

	
	//文件查询
	public static final int IOCTRL_TYPE_DC_GET_FILE_LIST_REQ = 200;
	public static final int IOCTRL_TYPE_DC_GET_FILE_LIST_RESP = 201;

	//删除文件
	public static final int IOCTRL_TYPE_DC_DELETE_FILE_REQ = 204;
	public static final int IOCTRL_TYPE_DC_DELETE_FILE_RESP = 205;

	//下载文件
	public static final int IOCTRL_TYPE_DC_DOWNLOAD_FILE_REQ = 206;
	public static final int IOCTRL_TYPE_DC_DOWNLOAD_FILE_RESP = 207;


	//在线回放
	public static final int IOCTRL_TYPE_DC_ONLINE_PLAY_FILE_REQ = 208;
	public static final int IOCTRL_TYPE_DC_ONLINE_PLAY_FILE_RESP = 209;

	//实时拍照
	public static final int IOCTRL_TYPE_DC_SNAP_REQ = 210;
	public static final int IOCTRL_TYPE_DC_SNAP_RESP = 211;

	//设置串口速度
	public static final int IOCTRL_TYPE_DC_SET_UART_REQ = 216;
	public static final int IOCTRL_TYPE_DC_SET_UART_RESP = 217;

	//停止下载文件
	public static final int IOCTRL_TYPE_DC_STOP_DOWNLOAD_FILE_REQ = 224;
	public static final int IOCTRL_TYPE_DC_STOP_DOWNLOAD_FILE_RESP = 225;

	//时间设置接口2
	public static final int IOCTRL_TYPE_GET_DEV_TIME2_REQ = 228;
	public static final int IOCTRL_TYPE_GET_DEV_TIME2_RESP = 229;
	
	public static final int IOCTRL_TYPE_SET_DEV_TIME2_REQ = 230;
	public static final int IOCTRL_TYPE_SET_DEV_TIME2_RESP = 231;

	//获取设备类型
	public static final int IOCTRL_TYPE_GET_DEV_HW_TYPE_REQ = 254;
	public static final int IOCTRL_TYPE_GET_DEV_HW_TYPE_RESP= 255;
	
	public static final int IOCTRL_TYPE_SET_PWM_REQ = 300;//设置PWM
	public static final int IOCTRL_TYPE_SET_PWM_RESP= 301;//设置PWM的回复
	
	
	
	public static class LIVE_START_REQ
	{
//		typedef struct _LIVE_START_REQ_{
//			UINT8 u8EnableVideoSend;  // 1: 发送视频，0 : 不发送视频 
//			UINT8 u8EnableAudioSend;  // 1. 发送音频. 0 : 不发送音频
//			UINT8 u8VideoChan;
//			UINT8 u8Reserved[9];
//		}LIVE_START_REQ;
	
	  public int MY_SIZE=12;
	  
	  public byte u8EnableVideoSend=0;
	  public byte u8EnableAudioSend=0;
	  public byte u8VideoChan=0;
	  
	  public LIVE_START_REQ(int bSendVideo, int bSendAudio, int nVideoChannel){
		  this.u8EnableVideoSend=(byte)bSendVideo;
		  this.u8EnableAudioSend=(byte)bSendAudio;
		  this.u8VideoChan=(byte)nVideoChannel;
	  }
	  
	  public byte[] getBytes(){
		  byte[] byts=new byte[MY_SIZE];
		  Arrays.fill(byts, (byte)0);
		  byts[0]=this.u8EnableVideoSend;
		  byts[1]=this.u8EnableAudioSend;
		  byts[2]=this.u8VideoChan;
		  return byts;
	  }
	}


	public static class GET_AP_PARAMETERS_REQ{
		public int MY_SIZE=8;
		public GET_AP_PARAMETERS_REQ(){}
		public byte[] getBytes(){
			  byte[] byts=new byte[MY_SIZE];
			  Arrays.fill(byts, (byte)0);
			  return byts;
		  }
	}
	/*
	typedef struct _ GET_AP_PARAMETERS_RESP{
		INT32 s32Result; 		// 命令执行结果，0x00000001-成功，其它值为错误码
		CHAR strSsid[64]; 		//WiFi ssid
		CHAR strPassword[64];	//WiFi 密码
		UINT8 u8Enctype; 		//加密模式，1:WEP, 2:WPA-PSK, 3:WPA2-PSK 4: WPA-PSK+ WPA2-PSK, 5:WAPI
		UINT8 u8SignalChannel;	//wifi 信道 [1,14]
		UINT8 u8WifiMode; 		//WiFi 工作模式，0-STA, 1-AP
		UINT8 reserved[17]; 	//保留17个字节，全为0x00
		} GET_AP_PARAMETERS_RESP;
	*/
	public static class GET_AP_PARAMETERS_RESP{
		public int MY_SIZE=152;
		
		private int s32Result=0;
		private String strSsid="";
		private String strPassword="";
		private int u8Enctype=4;
		private int u8SignalChannel=0;
		private int u8WifiMode=1;
		
		public GET_AP_PARAMETERS_RESP(byte[] data){ setData(data); }
		public void setData(byte[] data){
			if(data.length<MY_SIZE) return;
			int nPos=0;
			s32Result =(int)(Convert.byteArrayToInt_Little(data, nPos)&0xFFFF);
			nPos+=4;
			strSsid= Convert.bytesToString(data, nPos);
			nPos+=64;
			strPassword= Convert.bytesToString(data, nPos);
			nPos+=64;
			u8Enctype=(int)(data[nPos]&0xFF);
			nPos+=1;
			u8SignalChannel=(int)(data[nPos]&0xFF);
			nPos+=1;
			u8WifiMode=(int)(data[nPos]&0xFF);
			nPos+=1;
		}
		public int getResult()			{ return s32Result; 		}
		public String getSsid() 		{ return strSsid; 			}
		public String getPassword() 	{ return strPassword; 		}
		public int getEncType()			{ return u8Enctype; 		}
		public int getSignalChannel()	{ return u8SignalChannel; 	}
		public int getWifiMode()		{ return u8WifiMode; 		}
	}
	
	/*
	typedef struct _ SET_AP_PARAMETERS_REQ{
		CHAR strSsid[64]; 		//WiFi ssid
		CHAR strPassword[64];	//WiFi 密码
		UINT8 u8Enctype; 		//加密模式，1:WEP, 2:WPA-PSK, 3:WPA2-PSK 4: WPA-PSK+ WPA2-PSK, 5:WAPI
		UINT8 u8SignalChannel;	//wifi 信道 [1,14]
		UINT8 u8WifiMode; 		//WiFi 工作模式，0-STA, 1-AP
		UINT8 reserved[17]; 	//保留17个字节，全为0x00
	} SET_AP_PARAMETERS_REQ;
	*/
	public static class SET_AP_PARAMETERS_REQ{
		public int MY_SIZE=148;
		
		private String strSsid="";
		private String strPassword="";
		private int u8Enctype=4;
		private int u8SignalChannel=0;
		private int u8WifiMode=0;
		public SET_AP_PARAMETERS_REQ(String strSsid, String strPasswd, int nEncType, int nSignChannel, int nWifiMode){
			this.strSsid=strSsid;
			this.strPassword=strPasswd;
			this.u8Enctype=nEncType;
			this.u8SignalChannel=nSignChannel;
			this.u8WifiMode=nWifiMode;
		}
		public byte[] getBytes(){
			  byte[] byts=new byte[MY_SIZE];
			  byte[] bytTmp=null;
			  int nPos=0;
			  Arrays.fill(byts, (byte)0);
			  
			  bytTmp=strSsid.getBytes();
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=64;
			  
			  bytTmp=strPassword.getBytes();
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=64;
			  
			  byts[nPos]=(byte)u8Enctype;
			  nPos+=1;
			  
			  byts[nPos]=(byte)u8SignalChannel;
			  nPos+=1;
			  
			  byts[nPos]=(byte)u8WifiMode;
			  nPos+=1;		  
			  
			  return byts;
		  }
	}
	/*
	typedef struct _ SET_AP_PARAMETERS_RESP{
	INT32 s32Result; // 命令执行结果，0x00000001-成功，其它值为错误码
	UINT8 reserved[8]; //保留8 个字节，全为0x00
	} SET_AP_PARAMETERS_RESP;
	*/
	public static class SET_AP_PARAMETERS_RESP{
		public int MY_SIZE=12;
		
		private int s32Result=1000;
		public SET_AP_PARAMETERS_RESP(byte[] data){ setData(data); }
		public void setData(byte[] data){
			if(data.length<MY_SIZE) return;
			int nPos=0;
			s32Result =(int)(Convert.byteArrayToInt_Little(data, nPos)&0xFFFF);
			nPos+=4;
		}
		
		public int getResult(){ return s32Result; }
	}
	
	
//	typedef struct _SET_PWM_REQ
//	{
//	UINT32 u32Channel; //PWM 通道，支持2 路PWM，取值为0 或1
//	UINT32 u32Enable; //1-使能PWM，0-禁用PWM
//	UINT32 u32Period; //PWM 周期，单位us，取值范围[1, 20000000]
//	UINT32 u32Duty; //PWM 占空比，取值范围[0, 100]
//	UINT8 reserved[8]; //保留8 个字节，全为0x00
//	} SET_PWM_REQ;
	public static class SET_PWM_REQ{
		public int MY_SIZE=24;
		
		private int m_u32Channel=0;
		private int m_u32Enable=1;
		private int u32Period=1;
		private int u32Duty=0;
		
		public SET_PWM_REQ(int u32Channel, int u32Enable, int u32Period, int u32Duty){
			this.m_u32Channel=u32Channel;
			this.m_u32Enable=u32Enable;
			this.u32Period=u32Period;
			this.u32Duty=u32Duty;
		}
		public byte[] getBytes(){
			  byte[] byts=new byte[MY_SIZE];
			  byte[] bytTmp=null;
			  int nPos=0;
			  Arrays.fill(byts, (byte)0);
			  
			  bytTmp= Convert.intToByteArray_Little(m_u32Channel);
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=4;
			  
			  bytTmp= Convert.intToByteArray_Little(m_u32Enable);
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=4;  
			  
			  bytTmp= Convert.intToByteArray_Little(u32Period);
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=4;  
			  
			  bytTmp= Convert.intToByteArray_Little(u32Duty);
			  System.arraycopy(bytTmp, 0, byts, nPos, bytTmp.length);
			  nPos+=4;  
			  return byts;
		  }
	}
	
	
}
