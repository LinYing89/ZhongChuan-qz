package com.example.wfsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.utility.WF_ACodec;
import com.utility.WF_AVRecord;
import com.utility.WF_VCodec;
import com.wifi.IDataFromDevice;
import com.wifi.IMsg;
import com.wifi.IStream;
import com.wifi.SF_PROTOCOL;
import com.wifi.WF_NetAPI;
import com.wifi.WF_RAW_HEAD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class WF_AVObj implements IStream, IMsg{
	public static final int MAX_FRAMEBUF=3110400;
	
	public static final int PORT_CMD_SERVICE_OF_DEVICE =8192;
	public static final String IP_OF_TEST_DEVICE       =("192.168.12.100"); //ip must be real device ip. here to test.
	public static final int TIMEOUT_TO_CONNECT         =5000; //ms
	
	public static final int WHAT_RECEIVED_SERIAL	=10;
	private static final int WHAT_IP_ARRIVED		=1;
	
	private static WF_NetAPI m_objNetAPI=null;
	private static int m_nObjNum=0;
	
	int[] m_handleNet=new int[1];
    int[] m_handleVideoCodec=new int[1], m_handleAudioCodec=new int[1];
    int[] m_handleAVRecord=new int[1];
    int[] m_handleMsg=new int[1];
    int   m_nSampleRate=0, m_nAudioCodedID=0;
    int   m_nVideoCodecID=0;
    int   m_nUserData=0;
    
    boolean m_bRecording=false, m_bRecordingWaitedFirstFrame=false;
    private FIFO m_fifoVideo=new FIFO(), m_fifoAudio=new FIFO();
    
	private ThreadPlayAudio	 m_threadPlayAudio =null;
	private ThreadPlayVideo  m_threadPlayVideo =null;
	private static boolean m_bInitAudio   =false;
    private AudioTrack m_AudioTrack = null;
    
    private Object m_objSynchronizeRender=new Object();
    private WFRender myRender=null;
    private byte[] m_bytYuvToSnap=new byte[MAX_FRAMEBUF];
    int     m_nYuvOfJpgSize=1;
    int 	m_nVideoWidth=0, m_nVideoHeight=0;
    String  m_strFilenameWithPath=null;
    
    SF_PROTOCOL.GET_AP_PARAMETERS_RESP m_objAPParamGetResp=null;
    SF_PROTOCOL.SET_AP_PARAMETERS_RESP m_objAPParamSetResp=null;

    FileOutputStream m_fosRawH264=null; //20160902
    
    public WF_AVObj(){
    	m_nUserData=m_nObjNum;
    	m_nObjNum++;
    	if(m_objNetAPI==null) m_objNetAPI=new WF_NetAPI();

    	m_handleNet[0]		 =-1;
    	m_handleVideoCodec[0]=-1;
    	m_handleAudioCodec[0]=-1;
    	m_handleAVRecord[0]  =-1;
    	m_handleMsg[0]		 =-1;
    }
    
    //----{{//20160902
    public int openRawH264File(String strFilename)
    {
    	if(strFilename==null) return -1;
    	
    	boolean bErr=false;
		try {
			m_fosRawH264 = new FileOutputStream(strFilename);
			
		}catch(Exception e) {
			bErr = true;
			System.out.println("openRawH264File(.): " + e.getMessage());
		}finally {
			if(bErr) {
				if(m_fosRawH264 != null){
					try { m_fosRawH264.close(); }
					catch(IOException e) { e.printStackTrace(); }
				}
			}else System.out.println("openRawH264File(.): OK");
		}
    	return bErr ? 0 : -2;
    }
    public void delRawH264File(String strFilename)
    {
    	if(strFilename==null) return;
    	
    	File file=new File(strFilename);
		if(file.delete()) file=null;
    }
    //----}}//20160902
    
	public static void API_Init()
	{
		if(m_objNetAPI==null) m_objNetAPI=new WF_NetAPI();
	    m_objNetAPI.WFNET_Init();
	    WF_VCodec.WFVC_Init();
	    WF_ACodec.WFAC_Init();
	    WF_AVRecord.WFREC_Init();
	    
	    int nVer_wfnet=m_objNetAPI.WFNET_GetVer();
	    System.out.println("WF_NetAPI ver"+ Integer.toHexString(nVer_wfnet));
	}
	
	public static void API_Uninit()
	{
		m_objNetAPI.WFNET_Uninit();
		WF_VCodec.WFVC_Uninit();
		WF_ACodec.WFAC_Uninit();
		WF_AVRecord.WFREC_Uninit();		
	}
	
	private LinkedList<IDataFromDevice> m_listIStream =new LinkedList<IDataFromDevice>();
	public void regStreamListener(IDataFromDevice istream){
		synchronized(m_listIStream){
			if(istream!=null && !m_listIStream.contains(istream)) m_listIStream.addLast(istream);
		}
	}
	public void unregStreamListener(IDataFromDevice istream){
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
	private void updateStreamInfo(int nWidth, int nHeigh){
		synchronized (m_listIStream) {
			IDataFromDevice streamListener = null;
			for (int i = 0; i < m_listIStream.size(); i++) {
				streamListener = m_listIStream.get(i);
				streamListener.OnStreamInfo(nWidth, nHeigh);
			}
		}
	}
	private void updateStream(int eAVCodecID, byte[] bytAVData, int nDataSize){
		synchronized (m_listIStream) {
			IDataFromDevice streamListener = null;
			for(int i = 0; i < m_listIStream.size(); i++) {
				streamListener = m_listIStream.get(i);
				streamListener.OnStream(eAVCodecID, bytAVData, nDataSize);
			}
		}
	}
	private void updateMsg(int nMsgType, byte[] pData, int nDataSize){
		synchronized (m_listIStream) {
			IDataFromDevice streamListener = null;
			for (int i = 0; i < m_listIStream.size(); i++) {
				streamListener = m_listIStream.get(i);
				streamListener.OnMsg(this, nMsgType, pData, nDataSize);
			}
		}
	}
	
	public int API_Create(WFRender myRender)
	{
		this.myRender=myRender;
		int nRet=m_objNetAPI.WFNET_Create(m_handleNet, m_nUserData);
		System.out.println("API_Create="+nRet);
		if(nRet>=0) {
			m_objNetAPI.WFNET_MsgSetCallback2(m_handleNet[0], 1); //chgchg2
			m_objNetAPI.regAPIListener(this);
			m_objNetAPI.regAPIMsgListener(this);
		}
		return nRet;
	}
	
	public void API_Destroy()
	{
		m_objNetAPI.unregAPIListener(this);
		m_objNetAPI.unregAPIMsgListener(this);
		m_objNetAPI.WFNET_Destroy(m_handleNet);
		if(m_handleAVRecord[0]>-1) WF_AVRecord.WFREC_Destroy(m_handleAVRecord);	
	}

	public int API_Search()
	{
		return m_objNetAPI.WFNET_Search(m_handleNet[0], 0);
	}
	
	public void API_SetRender(WFRender myRender)
	{
		synchronized(m_objSynchronizeRender){
			this.myRender=myRender;
		}
	}
	public boolean API_IsVideoPlaying(){
		if(m_threadPlayVideo==null) return false;
		else return m_threadPlayVideo.isVideoPlaying();
	}
	
	public int API_SetProperty(String pKey, String pValue){
		return m_objNetAPI.WFNET_SetProperty(m_handleNet[0], pKey, pValue);
	}
	public int API_Connect(String strUrl)
	{
		int nRet=m_objNetAPI.WFNET_Open(m_handleNet[0], strUrl);
		System.out.println("[WF_AVObj.API_Connect] m_handleNet[0]="+m_handleNet[0]+","+strUrl);
	    if(nRet>=0){
	    	if(strUrl.startsWith("sf://")){ //chgchg2
	    		SF_PROTOCOL.LIVE_START_REQ req=new SF_PROTOCOL.LIVE_START_REQ(1,0,0);
	    		byte[] bytsReq=req.getBytes();
	    		int nRetSend=m_objNetAPI.WFNET_MsgSend2(m_handleNet[0], SF_PROTOCOL.IOCTRL_TYPE_LIVE_START_REQ, bytsReq, bytsReq.length);
	    		System.out.println("WFNET_MsgSend2(START_VIDEO)="+nRetSend);
	    	}
	    	m_nVideoCodecID=WF_NetAPI.WF_AV_CODEC_ID_NONE;
	    	//start thread video
	    	if(m_threadPlayVideo==null){
				m_threadPlayVideo=new ThreadPlayVideo();
				m_threadPlayVideo.start();
			}
	    	
	    	//start thread audio
	    	if(m_threadPlayAudio==null){
				m_threadPlayAudio=new ThreadPlayAudio();
				m_threadPlayAudio.start();
			}
	    }
	    return nRet;
	}
	
	public void API_Disconnect()
	{
	    if(m_handleNet[0]>-1) {
    		m_objNetAPI.WFNET_MsgSend2(m_handleNet[0], SF_PROTOCOL.IOCTRL_TYPE_LIVE_STOP_REQ, null, 0);
    		
	    	System.out.println("API_Disconnect. 0");
	    	m_objNetAPI.WFNET_Close(m_handleNet[0]);
	    	
	    	System.out.println("API_Disconnect. 1");
	    	//stop thread video
	    	if(m_threadPlayVideo!=null) {
				m_threadPlayVideo.stopPlay();
				m_threadPlayVideo=null;
			}
	    	
	    	//stop thread audio
	    	if(m_threadPlayAudio!=null) {
				m_threadPlayAudio.stopPlay();
				m_threadPlayAudio=null;
			}
	    }
	    
	    if(m_fosRawH264!=null){
	    	try {
				m_fosRawH264.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	m_fosRawH264=null;
	    }
	    System.out.println("API_Disconnect. end");
	}
	
	public int API_RecordStart(String strFullPathFilename)
	{
		if(m_bRecording) return 0;
		int nRet=-1;
		if(m_handleAVRecord[0]<0) nRet=WF_AVRecord.WFREC_Create(m_handleAVRecord);
		if(m_handleAVRecord[0]>-1){
			//set audio property
				//WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_stream_type", ""); //录像不带音频
				if(m_nAudioCodedID==WF_NetAPI.WF_AV_CODEC_ID_AAC){
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_stream_type", "aac");
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_channel_num", "2");
					System.out.println("WFREC_Open,0, aac 2");
				}else{
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_stream_type", "g711a");
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_channel_num", "1");
					System.out.println("WFREC_Open,0, g711a 1");
				}
				if(m_nSampleRate!=0) {
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_sample_rate", m_nSampleRate+"");//441000
					System.out.println("WFREC_Open,0, m_nSampleRate="+m_nSampleRate);
				}
			//set video property
				if(m_nVideoCodecID==WF_NetAPI.WF_AV_CODEC_ID_MJPEG){
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_v_stream_type", "mjpeg");
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_v_stream_width", m_nVideoWidth+"");
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_v_stream_heigh", m_nVideoHeight+"");
					
					WF_AVRecord.WFREC_SetProperty(m_handleAVRecord[0], "input_a_stream_type", "");
				}
			nRet=WF_AVRecord.WFREC_Open(m_handleAVRecord[0], strFullPathFilename);
			//System.out.println("WFREC_Open="+nRet+",m_nVideoCodecID="+m_nVideoCodecID+" m_nVideoWidth="+m_nVideoWidth+
			//					",m_handleAVRecord[0]="+m_handleAVRecord[0]+", strFullPathFilename="+strFullPathFilename);
			if(nRet>=0) {
				m_bRecordingWaitedFirstFrame=true;
				m_bRecording=true;
			}
		}
		return nRet;
	}
	
	public void API_RecordStop()
	{
	    if(m_handleAVRecord[0]>-1) {
	        m_bRecording=false;
	        m_bRecordingWaitedFirstFrame=false;
	        WF_AVRecord.WFREC_Close(m_handleAVRecord[0]);
	        m_handleAVRecord[0]=-1;
	    }		
	}
	
	public int API_GetAPWiFiName()
	{
		int nRet=0;
		if(m_handleNet[0]<0) return -1;
		
		SF_PROTOCOL.GET_AP_PARAMETERS_REQ req=new SF_PROTOCOL.GET_AP_PARAMETERS_REQ();
		byte[] bytsReq=req.getBytes();
		nRet=m_objNetAPI.WFNET_MsgSend2(m_handleNet[0], SF_PROTOCOL.IOCTRL_TYPE_GET_AP_PARAMETERS_REQ, bytsReq, bytsReq.length);
		System.out.println("WFNET_MsgSend2(IOCTRL_TYPE_GET_AP_PARAMETERS_REQ)="+nRet);
		return nRet;
	}
	public String getAPParam_ssid() {
		if(m_objAPParamGetResp==null) return "";
		else{
			return m_objAPParamGetResp.getSsid();
		}
	}
	public String getAPParam_password(){
		if(m_objAPParamGetResp==null) return "";
		else{
			return m_objAPParamGetResp.getPassword();
		}
	}
	public int API_SetAPWiFiName(String sWiFiName, String sWiFiPasswd, int nEncType, int nSignChannel, int nWifiMode)
	{
		int nRet=0;
		if(m_handleNet[0]<0) return -1;
		
		SF_PROTOCOL.SET_AP_PARAMETERS_REQ req=new SF_PROTOCOL.SET_AP_PARAMETERS_REQ(sWiFiName, sWiFiPasswd, nEncType, nSignChannel, nWifiMode);
		byte[] bytsReq=req.getBytes();
		nRet=m_objNetAPI.WFNET_MsgSend2(m_handleNet[0], SF_PROTOCOL.IOCTRL_TYPE_SET_AP_PARAMETERS_REQ, bytsReq, bytsReq.length);
		System.out.println("WFNET_MsgSend2(IOCTRL_TYPE_SET_AP_PARAMETERS_REQ)="+nRet);
		return nRet;
	}
	public int getAPParam_setResult(){
		if(m_objAPParamSetResp==null) return 1000;
		else{
			return m_objAPParamSetResp.getResult();
		}
	}
	
	public int API_SetPWM(int nChannel, int nEnable, int nPeriod, int nDuty)
	{
		int nRet=0;
		if(m_handleNet[0]<0) return -1;
		
		SF_PROTOCOL.SET_PWM_REQ req=new SF_PROTOCOL.SET_PWM_REQ(nChannel, nEnable, nPeriod, nDuty);
		byte[] bytsReq=req.getBytes();
		nRet=m_objNetAPI.WFNET_MsgSend2(m_handleNet[0], SF_PROTOCOL.IOCTRL_TYPE_SET_PWM_REQ, bytsReq, bytsReq.length);
		System.out.println("WFNET_MsgSend2(IOCTRL_TYPE_SET_PWM_REQ)="+nRet);
		return nRet;
	}
	
	public void API_SetSnapshotFilename(String strFilenameWithPath) { m_strFilenameWithPath=strFilenameWithPath; }
	public void API_Snapshot(String strFilenameWithPath)
	{
		if(m_threadPlayVideo==null) return;
		m_nYuvOfJpgSize=0;
		m_strFilenameWithPath=strFilenameWithPath;
		if(m_nVideoCodecID==WF_NetAPI.WF_AV_CODEC_ID_MJPEG){
			
		}else{
			ThreadSnapshot threadSnapshot = new ThreadSnapshot(strFilenameWithPath);
			threadSnapshot.start();
		}
	}
	
	class ThreadSnapshot extends Thread{
		String strFilename=null;		
		public ThreadSnapshot(String sFilename){ this.strFilename=sFilename; }
		
		private void yuv420_nv21(byte[] byt_yuv420, int width, int height)
		{
			int w_h=width*height, i=0;
			byte[] pY=new byte[w_h];
			byte[] pU=new byte[w_h/4];
			byte[] pV=new byte[w_h/4];
			
			System.arraycopy(byt_yuv420, w_h, pU, 0, w_h/4);
			System.arraycopy(byt_yuv420, (w_h+w_h/4), pV, 0, w_h/4);
			for(i=0; i<w_h/4; i++){
				byt_yuv420[w_h+2*i]	 =pV[i];
				byt_yuv420[w_h+2*i+1]=pU[i];
			}
		}
		
		public void run(){
			System.out.println("ThreadSnapshot is going...");
			super.run();
			while(true){
				if(m_nYuvOfJpgSize>1) break;
				else{
					try { Thread.sleep(200);} 
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				if(m_threadPlayVideo==null) return;
			}
				
			yuv420_nv21(m_bytYuvToSnap, m_nVideoWidth, m_nVideoHeight);
			YuvImage image=new YuvImage(m_bytYuvToSnap, ImageFormat.NV21, m_nVideoWidth, m_nVideoHeight, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			boolean bToJpg=image.compressToJpeg(new Rect(0, 0, m_nVideoWidth, m_nVideoHeight), 75, baos);
			System.out.println("API_Snapshot, bToJpg="+bToJpg);
			if(bToJpg){
				boolean bErr=false;
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(strFilename);
					fos.write(baos.toByteArray());
					fos.flush();
					fos.close();
				}catch(Exception e) {
					bErr = true;
					System.out.println("API_Snapshot(.): " + e.getMessage());
				}finally {
					if(bErr) {
						if(fos != null){
							try { fos.close(); }
							catch(IOException e) { e.printStackTrace(); }
						}
					}else System.out.println("API_Snapshot(.): OK");
				}
			}
		}
	}
	
	class ThreadPlayVideo extends Thread{
		boolean bPlaying=false;
		long mTick1	=0L;
		WF_RAW_HEAD stRawHead =new WF_RAW_HEAD();
		boolean bFirstFrame_video=true;
		int[]   out_yuv420Size =new int[1];
		int[]   out_width =new int[1], out_heigh=new int[1];
		byte[]  out_yuv420=new byte[MAX_FRAMEBUF];
		byte[]  bytH264=new byte[512*1024];
		long nTimeFPS=0L, nTimeFPSspan=0L, nFPSNum=0L;
		boolean mbSetProperty=false;
		
		long m_nFirstTickLocal_video=0L, m_nTick2_video=0L, m_nFirstTimestampDevice_video=0L;
		public void run(){
			super.run();
			
			WF_VCodec.WFVC_Create(m_handleVideoCodec);
			
			byte[] videoData=null;
			System.out.println("----ThreadPlayVideo going...");			
			out_yuv420Size[0]=0;
			out_width[0]=0;
			out_heigh[0]=0;
			
			m_fifoVideo.removeAll();
			//bytBuffer.clear();
			bPlaying=true;
			long[] arrRecvTS=new long[1];
			
			while(bPlaying){
				if(m_fifoVideo.isEmpty()){
					try { Thread.sleep(4); } 
					catch (InterruptedException e) { e.printStackTrace(); }
					continue;
				}
				//lose frame
				if(m_fifoVideo.getNum()>=80){
					m_fifoVideo.removeAll();
					bFirstFrame_video=true;
					continue;
				}
				videoData=m_fifoVideo.removeHead(arrRecvTS);
				if(videoData!=null) doPlayVideo(videoData, arrRecvTS[0]);
			};
			WF_VCodec.WFVC_Destroy(m_handleVideoCodec);
			System.out.println("===ThreadPlayVideo exit.");
		}
		
		public boolean isVideoPlaying(){
			return bPlaying;
		}
		
		public void stopPlay(){
			bPlaying=false;
			if(this.isAlive()) {
				try { this.join();}
				catch (InterruptedException e) { e.printStackTrace(); }	
			}
			byte[] pVideo=null;
			long[] arrRecvTS=new long[1];
			while(!m_fifoVideo.isEmpty()) {
				pVideo=m_fifoVideo.removeHead(arrRecvTS);
				pVideo=null;
			}
			m_nVideoWidth=0;
			m_nVideoHeight=0;
		}

		private void doPlayVideo(byte[] pAVData, long nRecvTS)
		{
			int nRet=0;
			long nTimeDecode=0L;
			stRawHead.setData(pAVData);
			m_nVideoCodecID=stRawHead.getAVCodecID();
			//System.out.println("doPlayVideo, m_nVideoCodecID="+m_nVideoCodecID+", mbSetProperty="+mbSetProperty);
			switch(stRawHead.getAVCodecID()){
				case WF_NetAPI.WF_AV_CODEC_ID_MPEG4: //chgchg3
					if(!mbSetProperty){
						mbSetProperty=true;
						System.out.println("doPlayVideo, WFVC_SetProperty(.,mpeg4)");
						WF_VCodec.WFVC_SetProperty(m_handleVideoCodec[0], "input_data_type", "mpeg4");
					}
				case WF_NetAPI.WF_AV_CODEC_ID_H264:{
					//20160902
//					if(m_fosRawH264!=null){ //testtest
//						try {
//							byte[] bytVideo=new byte[stRawHead.getRawDataLen()];
//							System.arraycopy(pAVData, WF_RAW_HEAD.LEN_HEAD, bytVideo, 0, stRawHead.getRawDataLen());
//							m_fosRawH264.write(bytVideo);
//							m_fosRawH264.flush();
//							bytVideo=null;
//						}catch(IOException e){ e.printStackTrace(); }
//					}
					
					if(bFirstFrame_video && stRawHead.getFrameFlag()!=WF_NetAPI.WF_V_FRAME_FLAG_I) break;
					bFirstFrame_video=false;
					
					System.arraycopy(pAVData, WF_RAW_HEAD.LEN_HEAD, bytH264, 0, stRawHead.getRawDataLen());
					out_yuv420Size[0]=MAX_FRAMEBUF;
					//out_bmp565Size[0]=MAX_FRAMEBUF;
					nTimeDecode=System.currentTimeMillis();
					nRet=WF_VCodec.WFVC_Decode(m_handleVideoCodec[0], bytH264, stRawHead.getRawDataLen(), 
									out_yuv420, out_yuv420Size, 
									null,null,
									null,null,//out_bmp565, out_bmp565Size,
									out_width, out_heigh);

//					H264Broadcaster.send(new byte[]{0x00, 0x01}, "192.168.1.101");
					//System.out.println("WFVC_Decode="+nRet+",len="+stRawHead.getRawDataLen()+", wXh="+out_width[0]+"X"+out_heigh[0]);
					//System.out.println("WFVC_Decode, bytH264=0x"+Integer.toString(bytH264[3]&0xFF, 16)+" "+Integer.toString(bytH264[4]&0xFF, 16)+" "+
					//					Integer.toString(bytH264[5]&0xFF, 16)+" "+Integer.toString(bytH264[6]&0xFF, 16)+" "+Integer.toString(bytH264[7]&0xFF, 16));
					
					nTimeDecode=System.currentTimeMillis()-nTimeDecode;
					if(m_bRecordingWaitedFirstFrame){
						if(stRawHead.getFrameFlag()==WF_NetAPI.WF_V_FRAME_FLAG_I) m_bRecordingWaitedFirstFrame=false;
					}
					if(!m_bRecordingWaitedFirstFrame && m_bRecording){
						int bKey=(stRawHead.getFrameFlag()==0) ? 1 : 0;
						int n=WF_AVRecord.WFREC_PutVideoFrame(m_handleAVRecord[0], bytH264, stRawHead.getRawDataLen(), stRawHead.getTimestamp(), bKey);
						//System.out.println("WFREC_PutVideoFrame,n="+n+",stRawHead.getTimestamp()="+stRawHead.getTimestamp());
					}
					if(nRet<0) break;
					
					if(out_width[0]!=m_nVideoWidth){
						m_nVideoWidth =out_width[0];
						m_nVideoHeight=out_heigh[0];
						updateStreamInfo(m_nVideoWidth, m_nVideoHeight);
						System.out.println("w="+m_nVideoWidth+"xh="+m_nVideoHeight);
					}
					if(m_nYuvOfJpgSize==0){
						System.arraycopy(out_yuv420, 0, m_bytYuvToSnap, 0, out_yuv420Size[0]);
						m_nYuvOfJpgSize=out_yuv420Size[0];
					}
					
					long nCurDevTimeStamp=0L, nDiffTimeStamp=0L;
					nCurDevTimeStamp=stRawHead.getTimestamp();
					m_nTick2_video=System.currentTimeMillis();
					if(m_nFirstTimestampDevice_video==0L || m_nFirstTickLocal_video==0L){
						m_nFirstTimestampDevice_video=nCurDevTimeStamp;
						m_nFirstTickLocal_video		 =m_nTick2_video;
					}
					if(m_nTick2_video<m_nFirstTickLocal_video || nCurDevTimeStamp<m_nFirstTimestampDevice_video){
						m_nFirstTimestampDevice_video=nCurDevTimeStamp;
						m_nFirstTickLocal_video		 =m_nTick2_video;
					}
					nDiffTimeStamp=(nCurDevTimeStamp-m_nFirstTimestampDevice_video) - (m_nTick2_video-m_nFirstTickLocal_video);
					//if(m_fifoVideo.getNum()>5)
//					{
//					System.out.println("nTimeDecode="+nTimeDecode+
//							",devDiff="+(nCurDevTimeStamp-m_nFirstTimestampDevice_video)+
//							",localDif="+(m_nTick2_video-m_nFirstTickLocal_video)+
//							", nDiffTimeStamp="+nDiffTimeStamp+", nCurDevTimeStamp="+nCurDevTimeStamp+",num="+m_fifoVideo.getNum());
//					}
					nDiffTimeStamp-=nTimeDecode;
					if(nDiffTimeStamp>2 && nDiffTimeStamp<35) {
						try { Thread.sleep(nDiffTimeStamp); } 
						catch (InterruptedException e) { e.printStackTrace(); }
					}
					
					/*
					nDiffTimeStamp=(nCurDevTimeStamp-m_nFirstTimestampDevice_video)-nTimeDecode;
					m_nFirstTimestampDevice_video=nCurDevTimeStamp;
					System.out.println("FrmNo="+stRawHead.getFrameNo()+" len="+stRawHead.getRawDataLen()+
									   ",  nDec="+nTimeDecode+",devTS="+nCurDevTimeStamp+",nDif="+nDiffTimeStamp+
									   ", num="+m_fifoVideo.getNum());
					if(nDiffTimeStamp>2 && nDiffTimeStamp<50) {
						try { Thread.sleep(nDiffTimeStamp); } 
						catch (InterruptedException e) { e.printStackTrace(); }
					}
					*/
					
//					System.out.println("FrmNo="+stRawHead.getFrameNo()+" len="+stRawHead.getRawDataLen()+
//							   ",  nDec="+nTimeDecode+",devTS="+nCurDevTimeStamp+",nRecvTS="+nRecvTS+
//							   ", num="+m_fifoVideo.getNum());
//					
//					long nSleepTime=40-nTimeDecode;
//					long nDiff=nRecvTS-m_nFirstTimestampDevice_video;
//					m_nFirstTimestampDevice_video=nRecvTS;
//					if(nDiff>10 && nDiff<45 && nSleepTime>0) {
//						try { Thread.sleep(nSleepTime); } 
//						catch (InterruptedException e) { e.printStackTrace(); }
//					}
					
					
					synchronized(m_objSynchronizeRender){
						//System.out.println("out_yuv420Size[0]="+out_yuv420Size[0]+", w="+m_nVideoWidth+"xh="+m_nVideoHeight);
						if(myRender!=null) myRender.writeSample(out_yuv420, m_nVideoWidth, m_nVideoHeight);
					}
					nFPSNum++;
					nTimeFPSspan=System.currentTimeMillis()-nTimeFPS;
					if(nTimeFPSspan>=2000){
						String str=String.format("fps=%.2f fifoVideo.getNum=%d", 
												(float)(nFPSNum*1000.0f/nTimeFPSspan), m_fifoVideo.getNum());
						System.out.println(str);
						nTimeFPS=System.currentTimeMillis();
						nFPSNum=0;
					}
					}break;
					
				case WF_NetAPI.WF_AV_CODEC_ID_MJPEG:{
					m_nFirstTickLocal_video=1;

					System.arraycopy(pAVData, WF_RAW_HEAD.LEN_HEAD, pAVData, 0, stRawHead.getRawDataLen());
					updateStream(WF_NetAPI.WF_AV_CODEC_ID_MJPEG, pAVData, stRawHead.getRawDataLen());
					if(m_nVideoWidth==0 || m_nVideoHeight==0){
						Bitmap bmp = BitmapFactory.decodeByteArray(pAVData, 0, stRawHead.getRawDataLen());
						m_nVideoWidth =bmp.getWidth();
						m_nVideoHeight=bmp.getHeight();
						bmp=null;
						System.out.println("MJpeg, m_nVideoWidth="+m_nVideoWidth+", m_nVideoHeight="+m_nVideoHeight);
					}
					//record
					if(m_bRecording && m_handleAVRecord[0]>-1){
						int n=WF_AVRecord.WFREC_PutVideoFrame(m_handleAVRecord[0], pAVData, stRawHead.getRawDataLen(), stRawHead.getTimestamp(), 1);
						//System.out.println("WFREC_PutVideoFrame,mjpeg, n="+n+",stRawHead.getTimestamp()="+stRawHead.getTimestamp());
					}
					
					//snapshot
					if(m_nYuvOfJpgSize==0){
						boolean bErr=false;
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(m_strFilenameWithPath);							
							fos.write(pAVData, 0, stRawHead.getRawDataLen());
							fos.flush();
							fos.close();
							System.out.println("doPlayVideo ok, size="+stRawHead.getRawDataLen()+","+m_strFilenameWithPath);
						}catch(Exception e) {
							bErr = true;
							System.out.println("doPlayVideo, " + e.getMessage());
						}finally {
							if(bErr) {
								if(fos != null){
									try { fos.close(); }
									catch(IOException e) { e.printStackTrace(); }
								}
							}
						}
						m_nYuvOfJpgSize=stRawHead.getRawDataLen();
					}
					}break;
				default:;
			}
		}
	}
	
	class ThreadPlayAudio extends Thread
	{
		public static final int MAX_AUDIOBUF =8192;//chgchg <--3200;

		byte[]  pRaw=new byte[MAX_AUDIOBUF];
		int nRet=0;
		int[] nSizePCM=new int[1];
		WF_RAW_HEAD stAVDataHead=new WF_RAW_HEAD();
		boolean bPlaying=false;
		long m_nFirstTickLocal_audio=0L, m_nTick2_audio=0L, m_nFirstTimestampDevice_audio=0L;
		int  nNoPlayCount=0;
		long nAudioTimeStampDev=0L, nAudioTimeStamp1=0L, nAudioTimeStamp2=0L; 
		long[] arrRecvTS=new long[1];
		
		@Override
		public void run()
		{
			byte[] audioData=null;
			boolean bFirst=true;			
			int  nAudioDataSize=0;
			m_fifoAudio.removeAll();
			long nTimestamp=0L;
			
			System.out.println("----ThreadPlayAudio going...");
			bPlaying=true;
			WF_ACodec.WFAC_Create(m_handleAudioCodec);
			
			while(bPlaying){
				if(m_fifoAudio.isEmpty()){
					try { Thread.sleep(6); } 
					catch (InterruptedException e) { e.printStackTrace(); }
					continue;
				}
				audioData=m_fifoAudio.removeHead(arrRecvTS);
				if(audioData!=null) {
					stAVDataHead.setData(audioData);
					if(bFirst){
						bFirst=false;
						boolean bRet=false;
						int nValue=0, nSampleRate=16000, nChannel=0;
						int nFrameFlag=stAVDataHead.getFrameFlag();
						nValue=nFrameFlag&0x01;
						if(nValue==0) nChannel=0;
						else if(nValue==1) nChannel=1;
						
						nValue=nFrameFlag>>1;
						if(nValue==0) nSampleRate=8000;
						else if(nValue==1) nSampleRate=16000;
						else if(nValue==2) nSampleRate=44100;
						m_nSampleRate=nSampleRate;

						m_nAudioCodedID=stAVDataHead.getAVCodecID();
						if(m_nAudioCodedID==WF_NetAPI.WF_AV_CODEC_ID_G711A){
							WF_ACodec.WFAC_SetProperty(m_handleAudioCodec[0], "audio_type", "g711a");
							bRet=initAudioDev(nSampleRate, 0, 1);//<--initAudioDev(16000, 0, 1);
							if(bRet) m_AudioTrack.play();
							
						}else if(m_nAudioCodedID==WF_NetAPI.WF_AV_CODEC_ID_AAC){
							WF_ACodec.WFAC_SetProperty(m_handleAudioCodec[0], "audio_type", "aac");
							bRet=initAudioDev(nSampleRate, 1, 1);//<--initAudioDev(44100, 1, 1);
							if(bRet) m_AudioTrack.play();
						}
					}
					nAudioDataSize=audioData.length-WF_RAW_HEAD.LEN_HEAD;
					System.arraycopy(audioData, WF_RAW_HEAD.LEN_HEAD, audioData, 0, nAudioDataSize);
					nTimestamp=stAVDataHead.getTimestamp();
					doPlayAudio(stAVDataHead, nTimestamp, audioData, nAudioDataSize);
				}
			}//while-end
			WF_ACodec.WFAC_Destroy(m_handleAudioCodec);
			deinitAudioDev();
			System.out.println("===ThreadPlayAudio exit.");
		}
		
		public synchronized boolean initAudioDev(int sampleRateInHz, int channel, int dataBit) {
			if(!m_bInitAudio) {
				int channelConfig= 2;
				int audioFormat  = 2;
				int mMinBufSize  = 0;

				channelConfig =(channel == WF_NetAPI.ACHANNEL_STERO) ? AudioFormat.CHANNEL_CONFIGURATION_STEREO:AudioFormat.CHANNEL_CONFIGURATION_MONO;
				audioFormat = (dataBit == WF_NetAPI.ADATABITS_16) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
				mMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
			    if(mMinBufSize ==AudioTrack.ERROR_BAD_VALUE || mMinBufSize ==AudioTrack.ERROR)  return false;	    
				try {
					m_AudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, mMinBufSize,AudioTrack.MODE_STREAM);				
				} catch(IllegalArgumentException iae) {				
					iae.printStackTrace();
					return false;
				}
				m_AudioTrack.play();
				m_bInitAudio = true;					
				return true;
				
			}else return false;
	    }
	    	
	    public synchronized void deinitAudioDev() {
	    	if(m_bInitAudio){
	    		if (m_AudioTrack != null) {
	    			m_AudioTrack.stop();
	    			m_AudioTrack.release();
	    			m_AudioTrack=null;
	    		}
				m_bInitAudio = false;
			}
	    }
	    
		public void stopPlay(){
			bPlaying=false;
			if(this.isAlive()) {
				try { this.join();}
				catch (InterruptedException e) { e.printStackTrace(); }	
			}
			
			byte[] pAudio=null;
			long[] arrRecvTS=new long[1];
			while(!m_fifoAudio.isEmpty()) {
				pAudio=m_fifoAudio.removeHead(arrRecvTS);
				pAudio=null;
			}
		}
		
		private void doPlayAudio(WF_RAW_HEAD stAVDataHead, long nDevTimestamp, byte[] bytAudioData, int nAudioDataSize)
		{
			switch(stAVDataHead.getAVCodecID())
			{
				case WF_NetAPI.WF_AV_CODEC_ID_G711A:
					{
						if(m_bRecording){
							int n=WF_AVRecord.WFREC_PutAudioData(m_handleAVRecord[0], bytAudioData, stAVDataHead.getRawDataLen(), stAVDataHead.getTimestamp());
							//System.out.println("WFREC_PutAudioData,n="+n+",stAVDataHead.getTimestamp()="+stAVDataHead.getTimestamp());
						}
						nSizePCM[0]=MAX_AUDIOBUF;
		                nRet=WF_ACodec.WFAC_Decode(m_handleAudioCodec[0], bytAudioData, stAVDataHead.getRawDataLen(), pRaw, nSizePCM);
		                if(nRet>=0) m_AudioTrack.write(pRaw, 0, nSizePCM[0]);
					}
					break;
					
				case WF_NetAPI.WF_AV_CODEC_ID_AAC: //chgchg
					{
						if(m_bRecording){
							/*
							if(nAudioTimeStamp1==0L && stAVDataHead.getTimestamp()!=0){
								nAudioTimeStampDev=stAVDataHead.getTimestamp();
								nAudioTimeStamp1=System.currentTimeMillis();
							}
							nAudioTimeStamp2=System.currentTimeMillis()-nAudioTimeStamp1+nAudioTimeStampDev;
							*/
							nAudioTimeStamp2=stAVDataHead.getTimestamp();
							int n=WF_AVRecord.WFREC_PutAudioData(m_handleAVRecord[0], bytAudioData, stAVDataHead.getRawDataLen(), nAudioTimeStamp2);
							//System.out.println("WFREC_PutAudioData,n="+n+",getRawDataLen="+stAVDataHead.getRawDataLen()+",stAVDataHead.getTimestamp()="+stAVDataHead.getTimestamp());
						}
						nSizePCM[0]=MAX_AUDIOBUF;
		                nRet=WF_ACodec.WFAC_Decode(m_handleAudioCodec[0], bytAudioData, stAVDataHead.getRawDataLen(), pRaw, nSizePCM);
		                //System.out.println("WFAC_Decode,AAC nRet="+nRet+",dataSize="+stAVDataHead.getRawDataLen()+",getNum()="+m_fifoAudio.getNum());
		                if(nRet>=0) m_AudioTrack.write(pRaw, 0, nSizePCM[0]);
					}
					break;
				default:;
			}
		}
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case WHAT_IP_ARRIVED:{
					byte[] pData=(byte[])msg.obj;
					updateStream(WF_NetAPI.WF_AV_IP_MSG, pData, pData.length);
					}break;
				default:;
			}
		}
	};
	
	@Override
	public int OnCallbackStream(byte[] pData, int nDataSize, int nUserData) {
		if(nUserData!=m_nUserData) return -1;
		WF_RAW_HEAD head=new WF_RAW_HEAD();
		head.setData(pData);
		
		if(head.getAVCodecID()<WF_NetAPI.WF_AV_CODEC_ID_FIRST_AUDIO){
			if(head.getAVCodecID()==WF_NetAPI.WF_AV_IP_MSG){
				Message msg=handler.obtainMessage();
				msg.what=WHAT_IP_ARRIVED;
				msg.obj =pData;
				handler.sendMessage(msg);
			}else {
				//System.out.println("OnCallbackStream, nDataSize="+nDataSize);
				m_fifoVideo.addLast(pData, nDataSize);
				for(int i = 0; i < m_listIStream.size(); i++) {
					m_listIStream.get(i).OnH264(pData, nDataSize);
				}
//				H264Broadcaster.send(pData, "192.168.1.101");
			}
		}else if(head.getAVCodecID()==WF_NetAPI.WF_AV_CODEC_ID_G711A) m_fifoAudio.addLast(pData, nDataSize);
		else if(head.getAVCodecID()==WF_NetAPI.WF_AV_CODEC_ID_AAC) m_fifoAudio.addLast(pData, nDataSize);
		
		return 0;
	}
	
	@Override
	public int OnCallbackMsg(int nCmdType, byte[] pData, int nDataSize, int nUserData) {
		if(nUserData!=m_nUserData) return -1;
		switch(nCmdType){
			case SF_PROTOCOL.IOCTRL_TYPE_LIVE_START_RESP:{				
				}break;
				
			case SF_PROTOCOL.IOCTRL_TYPE_GET_AP_PARAMETERS_RESP:{
				if(null==m_objAPParamGetResp) m_objAPParamGetResp=new SF_PROTOCOL.GET_AP_PARAMETERS_RESP(pData);
				else m_objAPParamGetResp.setData(pData);
				System.out.println("IOCTRL_TYPE_GET_AP_PARAMETERS_RESP, ssid="+m_objAPParamGetResp.getSsid());
				}break;
				
			case SF_PROTOCOL.IOCTRL_TYPE_SET_AP_PARAMETERS_RESP:{
				if(null==m_objAPParamSetResp) m_objAPParamSetResp=new SF_PROTOCOL.SET_AP_PARAMETERS_RESP(pData);
				else m_objAPParamSetResp.setData(pData);
				System.out.println("IOCTRL_TYPE_SET_AP_PARAMETERS_RESP, result="+m_objAPParamSetResp.getResult());
				}break;
			case SF_PROTOCOL.IOCTRL_TYPE_SET_PWM_RESP:{
				System.out.println("IOCTRL_TYPE_SET_PWM_RESP, nDataSize="+nDataSize);
				}break;
				
			default:;				
		}		
		updateMsg(nCmdType, pData, nDataSize);
		
		return 0;
	}

}
