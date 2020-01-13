package com.bairock.zhongchuan.qz.view.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.MainActivity;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.adapter.NewMsgAdpter;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.common.NetUtil;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.view.ChatActivity;

//消息
public class FragmentMsg extends Fragment implements OnClickListener,
        OnItemClickListener {
    private View layout, layout_head;
    ;
    private ListView lvContact;
    private NewMsgAdpter adpter;
    private List<ZCConversation> conversationList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (layout == null) {
            layout = this.getActivity().getLayoutInflater().inflate(R.layout.fragment_msg,
                    null);
            lvContact = layout.findViewById(R.id.listview);
            setListener();
        } else {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        conversationList.clear();
        initViews();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        conversationList.clear();
        initViews();
    }

    private void initViews() {
        conversationList.addAll(loadConversationsWithRecentChat());
        if (conversationList != null && conversationList.size() > 0) {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.GONE);
            adpter = new NewMsgAdpter(getActivity(), conversationList);
            lvContact.setAdapter(adpter);
        } else {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取所有会话
     *
     * @return +
     */
    private List<ZCConversation> loadConversationsWithRecentChat() {

        if(ConversationUtil.conversations.size() > 0){
            return ConversationUtil.conversations;
        }

        MessageRoot<ZCMessage> messageRoot = new MessageRoot<>();
        messageRoot.setType(MessageRootType.CHAT);
        messageRoot.setFrom("8081");
        messageRoot.setTo("8080");
        messageRoot.setTime(new Date().getTime());
        messageRoot.setMsgId(UUID.randomUUID().toString());

        ZCMessage message = new ZCMessage();
        message.setContent("test");
        message.setDirect(ZCMessageDirect.RECEIVE);
        message.setMessageType(ZCMessageType.TXT);
        messageRoot.setData(message);
        ConversationUtil.addReceivedMessage(messageRoot);

        MessageRoot<ZCMessage> messageRoot1 = new MessageRoot<>();
        messageRoot1.setType(MessageRootType.CHAT);
        messageRoot1.setFrom("8082");
        messageRoot1.setTo("8080");
        messageRoot1.setTime(new Date().getTime());
        messageRoot1.setMsgId(UUID.randomUUID().toString());

        ZCMessage message1 = new ZCMessage();
        message1.setContent("hello");
        message1.setDirect(ZCMessageDirect.SEND);
        message1.setMessageType(ZCMessageType.TXT);
        messageRoot1.setData(message1);
        ConversationUtil.addReceivedMessage(messageRoot1);
        // 排序
        sortConversationByLastChatTime(ConversationUtil.conversations);
        return ConversationUtil.conversations;
    }

    /**
     * 根据最后一条消息的时间排序
     */
    private void sortConversationByLastChatTime(List<ZCConversation> conversationList) {
        Collections.sort(conversationList, new Comparator<ZCConversation>() {
            @Override
            public int compare(final ZCConversation con1,
                               final ZCConversation con2) {

                MessageRoot con2LastMessage = con2.getLastMessage();
                MessageRoot con1LastMessage = con1.getLastMessage();
                if (null == con2LastMessage || null == con1LastMessage) {
                    return 0;
                }
                return Long.compare(con2LastMessage.getTime(), con1LastMessage.getTime());
            }
        });
    }

    private void setListener() {
        lvContact.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        ((MainActivity) getActivity()).updateUnreadLabel();
        ZCConversation conversation = conversationList.get(position);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Hashtable<String, String> ChatRecord = adpter.getChatRecord();
        if (ChatRecord != null) {
            intent.putExtra(Constants.NAME, conversation.getUsername());// 设置昵称
            intent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_SINGLE);
            intent.putExtra(Constants.User_ID, conversation.getUsername());
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_error_item:
                NetUtil.openSetNetWork(getActivity());
                break;
            default:
                break;
        }
    }

}
