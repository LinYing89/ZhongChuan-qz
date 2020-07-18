package com.bairock.zhongchuan.qz;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bairock.zhongchuan.qz.dialog.TitlePopup;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.TcpServer;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.netty.VoiceBroadcaster;
import com.bairock.zhongchuan.qz.netty.file.FileUploadServer;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.HeartThread;
import com.bairock.zhongchuan.qz.utils.TcpClientUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.bairock.zhongchuan.qz.view.activity.VideoCallActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceCallActivity;
import com.bairock.zhongchuan.qz.view.fragment.FragmentVoiceUpload;
import com.bairock.zhongchuan.qz.view.fragment.FragmentContact;
import com.bairock.zhongchuan.qz.view.fragment.FragmentMsg;
import com.bairock.zhongchuan.qz.view.fragment.FragmentVideoUpload;

public class MainActivity extends FragmentActivity implements OnClickListener {
    private TextView txt_title;
    private ImageView img_right;
    private NewMessageBroadcastReceiver msgReceiver;
    private MediaBroadcastReceiver mediaBroadcastReceiver;
    private LogoutBroadcastReceiver logoutBroadcastReceiver;
    protected static final String TAG = "MainActivity";
    private TitlePopup titlePopup;
    private TextView unreaMsgdLabel;// 未读消息textview
    private Fragment[] fragments;
    public FragmentMsg fragmentMsg;
    private FragmentContact fragmentContact;
    private FragmentVoiceUpload fragmentVoiceUpload;
    private FragmentVideoUpload fragmentVideoUpload;
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private String connectMsg = "";;
    private int index;
    private int currentTabIndex;// 当前fragment的index
    private AMapLocationClient mLocationClient = null;
    public final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance2().addActivity(this);
        findViewById();
        initViews();
        initTabView();
        setOnListener();
//        initPopWindow();
//        initReceiver();

//        UserUtil.initUsers();

        H264Broadcaster.getIns().bind();
        VoiceBroadcaster.getIns().bind();

        new HeartThread().start();
        try {
            TcpServer.getIns().run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileUploadServer.getIns().bind();

        FileUtil.createPolicePath();

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},200);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},200);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String []{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO},200);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }

        initReceiver();
        if(null == mLocationClient){
            initLocation();
        }
    }

    private void initTabView() {
        unreaMsgdLabel = findViewById(R.id.unread_msg_number);
        fragmentMsg = new FragmentMsg();
        fragmentContact = new FragmentContact();
        fragmentVoiceUpload = new FragmentVoiceUpload();
        fragmentVideoUpload = new FragmentVideoUpload();
        fragments = new Fragment[] {fragmentContact, fragmentMsg,
                fragmentVoiceUpload, fragmentVideoUpload};
        imagebuttons = new ImageView[4];
        imagebuttons[0] = findViewById(R.id.ib_contact_list);
        imagebuttons[1] = findViewById(R.id.ib_weixin);
        imagebuttons[2] = findViewById(R.id.ib_find);
        imagebuttons[3] = findViewById(R.id.ib_profile);

        imagebuttons[0].setSelected(true);
        textviews = new TextView[4];
        textviews[0] = findViewById(R.id.tv_contact_list);
        textviews[1] = findViewById(R.id.tv_weixin);
        textviews[2] = findViewById(R.id.tv_find);
        textviews[3] = findViewById(R.id.tv_profile);
        textviews[0].setTextColor(0xFF45C01A);
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragmentContact)
                .add(R.id.fragment_container, fragmentMsg)
                .add(R.id.fragment_container, fragmentVideoUpload)
                .add(R.id.fragment_container, fragmentVoiceUpload)
                .hide(fragmentMsg).hide(fragmentVideoUpload)
                .hide(fragmentVoiceUpload).show(fragmentContact).commit();
        updateUnreadLabel();
    }

    public void onTabClicked(View view) {
        img_right.setVisibility(View.GONE);
        switch (view.getId()) {
            case R.id.re_weixin:
//                img_right.setVisibility(View.VISIBLE);
                index = 1;
                if (fragmentMsg != null) {
                    fragmentMsg.refresh();
                }
                txt_title.setText(R.string.message);
//                img_right.setImageResource(R.drawable.icon_add);
                break;
            case R.id.re_contact_list:
                index = 0;
                txt_title.setText(R.string.contacts);
//                img_right.setVisibility(View.VISIBLE);
//                img_right.setImageResource(R.drawable.icon_titleaddfriend);
                break;
            case R.id.re_find:
                index = 2;
                txt_title.setText(R.string.voice_upload);
                break;
            case R.id.re_profile:
                index = 3;
                txt_title.setText(R.string.video_upload);
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(0xFF999999);
        textviews[index].setTextColor(0xFF45C01A);
        currentTabIndex = index;
    }

    @Override
    protected void onResume() {
        updateUnreadLabel();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(null != msgReceiver) {
            unregisterReceiver(msgReceiver);
            msgReceiver = null;
        }
        if(null != mediaBroadcastReceiver) {
            unregisterReceiver(mediaBroadcastReceiver);
            mediaBroadcastReceiver = null;
        }
        if(null != logoutBroadcastReceiver) {
            unregisterReceiver(logoutBroadcastReceiver);
            logoutBroadcastReceiver = null;
        }
        if(null != mLocationClient) {
            mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
            mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
            mLocationClient = null;
        }
        super.onDestroy();
    }

    private void findViewById() {
        txt_title = findViewById(R.id.txt_title);
        img_right = findViewById(R.id.img_right);
    }

    private void initViews() {
        // 设置消息页面为初始页面
//        img_right.setVisibility(View.VISIBLE);
//        img_right.setImageResource(R.drawable.icon_add);
    }

    private void setOnListener() {
//        img_right.setOnClickListener(this);
    }

    private int keyBackClickCount = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (keyBackClickCount++) {
                case 0:
                    Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            keyBackClickCount = 0;
                        }
                    }, 3000);
                    break;
                case 1:
                    App.getInstance2().exit();
                    finish();
                    overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_right:
                if (index == 0) {
//                    titlePopup.show(findViewById(R.id.layout_bar));
                } else {
//                    Utils.start_Activity(MainActivity.this, PublicActivity.class,
//                            new BasicNameValuePair(Constants.NAME, "添加朋友"));
                }
                break;
            default:
                break;
        }
    }

    private void initReceiver() {
        // 注册接收消息广播
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConversationUtil.CHAT_ACTION);
        // 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);

        mediaBroadcastReceiver = new MediaBroadcastReceiver();
        IntentFilter intentFilter1 = new IntentFilter(ConversationUtil.VOICE_ASK_ACTION);
        intentFilter1.addAction(ConversationUtil.VIDEO_ASK_ACTION);
        registerReceiver(mediaBroadcastReceiver, intentFilter1);

        logoutBroadcastReceiver = new LogoutBroadcastReceiver();
        IntentFilter intentFilter2 = new IntentFilter(ConversationUtil.LOGOUT_ACTION);
        registerReceiver(logoutBroadcastReceiver, intentFilter2);
    }

    private void initLocation(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mAMapLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        mLocationOption.setSensorEnable(true);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(5000);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(false);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private AMapLocationListener mAMapLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(final AMapLocation amapLocation) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    locationRefresh(amapLocation);
                }});
        }
    };

    private void locationRefresh( final AMapLocation amapLocation){
        Double lat = null;
        Double lng = null;
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                lat = amapLocation.getLatitude();//获取纬度
                lng = amapLocation.getLongitude();//获取经度
                UserUtil.MY_LOCATION.setLat(lat);
                UserUtil.MY_LOCATION.setLng(lng);
                Log.e("MainActivity", "lng:" + lng + ",lat:" + lat);
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
//        UserUtil.sendMyHeart(lat, lng);
    }

    /**
     * 新消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            // 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

            String from = intent.getStringExtra("from");
            String content = intent.getStringExtra("content");
            // 消息id
            String msgId = intent.getStringExtra("msgid");

            if(App.isScreenLocked2(context)){
                //如果在短消息发送界面，则关闭短消息发送界面
                //如此是为了防止用户没有点击通知栏而直接开屏进入短消息发送界面，造成下面的将短消息界面的MsgNumBean改掉了引起的错误
//                if(null != MsgSenderActivity.myHandler){
//                    MsgSenderActivity.myHandler.obtainMessage(MsgSenderActivity.CLOSE).sendToTarget();
//                }
                String channelID = "1";
                String channelName = "channel_name";
                NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

                //获取PendingIntent
//                MsgSenderActivity.msgNumBean = msgNumBean;
                Intent mainIntent = new Intent(context, ChatActivity.class);
                mainIntent.putExtra(Constants.NAME, from);// 设置昵称
                mainIntent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_SINGLE);
                mainIntent.putExtra(Constants.User_ID, from);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
                Notification notification = new NotificationCompat.Builder(context, channelID)
                        .setContentText(content)
                        .setContentTitle("单兵取证新短消息")
                        .setSmallIcon(R.mipmap.head)
                        .setContentIntent(mainPendingIntent)
                        .setAutoCancel(true)//点击通知头自动取消
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build();
                manager.notify(1,notification);
            }

            // 注销广播接收者，否则在ChatActivity中会收到这个广播
            abortBroadcast();
            // 刷新bottom bar消息未读数
            updateUnreadLabel();
            if (currentTabIndex == 1) {
                // 当前页面如果为聊天历史页面，刷新此页面
                if (fragmentMsg != null) {
                    fragmentMsg.refresh();
                }
            }
        }
    }

    private class MediaBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String type = intent.getStringExtra(Constants.MEDIA_TYPE);
            Bundle bundle = intent.getBundleExtra("myBundle");
            String type = bundle.getString(Constants.MEDIA_TYPE);
            String name = bundle.getString(Constants.NAME);
            if(type.equals(Constants.MEDIA_TYPE_VOICE)){
                // 收到语音请求
                if(VoiceCallActivity.name.isEmpty()) {
                    Intent intent1 = new Intent(MainActivity.this, VoiceCallActivity.class);
                    // 进入应答界面
                    intent1.putExtra(Constants.VOICE_TYPE, Constants.VOICE_ANS);
                    intent1.putExtra(Constants.NAME, name);
                    MainActivity.this.startActivity(intent1);
                }else{
                    if(!VoiceCallActivity.name.equals(name)) {
                        MessageBroadcaster.send(UdpMessageHelper.createVoiceCallAns(UserUtil.user.getUsername(), 1), name);
                    }
                }
            }else if(type.equals(Constants.MEDIA_TYPE_VIDEO)){
                // 收到视频请求
                if(VideoCallActivity.name.isEmpty()) {
                    Intent intent1 = new Intent(MainActivity.this, VideoCallActivity.class);
                    // 进入应答界面
                    intent1.putExtra(Constants.VIDEO_TYPE, Constants.VIDEO_ANS);
                    intent1.putExtra(Constants.NAME, name);
                    MainActivity.this.startActivity(intent1);
                }else{
                    if(!VideoCallActivity.name.equals(name)) {
                        MessageBroadcaster.send(UdpMessageHelper.createVideoCallAns(UserUtil.user.getUsername(), 1), name);
                    }
                }
            }
        }
    }

    private class LogoutBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("exit").equals("1")){
                Toast.makeText(context, "很抱歉，程序出现异常，请联系管理员, 即将退出", Toast.LENGTH_SHORT).show();
            }
            MessageBroadcaster.getIns().stop();
            H264Broadcaster.getIns().stop();
            VoiceBroadcaster.getIns().stop();
            FileUploadServer.getIns().close();
            TcpServer.getIns().close();
            TcpClientUtil.close();

            finish();
//            System.exit(0);
        }
    }

    /**
     * 获取未读消息数
     */
    public void updateUnreadLabel() {
        int count = ConversationUtil.getUnreadCount();
        if (count > 0) {
            unreaMsgdLabel.setText(String.valueOf(count));
            unreaMsgdLabel.setVisibility(View.VISIBLE);
        } else {
            unreaMsgdLabel.setVisibility(View.INVISIBLE);
        }
    }

}