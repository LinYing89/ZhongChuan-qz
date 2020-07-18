package com.example.wfsample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bairock.zhongchuan.qz.R;
import com.utility.Convert;
import com.utility.IStreamJpeg;
import com.utility.Mjpeg2Jpeg;
import com.wifi.IDataFromDevice;
import com.wifi.SF_PROTOCOL;
import com.wifi.WF_NetAPI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TelescopeActivity extends AppCompatActivity implements IDataFromDevice, IStreamJpeg {

    public static final int WHAT_UPDATE_STREAM_INFO =1000;
    public static final int WHAT_H264_ARRIVED	    =1001;
    public static final int WHAT_MJPEG_ARRIVED	    =1003;
    public static final int WHAT_GET_AV_IP_MSG 		=1004;

    public static final String INTENT_NAME_MJPEG_VIDEO="mjpeg_video";

    public static WF_AVObj  m_objAV=null;
    private InputMethodManager m_objImm=null;
    private String FILE_ROOT_PATH="";
    private String URL_FILE=null;
    private EditText edt_url=null;
    private Button btn_connect=null, btn_getip=null, btn_startRecord=null, btn_snapshot=null;
    private Button   btn_getwifi=null, btn_setwifi=null, btn_setPwm=null;
    private Button   btn_serialPort=null;
    private CheckBox btn_chk1=null, btn_chk2=null;

    private boolean m_bConnected=false;
    private boolean m_bStartRecord=false;
    private int m_nScreenWidth=0, m_nScreenHeigh=0;

    private int m_bJPEGType=1; //0=data from WF_AVObj, 1=data from Mjpeg2Jpeg
    private WFImageView1083 wf_imageview=null; //tmptmp

    private WFRender myRender= null;
    private WFSurfaceView myGlSurfaceView = null;
    private GestureDetector mGestureDetector=null;

    private boolean m_bRecvDeviceData=false;
    private boolean m_bMJpegVideo=false;

    private AssetManager m_am=null;
    public static Mjpeg2Jpeg m_objMjpeg=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telescope);

        mGestureDetector=new GestureDetector(this,new OnDoubleClick());

        File dir = new File(Environment.getExternalStorageDirectory() + "/WFSample");
        if(!dir.exists()){
            try {
                dir.mkdir();
                FILE_ROOT_PATH=dir.getAbsolutePath().toString();
            }catch(SecurityException se){
                System.out.println("FILE_ROOT_PATH, "+se.getMessage());
            }
        }else FILE_ROOT_PATH=dir.getAbsolutePath().toString();

        URL_FILE=FILE_ROOT_PATH+"/url.txt";
        System.out.println("URL_FILE="+URL_FILE);

        m_objImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        m_objAV=new WF_AVObj();
        WF_AVObj.API_Init();
        findView();
        setListenner();
        m_objAV.regStreamListener(this);
        m_objAV.API_Create(myRender);
        m_objAV.API_SetSnapshotFilename(getSnapFilename());
        //String sUrl=loadUrl(URL_FILE);
        //if(sUrl==null || sUrl.length()==0)	edt_url.setText("sf://192.168.11.10:54188");
        //else edt_url.setText(sUrl);
        edt_url.setText("sf://192.168.11.10:54188");

        //String fileName = FILE_ROOT_PATH + "/video.h264";
        //m_objAV.delRawH264File(fileName);
        //m_objAV.openRawH264File(fileName);

        m_am=this.getAssets();
        m_objMjpeg=new Mjpeg2Jpeg();
        m_objMjpeg.native_init();
        m_objMjpeg.regAPIListener(this);

        wf_imageview.regAVObj(m_objMjpeg);
        btnEnable(false);
    }


    @Override
    protected void onDestroy(){
        if(myRender != null) myRender.destroyShaders();
        wf_imageview.unregAVObj(m_objMjpeg); //tmptmp
        //wf_imageview.unregAVObj(m_objAV);
        m_objAV.unregStreamListener(this);
        m_objAV.API_RecordStop();
        m_objAV.API_Disconnect();
        m_objAV.API_Destroy();
        WF_AVObj.API_Uninit();

        m_objMjpeg.unregAPIListener(this);
        m_objMjpeg.native_uninit();

        super.onDestroy();
    }

    private void findView() {
        edt_url		=(EditText)findViewById(R.id.edt_url);
        btn_connect	=(Button)findViewById(R.id.btn_connect);
        btn_getip		=(Button)findViewById(R.id.btn_getip);
        btn_startRecord	=(Button)findViewById(R.id.btn_startRecord);
        btn_snapshot=(Button)findViewById(R.id.btn_snapshot);
        btn_getwifi =(Button)findViewById(R.id.btn_getwifi);
        btn_setwifi =(Button)findViewById(R.id.btn_setwifi);
        btn_setPwm	=(Button)findViewById(R.id.btn_setPwm);
        btn_serialPort=(Button)findViewById(R.id.btn_serialPort);
        btn_chk1	=(CheckBox)findViewById(R.id.btn_chk1);
        btn_chk2	=(CheckBox)findViewById(R.id.btn_chk2);

        //wf_imageview= (WFImageView) findViewById(R.id.wf_imageview); //tmptmp
        wf_imageview= (WFImageView1083) findViewById(R.id.wf_imageview);

        myGlSurfaceView = (WFSurfaceView) findViewById(R.id.my_gl_surfaceview);
        myRender = new WFRender(myGlSurfaceView);
        myGlSurfaceView.setRenderer(myRender);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_nScreenWidth = dm.widthPixels;
        m_nScreenHeigh = dm.heightPixels;
    }
    private void setListenner() {
        btn_connect.setOnClickListener(btnClickListener);
        btn_getip.setOnClickListener(btnClickListener);
        btn_startRecord.setOnClickListener(btnClickListener);
        btn_snapshot.setOnClickListener(btnClickListener);
        btn_getwifi.setOnClickListener(btnClickListener);
        btn_setwifi.setOnClickListener(btnClickListener);
        btn_setPwm.setOnClickListener(btnClickListener);
        btn_serialPort.setOnClickListener(btnClickListener);
        btn_chk1.setOnClickListener(btnClickListener);
        btn_chk2.setOnClickListener(btnClickListener);
        myGlSurfaceView.setOnClickListener(btnClickListener);
    }

    private String  loadUrl(String fileName) {
        if(fileName == null || fileName.length() <= 0) return null;
        FileInputStream fis=null;
        String strUrl=null;
        boolean bErr = false;
        try{
            int i=0;
            byte[] buffer=new byte[256];
            fis=new FileInputStream(fileName);
            fis.read(buffer);
            fis.close();
            for(i=0; i<256; i++){
                if(buffer[i]==(byte)0) {
                    if(i!=0){
                        byte[] bytReadTxt=new byte[i];
                        System.arraycopy(buffer, 0, bytReadTxt, 0, i);
                        strUrl= Convert.bytesToString(bytReadTxt, 0);
                    }
                    break;
                }
            }
        }catch(Exception e) {
            bErr = true;
            System.out.println("loadUrl(.): " + e.getMessage());
        }finally {
            if(bErr) {
                if(fis != null){
                    try { fis.close(); }
                    catch(IOException e) { e.printStackTrace(); }
                }
            }
        }
        return strUrl;
    }
    private boolean saveUrl(String fileName, String strUrl) {
        if(fileName == null || fileName.length() <= 0) return false;
        boolean bErr = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            byte[] bytUrl=strUrl.getBytes();
            fos.write(bytUrl, 0, bytUrl.length);
            fos.flush();
            fos.close();
        }catch(Exception e) {
            bErr = true;
            System.out.println("saveUrl(.): " + e.getMessage());
        }finally {
            if(bErr) {
                if(fos != null){
                    try { fos.close(); }
                    catch(IOException e) { e.printStackTrace(); }
                }
                return false;
            }
        }
        return true;
    }

    private String getRecFilename(){
        String fileName = FILE_ROOT_PATH + "/WFSample.mp4";
        return fileName;
    }
    private String getSnapFilename(){
        String fileName = FILE_ROOT_PATH + "/WFSample.jpg";
        return fileName;
    }


    void btnEnable(boolean bEnable){
        btn_startRecord.setEnabled(bEnable);
        btn_snapshot.setEnabled(bEnable);
        btn_getwifi.setEnabled(bEnable);
        btn_setwifi.setEnabled(bEnable);
        btn_setPwm.setEnabled(bEnable);
    }

    private ThreadReadDataFromUvcRecv m_threadReadData=null;
    private boolean m_bRunning=false;

    private View.OnClickListener btnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_chk1:{
                    if(btn_chk1.isChecked()) btn_chk2.setChecked(false);
                    else btn_chk2.setChecked(true);
                }break;

                case R.id.btn_chk2:{
                    if(btn_chk2.isChecked()) btn_chk1.setChecked(false);
                    else btn_chk1.setChecked(true);
                }break;

                case R.id.btn_getip:{
                    m_objAV.API_Search();
                }break;
                case R.id.btn_connect:{
                    m_objImm.hideSoftInputFromWindow(edt_url.getWindowToken(), 0);
                    String sUrl=edt_url.getText().toString();
                    saveUrl(URL_FILE, sUrl);

                    m_bConnected=!m_bConnected;
                    if(m_bConnected){
                        btn_connect.setText(R.string.btn_disconnect);
                        String strUrl=edt_url.getText().toString();
                        if(strUrl.length()<=0) break;

                        int nRet=-1;
                        if(btn_chk1.isChecked()) nRet=m_objAV.API_SetProperty("rtsp_transport", "udp");
                        else if(btn_chk2.isChecked()) nRet=m_objAV.API_SetProperty("rtsp_transport", "tcp");
                        else {
                            Alert.showToast(TelescopeActivity.this, "请选over udp或over tcp.");
                            return;
                        }
                        m_bRecvDeviceData=false;
                        if(nRet>=0) {
                            nRet=m_objAV.API_Connect(strUrl);
                            if(nRet<0) Alert.showToast(TelescopeActivity.this, "Connect fails("+nRet+")");
                            else btnEnable(true);
                        }else Alert.showToast(TelescopeActivity.this, "SetProperty fails("+nRet+")");

                        System.out.println("API_Connect(.)="+nRet);
                    }else{ //disconnect
                        btn_connect.setText(R.string.btn_connect);
                        m_objAV.API_Disconnect();
                    }
                }break;

                case R.id.btn_startRecord:{
                    m_bStartRecord=!m_bStartRecord;
                    if(m_bStartRecord){
                        btn_startRecord.setText(R.string.btn_stop_rec);
                        String strFilename=getRecFilename();
                        int nRet=m_objAV.API_RecordStart(strFilename);
                        System.out.println("API_RecordStart(.)="+nRet+",strFilename="+strFilename);
                    }else{
                        btn_startRecord.setText(R.string.btn_start_rec);
                        m_objAV.API_RecordStop();
                    }
                }break;

                case R.id.btn_snapshot:{
                    String strFilename=getSnapFilename();
                    m_objAV.API_Snapshot(strFilename);
                }break;

                case R.id.btn_getwifi:{
                    m_objAV.API_GetAPWiFiName();
                }break;

                case R.id.btn_setwifi:{
                    alert_setWiFi();
                }break;

                case R.id.btn_setPwm:{
                    alert_setPwm();
                }break;

                case R.id.btn_serialPort:{
//                    Intent intent = new Intent(TelescopeActivity.this, ActivitySerial.class);
//                    startActivity(intent);
                }break;

                case R.id.my_gl_surfaceview:{
                }break;

                default:;
            }
        }
    };

    public void alert_setPwm(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View viewPWMDlg = factory.inflate(R.layout.activity_pwm_dlg, null);
        final EditText edtPwmChannel= (EditText)viewPWMDlg.findViewById(R.id.edtPwmChannel);
        final EditText edtPwdEnable = (EditText)viewPWMDlg.findViewById(R.id.edtPwdEnable);
        final EditText edtPwdPeriod = (EditText)viewPWMDlg.findViewById(R.id.edtPwdPeriod);
        final EditText edtPwmDuty= (EditText)viewPWMDlg.findViewById(R.id.edtPwmDuty);
        new AlertDialog.Builder(this).setTitle("请输入要设置的PWM")
                .setIcon(R.mipmap.ic_launcher)
                .setView(viewPWMDlg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String v1=edtPwmChannel.getText().toString();
                        String v2=edtPwdEnable.getText().toString();
                        String v3=edtPwdPeriod.getText().toString();
                        String v4=edtPwmDuty.getText().toString();
                        String strTip=null;

                        int nRet=1000, nPwmChannel=0, nPwdEnable=0, nPwdPeriod=1, nPwmDuty=0;
                        if(v1.isEmpty()) nPwmChannel=0;
                        else nPwmChannel=Integer.parseInt(v1, 10);

                        if(v2.isEmpty()) nPwdEnable=0;
                        else nPwdEnable=Integer.parseInt(v2, 10);

                        if(v3.isEmpty()) nPwdPeriod=0;
                        else nPwdPeriod=Integer.parseInt(v3, 10);

                        if(v4.isEmpty()) nPwmDuty=0;
                        else nPwmDuty=Integer.parseInt(v4, 10);

                        nRet=m_objAV.API_SetPWM(nPwmChannel, nPwdEnable, nPwdPeriod, nPwmDuty);
                        if(nRet<0) strTip=String.format("发送失败(%d)", nRet);
                        else strTip=String.format("发送成功(%d),等待设备回复", nRet);
                        Alert.showToast(TelescopeActivity.this, strTip);
                    }
                }).setNegativeButton("取消",null).show();
    }

    public void alert_setWiFi(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View viewWifiDlg = factory.inflate(R.layout.activity_wifi_dlg, null);
        final EditText edtWiFiSSID= (EditText)viewWifiDlg.findViewById(R.id.edtWiFiSSID);
        final EditText edtWiFiPwd = (EditText)viewWifiDlg.findViewById(R.id.edtWiFiPwd);
        final EditText edtEncType = (EditText)viewWifiDlg.findViewById(R.id.edtEncType);
        final EditText edtWifiMode = (EditText)viewWifiDlg.findViewById(R.id.edtWifiMode);
        edtEncType.setText("4");
        edtWifiMode.setText("0");
        new AlertDialog.Builder(this).setTitle("请输入要设置的WiFi信息")
                .setIcon(R.mipmap.ic_launcher)
                .setView(viewWifiDlg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String sSSID=edtWiFiSSID.getText().toString();
                        String sPwd=edtWiFiPwd.getText().toString();
                        String strTip="", strTmp="";
                        int nRet=1000, nEncType=4, nWifiMode=1;
                        if(sSSID.length()<=0){
                            strTip="WiFi SSID不能为空";
                            Alert.showToast(TelescopeActivity.this, strTip);
                            return;
                        }
                        if(sSSID.length()>=64){
                            strTip="WiFi SSID不能超过64个字符";
                            Alert.showToast(TelescopeActivity.this, strTip);
                            return;
                        }
                        if(sSSID.length()>=64){
                            strTip="WiFi 密码不能超过64个字符";
                            Alert.showToast(TelescopeActivity.this, strTip);
                            return;
                        }
                        strTmp=edtEncType.getText().toString();
                        if(strTmp.length()<=0) nEncType=4;
                        else nEncType= Integer.parseInt(strTmp);

                        strTmp=edtWifiMode.getText().toString();
                        if(strTmp.length()<=0) nWifiMode=0;
                        else nWifiMode= Integer.parseInt(strTmp);

                        nRet=m_objAV.API_SetAPWiFiName(sSSID, sPwd, nEncType, 10, nWifiMode);
                        if(nRet<0) strTip=String.format("发送失败(%d)", nRet);
                        else strTip=String.format("发送成功(%d),等待设备回复", nRet);
                        Alert.showToast(TelescopeActivity.this, strTip);
                    }
                }).setNegativeButton("取消",null).show();
    }

    class ThreadReadDataFromUvcRecv extends Thread{
        public void run() {
            super.run();

            int nREAD_SIZE = 20*1024, nBUF_SIZE=100*1024;
            byte[] bytRead = new byte[nREAD_SIZE];
            byte[] bytBuf  =new byte[nBUF_SIZE];
            byte[] bytBufMjpeg=new byte[nBUF_SIZE];
            int[] pnDataSize=new int[1], pnLastPos=new int[1];
            int nUserDataCallback=0;

            m_bMJpegVideo=false;
            pnDataSize[0]=0;
            pnLastPos[0]=0;
            if(m_objMjpeg==null) return;

            //byte endPoint = 1;
            //while(m_bRunning){
            	/*
                //recieve frame data
                if(!usbDevice.UvcRecv(endPoint, bytRead, nREAD_SIZE)) {
                    //PrintLog("UvcRecv error.\n");
                    break;
                }
                System.arraycopy(bytRead, 0, bytBuf, pnLastPos[0], nREAD_SIZE);
                pnDataSize[0]=nREAD_SIZE;
                pnDataSize[0]+=pnLastPos[0];
                m_objMjpeg.native_parseJPEG(bytBuf, pnDataSize, pnLastPos, bytBufMjpeg, nUserDataCallback);
                */

            try {
                byte[] pMJpgData=new byte[100*1024];
                int pUserDataCallback=1, nRet=0;

                InputStream is=m_am.open("000.jpg");
                byte[] bytData=readDataFromInputStream(is);
                System.out.println("bytData.length="+bytData.length);

                pnDataSize[0]=bytData.length;
                pnLastPos[0]=0;

                nRet=m_objMjpeg.native_parseJPEG(1, bytData, pnDataSize, pnLastPos, pMJpgData, pUserDataCallback);
                System.out.println("native_parseJPEG(..)="+nRet);

            }catch(IOException e){
                e.printStackTrace();
            }
            //}
            m_bRunning=false;
            m_threadReadData=null;
        }
    }



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case WHAT_UPDATE_STREAM_INFO:{
                    wf_imageview.setVisibility(View.GONE);
                    myGlSurfaceView.setVisibility(View.VISIBLE);

                    int width=m_nScreenWidth;
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,width*msg.arg2/msg.arg1);
                    lp.gravity = Gravity.CENTER;
                    myGlSurfaceView.setLayoutParams(lp);
                }break;

                case WHAT_MJPEG_ARRIVED:{
                    wf_imageview.setVisibility(View.VISIBLE);
                    myGlSurfaceView.setVisibility(View.GONE);
                    //wf_imageview.regAVObj(m_objAV); //tmptmp
                }break;

                case WHAT_GET_AV_IP_MSG:{
                    String strRTSP=(String)msg.obj;
                    if(strRTSP!=null){
                        edt_url.setText(strRTSP);
                        Alert.showToast(TelescopeActivity.this, strRTSP);
                    }}break;

                case SF_PROTOCOL.IOCTRL_TYPE_GET_AP_PARAMETERS_RESP:{
                    WF_AVObj o=(WF_AVObj)msg.obj;
                    if(o!=null){
                        String strResp=String.format("ssid=%s  password=%s", o.getAPParam_ssid(), o.getAPParam_password());
                        Alert.showToast(TelescopeActivity.this, strResp);
                    }}break;
                case SF_PROTOCOL.IOCTRL_TYPE_SET_AP_PARAMETERS_RESP:{
                    WF_AVObj o=(WF_AVObj)msg.obj;
                    if(o!=null){
                        String strResp=String.format("set wifi param: result=%d", o.getAPParam_setResult());
                        Alert.showToast(TelescopeActivity.this, strResp);
                    }
                }break;
                default:;
            }
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            System.out.println("ActivityMain.java onDoubleTap");
//            if(m_objAV.API_IsVideoPlaying())
//            {
//                Intent intent = new Intent(TelescopeActivity.this, ActivityFullLiveView.class);
//                intent.putExtra(INTENT_NAME_MJPEG_VIDEO, m_bMJpegVideo);
//                startActivityForResult(intent, 0);
//            }
            return false;
        }
//	    @Override
//	    public boolean onDoubleTapEvent(MotionEvent e) {
//	        return super.onDoubleTapEvent(e);
//	    }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(myRender!=null) m_objAV.API_SetRender(myRender);
        //System.out.println("ActivityMain.java onStart, myRender="+myRender);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    public void OnStream(int eAVCodecID, byte[] bytAVData, int nDataSize) {
        if(!m_bRecvDeviceData){
            m_bRecvDeviceData=true;

            if(eAVCodecID== WF_NetAPI.WF_AV_CODEC_ID_MJPEG) {
                m_bMJpegVideo=true;

                Message msg = handler.obtainMessage();
                msg.what = WHAT_MJPEG_ARRIVED;
                handler.sendMessage(msg);
            }else m_bMJpegVideo=false;
        }

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


    public byte[] readDataFromInputStream(InputStream is){
        BufferedInputStream bis = new BufferedInputStream(is);
        String str="", s="";

        int c = 0;
        byte[] buf = new byte[80*1024];
        while(true){
            try{ c = bis.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
                buf=null;
            }

            if (c == -1) break;
            else{
//                try{
//                    s = new String(buf, 0, c, "UTF-8");
//                } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
//                str += s;
            }
        }

        try { bis.close();  }
        catch (IOException e) {
            e.printStackTrace();
            buf=null;
        }

        //return str;
        return buf;
    }

    @Override
    public int OnCallbackStream(byte[] pData, int nDataSize, int pUserData) {
        System.out.println("jpeg nDataSize="+nDataSize+", pUserData="+pUserData);
        if(!m_bRecvDeviceData){
            m_bRecvDeviceData=true;

            m_bMJpegVideo=true;
            Message msg = handler.obtainMessage();
            msg.what = WHAT_MJPEG_ARRIVED;
            handler.sendMessage(msg);
        }
        return 0;
    }
}