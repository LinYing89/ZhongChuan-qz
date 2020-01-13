package com.bairock.zhongchuan.qz.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.utils.SmileUtils;
import com.bairock.zhongchuan.qz.common.ViewHolder;
import com.bairock.zhongchuan.qz.widght.SwipeLayout;

import static com.bairock.zhongchuan.qz.bean.ZCMessageDirect.RECEIVE;

public class NewMsgAdpter extends BaseAdapter {
	protected Context context;
	private List<ZCConversation> conversationList;
	private int deleteID;
	private String ChatID;
	private String userid;
	private Hashtable<String, String> ChatRecord = new Hashtable<>();

	public NewMsgAdpter(Context ctx, List<ZCConversation> objects) {
		context = ctx;
		conversationList = objects;
	}

	public Hashtable<String, String> getChatRecord() {
		return ChatRecord;
	}

	@Override
	public int getCount() {
		return conversationList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_item_msg, parent, false);
		}
		ImageView img_avar = ViewHolder.get(convertView,
				R.id.contactitem_avatar_iv);
		TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
		TextView txt_state = ViewHolder.get(convertView, R.id.txt_state);
		TextView txt_del = ViewHolder.get(convertView, R.id.txt_del);
		TextView txt_content = ViewHolder.get(convertView, R.id.txt_content);
		TextView txt_time = ViewHolder.get(convertView, R.id.txt_time);
		TextView unreadLabel = ViewHolder.get(convertView,
				R.id.unread_msg_number);
		SwipeLayout swipe = ViewHolder.get(convertView, R.id.swipe);
			swipe.setSwipeEnabled(true);
			// 获取与此用户/群组的会话
			final ZCConversation conversation = conversationList.get(position);
			ChatID = conversation.getUsername();
					txt_name.setText(conversation.getUsername());
			if (conversation.getUnreadCount() > 0) {
				// 显示与此用户的消息未读数
				unreadLabel.setText(String.valueOf(conversation.getUnreadCount()));
				unreadLabel.setVisibility(View.VISIBLE);
			} else {
				unreadLabel.setVisibility(View.INVISIBLE);
			}
			if (conversation.getMsgCount() != 0) {
				// 把最后一条消息的内容作为item的message内容
				MessageRoot<ZCMessage> lastMessage = conversation.getLastMessage();
				txt_content.setText(
						SmileUtils.getSmiledText(context,
								getMessageDigest(lastMessage, context)),
						BufferType.SPANNABLE);
				Date date = new Date(lastMessage.getTime());
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
				txt_time.setText(dateFormat.format(date));
			}

			txt_del.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteID = position;
				}
			});
		return convertView;
	}

	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 *
	 * @param context
	 * @return
	 */
	private String getMessageDigest(MessageRoot<ZCMessage> messageRoot, Context context) {
		String digest = "";
		ZCMessage message = messageRoot.getData();
		switch (message.getMessageType()) {
		case LOCATION: // 位置消息
			if (message.getDirect() == RECEIVE) {
				digest = getStrng(context, R.string.location_recv);
				String name = messageRoot.getFrom();
				digest = String.format(digest, name);
				return digest;
			} else {
				digest = getStrng(context, R.string.location_prefix);
			}
			break;
		case IMAGE: // 图片消息
//			ImageMessageBody imageBody = (ImageMessageBody) message.getContent();
			digest = getStrng(context, R.string.picture)
					+ message.getContent();
			break;
		case VOICE:// 语音消息
			digest = getStrng(context, R.string.voice_msg);
			break;
		case VIDEO: // 视频消息
			digest = getStrng(context, R.string.video);
			break;
		case TXT: // 文本消息
			digest = message.getContent();
			break;
		case FILE: // 普通文件消息
			digest = getStrng(context, R.string.file);
			break;
		default:
			System.err.println("error, unknow type");
			return "";
		}
		return digest;
	}

	String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}
}
