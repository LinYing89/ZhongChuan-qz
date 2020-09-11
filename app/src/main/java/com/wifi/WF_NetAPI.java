package com.wifi;

import com.bairock.zhongchuan.qz.netty.H264Broadcaster;

import java.util.LinkedList;

public class WF_NetAPI {
	static {
		try { 
			System.loadLibrary("ffmpeg");
			System.loadLibrary("WFNetAPI"); }
		catch(UnsatisfiedLinkError ule) { System.out.println("[loading libWFNetAPI.so]," + ule.getMessage()); }
	}
	//error code-----------------------------------------------------------
	
	public static final int ENO_NET_INVALID_PROPERTY_VALUE=-7;
	public static final int ENO_NET_OPENED		 =-6;
	public static final int ENO_NET_WRONG_USER_OR_PASSWD=-5;
	public static final int ENO_NET_FAIL_CONNECT =-4;
	public static final int ENO_NET_INVALID_PARAM=-3;
	public static final int ENO_NET_UNKNOWN		 =-2;
	public static final int ENO_NET_NOT_INIT	 =-1;
	public static final int ENO_NET_OK			 =0;
	
	//WF_ENUM_AVCodecID----------------------------------------------------
	public static final int WF_AV_CODEC_ID_NONE	 =0;
	public static final int WF_AV_IP_MSG	 	 =1;
	
    // video codecs 
	public static final int WF_AV_CODEC_ID_MJPEG =8;
	public static final int WF_AV_CODEC_ID_MPEG4 =13; //chgchg3
	public static final int WF_AV_CODEC_ID_H264	 =28;
    // various PCM "codecs"
	public static final int WF_AV_CODEC_ID_FIRST_AUDIO	= 0x10000; ///< A dummy id pointing at the start of audio codecs
	public static final int WF_AV_CODEC_ID_G711U		= 0x10006;
	public static final int WF_AV_CODEC_ID_G711A		= 0x10007;
	public static final int WF_AV_CODEC_ID_AAC			= 0x15002; //chgchg
	
	//WF_ENUM_VIDEO_FRAME--------------------------------------------------
	public static final int WF_V_FRAME_FLAG_I	= 0x00;
	public static final int WF_V_FRAME_FLAG_P	= 0x01;
	public static final int WF_V_FRAME_FLAG_B	= 0x02;

	public static final int ADATABITS_8		=0;
	public static final int ADATABITS_16	=1;
	public static final int ACHANNEL_MONO	=0;
	public static final int ACHANNEL_STERO	=1;
	
	//reg/unreg IStream----------------------------------------------------
	private LinkedList<IStream> m_listIStream =new LinkedList<IStream>();
	private LinkedList<IMsg> m_listIMsg =new LinkedList<IMsg>();
	
	public void regAPIListener(IStream istream){
		synchronized(m_listIStream){
			if(istream!=null && !m_listIStream.contains(istream)) m_listIStream.addLast(istream);
		}
	}
	public void unregAPIListener(IStream istream){
		synchronized(m_listIStream){
			if(istream!=null && !m_listIStream.isEmpty()){
				for(int i=0; i<m_listIStream.size(); i++){
					if(m_listIStream.get(i)==istream) {
						m_listIStream.remove(i);
						break;
					}
				}
			}
		}
	}
	
	public void regAPIMsgListener(IMsg imsg){
		synchronized(m_listIMsg){
			if(imsg!=null && !m_listIMsg.contains(imsg)) m_listIMsg.addLast(imsg);
		}
	}
	public void unregAPIMsgListener(IMsg imsg){
		synchronized(m_listIMsg){
			if(imsg!=null && !m_listIMsg.isEmpty()){
				for(int i=0; i<m_listIMsg.size(); i++){
					if(m_listIMsg.get(i)==imsg) {
						m_listIMsg.remove(i);
						break;
					}
				}
			}
		}
	}
	
	
	void WF_CallbackStream(byte[] pData, int nDataLen, int nUserData)
	{
		synchronized(m_listIStream){
			IStream curIStream=null;
			for(int i=0; i<m_listIStream.size(); i++){
				curIStream=m_listIStream.get(i);
				curIStream.OnCallbackStream(pData, nDataLen, nUserData);
			}
		}
	}
	
	void WF_CallbackMsg(int nCmdType, byte[] pData, int nDataLen, int nUserData)
	{
		synchronized(m_listIMsg){
			IMsg curIMsg=null;
			for(int i=0; i<m_listIMsg.size(); i++){
				curIMsg=m_listIMsg.get(i);
				curIMsg.OnCallbackMsg(nCmdType, pData, nDataLen, nUserData);
			}
		}
	}
	
	
	//WF_NetAPI API------------------------------------------------------
	public WF_NetAPI() {}
	public native  int	WFNET_GetVer();
	public native  int	WFNET_Init();
	public native  void	WFNET_Uninit();
	public native  int	WFNET_Create(int[] ppHandle, int pUserData);
	public native  void	WFNET_Destroy(int[] ppHandle);
	public native  int	WFNET_Search(int pHandle, int nFlag);
	
	//Parameter:
		//	pKey					pValue
		//	--------				--------
		//	"rtsp_transport"		"udp" (default)
		//	"rtsp_transport"		"tcp"
		//
	public native  int WFNET_SetProperty(int pHandle, String pKey, String pValue);
	public native  int WFNET_Open(int pHandle, String pchUrl);
	public native  int WFNET_Close(int pHandle);
	public native  int WFNET_MsgSetCallback2(int pHandle, int pUserData);
	public native  int WFNET_MsgSend2(int pHandle, int nCmdType, byte[] pCmd, int nCmdSize);
	
	
	//control command api
	public native  int	WFNET_MsgCreate(int[] ppCtrlHandle, String pAddr, int nPort, int nTimeout_ms);
	public native  int	WFNET_MsgDestroy(int[] ppCtrlHandle);
	public native  int	WFNET_MsgSetCallback(int pCtrlHandle, int pUserData);
	public native  int  WFNET_MsgSend(int pCtrlHandle, int nCmdType, byte[] pCmd, int nCmdSize);
}
