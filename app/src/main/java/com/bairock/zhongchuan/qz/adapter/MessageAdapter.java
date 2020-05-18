package com.bairock.zhongchuan.qz.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import androidx.core.content.FileProvider;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.chat.LoadImageTask;
import com.bairock.zhongchuan.qz.chat.LoadVideoImageTask;
import com.bairock.zhongchuan.qz.chat.VoicePlayClickListener;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.ImageCache;
import com.bairock.zhongchuan.qz.utils.SmileUtils;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.bairock.zhongchuan.qz.view.ShowBigImage;

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;

    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    // reference to conversation object in chatsdk
    private ZCConversation conversation;

    private Context context;

    public MessageAdapter(Context context, String username) {
        this.username = username;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        this.conversation = ConversationUtil.getConversation(username);
    }

    // public void setUser(String user) {
    // this.user = user;
    // }

    /**
     * 获取item数
     */
    public int getCount() {
        return conversation.getMsgCount();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }

    public MessageRoot<ZCMessage> getItem(int position) {
        return conversation.getMessage(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        MessageRoot<ZCMessage> messageRoot = conversation.getMessage(position);
        ZCMessage message = messageRoot.getData();
        if (message.getMessageType() == ZCMessageType.TXT) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_TXT
                    : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getMessageType() == ZCMessageType.IMAGE) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE
                    : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getMessageType() == ZCMessageType.LOCATION) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION
                    : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getMessageType() == ZCMessageType.VOICE) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_VOICE
                    : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getMessageType() == ZCMessageType.VIDEO) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO
                    : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getMessageType() == ZCMessageType.FILE) {
            return message.getDirect() == ZCMessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_FILE
                    : MESSAGE_TYPE_SENT_FILE;
        }

        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 16;
    }

    private View createViewByMessage(ZCMessage message, int position) {
        switch (message.getMessageType()) {
            case LOCATION:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_location, null) : inflater
                        .inflate(R.layout.row_sent_location, null);
            case IMAGE:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_picture, null) : inflater
                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_voice, null) : inflater
                        .inflate(R.layout.row_sent_voice, null);
            case VIDEO:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_video, null) : inflater
                        .inflate(R.layout.row_sent_video, null);
            case FILE:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_file, null) : inflater
                        .inflate(R.layout.row_sent_file, null);
            default:
                return message.getDirect() == ZCMessageDirect.RECEIVE ? inflater
                        .inflate(R.layout.row_received_message, null) : inflater
                        .inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MessageRoot<ZCMessage> messageRoot = getItem(position);
        final ZCMessage message = messageRoot.getData();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getMessageType() == ZCMessageType.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_sendPicture));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getMessageType() == ZCMessageType.TXT) {

                try {
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_chatcontent);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getMessageType() == ZCMessageType.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_voice));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView
                            .findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            } else if (message.getMessageType() == ZCMessageType.LOCATION) {
                try {
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getMessageType() == ZCMessageType.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.chatting_content_iv));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView
                            .findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView
                            .findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView
                            .findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView
                            .findViewById(R.id.container_status_btn);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);

                } catch (Exception e) {
                }
            } else if (message.getMessageType() == ZCMessageType.FILE) {
                try {
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv_file_name = (TextView) convertView
                            .findViewById(R.id.tv_file_name);
                    holder.tv_file_size = (TextView) convertView
                            .findViewById(R.id.tv_file_size);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_file_download_state = (TextView) convertView
                            .findViewById(R.id.tv_file_state);
                    holder.ll_container = (LinearLayout) convertView
                            .findViewById(R.id.ll_file_container);
                    // 这里是进度值
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);
                } catch (Exception e) {
                }
                try {
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 如果是发送的消息并且不是群聊消息，显示已读textview
        if (message.getDirect() == ZCMessageDirect.SEND) {
            holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
            holder.tv_delivered = (TextView) convertView
                    .findViewById(R.id.tv_delivered);
        } else {
            // 如果是文本或者地图消息并且不是group messgae，显示的时候给对方发送已读回执
            if ((message.getMessageType() == ZCMessageType.TXT || message.getMessageType() == ZCMessageType.LOCATION)) {

            }
        }

        switch (message.getMessageType()) {
            // 根据消息type显示item
            case IMAGE: // 图片
			    handleImageMessage(messageRoot, holder, position);
                break;
            case TXT: // 文本
                handleTextMessage(message, holder, position);
                break;
            case LOCATION: // 位置
//			handleLocationMessage(message, holder, position, convertView);
                break;
            case VOICE: // 语音
                handleVoiceMessage(messageRoot, holder);
                break;
            case VIDEO: // 视频
			    handleVideoMessage(messageRoot, holder);
                break;
            case FILE: // 一般文件
                handleFileMessage(message, holder, position, convertView);
                break;
            default:
                // not supported
        }

        if (message.getDirect() == ZCMessageDirect.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            // 重发按钮点击事件
            statusView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 显示重发消息的自定义alertdialog
                    Intent intent = new Intent(activity, AlertDialog.class);
                    intent.putExtra("msg",
                            activity.getString(R.string.confirm_resend));
                    intent.putExtra("title",
                            activity.getString(R.string.resend));
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    if (message.getMessageType() == ZCMessageType.TXT)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_TEXT);
                    else if (message.getMessageType() == ZCMessageType.VOICE)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_VOICE);
                    else if (message.getMessageType() == ZCMessageType.IMAGE)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_PICTURE);
                    else if (message.getMessageType() == ZCMessageType.LOCATION)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_LOCATION);
                    else if (message.getMessageType() == ZCMessageType.FILE)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_FILE);
                    else if (message.getMessageType() == ZCMessageType.VIDEO)
                        activity.startActivityForResult(intent,
                                ChatActivity.REQUEST_CODE_VIDEO);

                }
            });
        }

        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
        if (position == 0) {
            timestamp.setText(dateFormat.format(new Date(messageRoot.getTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            if (messageRoot.getTime() - conversation.getMessage(position - 1).getTime() < 60000) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(dateFormat.format(new Date(messageRoot.getTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    /**
     * 文本消息
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(ZCMessage message, ViewHolder holder,
                                   final int position) {
//		TextMessageBody txtBody = (TextMessageBody) message.getContent();
        Spannable span = SmileUtils.getSmiledText(context, message.getContent());
        // 设置内容
        holder.tv.setText(span, BufferType.SPANNABLE);

        if (message.getDirect() == ZCMessageDirect.SEND) {
//			switch (message.status) {
//			case SUCCESS: // 发送成功
            holder.pb.setVisibility(View.GONE);
            holder.staus_iv.setVisibility(View.GONE);
//				break;
//			case FAIL: // 发送失败
//				holder.pb.setVisibility(View.GONE);
//				holder.staus_iv.setVisibility(View.VISIBLE);
//				break;
//			case INPROGRESS: // 发送中
//				holder.pb.setVisibility(View.VISIBLE);
//				holder.staus_iv.setVisibility(View.GONE);
//				break;
//			default:
//				// 发送消息
//				sendMsgInBackground(message, holder);
//			}
        }
    }

    private void handleImageMessage(MessageRoot<ZCMessage> messageRoot, ViewHolder holder,
                                   final int position) {
        final ZCMessage message = messageRoot.getData();
        if(message.getContent() == null || message.getContent().isEmpty()){
            return;
        }
        Bitmap bitmap = ImageCache.getInstance().get(message.getContent());
        if (bitmap != null) {
            holder.iv.setImageBitmap(bitmap);
            holder.iv.setClickable(true);
            holder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO 查看大图
                    System.err.println("image view on click");
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(message.getContent());
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                    }
                    activity.startActivity(intent);
                }
            });
        }else{
            new LoadImageTask().execute(message.getContent(), holder.iv, activity);
        }
    }

    private void handleVoiceMessage(final MessageRoot<ZCMessage> messageRoot,
                                    final ViewHolder holder) {
        ZCMessage message = messageRoot.getData();
//        holder.tv.setText(voiceBody.getLength() + "\"");
        holder.iv.setOnClickListener(new VoicePlayClickListener(messageRoot,
                holder.iv, holder.iv_read_status, this, activity));
        if (((ChatActivity) activity).playMsgId != null
                && ((ChatActivity) activity).playMsgId.equals(messageRoot
                .getMsgId()) && VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.getDirect() == ZCMessageDirect.RECEIVE) {
                holder.iv.setImageResource(R.drawable.voice_from_icon);
            } else {
                holder.iv.setImageResource(R.drawable.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.getDirect() == ZCMessageDirect.RECEIVE) {
                holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.drawable.chatto_voice_playing);
            }
        }

        if (message.getDirect() == ZCMessageDirect.RECEIVE) {
//            if (messageRoot.isListened()) {
//                // 隐藏语音未听标志
//                holder.iv_read_status.setVisibility(View.INVISIBLE);
//            } else {
//                holder.iv_read_status.setVisibility(View.VISIBLE);
//            }
            System.err.println("it is receive msg");
            return;
        }
    }

    private void handleVideoMessage(MessageRoot<ZCMessage> messageRoot, ViewHolder holder){
        final ZCMessage message = messageRoot.getData();
        if(message.getContent() == null || message.getContent().isEmpty()){
            return;
        }
        Bitmap bitmap = ImageCache.getInstance().get(message.getContent());
        if (bitmap != null) {
            holder.iv.setImageBitmap(bitmap);
            holder.iv.setClickable(true);
            holder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File videoFile = new File(message.getContent());
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        String authority = "com.bairock.zhongchuan.qz.fileprovider"; //【清单文件中provider的authorities属性的值】
                        uri = FileProvider.getUriForFile(activity, authority, videoFile);
                    } else {
                        uri = Uri.fromFile(videoFile);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "video/*");
                    activity.startActivity(intent);
                }
            });
        }else{
            new LoadVideoImageTask().execute(message.getContent(), holder.iv, activity);
        }

    }

    /**
     * 文件消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleFileMessage(final ZCMessage message,
                                   final ViewHolder holder, int position, View convertView) {
//		final NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message
//				.getBody();
//		final String filePath = fileMessageBody.getLocalUrl();
//		holder.tv_file_name.setText(fileMessageBody.getFileName());
//		holder.tv_file_size.setText(TextFormater.getDataSize(fileMessageBody
//				.getFileSize()));
//		holder.ll_container.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				File file = new File(filePath);
//				if (file != null && file.exists()) {
//					// 文件存在，直接打开
//					FileUtils.openFile(file, (Activity) context);
//				} else {
//					// 下载
//					// TODO 下载文件
////					context.startActivity(new Intent(context,
////							ShowNormalFileActivity.class).putExtra("msgbody",
////							fileMessageBody));
//				}
//				if (message.direct == EMMessage.Direct.RECEIVE
//						&& !message.isAcked) {
//					try {
//						EMChatManager.getInstance().ackMessageRead(
//								message.getFrom(), message.getMsgId());
//						message.isAcked = true;
//					} catch (EaseMobException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//		String st1 = context.getResources().getString(R.string.Have_downloaded);
//		String st2 = context.getResources()
//				.getString(R.string.Did_not_download);
//		if (message.direct == EMMessage.Direct.RECEIVE) { // 接收的消息
//			System.err.println("it is receive msg");
//			File file = new File(filePath);
//			if (file != null && file.exists()) {
//				holder.tv_file_download_state.setText(st1);
//			} else {
//				holder.tv_file_download_state.setText(st2);
//			}
//			return;
//		}
//
//		// until here, deal with send voice msg
//		switch (message.status) {
//		case SUCCESS:
//			holder.pb.setVisibility(View.INVISIBLE);
//			holder.tv.setVisibility(View.INVISIBLE);
//			holder.staus_iv.setVisibility(View.INVISIBLE);
//			break;
//		case FAIL:
//			holder.pb.setVisibility(View.INVISIBLE);
//			holder.tv.setVisibility(View.INVISIBLE);
//			holder.staus_iv.setVisibility(View.VISIBLE);
//			break;
//		case INPROGRESS:
//			if (timers.containsKey(message.getMsgId()))
//				return;
//			// set a timer
//			final Timer timer = new Timer();
//			timers.put(message.getMsgId(), timer);
//			timer.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					activity.runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							holder.pb.setVisibility(View.VISIBLE);
//							holder.tv.setVisibility(View.VISIBLE);
//							holder.tv.setText(message.progress + "%");
//							if (message.status == EMMessage.Status.SUCCESS) {
//								holder.pb.setVisibility(View.INVISIBLE);
//								holder.tv.setVisibility(View.INVISIBLE);
//								timer.cancel();
//							} else if (message.status == EMMessage.Status.FAIL) {
//								holder.pb.setVisibility(View.INVISIBLE);
//								holder.tv.setVisibility(View.INVISIBLE);
//								holder.staus_iv.setVisibility(View.VISIBLE);
//								Toast.makeText(
//										activity,
//										activity.getString(R.string.send_fail)
//												+ activity
//														.getString(R.string.connect_failuer_toast),
//										Toast.LENGTH_LONG).show();
//								timer.cancel();
//							}
//
//						}
//					});
//
//				}
//			}, 0, 500);
//			break;
//		default:
//			// 发送消息
//			sendMsgInBackground(message, holder);
//		}

    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
    }

}