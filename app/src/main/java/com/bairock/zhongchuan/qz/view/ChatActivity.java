package com.bairock.zhongchuan.qz.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.MainActivity;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.adapter.ExpressionPagerAdapter;
import com.bairock.zhongchuan.qz.adapter.MessageAdapter;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.netty.MessageBroadcaster;
import com.bairock.zhongchuan.qz.netty.file.FileUploadClient;
import com.bairock.zhongchuan.qz.netty.file.FileUploadFile;
import com.bairock.zhongchuan.qz.netty.file.FileUploadServer;
import com.bairock.zhongchuan.qz.recorderlib.utils.Logger;
import com.bairock.zhongchuan.qz.utils.CommonUtils;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.utils.FileUtil;
import com.bairock.zhongchuan.qz.utils.MyVoiceRecorder;
import com.bairock.zhongchuan.qz.utils.TcpClientUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.activity.ChatVideoActivity;
import com.bairock.zhongchuan.qz.view.activity.LoginActivity;
import com.bairock.zhongchuan.qz.view.activity.VideoCallActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceCallActivity;
import com.bairock.zhongchuan.qz.widght.PasteEditText;
import com.easemob.EMError;

//聊天页面
public class ChatActivity extends AppCompatActivity implements OnClickListener {

	private static final int REQUEST_CODE_EMPTY_HISTORY = 2;
	public static final int REQUEST_CODE_CONTEXT_MENU = 3;
	private static final int REQUEST_CODE_MAP = 4;
	public static final int REQUEST_CODE_TEXT = 5;
	public static final int REQUEST_CODE_VOICE = 6;
	public static final int REQUEST_CODE_PICTURE = 7;
	public static final int REQUEST_CODE_LOCATION = 8;
	public static final int REQUEST_CODE_NET_DISK = 9;
	public static final int REQUEST_CODE_FILE = 10;
	public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
	public static final int REQUEST_CODE_PICK_VIDEO = 12;
	public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
	public static final int REQUEST_CODE_VIDEO = 14;
	public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
	public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
	public static final int REQUEST_CODE_SEND_USER_CARD = 17;
	public static final int REQUEST_CODE_CAMERA = 18;
	public static final int REQUEST_CODE_LOCAL = 19;
	public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
	public static final int REQUEST_CODE_GROUP_DETAIL = 21;
	public static final int REQUEST_CODE_SELECT_VIDEO = 23;
	public static final int REQUEST_CODE_SELECT_FILE = 24;
	public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

	public static final int RESULT_CODE_COPY = 1;
	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;
	public static final int RESULT_CODE_OPEN = 4;
	public static final int RESULT_CODE_DWONLOAD = 5;
	public static final int RESULT_CODE_TO_CLOUD = 6;
	public static final int RESULT_CODE_EXIT_GROUP = 7;

	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	public static final String COPY_IMAGE = "EASEMOBIMG";

	public static MyHandler handler;
	private View recordingContainer;
	private ImageView micImage;
	private TextView recordingHint;
	private ListView listView;
	private PasteEditText mEditTextContent;
	private View buttonSetModeKeyboard;
	private View buttonSetModeVoice;
	private View buttonSend;
	private View buttonPressToSpeak;
	// private ViewPager expressionViewpager;
//	private LinearLayout emojiIconContainer;
	private LinearLayout btnContainer;
	// private ImageView locationImgview;
	private View more;
	private int position;
	private ClipboardManager clipboard;
//	private ViewPager expressionViewpager;
	private InputMethodManager manager;
	private List<String> reslist;
	private Drawable[] micImages;
	private int chatType;
	private ZCConversation conversation;
	private NewMessageBroadcastReceiver receiver;
	public static ChatActivity activityInstance = null;
	// 给谁发送消息
	private String Name;
	private String toChatUsername;
	private MessageAdapter adapter;
	private File cameraFile;
	static int resendPos;

	private TextView txt_title;
//	private ImageView iv_emoticons_normal;
	private ImageView img_right;
	private ImageView iv_emoticons_checked;
	private RelativeLayout edittext_layout;
	private ProgressBar loadmorePB;
	private boolean isloading;
	private final int pagesize = 20;
	private boolean haveMoreData = true;
	private Button btnMore;
	public String playMsgId;
	private AnimationDrawable animationDrawable;

	private MyVoiceRecorder voiceRecorder;
	// private EMGroup group;
	private String ip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getSupportActionBar().hide();
		initView();
		setUpView();
		setListener();

		ip = UserUtil.findIpByUsername(toChatUsername);
		if(null == ip){
			Toast.makeText(this, "对方不在线", Toast.LENGTH_SHORT).show();
			finish();
		}

		handler = new MyHandler(this);
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
		recordingContainer = findViewById(R.id.view_talk);
		img_right = findViewById(R.id.img_right);
		micImage = findViewById(R.id.mic_image);
		animationDrawable = (AnimationDrawable) micImage.getBackground();
		animationDrawable.setOneShot(false);
		recordingHint = findViewById(R.id.recording_hint);
		listView = findViewById(R.id.list);
		mEditTextContent = findViewById(R.id.et_sendmessage);
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		edittext_layout = findViewById(R.id.edittext_layout);
		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
		buttonSend = findViewById(R.id.btn_send);
		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
//		expressionViewpager = findViewById(R.id.vPager);
//		emojiIconContainer = findViewById(R.id.ll_face_container);
		btnContainer = findViewById(R.id.ll_btn_container);
		// locationImgview = (ImageView) findViewById(R.id.btn_location);
//		iv_emoticons_normal = findViewById(R.id.iv_emoticons_normal);
//		iv_emoticons_checked = findViewById(R.id.iv_emoticons_checked);
		loadmorePB = findViewById(R.id.pb_load_more);
		btnMore = findViewById(R.id.btn_more);
//		iv_emoticons_normal.setVisibility(View.VISIBLE);
//		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		more = findViewById(R.id.more);
		edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

		// 表情list
		reslist = getExpressionRes(62);
		// 初始化表情viewpager
		List<View> views = new ArrayList<>();
//		expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
		edittext_layout.requestFocus();
		voiceRecorder = new MyVoiceRecorder();
		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

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
		mEditTextContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edittext_layout
						.setBackgroundResource(R.drawable.input_bar_bg_active);
				more.setVisibility(View.GONE);
//				iv_emoticons_normal.setVisibility(View.VISIBLE);
//				iv_emoticons_checked.setVisibility(View.INVISIBLE);
//				emojiIconContainer.setVisibility(View.GONE);
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
//		iv_emoticons_normal.setOnClickListener(this);
//		iv_emoticons_checked.setOnClickListener(this);
		// position = getIntent().getIntExtra("position", -1);
		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
		// 判断单聊还是群聊
		chatType = getIntent().getIntExtra(Constants.TYPE, CHATTYPE_SINGLE);
		Name = getIntent().getStringExtra(Constants.NAME);
		img_right.setVisibility(View.VISIBLE);
		toChatUsername = getIntent().getStringExtra(Constants.User_ID);
		img_right.setImageResource(R.drawable.icon_chat_user);
		txt_title.setText(Name);
		conversation = ConversationUtil.activeConversation(toChatUsername);
		// 把此会话的未读数置为0
//		conversation.setUnreadCount(0);
		adapter = new MessageAdapter(this, toChatUsername);
		// 显示消息
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new ListScrollListener());
		int count = listView.getCount();
		if (count > 0) {
			listView.setSelection(count);
		}

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				more.setVisibility(View.GONE);
//				iv_emoticons_normal.setVisibility(View.VISIBLE);
//				iv_emoticons_checked.setVisibility(View.INVISIBLE);
//				emojiIconContainer.setVisibility(View.GONE);
				btnContainer.setVisibility(View.GONE);
				return false;
			}
		});
		// 注册接收消息广播
		receiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(ConversationUtil.CHAT_ACTION);
		// 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);
	}

	protected void setListener() {
		findViewById(R.id.img_back).setVisibility(View.VISIBLE);
		findViewById(R.id.img_back).setOnClickListener(this);
		findViewById(R.id.view_camera).setOnClickListener(this);
		findViewById(R.id.view_file).setOnClickListener(this);
		findViewById(R.id.view_video).setOnClickListener(this);
		findViewById(R.id.view_photo).setOnClickListener(this);
		findViewById(R.id.view_location).setOnClickListener(this);
		findViewById(R.id.view_audio).setOnClickListener(this);
		findViewById(R.id.img_back).setOnClickListener(this);
		img_right.setOnClickListener(this);
	}

	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		btnContainer.setVisibility(View.GONE);
		listView.setSelection(listView.getCount());
		if (resultCode == RESULT_CODE_EXIT_GROUP) {
			setResult(RESULT_OK);
			finish();
			return;
		}
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
			case RESULT_CODE_COPY: // 复制消息
//				EMMessage copyMsg = ((EMMessage) adapter.getItem(data
//						.getIntExtra("position", -1)));
//				// clipboard.setText(SmileUtils.getSmiledText(ChatActivity.this,
//				// ((TextMessageBody) copyMsg.getBody()).getMessage()));
//				clipboard.setText(((TextMessageBody) copyMsg.getBody())
//						.getMessage());
				break;
			default:
				break;
			}
		}
		if (resultCode == RESULT_OK) { // 清空消息
			if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {
				// 清空会话
				adapter.refresh();
			} else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
				if (cameraFile != null && cameraFile.exists())
					sendPicture(cameraFile.getAbsolutePath());
			} else if (requestCode == REQUEST_CODE_VIDEO) { // 发送本地选择的视频
				if (data != null) {
					String filePath = data.getStringExtra("filePath");
					if (filePath != null) {
						sendFile(filePath);
					}
				}
			} else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						sendPicByUri(selectedImage);
					}
				}
			} else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						sendFile(uri);
					}
				}

			} else if (requestCode == REQUEST_CODE_MAP) { // 地图
				double latitude = data.getDoubleExtra("latitude", 0);
				double longitude = data.getDoubleExtra("longitude", 0);
				String locationAddress = data.getStringExtra("address");
				if (locationAddress != null && !locationAddress.equals("")) {
					more(more);
					sendLocationMsg(latitude, longitude, "", locationAddress);
				} else {
					String st = getResources().getString(
							R.string.unable_to_get_loaction);
					Toast.makeText(this, st, Toast.LENGTH_LONG).show();
				}
			} else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
				// 粘贴
				if (!TextUtils.isEmpty(clipboard.getText())) {
					String pasteText = clipboard.getText().toString();
					if (pasteText.startsWith(COPY_IMAGE)) {
						// 把图片前缀去掉，还原成正常的path
						sendPicture(pasteText.replace(COPY_IMAGE, ""));
					}

				}
			} else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) { // 移入黑名单
//				EMMessage deleteMsg = (EMMessage) adapter.getItem(data
//						.getIntExtra("position", -1));
//				addUserToBlacklist(deleteMsg.getFrom());
			} else if (conversation.getMsgCount() > 0) {
				adapter.refresh();
				setResult(RESULT_OK);
			} else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
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
			Utils.finish(ChatActivity.this);
			break;
		case R.id.img_right:
			if (chatType == CHATTYPE_SINGLE) { // 单聊
//				Utils.start_Activity(this, FriendMsgActivity.class,
//						new BasicNameValuePair(Constants.User_ID,
//								toChatUsername), new BasicNameValuePair(
//								Constants.NAME, Name));
			} else {
			}
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
			startActivityForResult(new Intent(this, ChatVideoActivity.class), REQUEST_CODE_VIDEO);
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
//		case R.id.iv_emoticons_normal:
			// 点击显示表情框
//			more.setVisibility(View.VISIBLE);
//			iv_emoticons_normal.setVisibility(View.INVISIBLE);
//			iv_emoticons_checked.setVisibility(View.VISIBLE);
//			btnContainer.setVisibility(View.GONE);
//			emojiIconContainer.setVisibility(View.VISIBLE);
//			hideKeyboard();
//			break;
//		case R.id.iv_emoticons_checked:// 点击隐藏表情框
//			iv_emoticons_normal.setVisibility(View.VISIBLE);
//			iv_emoticons_checked.setVisibility(View.INVISIBLE);
//			btnContainer.setVisibility(View.VISIBLE);
//			emojiIconContainer.setVisibility(View.GONE);
//			more.setVisibility(View.GONE);
//			break;
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
				REQUEST_CODE_CAMERA);
	}

	/**
	 * 选择文件
	 */
	private void selectFileFromLocal() {
		Intent intent = null;
//		if (Build.VERSION.SDK_INT < 19) {
//			intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("*/*");
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);

//		} else {
//			intent = new Intent(
//					Intent.ACTION_PICK,
//					MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		}
        intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
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
			TcpClientUtil.send(messageRoot);
//			MessageBroadcaster.send(messageRoot);
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			mEditTextContent.setText("");
			setResult(RESULT_OK);
		}
	}

	/**
	 * 发送语音
	 * 
	 * @param filePath
	 * @param length
	 */
	private void sendVoice(String filePath, String length) {
		if (!(new File(filePath).exists())) {
			return;
		}
		String msgId = UUID.randomUUID().toString();
		final MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.VOICE, UserUtil.user.getUsername(), toChatUsername);
		ZCMessage message = messageRoot.getData();
		messageRoot.setMsgId(msgId);
		message.setContent(filePath);
//		byte[] bytes = FileUtil.getImageStream(filePath);
//		message.setStream(bytes);
		TcpClientUtil.send(messageRoot);
		ConversationUtil.addSendMessage(messageRoot);

		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);

		FileUploadFile uploadFile = new FileUploadFile();
		File file = new File(filePath);
		String fileMd5 = file.getName();// 文件名
		uploadFile.setFrom(UserUtil.user.getUsername());
		uploadFile.setMsgId(msgId);
		uploadFile.setFile(file);
		uploadFile.setFile_md5(fileMd5);
		uploadFile.setStarPos(0);// 文件开始位置
		new FileUploadClient().connect(FileUploadServer.PORT, ip, uploadFile);

	}

	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
//	private void sendPicture(final String filePath) {
//		final MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.IMAGE, UserUtil.user.getUsername(), toChatUsername);
//		ZCMessage message = messageRoot.getData();
//		message.setContent(filePath);
//		byte[] bytes = FileUtil.getImageStream(filePath);
//		message.setStream(bytes);
////		conversation.addMessage(messageRoot);
//		TcpClientUtil.send(messageRoot);
//		ConversationUtil.addSendMessage(messageRoot);
////		MessageBroadcaster.send(messageRoot);
//
//		listView.setAdapter(adapter);
//		adapter.refresh();
//		listView.setSelection(listView.getCount() - 1);
//		setResult(RESULT_OK);
//		// more(more);
//	}

	private void sendPicture(final String filePath) {
		String msgId = UUID.randomUUID().toString();
		final MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.IMAGE, UserUtil.user.getUsername(), toChatUsername);
		ZCMessage message = messageRoot.getData();
		messageRoot.setMsgId(msgId);
		message.setContent(filePath);
//		byte[] bytes = FileUtil.getImageStream(filePath);
//		message.setStream(bytes);
//		conversation.addMessage(messageRoot);
		TcpClientUtil.send(messageRoot);
		ConversationUtil.addSendMessage(messageRoot);
//		MessageBroadcaster.send(messageRoot);

		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);
		 more(more);
		FileUploadFile uploadFile = new FileUploadFile();
		File file = new File(filePath);
		String fileMd5 = file.getName();// 文件名
		uploadFile.setFrom(UserUtil.user.getUsername());
		uploadFile.setMsgId(msgId);
		uploadFile.setFile(file);
		uploadFile.setFile_md5(fileMd5);
		uploadFile.setStarPos(0);// 文件开始位置
		new FileUploadClient().connect(FileUploadServer.PORT, ip, uploadFile);
	}

	/**
	 * 发送视频消息
	 */
	private void sendVideo(final String filePath, final String thumbPath,
			final int length) {
		final File videoFile = new File(filePath);
		if (!videoFile.exists()) {
			return;
		}
		try {
			final MessageRoot<ZCMessage> message = ConversationUtil.createSendMessage(ZCMessageType.VIDEO, "8090", "8090");
			conversation.addMessage(message);
			listView.setAdapter(adapter);
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			setResult(RESULT_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	 * 发送位置信息
	 * 
	 * @param latitude
	 * @param longitude
	 * @param imagePath
	 * @param locationAddress
	 */
	private void sendLocationMsg(double latitude, double longitude,
			String imagePath, String locationAddress) {
		final MessageRoot<ZCMessage> message = ConversationUtil.createSendMessage(ZCMessageType.LOCATION, "8090", "8090");
		conversation.addMessage(message);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);

	}

	/**
	 * 发送文件
	 * 
	 * @param uri
	 */
	private void sendFile(Uri uri) {
		String filePath = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {MediaStore.Images.Media.DATA};
			Cursor cursor;

			try {
				cursor = getContentResolver().query(uri, projection, null,
						null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(column_index);
                    }
                    cursor.close();
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			filePath = uri.getPath();
		}
		File file = new File(filePath);
		if (!file.exists()) {
			String st7 = getResources().getString(R.string.File_does_not_exist);
			Toast.makeText(getApplicationContext(), st7, Toast.LENGTH_LONG).show();
			return;
		}
		if (file.length() > 10 * 1024 * 1024) {
			String st6 = getResources().getString(
					R.string.The_file_is_not_greater_than_10_m);
			Toast.makeText(getApplicationContext(), st6, Toast.LENGTH_LONG).show();
			return;
		}

		// 创建一个文件消息
		sendFile(filePath);
	}

	private void sendFile(String filePath){
		String msgId = UUID.randomUUID().toString();
		final MessageRoot<ZCMessage> messageRoot = ConversationUtil.createSendMessage(ZCMessageType.VIDEO, UserUtil.user.getUsername(), toChatUsername);
		ZCMessage message = messageRoot.getData();
		message.setContent(filePath);
		messageRoot.setMsgId(msgId);
//		byte[] bytes = FileUtil.getImageStream(filePath);
//		message.setStream(bytes);
		TcpClientUtil.send(messageRoot);
		conversation.addMessage(messageRoot);
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);
		more(more);

		FileUploadFile uploadFile = new FileUploadFile();
		File file = new File(filePath);
		String fileMd5 = file.getName();// 文件名
		uploadFile.setFrom(UserUtil.user.getUsername());
		uploadFile.setMsgId(msgId);
		uploadFile.setFile(file);
		uploadFile.setFile_md5(fileMd5);
		uploadFile.setStarPos(0);// 文件开始位置
		new FileUploadClient().connect(FileUploadServer.PORT, ip, uploadFile);
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
//		iv_emoticons_normal.setVisibility(View.VISIBLE);
//		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		btnContainer.setVisibility(View.VISIBLE);
//		emojiIconContainer.setVisibility(View.GONE);

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
	 * 点击清空聊天记录
	 * 
	 * @param view
	 */
	public void emptyHistory(View view) {
		String st5 = getResources().getString(
				R.string.Whether_to_empty_all_chats);
		startActivityForResult(
				new Intent(this, AlertDialog.class)
						.putExtra("titleIsCancel", true).putExtra("msg", st5)
						.putExtra("cancel", true), REQUEST_CODE_EMPTY_HISTORY);
	}

	/**
	 * 显示或隐藏图标按钮页
	 * 
	 * @param view
	 */
	public void more(View view) {
		if (more.getVisibility() == View.GONE) {
			System.out.println("more gone");
			hideKeyboard();
			more.setVisibility(View.VISIBLE);
			btnContainer.setVisibility(View.VISIBLE);
//			emojiIconContainer.setVisibility(View.GONE);
		} else {
//			if (emojiIconContainer.getVisibility() == View.VISIBLE) {
//				emojiIconContainer.setVisibility(View.GONE);
//				btnContainer.setVisibility(View.VISIBLE);
//				iv_emoticons_normal.setVisibility(View.VISIBLE);
//				iv_emoticons_checked.setVisibility(View.INVISIBLE);
//			} else {
				more.setVisibility(View.GONE);
//			}

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
//			iv_emoticons_normal.setVisibility(View.VISIBLE);
//			iv_emoticons_checked.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * 消息广播接收者
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 记得把广播给终结掉
			abortBroadcast();

			Log.e("ChatActivity", "receiver");
			String username = intent.getStringExtra("from");
//			String msgid = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
//			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
			if (!username.equals(toChatUsername)) {
				// 消息不是发给当前会话，return
				// notifyNewMessage(message);
				return;
			}
			conversation.setUnreadCount(0);
			// 通知adapter有新消息，更新ui
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
		}
	}

	private PowerManager.WakeLock wakeLock;

	/**
	 * 按住说话listener
	 * 
	 */
	class PressToSpeakListen implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				animationDrawable.start();
				if (!CommonUtils.isExitsSdcard()) {
					String st4 = getResources().getString(
							R.string.Send_voice_need_sdcard_support);
					Toast.makeText(ChatActivity.this, st4, Toast.LENGTH_SHORT)
							.show();
					return false;
				}
				try {
					v.setPressed(true);
					wakeLock.acquire(60000);
					recordingContainer.setVisibility(View.VISIBLE);
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
					voiceRecorder.startRecording();
				} catch (Exception e) {
					e.printStackTrace();
					v.setPressed(false);
					if (wakeLock.isHeld())
						wakeLock.release();
					recordingContainer.setVisibility(View.INVISIBLE);
					if (voiceRecorder != null) {
						voiceRecorder.discardRecording();
					}
					Toast.makeText(ChatActivity.this, R.string.recoding_fail,
							Toast.LENGTH_SHORT).show();
					return false;
				}

				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					recordingHint.setText(getString(R.string.release_to_cancel));
					recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
				} else {
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
					animationDrawable.start();
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				if (animationDrawable.isRunning()) {
					animationDrawable.stop();
				}
				v.setPressed(false);
				recordingContainer.setVisibility(View.INVISIBLE);
				if (wakeLock.isHeld())
					wakeLock.release();
				if (event.getY() < 0) {
					// discard the recorded audio.
					voiceRecorder.discardRecording();

				} else {
					// stop recording and send voice file
					String st1 = getResources().getString(
							R.string.Recording_without_permission);
					String st2 = getResources().getString(
							R.string.The_recording_time_is_too_short);
					String st3 = getResources().getString(
							R.string.send_failure_please);
					try {
						int length = voiceRecorder.stopRecoding();
						if (length > 0) {
							sendVoice(voiceRecorder.getVoiceFilePath(), Integer.toString(length));
						} else if (length == EMError.INVALID_FILE) {
							Toast.makeText(getApplicationContext(), st1,
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), st2,
									Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(ChatActivity.this, st3,
								Toast.LENGTH_SHORT).show();
					}

				}
				return true;
			default:
				recordingContainer.setVisibility(View.INVISIBLE);
				return false;
			}
		}
	}

	public List<String> getExpressionRes(int getSum) {
		List<String> reslist = new ArrayList<>();
		for (int x = 0; x <= getSum; x++) {
			String filename = "f_static_0" + x;

			reslist.add(filename);

		}
		return reslist;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityInstance = null;
		// 注销广播
		try {
			unregisterReceiver(receiver);
			receiver = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.refresh();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (wakeLock.isHeld())
			wakeLock.release();
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
//			iv_emoticons_normal.setVisibility(View.VISIBLE);
//			iv_emoticons_checked.setVisibility(View.INVISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * listview滑动监听listener
	 * 
	 */
	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (view.getFirstVisiblePosition() == 0 && !isloading
						&& haveMoreData) {
					loadmorePB.setVisibility(View.VISIBLE);
					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
					List<ZCMessage> messages = new ArrayList<>();
					try {
						// 获取更多messges，调用此方法的时候从db获取的messages
						// sdk会自动存入到此conversation中
//						if (chatType == CHATTYPE_SINGLE)
//							messages = conversation.loadMoreMsgFromDB(adapter
//									.getItem(0).getMsgId(), pagesize);
//						else
//							messages = conversation.loadMoreGroupMsgFromDB(
//									adapter.getItem(0).getMsgId(), pagesize);
					} catch (Exception e1) {
						loadmorePB.setVisibility(View.GONE);
						return;
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
					if (messages.size() != 0) {
						// 刷新ui
						adapter.notifyDataSetChanged();
						listView.setSelection(messages.size() - 1);
						if (messages.size() != pagesize)
							haveMoreData = false;
					} else {
						haveMoreData = false;
					}
					loadmorePB.setVisibility(View.GONE);
					isloading = false;

				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

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

	public static class MyHandler extends Handler {
		WeakReference<ChatActivity> mActivity;

		MyHandler(ChatActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final ChatActivity theActivity = mActivity.get();
			theActivity.adapter.refresh();

		}
	}
}
