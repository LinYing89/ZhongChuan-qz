package com.wifi;

public class WF_RAW_HEAD {
	public static final int LEN_HEAD	=24;
	
	int nAVCodecID = WF_NetAPI.WF_AV_CODEC_ID_NONE;
	int nRawDataLen=0;
	long nTimestamp=0L;
	byte   nAVParam=0; //video: 0,I Frame; 1,P Frame; 2,B Frame; audio: (samplerate << 1) | (channel)
	int  nFrameNo=0;
	
	public WF_RAW_HEAD() {}
	public void setData(byte[] byts)
	{
		if(byts==null || byts.length<LEN_HEAD) reset();
		else {
			nAVCodecID  =byts[0]&0xFF | (byts[1]&0xFF)<<8 | (byts[2]&0xFF)<<16 | (byts[3]&0xFF)<<24;
			nRawDataLen =(0xff & byts[4]) | (0xff & byts[5])<<8 | (0xff & byts[6])<<16 | (0xff & byts[7])<<24;
			nTimestamp  =(0xff & byts[8]) | (0xff & byts[9])<<8 | (0xff & byts[10])<<16 | (0xff & byts[11])<<24;
			nAVParam=byts[12];
			nFrameNo=(0xff & byts[16]) | (0xff & byts[17])<<8 | (0xff & byts[18])<<16 | (0xff & byts[19])<<24;
		}
	}
	
	private void reset(){
		nAVCodecID = WF_NetAPI.WF_AV_CODEC_ID_NONE;
		nRawDataLen=0;
		nTimestamp=0L;
		nAVParam=0;
	}
	
	public int getAVCodecID() { return nAVCodecID; 	  }
	public int getRawDataLen(){ return nRawDataLen;	  }
	public long getTimestamp(){ return nTimestamp;	  }
	public int  getFrameFlag(){ return (int)nAVParam; }
	public int  getFrameNo()  { return nFrameNo;	  }
	
}
