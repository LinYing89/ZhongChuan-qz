package com.example.wfsample;

import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;


public class TcpMessageUtils {
    //端口号
    public final static int PORT_DEFAULT = 2001;

    private Socket socket;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private byte[] buffer= new byte[8];
    private String mIPstr="192.168.11.10";
    private int mPort=PORT_DEFAULT;
    

    //Double CheckLock（DCL模式单例）
    private static TcpMessageUtils utils;

    private LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<>();
    private WriterThread mWriterThread;
    private ReaderThread mReaderThread;
    private Handler mHandler;

    private TcpMessageUtils() {

    }

    /**
     * 初始化，创建TCP客户端，只执行一次
     */
    public void init(Handler handler, String sIP, int nPort) {
        mQueue.clear();
        mHandler = handler;
        mIPstr=sIP;
        mPort=nPort;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(mIPstr, mPort), 3000);

                    //代码执行到这里，说明socket连接成功
                    mOutputStream = socket.getOutputStream();
                    mInputStream = socket.getInputStream();

                    mWriterThread = new WriterThread();
                    mReaderThread = new ReaderThread();
                    mWriterThread.start();
                    mReaderThread.start();

                    //初始化
                    //byte[] data = {0x33};
                    //mQueue.offer(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获得单例对象
     *
     * @return
     */
    public static TcpMessageUtils getInstance() {
        if (utils == null) {
            synchronized (TcpMessageUtils.class) {
                if (utils == null) {
                    utils = new TcpMessageUtils();
                }
            }
        }
        return utils;
    }

    public void send(byte[] data) {
        synchronized (TcpMessageUtils.class) {
            mQueue.offer(data);
        }
    }

    //发送数据线程
    class WriterThread extends Thread {
        private boolean mRunning = true;
        @Override
        public void run() {
            while (mRunning) {
                try {
                    byte[] data = mQueue.take();
                    mOutputStream.write(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopThread() {
            mRunning = false;
            interrupt();
        }
    }

    //读取数据线程
    class ReaderThread extends Thread {
        private boolean mRunning = true;
        private int i=0;
        
        @Override
        public void run(){
            while(mRunning){
                try {
                	Arrays.fill(buffer, (byte)0);
                    for(i = 0; i < buffer.length; i++) buffer[i] = (byte) mInputStream.read();
                    //System.out.println("ReaderThread, i"+i);
                    if (mHandler != null){
                        Message msg = mHandler.obtainMessage();
                        msg.obj = buffer;
                        msg.what = WF_AVObj.WHAT_RECEIVED_SERIAL;
                        mHandler.sendMessage(msg);
                    }
                    Thread.sleep(10);
                }catch (Exception e){  e.printStackTrace(); }
            }
        }

        public void stopThread() {
            mRunning = false;
            interrupt();
        }
    }

    /**
     * 关闭tcp连接.
     */
    public void close() {
        mHandler = null;
        try {
            if (mWriterThread != null) mWriterThread.stopThread();
            if (mReaderThread != null) mReaderThread.stopThread(); 

            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }

            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
