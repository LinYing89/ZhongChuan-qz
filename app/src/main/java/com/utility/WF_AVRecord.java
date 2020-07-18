package com.utility;

public class WF_AVRecord {
	static {
		try {
			 System.loadLibrary("ffmpeg");
			 System.loadLibrary("WFAVRecord"); }
		catch (UnsatisfiedLinkError ule) { System.out.println("[loading libWFAVRecord.so]," + ule.getMessage()); }
	}
	
	//error code-----------------------------------------------------------
	public static final int ENO_AVRECORD_BASE  =-600;

	public static final int ENO_REC_WRITE_FRAME_FAIL		=ENO_AVRECORD_BASE-11;
	public static final int ENO_REC_FILE_CLOSED				=ENO_AVRECORD_BASE-10;
	public static final int ENO_REC_OPEN_FAIL				=ENO_AVRECORD_BASE-9;
	public static final int ENO_REC_ALLOC_CONTEXT_FAIL		=ENO_AVRECORD_BASE-8;
	public static final int ENO_REC_OPENED					=ENO_AVRECORD_BASE-7;
	public static final int ENO_REC_INVALID_PROPERTY_VALUE	=ENO_AVRECORD_BASE-6;
	public static final int ENO_REC_INVALID_PROPERTY_KEY	=ENO_AVRECORD_BASE-5;
	public static final int ENO_REC_NOT_IMPLEMENT	=ENO_AVRECORD_BASE-4;
	public static final int ENO_REC_INVALID_PARAM	=ENO_AVRECORD_BASE-3;
	public static final int ENO_REC_UNKNOWN			=ENO_AVRECORD_BASE-2;
	public static final int ENO_REC_NOT_INIT		=ENO_AVRECORD_BASE-1;
	public static final int ENO_REC_OK				=0;
	
	//WF_AVRecord API--------------------------------------------------------
	public native static int	WFREC_GetVer();
	public native static int	WFREC_Init();
	public native static void	WFREC_Uninit();
	public native static int	WFREC_Create(int[] ppHandle);
	public native static void	WFREC_Destroy(int[] ppHandle);

	//Parameter:
	//	pKey					pValue
	//	--------				--------
	//	"input_v_stream_type"	"h264"		(default)
	//	"input_v_stream_type"	"mjpeg"
	//	"input_v_stream_width"	e.g.: "640" (default)
	//	"input_v_stream_heigh"	e.g.: "480" (default)

	//	"input_a_stream_type"	"g711a"		(default)
	//	"input_a_stream_type"	"aac"
	//	"input_a_stream_type"	""			none audio
	//	"input_a_sample_rate"	"16000"		(default)
	//	"input_a_data_bit"		"16"		(default)
	//	"input_a_channel_num"	"1"			(default)

	//	"output_file_format"	"mp4"		(default)
	//
	public native static int  WFREC_SetProperty(int pHandle, String pKey, String pValue);
	public native static int  WFREC_Open(int pHandle, String pchFullPathFilename);
	public native static void WFREC_Close(int pHandle);
	//Description:
	//	the first video frame must be I frame.
	//
	public native static int WFREC_PutVideoFrame(int pHandle, byte[] pData, int nDataSize, long nTimestamp, int bKeyFrame);
	public native static int WFREC_PutAudioData(int pHandle, byte[] pData, int nDataSize, long nTimestamp);
	
}
