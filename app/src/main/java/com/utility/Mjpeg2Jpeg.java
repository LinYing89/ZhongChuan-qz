package com.utility;

import java.util.LinkedList;

public class Mjpeg2Jpeg {
	static {
		try {System.loadLibrary("mjpeg2jpeg"); }
		catch(UnsatisfiedLinkError ule) { System.out.println("[loading libmjpeg2jpeg.so]" + ule.getMessage()); }
		System.out.println("loading libmjpeg2jpeg.so 2");
	}
	
	//reg/unreg IStream----------------------------------------------------
		private LinkedList<IStreamJpeg> m_listIStreamJpeg =new LinkedList<IStreamJpeg>();
		
		public void regAPIListener(IStreamJpeg istream){
			synchronized(m_listIStreamJpeg){
				if(istream!=null && !m_listIStreamJpeg.contains(istream)) m_listIStreamJpeg.addLast(istream);
			}
		}
		public void unregAPIListener(IStreamJpeg istream){
			synchronized(m_listIStreamJpeg){
				if(istream!=null && !m_listIStreamJpeg.isEmpty()){
					for(int i=0; i<m_listIStreamJpeg.size(); i++){
						if(m_listIStreamJpeg.get(i)==istream) {
							m_listIStreamJpeg.remove(i);
							break;
						}
					}
				}
			}
		}
		
	void callback_jpegStream(byte[] pData, int nDataLen, int nUserData)
	{
		synchronized(m_listIStreamJpeg){
			IStreamJpeg curIStream=null;
			for(int i=0; i<m_listIStreamJpeg.size(); i++){
				curIStream=m_listIStreamJpeg.get(i);
				curIStream.OnCallbackStream(pData, nDataLen, nUserData);
			}
		}
	}
	
	public Mjpeg2Jpeg() {};
	
	public native  int	native_getVer();
	public native  void	native_init();
	public native  void	native_uninit();
	//nFlag=0  不需要mjpeg to jpeg
	//nFlag=1 需要mjpeg to jpeg
	public native  int	native_parseJPEG(int nFlag, byte[] pBuf, int[] pnDataSize, int[] pnLastPos, byte[] pMJpgData, int pUserDataCallback);
	
}
