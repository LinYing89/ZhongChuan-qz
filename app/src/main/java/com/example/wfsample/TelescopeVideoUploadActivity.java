package com.example.wfsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.activity.VideoUploadThirdActivity;
import com.library.live.Publish;
import com.wifi.IDataFromDevice;
import com.wifi.SF_PROTOCOL;
import com.wifi.WF_NetAPI;

import java.io.File;

public class TelescopeVideoUploadActivity extends AppCompatActivity implements IDataFromDevice {

    public static final int WHAT_UPDATE_STREAM_INFO =1000;
    public static final int WHAT_H264_ARRIVED	    =1001;
    public static final int WHAT_MJPEG_ARRIVED	    =1003;
    public static final int WHAT_GET_AV_IP_MSG 		=1004;

    private Chronometer chronometer;
    private ImageView imgHangUp;
    private TextView txtIp;
    private TextView txtMessage;

    public static WF_AVObj  m_objAV=null;
    private WFRender myRender= null;
    private WFSurfaceView myGlSurfaceView = null;

    private GestureDetector mGestureDetector=null;

    private String mainServerIp;
    public static String strUrl;
    private int m_nScreenWidth=0, m_nScreenHeigh=0;

    private boolean upload;
    private AskBroadcastReceiver receiver;
    private SendUdpThread sendUdpThread;
    private boolean askMainServer = true;

    private boolean keeping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telescope_video_upload);

        //仅去掉标题栏，系统状态栏还是会显示
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        m_objAV=new WF_AVObj();
//        WF_AVObj.API_Init();
        findView();
        m_objAV.regStreamListener(this);
        m_objAV.API_Create(myRender);
        m_objAV.API_SetSnapshotFilename(FileUtil.getPoliceImagePath() + "/WFSample.jpg");

        mainServerIp = UserUtil.findMainServerIp();
        if(mainServerIp == null){
            Toast.makeText(this, "信息处理设备不在线", Toast.LENGTH_SHORT).show();
//            finish();
        }

        // 注册接收消息广播
        receiver = new AskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
        registerReceiver(receiver, intentFilter);

        mGestureDetector=new GestureDetector(this,new OnDoubleClick());

        // 是否请求信息处理设备, 如果试主动上传则需要请求, 如果是被动上传(信息处理设备主动请求)则不需要请求
        askMainServer = getIntent().getBooleanExtra("askMainServer", true);

//        if(null != strUrl && !strUrl.isEmpty()){
//            txtIp.setText(strUrl);
//            connect();
//            if(askMainServer) {
//                if (mainServerIp != null) {
//                    txtMessage.setText("正在请求信息处理设备...");
//                    requireMainServer();
//                }
//            }else {
//                upload = true;
//            }
//        }else{
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                m_objAV.API_Search();
//            }
//        }).start();
            m_objAV.API_Search();
            txtMessage.setText("正在请求摄像望远镜...");
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(myRender!=null) m_objAV.API_SetRender(myRender);
    }

    @Override
    protected void onDestroy(){
        keeping = false;
        if(null != mainServerIp) {
            MessageBroadcaster.sendIp(UdpMessageHelper.createCallMainServerStopAsk(UserUtil.user.getUsername()), mainServerIp);
        }
        if(myRender != null) myRender.destroyShaders();
        //wf_imageview.unregAVObj(m_objAV);
//        m_objAV.API_RecordStop();
        m_objAV.unregStreamListener(this);
        m_objAV.API_RecordStop();
        m_objAV.API_Disconnect();
        m_objAV.API_Destroy();
//        WF_AVObj.API_Uninit();
        chronometer.stop();
        try {
            unregisterReceiver(receiver);
            receiver = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(null != sendUdpThread){
            sendUdpThread.interrupt();
        }
        super.onDestroy();
    }


    @Override
    public void OnStreamInfo(int nWidth, int nHeigh) {
        Message msg = handler.obtainMessage();
        msg.what = WHAT_UPDATE_STREAM_INFO;
        msg.arg1=nWidth;
        msg.arg2=nHeigh;
        handler.sendMessage(msg);
    }

    @Override
    public void OnMsg(Object o, int nCmdType, byte[] pData, int nDataSize) {
        Message msg = handler.obtainMessage();
        msg.what = nCmdType;
        msg.obj= o;
        handler.sendMessage(msg);
    }

    @Override
    public void OnH264(byte[] bytAVData, int nDataSize) {
        if(upload){
            H264Broadcaster.send(bytAVData, mainServerIp);
        }
    }

    @Override
    public void OnStream(int eAVCodecID, byte[] bytAVData, int nDataSize) {
//        if(!m_bRecvDeviceData){
//            m_bRecvDeviceData=true;
//
//            if(eAVCodecID== WF_NetAPI.WF_AV_CODEC_ID_MJPEG) {
//                m_bMJpegVideo=true;
//
//                Message msg = handler.obtainMessage();
//                msg.what = WHAT_MJPEG_ARRIVED;
//                handler.sendMessage(msg);
//            }else m_bMJpegVideo=false;
//        }

        if(eAVCodecID==WF_NetAPI.WF_AV_IP_MSG){
            if(bytAVData!=null && bytAVData.length>=nDataSize){
                String strRTSP;
                int nPort=0;
                nPort=(bytAVData[9]&0xFF)<<8 | (bytAVData[8]&0xFF);
                //strRTSP=String.format("http://%d.%d.%d.%d:%d", //  /1/mjpeg
                //		bytAVData[4]&0xFF, bytAVData[5]&0xFF, bytAVData[6]&0xFF, bytAVData[7]&0xFF, nPort);
                strRTSP=String.format("sf://%d.%d.%d.%d:%d", //  /1/mjpeg
                        bytAVData[4]&0xFF, bytAVData[5]&0xFF, bytAVData[6]&0xFF, bytAVData[7]&0xFF, nPort);
                Message msg=handler.obtainMessage();
                msg.what=WHAT_GET_AV_IP_MSG;
                msg.obj=strRTSP;
                handler.sendMessage(msg);
            }
        }
    }

    private void findView() {
        txtMessage = findViewById(R.id.txtMessage);
        imgHangUp = findViewById(R.id.imgHangUp);
        chronometer = findViewById(R.id.chronometer);
        txtIp = findViewById(R.id.txtIp);
        myGlSurfaceView = findViewById(R.id.my_gl_surfaceview);
        myRender = new WFRender(myGlSurfaceView);
        myGlSurfaceView.setRenderer(myRender);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_nScreenWidth = dm.widthPixels;
        m_nScreenHeigh = dm.heightPixels;

        imgHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    boolean connected = false;
    private void connect(){
        if(!connected) {
            connected = true;
//            final int[] nRet = {-1};
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    nRet[0] = m_objAV.API_SetProperty("rtsp_transport", "udp");
//                    if(nRet[0] >= 0) {
//                        nRet[0] = m_objAV.API_Connect(strUrl);
//                        if(nRet[0] >= 0) {
////                            m_objAV.API_RecordStart(FileUtil.getPoliceTelescopeVideoPath() + FileUtil.getPoliceFileName() + ".mp4");
//                        }
//                    }
//                }
//            }).start();
//            if (nRet[0] < 0) {
//                Alert.showToast(this, "连接失败(" + nRet[0] + ")");
//            }

            int nRet = m_objAV.API_SetProperty("rtsp_transport", "udp");
            if (nRet >= 0) {
                nRet = m_objAV.API_Connect(strUrl);
                if (nRet < 0) {
                    Alert.showToast(this, "连接失败(" + nRet + ")");
                } else {
//                    m_objAV.API_RecordStart(FileUtil.getPoliceTelescopeVideoPath() + FileUtil.getPoliceFileName() + ".mp4");
                }
            } else {
                Alert.showToast(this, "SetProperty fails(" + nRet + ")");
            }
            chronometer.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(m_objAV.API_IsVideoPlaying()){
                Intent intent = new Intent(TelescopeVideoUploadActivity.this, ActivityFullLiveView.class);
//                intent.putExtra(INTENT_NAME_MJPEG_VIDEO, m_bMJpegVideo);
                startActivityForResult(intent, 0);
            }
            return false;
        }
//	    @Override
//	    public boolean onDoubleTapEvent(MotionEvent e) {
//	        return super.onDoubleTapEvent(e);
//	    }
    }

    private void requireMainServer(){
        if(null == mainServerIp){
            return;
        }
        sendUdpThread = new SendUdpThread(UdpMessageHelper.createVideoCallMainServerAsk(UserUtil.user.getUsername()), mainServerIp);
        sendUdpThread.setOnNoAnswerListener(new SendUdpThread.OnNoAnswerListener() {
            @Override
            public void onNoAnswer() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TelescopeVideoUploadActivity.this, "信息处理设备无应答", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        sendUdpThread.start();
    }

    private class AskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            // 记得把广播给终结掉
            abortBroadcast();
            SendUdpThread.answered = true;
            if(null != sendUdpThread) {
                sendUdpThread.interrupt();
            }
            String result = intent.getStringExtra("result");
            if (result.equals("0")) {
                //接受
                upload = true;
            } else if (result.equals("1")) {
                //拒绝1/挂断2
                Toast.makeText(TelescopeVideoUploadActivity.this, "信息处理设备拒绝请求", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case WHAT_UPDATE_STREAM_INFO:{
                    myGlSurfaceView.setVisibility(View.VISIBLE);

                    int width=m_nScreenWidth;
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,width*msg.arg2/msg.arg1);
                    lp.gravity = Gravity.CENTER;
                    myGlSurfaceView.setLayoutParams(lp);
                }break;

                case WHAT_MJPEG_ARRIVED:{
                    myGlSurfaceView.setVisibility(View.GONE);
                    //wf_imageview.regAVObj(m_objAV); //tmptmp
                }break;

                case WHAT_GET_AV_IP_MSG:{
                    String strRTSP=(String)msg.obj;
                    if(strRTSP!=null){
                        if(strUrl == null || strUrl.isEmpty()) {
                            strUrl = strRTSP;
                            txtMessage.setText("");
                            txtIp.setText(strUrl);
                            connect();
                            if(askMainServer) {
                                requireMainServer();
                            }else {
                                upload = true;
                            }
                        }
                        Alert.showToast(TelescopeVideoUploadActivity.this, strRTSP);
                    }}break;

                case SF_PROTOCOL.IOCTRL_TYPE_GET_AP_PARAMETERS_RESP:{
                    WF_AVObj o=(WF_AVObj)msg.obj;
                    if(o!=null){
//                        String strResp=String.format("ssid=%s  password=%s", o.getAPParam_ssid(), o.getAPParam_password());
//                        Alert.showToast(TelescopeActivity.this, strResp);
                    }}break;
                case SF_PROTOCOL.IOCTRL_TYPE_SET_AP_PARAMETERS_RESP:{
                    WF_AVObj o=(WF_AVObj)msg.obj;
                    if(o!=null){
//                        String strResp=String.format("set wifi param: result=%d", o.getAPParam_setResult());
//                        Alert.showToast(TelescopeActivity.this, strResp);
                    }
                }break;
                default:;
            }
        }
    };

}