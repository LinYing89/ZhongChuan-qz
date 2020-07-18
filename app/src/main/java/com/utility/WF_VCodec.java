package com.utility;

public class WF_VCodec {
	static {
		try { 
			System.loadLibrary("ffmpeg");
			System.loadLibrary("WFVCodec");} 
		catch (UnsatisfiedLinkError ule) { System.out.println("[loading libWFVCodec.so]," + ule.getMessage()); }
	}
	
	//error code-----------------------------------------------------------
	public static final int ENO_VC_BASE  =-200;
	
	public static final int ENO_VC_ENCODE_FAIL				=ENO_VC_BASE-10;
	public static final int ENO_VC_DECODE_FAIL				=ENO_VC_BASE-9;
	public static final int ENO_VC_ENCODE_DECODE_STARTED	=ENO_VC_BASE-8;
	public static final int ENO_VC_UNKNOWN_IN_DATA_TYPE		=ENO_VC_BASE-7;
	public static final int ENO_VC_INVALID_PROPERTY_VALUE	=ENO_VC_BASE-6;
	public static final int ENO_VC_INVALID_PROPERTY_KEY		=ENO_VC_BASE-5;
	public static final int ENO_VC_NOT_IMPLEMENT	=ENO_VC_BASE-4;
	public static final int ENO_VC_INVALID_PARAM	=ENO_VC_BASE-3;
	public static final int ENO_VC_UNKNOWN			=ENO_VC_BASE-2;
	public static final int ENO_VC_NOT_INIT			=ENO_VC_BASE-1;
	public static final int ENO_VC_OK				=0;
	
	
	//WF_VCodec API--------------------------------------------------------
	public native static int	WFVC_GetVer();
	public native static int	WFVC_Init();
	public native static void	WFVC_Uninit();
	public native static int	WFVC_Create(int[] ppHandle);
	public native static void	WFVC_Destroy(int[] ppHandle);
	//Parameter:
	//	pKey					pValue
	//	--------				--------
	//	"input_data_type"		"h264"	(default)
	//	"input_data_type"		"mpeg4"
	//	
	public native static int WFVC_SetProperty(int pHandle, String pKey, String pValue);
	public native static int WFVC_Encode(int pHandle, byte[] inData, int inDataSize, byte[] outBuf, int[] outLen);
	public native static int WFVC_Decode(int pHandle, byte[] inData, int inDataSize, 
			byte[] outYUV420,int[] in_outYUV420Size, 
			byte[] outRGB24, int[] in_outRGB24Size, 
			byte[] outRGB565,int[] in_outRGB565Size, 
			int[] pVWidth, int[] pVHeight);	
}
