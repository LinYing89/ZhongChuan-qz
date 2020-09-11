package com.example.wfsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bairock.zhongchuan.qz.R;

public class TelescopeWifiActivity extends AppCompatActivity {

    private EditText edtWiFiSSID;
    private EditText edtWiFiPwd;
    private EditText edtEncType;
    private EditText edtWifiMode;
    private Button btnOk;

    public static WF_AVObj m_objAV=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telescope_wifi);
        findViews();

        m_objAV=new WF_AVObj();
//        WF_AVObj.API_Init();
        m_objAV.API_Create(null);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sSSID=edtWiFiSSID.getText().toString();
                String sPwd=edtWiFiPwd.getText().toString();
                String strTip="", strTmp="";
                int nRet=1000, nEncType=4, nWifiMode=1;
                if(sSSID.length()<=0){
                    strTip="WiFi SSID不能为空";
                    Alert.showToast(TelescopeWifiActivity.this, strTip);
                    return;
                }
                if(sSSID.length()>=64){
                    strTip="WiFi SSID不能超过64个字符";
                    Alert.showToast(TelescopeWifiActivity.this, strTip);
                    return;
                }
                if(sSSID.length()>=64){
                    strTip="WiFi 密码不能超过64个字符";
                    Alert.showToast(TelescopeWifiActivity.this, strTip);
                    return;
                }
                strTmp=edtEncType.getText().toString();
                if(strTmp.length()<=0) nEncType=4;
                else nEncType= Integer.parseInt(strTmp);

                strTmp=edtWifiMode.getText().toString();
                if(strTmp.length()<=0) nWifiMode=0;
                else nWifiMode= Integer.parseInt(strTmp);

                nRet=m_objAV.API_Connect("sf://192.168.11.10:54188");
                if(nRet<0) Alert.showToast(TelescopeWifiActivity.this, "Connect fails("+nRet+")");

                nRet=m_objAV.API_SetAPWiFiName(sSSID, sPwd, nEncType, 10, nWifiMode);
                if(nRet<0) strTip=String.format("发送失败(%d)", nRet);
                else strTip=String.format("发送成功(%d),等待设备回复", nRet);
                Alert.showToast(TelescopeWifiActivity.this, strTip);
            }
        });
    }

    @Override
    protected void onDestroy(){
        m_objAV.API_Disconnect();
        m_objAV.API_Destroy();
//        WF_AVObj.API_Uninit();

        super.onDestroy();
    }

    private void findViews(){
        edtWiFiSSID= findViewById(R.id.edtWiFiSSID);
        edtWiFiPwd = findViewById(R.id.edtWiFiPwd);
        edtEncType = findViewById(R.id.edtEncType);
        edtWifiMode = findViewById(R.id.edtWifiMode);
        edtEncType.setText("4");
        edtWifiMode.setText("0");
        btnOk = findViewById(R.id.btnOk);
    }
}