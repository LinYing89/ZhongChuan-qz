package com.bairock.zhongchuan.qz;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.dialog.ActionItem;
import com.bairock.zhongchuan.qz.dialog.TitlePopup;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.utils.HeartThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.fragment.FragmentVoiceUpload;
import com.bairock.zhongchuan.qz.view.fragment.FragmentContact;
import com.bairock.zhongchuan.qz.view.fragment.FragmentMsg;
import com.bairock.zhongchuan.qz.view.fragment.FragmentVideoUpload;

public class MainActivity extends FragmentActivity implements OnClickListener {
    private TextView txt_title;
    private ImageView img_right;
    private NewMessageBroadcastReceiver msgReceiver;
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

        new HeartThread().start();
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

    private void initPopWindow() {
        // 实例化标题栏弹窗
        titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        titlePopup.setItemOnClickListener(onitemClick);
        // 给标题栏弹窗添加子类
        titlePopup.addAction(new ActionItem(this, R.string.menu_groupchat,
                R.drawable.icon_menu_group));
        titlePopup.addAction(new ActionItem(this, R.string.menu_addfriend,
                R.drawable.icon_menu_addfriend));
        titlePopup.addAction(new ActionItem(this, R.string.menu_qrcode,
                R.drawable.icon_menu_sao));
        titlePopup.addAction(new ActionItem(this, R.string.menu_money,
                R.drawable.abv));
    }

    private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {

        @Override
        public void onItemClick(ActionItem item, int position) {
            // mLoadingDialog.show();
            switch (position) {
                case 0:// 发起群聊
//                    Utils.start_Activity(MainActivity.this,
//                            AddGroupChatActivity.class);
                    break;
                case 1:// 添加朋友
//                    Utils.start_Activity(MainActivity.this, PublicActivity.class,
//                            new BasicNameValuePair(Constants.NAME, "添加朋友"));
                    break;
                case 2:// 扫一扫
//                    Utils.start_Activity(MainActivity.this, CaptureActivity.class);
                    break;
                case 3:// 收钱
//                    Utils.start_Activity(MainActivity.this, GetMoneyActivity.class);
                    break;
                default:
                    break;
            }
        }
    };

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

    private void initVersion() {
        // TODO 检查版本更新
        String versionInfo = Utils.getValue(this, Constants.VersionInfo);
        if (!TextUtils.isEmpty(versionInfo)) {
//            Tipdialog = new WarnTipDialog(this,
//                    "发现新版本：  1、更新啊阿三达到阿德阿   2、斯顿阿斯顿撒旦？");
//            Tipdialog.setBtnOkLinstener(onclick);
//            Tipdialog.show();
        }
    }

    private DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Utils.showLongToast(MainActivity.this, "正在下载...");// TODO
//            Tipdialog.dismiss();
        }
    };

    private void initReceiver() {
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
//
//        // 注册一个ack回执消息的BroadcastReceiver
//        IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager
//                .getInstance().getAckMessageBroadcastAction());
//        ackMessageIntentFilter.setPriority(3);
//        registerReceiver(ackMessageReceiver, ackMessageIntentFilter);
//
//        // 注册一个透传消息的BroadcastReceiver
//        IntentFilter cmdMessageIntentFilter = new IntentFilter(EMChatManager
//                .getInstance().getCmdMessageBroadcastAction());
//        cmdMessageIntentFilter.setPriority(3);
//        registerReceiver(cmdMessageReceiver, cmdMessageIntentFilter);
//        // setContactListener监听联系人的变化等
//        // EMContactManager.getInstance().setContactListener(
//        // new MyContactListener());
//        // 注册一个监听连接状态的listener
//        // EMChatManager.getInstance().addConnectionListener(
//        // new MyConnectionListener());
//        // // 注册群聊相关的listener
//        EMGroupManager.getInstance().addGroupChangeListener(
//                new MyGroupChangeListener());
//        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
//        EMChat.getInstance().setAppInited();
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
            if (currentTabIndex == 0) {
                // 当前页面如果为聊天历史页面，刷新此页面
                if (fragmentMsg != null) {
                    fragmentMsg.refresh();
                }
            }
        }
    }

    /**
     * 获取未读消息数
     */
    public void updateUnreadLabel() {
        int count = 0;
//        count = EMChatManager.getInstance().getUnreadMsgsCount();
        if (count > 0) {
            unreaMsgdLabel.setText(String.valueOf(count));
            unreaMsgdLabel.setVisibility(View.VISIBLE);
        } else {
            unreaMsgdLabel.setVisibility(View.INVISIBLE);
        }
    }

}