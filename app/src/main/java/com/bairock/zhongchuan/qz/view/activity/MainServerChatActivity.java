package com.bairock.zhongchuan.qz.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.adapter.MessageAdapter;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.UdpMessage;
import com.bairock.zhongchuan.qz.netty.UdpMessageHelper;
import com.bairock.zhongchuan.qz.netty.file.FileUploadClient;
import com.bairock.zhongchuan.qz.netty.file.FileUploadFile;
import com.bairock.zhongchuan.qz.netty.file.FileUploadServer;
import com.bairock.zhongchuan.qz.utils.CommonUtils;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.MyVoiceRecorder;
import com.bairock.zhongchuan.qz.utils.TcpClientUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.bairock.zhongchuan.qz.widght.PasteEditText;
import com.easemob.EMError;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bairock.zhongchuan.qz.view.ChatActivity.REQUEST_CODE_LOCAL;

public class MainServerChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView micImage;
    private ListView listView;
    private PasteEditText mEditTextContent;
    private View buttonSetModeKeyboard;
    private View buttonSetModeVoice;
    private View buttonSend;
    private View buttonPressToSpeak;
    private LinearLayout btnContainer;
    private View more;
    private InputMethodManager manager;
    private ZCConversation conversation;
    public static MainServerChatActivity activityInstance = null;
    // 给谁发送消息
    private String Name;
    private String toChatUsername;
    private MessageAdapter adapter;
    private File cameraFile;

    private TextView txt_title;
    private ImageView img_right;
    private RelativeLayout edittext_layout;
    private Button btnMore;
    private AnimationDrawable animationDrawable;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server_chat);
        App.getInstance2().addActivity(this);
        getSupportActionBar().hide();
        initView();
        setUpView();
        setListener();

        ip = UserUtil.findIpByUsername(toChatUsername);
        if(null == ip){
            Toast.makeText(this, "对方不在线", Toast.LENGTH_SHORT).show();
            finish();
        }

//        ip = "192.168.137.1";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.finish(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * initView
     */
    protected void initView() {
        txt_title = findViewById(R.id.txt_title);
        img_right = findViewById(R.id.img_right);
        micImage = findViewById(R.id.mic_image);
        animationDrawable = (AnimationDrawable) micImage.getBackground();
        animationDrawable.setOneShot(false);
        listView = findViewById(R.id.list);
        mEditTextContent = findViewById(R.id.et_sendmessage);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        edittext_layout = findViewById(R.id.edittext_layout);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSend = findViewById(R.id.btn_send);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        btnContainer = findViewById(R.id.ll_btn_container);
        btnMore = findViewById(R.id.btn_more);
        more = findViewById(R.id.more);
        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

        edittext_layout.requestFocus();
        mEditTextContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_active);
                } else {
                    edittext_layout
                            .setBackgroundResource(R.drawable.input_bar_bg_normal);
                }

            }
        });
        mEditTextContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                edittext_layout
                        .setBackgroundResource(R.drawable.input_bar_bg_active);
                more.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
        // 监听文字框
        mEditTextContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!TextUtils.isEmpty(s)) {
                    btnMore.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @SuppressLint("InvalidWakeLockTag")
    private void setUpView() {
        activityInstance = this;
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Name = getIntent().getStringExtra(Constants.NAME);
        img_right.setVisibility(View.VISIBLE);
        toChatUsername = getIntent().getStringExtra(Constants.User_ID);
        img_right.setImageResource(R.drawable.icon_chat_user);
        txt_title.setText(Name);
        conversation = ConversationUtil.activeConversation(toChatUsername);
        adapter = new MessageAdapter(this, toChatUsername);
        // 显示消息
        listView.setAdapter(adapter);
        int count = listView.getCount();
        if (count > 0) {
            listView.setSelection(count);
        }

        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                more.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
        });
    }

    protected void setListener() {
        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.view_camera).setOnClickListener(this);
        findViewById(R.id.view_file).setVisibility(View.GONE);
        findViewById(R.id.view_video).setVisibility(View.GONE);
        findViewById(R.id.view_photo).setOnClickListener(this);
        findViewById(R.id.view_location).setVisibility(View.GONE);
        findViewById(R.id.view_audio).setVisibility(View.GONE);
        img_right.setOnClickListener(this);
    }

    /**
     * onActivityResult
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnContainer.setVisibility(View.GONE);
        listView.setSelection(listView.getCount());
        if (resultCode == ChatActivity.RESULT_CODE_EXIT_GROUP) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        if (requestCode == ChatActivity.REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
                case ChatActivity.RESULT_CODE_COPY: // 复制消息
                    break;
                default:
                    break;
            }
        }
        if (resultCode == RESULT_OK) { // 清空消息
            if (requestCode == ChatActivity.REQUEST_CODE_CAMERA) { // 发送照片
                if (cameraFile != null && cameraFile.exists())
                    sendPicture(cameraFile.getAbsolutePath());
            } else if (requestCode == ChatActivity.REQUEST_CODE_VIDEO) { // 发送本地选择的视频
                if (data != null) {
                    String filePath = data.getStringExtra("filePath");
                    if (filePath != null) {
//                        sendFile(filePath);
                    }
                }
            } else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            } else if (requestCode == ChatActivity.REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
//                        sendFile(uri);
                    }
                }
            } else if (conversation.getMsgCount() > 0) {
                adapter.refresh();
                setResult(RESULT_OK);
            } else if (requestCode == ChatActivity.REQUEST_CODE_GROUP_DETAIL) {
                adapter.refresh();
            }
        }
    }

    /**
     * 消息图标点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        hideKeyboard();
        int id = view.getId();
        switch (id) {
            case R.id.img_back:
                Utils.finish(MainServerChatActivity.this);
                break;
            case R.id.view_camera:
                selectPicFromCamera();// 点击照相图标
                more.setVisibility(View.GONE);
                break;
            case R.id.view_file:
                // 发送文件
                selectFileFromLocal();
                more.setVisibility(View.GONE);
                break;
            case R.id.view_video:
                startActivityForResult(new Intent(this, ChatVideoActivity.class), ChatActivity.REQUEST_CODE_VIDEO);
                more.setVisibility(View.GONE);
                break;
            case R.id.view_photo:
                selectPicFromLocal(); // 点击图片图标
                more.setVisibility(View.GONE);
                break;
            case R.id.view_location:
                Intent intent = new Intent(this, VideoCallActivity.class);
                intent.putExtra(Constants.VIDEO_TYPE, Constants.VIDEO_ASK);
                intent.putExtra(Constants.NAME, Name);
                startActivity(intent);
                more.setVisibility(View.GONE);
                break;
            case R.id.view_audio:
                // 语音通话
                Intent intent1 = new Intent(this, VoiceCallActivity.class);
                intent1.putExtra(Constants.VOICE_TYPE, Constants.VOICE_ASK);
                intent1.putExtra(Constants.NAME, Name);
                startActivity(intent1);
                more.setVisibility(View.GONE);
                break;
            case R.id.btn_send:
                // 点击发送按钮(发文字和表情)
                String s = mEditTextContent.getText().toString();
                sendText(s);
                break;
            default:
                break;
        }
    }

    /**
     * 照相获取图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            String st = getResources().getString(
                    R.string.sd_card_does_not_exist);
            Toast.makeText(getApplicationContext(), st, Toast.LENGTH_LONG).show();
            return;
        }

        cameraFile = new File(FileUtil.getSDPath(), FileUtil.getSubPolicePath()
                + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = "com.bairock.zhongchuan.qz.fileprovider"; //【清单文件中provider的authorities属性的值】
            uri = FileProvider.getUriForFile(this, authority, cameraFile);
        } else {
            uri = Uri.fromFile(cameraFile);
        }
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, uri),
                ChatActivity.REQUEST_CODE_CAMERA);
    }

    /**
     * 选择文件
     */
    private void selectFileFromLocal() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ChatActivity.REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * 发送文本消息
     *
     * @param content
     *            message content
     */
    private void sendText(String content) {
        if (content.length() > 0) {
            MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.TXT, UserUtil.user.getUsername(), toChatUsername);
            messageRoot.getData().setContent(content);
            conversation.addMessage(messageRoot);
//            TcpClientUtil.send(messageRoot);
            UdpMessage udpMessage = UdpMessageHelper.createTextMessage(UserUtil.user.getUsername(), content);
			MessageBroadcaster.sendIp(udpMessage, ip);
            adapter.refresh();
            listView.setSelection(listView.getCount() - 1);
            mEditTextContent.setText("");
            setResult(RESULT_OK);
        }
    }

    private void sendPicture(final String filePath) {
        String msgId = UUID.randomUUID().toString();
        final MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.IMAGE, UserUtil.user.getUsername(), toChatUsername);
        ZCMessage message = messageRoot.getData();
        messageRoot.setMsgId(msgId);
        message.setContent(filePath);
//		final byte[] bytes = FileUtil.getImageStream(filePath);
//		message.setStream(bytes);
//		conversation.addMessage(messageRoot);
//        TcpClientUtil.send(messageRoot);
        ConversationUtil.addSendMessage(messageRoot);
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = fis.read(b)) != -1) {
                        byte[] bytes1 = new byte[n];
                        System.arraycopy(b, 0, bytes1, 0, n);
                        UdpMessage udpMessage = UdpMessageHelper.createSubImageMessage1(UserUtil.user.getUsername(), bytes1, 1);
                        MessageBroadcaster.sendIp(udpMessage, ip);
                    }
                    UdpMessage udpMessage = UdpMessageHelper.createSubImageMessage1(UserUtil.user.getUsername(), new byte[0], 0);
                    MessageBroadcaster.sendIp(udpMessage, ip);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
//        UdpMessage udpMessage = UdpMessageHelper.createImageMessage(UserUtil.user.getUsername(), bytes);
//		MessageBroadcaster.sendIp(udpMessage, ip);

        listView.setAdapter(adapter);
        adapter.refresh();
        listView.setSelection(listView.getCount() - 1);
        setResult(RESULT_OK);
        more(more);

//        FileUploadFile uploadFile = new FileUploadFile();
//        File file = new File(filePath);
//        String fileMd5 = file.getName();// 文件名
//        uploadFile.setFrom(UserUtil.user.getUsername());
//        uploadFile.setMsgId(msgId);
//        uploadFile.setFile(file);
//        uploadFile.setFile_md5(fileMd5);
//        uploadFile.setStarPos(0);// 文件开始位置
//        new FileUploadClient().connect(FileUploadServer.PORT, ip, uploadFile);
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    private void sendPicByUri(Uri selectedImage) {
        // String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, null, null,
                null, null);
        String st8 = getResources().getString(R.string.cant_find_pictures);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendPicture(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendPicture(file.getAbsolutePath());
        }

    }

    /**
     * 显示语音图标按钮
     *
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        btnContainer.setVisibility(View.VISIBLE);

    }

    /**
     * 显示键盘图标
     *
     * @param view
     */
    public void setModeKeyboard(View view) {
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
        } else {
            more.setVisibility(View.GONE);
        }

    }

    /**
     * 点击文字输入框
     *
     * @param v
     */
    public void editClick(View v) {
        listView.setSelection(listView.getCount() - 1);
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 覆盖手机返回键
     */
    @Override
    public void onBackPressed() {
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }
}
