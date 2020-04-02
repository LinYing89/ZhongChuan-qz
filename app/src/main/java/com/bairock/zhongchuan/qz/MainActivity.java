package com.bairock.zhongchuan.qz;

import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bairock.zhongchuan.qz.dialog.TitlePopup;
import com.bairock.zhongchuan.qz.netty.H264Broadcaster;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.TcpServer;
import com.bairock.zhongchuan.qz.netty.VoiceBroadcaster;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.HeartThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
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

        UserUtil.initUsers();
        MessageBroadcaster messageBroadcaster = new MessageBroadcaster();
        messageBroadcaster.bind();

        H264Broadcaster h264bRoadcaster = new H264Broadcaster();
        h264bRoadcaster.bind();

        VoiceBroadcaster voiceBroadcaster = new VoiceBroadcaster();
        voiceBroadcaster.bind();

        new HeartThread().start();
        try {
            new TcpServer().run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileUtil.createPolicePath();

//        if (Build.VERSION.SDK_INT >= 23) {
//            int REQUEST_CODE_CONTACT = 101;
//            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//            //验证是否许可权限
//            for (String str : permissions) {
//                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
//                    //申请权限
//                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
//                }
//            }
//        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},200);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
        }

//        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},200);
//        }

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
                img_right.setVisibility(View.VISIBLE);
                index = 1;
                if (fragmentMsg != null) {
                    fragmentMsg.refresh();
                }
                txt_title.setText(R.string.message);
                img_right.setImageResource(R.drawable.icon_add);
                break;
            case R.id.re_contact_list:
                index = 0;
                txt_title.setText(R.string.contacts);
                img_right.setVisibility(View.VISIBLE);
                img_right.setImageResource(R.drawable.icon_titleaddfriend);
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
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findViewById() {
        txt_title = findViewById(R.id.txt_title);
        img_right = findViewById(R.id.img_right);
    }

    private void initViews() {
        // 设置消息页面为初始页面
        img_right.setVisibility(View.VISIBLE);
        img_right.setImageResource(R.drawable.icon_add);
    }

    private void setOnListener() {
        img_right.setOnClickListener(this);
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
                    titlePopup.show(findViewById(R.id.layout_bar));
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
        IntentFilter intentFilter1 = new IntentFilter(ConversationUtil.VOICE_ANS_ACTION);
        registerReceiver(mediaBroadcastReceiver, intentFilter1);

//        Intent intent = new Intent(this, UpdateService.class);
//        startService(intent);
//        registerReceiver(new MyBroadcastReceiver(), new IntentFilter(
//                "com.juns.wechat.Brodcast"));
//        // 注册一个接收消息的BroadcastReceiver
//        msgReceiver = new NewMessageBroadcastReceiver();
//        IntentFilter intentFilter = new IntentFilter(EMChatManager
//                .getInstance().getNewMessageBroadcastAction());
//        intentFilter.setPriority(3);
//        registerReceiver(msgReceiver, intentFilter);

    }

    // 自己联系人 群组数据返回监听
    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Bundle bundle = intent.getExtras();
            fragmentMsg.refresh();
        }
    }

    /**
     * 新消息广播接收者
     *
     *
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

            String from = intent.getStringExtra("from");
            // 消息id
            String msgId = intent.getStringExtra("msgid");

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
            String type = intent.getStringExtra(Constants.MEDIA_TYPE);
            String name = intent.getStringExtra(Constants.NAME);
            if(type.equals(Constants.MEDIA_TYPE_VOICE)){
                //语音请求
                Intent intent1 = new Intent(MainActivity.this, VoiceCallActivity.class);
                intent1.putExtra(Constants.VOICE_TYPE, Constants.VOICE_ANS);
                intent1.putExtra(Constants.NAME, name);
                MainActivity.this.startActivity(intent);
            }else if(type.equals(Constants.MEDIA_TYPE_VIDEO)){
                // 视频请求
                Intent intent1 = new Intent(MainActivity.this, VideoCallActivity.class);
                intent1.putExtra(Constants.VIDEO_TYPE, Constants.VIDEO_ANS);
                intent1.putExtra(Constants.NAME, name);
                MainActivity.this.startActivity(intent);
            }
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