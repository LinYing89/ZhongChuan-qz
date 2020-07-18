package com.utility;

public class WF_ACodec {
	static {
		try {System.loadLibrary("ffmpeg"); 
			 System.loadLibrary("WFACodec");} 
		catch (UnsatisfiedLinkError ule) { System.out.println("[loading libWFACodec.so]," + ule.getMessage()); }
	}
	
	//error code-----------------------------------------------------------
	public static final int ENO_AC_BASE  =-400;
	
	public static final int ENO_AC_INVALID_PROPERTY_VALUE	=ENO_AC_BASE-7;
	public static final int ENO_AC_INVALID_PROPERTY_KEY		=ENO_AC_BASE-6;
	public static final int ENO_AC_ENCODE_DECODE_STARTED	=ENO_AC_BASE-5;
	public static final int ENO_AC_NOT_IMPLEMENT	=ENO_AC_BASE-4;
	public static final int ENO_AC_INVALID_PARAM	=ENO_AC_BASE-3;
	public static final int ENO_AC_UNKNOWN			=ENO_AC_BASE-2;
	public static final int ENO_AC_NOT_INIT			=ENO_AC_BASE-1;
	public static final int ENO_AC_OK				=0;
	
	//WF_ACodec API--------------------------------------------------------
	public native static int	WFAC_GetVer();
	public native static int	WFAC_Init();
	public native static void	WFAC_Uninit();
	public native static int	WFAC_Create(int[] ppHandle);
	public native static void	WFAC_Destroy(int[] ppHandle);
	
	//Parameter:
	//	pKey				pValue
	//	--------			--------
	//	"audio_type"		"g711a" (default)
	//	"audio_type"		"g711u"
	//
	public native static int WFAC_SetProperty(int pHandle, String pKey, String pValue);
	public native static int WFAC_Encode(int pHandle, byte[] inBuf, int inLen, byte[] outBuf, int[] outLen);
	public native static int WFAC_Decode(int pHandle, byte[] inBuf, int inLen, byte[] outBuf, int[] outLen);
}
