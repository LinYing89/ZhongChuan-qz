package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.os.Bundle;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.bean.ZCMessageDirect;
import com.bairock.zhongchuan.qz.bean.ZCMessageType;
import com.bairock.zhongchuan.qz.enums.ClientBaseType;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.activity.LoginActivity;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private static final String TAG = "MessageDecoder";

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
//        ByteBuf data = msg.content(); // 获取对DatagramPacket 中的数据（ByteBuf）的引用
//        String strMsg = data.toString();
        ByteBuf byteBuf = msg.copy().content();
        if(byteBuf.readableBytes() < 6){
            return;
        }
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        byteBuf.release();
//        Log.e(TAG, "bytes " + Util.bytesToHexString(req));

        int memberNumber = Util.bytesToInt(new byte[]{req[0], req[1]});
//        Log.e(TAG, "memberNumber: " + memberNumber);
        //过滤掉自己发的信息
        if(UserUtil.user.getUsername() == null){
            return;
        }
        if(memberNumber == Integer.parseInt(UserUtil.user.getUsername())){
            return;
        }
        byte factionCode = req[2];
        byte errCode = req[3];
//        Log.e(TAG, "factionCode: " + factionCode);
        int length = Util.bytesToInt(new byte[]{req[4], req[5]});
        byte[] data = new byte[length];
        if(length > 0){
            System.arraycopy(req, 6, data, 0, length);
        }
        switch (factionCode){
            case UdpMessageHelper.HEART:
                //自己的心跳已在上面过滤掉了
                int userType = data[0];
                int ip1 = data[1] & 0xff;
                int ip2 = data[2] & 0xff;
                int ip3 = data[3] & 0xff;
                int ip4 = data[4] & 0xff;
                String ip = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                int lng = UdpMessageHelper.bytesToInt(new byte[]{data[5], data[6], data[7], data[8]});
                int lat = UdpMessageHelper.bytesToInt(new byte[]{data[9], data[10], data[11], data[12]});
                double dlng = lng / 10000000d;
                double dlat = lat / 10000000d;
                UserUtil.setHeartInfo(String.valueOf(memberNumber), userType, ip, new Location(dlng, dlat));
                break;
            case UdpMessageHelper.LOGIN_ANS:
                handleLogin(errCode, data);
                break;
            case UdpMessageHelper.VOICE_CALL_ANS:
                // 语音通话应答
                String result = String.valueOf(errCode);
                //发送应答广播
                Intent i2 = new Intent(ConversationUtil.VOICE_ANS_ACTION);
                i2.putExtra("result", result);
                App.getInstance().sendOrderedBroadcast(i2, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VOICE_CALL_ASK :
                //语音通话请求
                Intent i1 = new Intent(ConversationUtil.VOICE_ASK_ACTION);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VOICE);
                bundle.putString(Constants.NAME, String.valueOf(memberNumber));
                bundle.putInt(Constants.CLIENT_TYPE, 1);
                i1.putExtra("myBundle", bundle);
                App.getInstance().sendOrderedBroadcast(i1, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_ANS:
                // 语音通话应答
                String result1 = String.valueOf(errCode);
                //发送应答广播
                Intent i = new Intent(ConversationUtil.VIDEO_ANS_ACTION);
                i.putExtra("result", result1);
                App.getInstance().sendOrderedBroadcast(i, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_ASK :
                //语音通话请求
                Intent i3 = new Intent(ConversationUtil.VIDEO_ASK_ACTION);
                Bundle bundle1 = new Bundle();
                bundle1.putString(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
                bundle1.putString(Constants.NAME, String.valueOf(memberNumber));
                bundle1.putInt(Constants.CLIENT_TYPE, 1);
                i3.putExtra("myBundle", bundle1);
                App.getInstance().sendOrderedBroadcast(i3, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_THIRD_ANS:
                // 第三方设备视频流推送应答
                result = String.valueOf(errCode);
                //发送应答广播
                Intent intent = new Intent(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
                intent.putExtra("result", result);
                intent.putExtra("source", "third");
                App.getInstance().sendOrderedBroadcast(intent, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VOICE_CALL_THIRD_ANS:
                // 第三方设备音频流推送应答
                result = String.valueOf(errCode);
                //发送应答广播
                intent = new Intent(ConversationUtil.VOICE_UPLOAD_ANS_ACTION);
                intent.putExtra("result", result);
                intent.putExtra("source", "third");
                App.getInstance().sendOrderedBroadcast(intent, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_MAIN_SERVER_ANS:
                // 信息处理终端视频流推送应答
                String result2 = String.valueOf(errCode);
                //发送应答广播
                Intent i4 = new Intent(ConversationUtil.VIDEO_UPLOAD_ANS_ACTION);
                i4.putExtra("result", result2);
                i4.putExtra("source", "main");
                App.getInstance().sendOrderedBroadcast(i4, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VOICE_CALL_MAIN_SERVER_ANS:
                // 信息处理终端音频流推送应答
                String result3 = String.valueOf(errCode);
                //发送应答广播
                Intent i5 = new Intent(ConversationUtil.VOICE_UPLOAD_ANS_ACTION);
                i5.putExtra("result", result3);
                i5.putExtra("source", "main");
                App.getInstance().sendOrderedBroadcast(i5, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.MAIN_SERVER_TEXT_MESSAGE:
                String text = new String(data, StandardCharsets.UTF_8);
                MessageRoot<ZCMessage> messageRoot = new MessageRoot<>();
                messageRoot.setFrom(String.valueOf(memberNumber));
                messageRoot.setTime(new Date().getTime());
                messageRoot.setTo(UserUtil.user.getUsername());
                messageRoot.setType(MessageRootType.CHAT);
                ZCMessage zcMessage = new ZCMessage();
                messageRoot.setData(zcMessage);
                zcMessage.setContent(text);
                zcMessage.setDirect(ZCMessageDirect.RECEIVE);
                zcMessage.setMessageType(ZCMessageType.TXT);
                zcMessage.setUnread(true);

                ConversationUtil.addReceivedMessage(messageRoot);
                Intent i6 = new Intent(ConversationUtil.CHAT_ACTION);
                i6.putExtra("from", messageRoot.getFrom());
                i6.putExtra("content", messageRoot.getData().getContent());
                App.getInstance().sendOrderedBroadcast(i6, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                UdpMessage udpMessage = UdpMessageHelper.createMainServerTextMessageAns(UserUtil.user.getUsername(), 0);
                byte[] bytes = UdpMessageHelper.createBytes(udpMessage);
                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),
                        msg.sender()));
                break;
            case UdpMessageHelper.MAIN_SERVER_VIDEO_CALL_ASK:
                UdpMessage udpMessage2 = UdpMessageHelper.createMainServerVideoCallAns(UserUtil.user.getUsername(), 0);
                byte[] bytes2 = UdpMessageHelper.createBytes(udpMessage2);
                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes2),
                        msg.sender()));

                Intent i7 = new Intent(ConversationUtil.VIDEO_ASK_ACTION);
                Bundle bundle7 = new Bundle();
                bundle7.putString(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
                bundle7.putString(Constants.NAME, String.valueOf(memberNumber));
                bundle7.putInt(Constants.CLIENT_TYPE, 5);
                i7.putExtra("myBundle", bundle7);
                App.getInstance().sendOrderedBroadcast(i7, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.MAIN_SERVER_VOICE_CALL_ASK:
                UdpMessage udpMessage3 = UdpMessageHelper.createMainServerVoiceCallAns(UserUtil.user.getUsername(), 0);
                byte[] bytes3 = UdpMessageHelper.createBytes(udpMessage3);
                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes3),
                        msg.sender()));

                Intent i8 = new Intent(ConversationUtil.VOICE_ASK_ACTION);
                Bundle bundle8 = new Bundle();
                bundle8.putString(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VOICE);
                bundle8.putString(Constants.NAME, String.valueOf(memberNumber));
                bundle8.putInt(Constants.CLIENT_TYPE, 5);
                i8.putExtra("myBundle", bundle8);
                App.getInstance().sendOrderedBroadcast(i8, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;

            default: break;
        }
        out.add(byteBuf);
    }

    private void handleLogin(int errCode, byte[] data){
        if(null == LoginActivity.handler){
            return;
        }

        if(errCode == 1){
            //登录失败
            LoginActivity.handler.obtainMessage(1);
        }else{
            if(data.length % 3 != 0){
                //数据个数错误, data长度不是3的倍数
                return;
            }
            int index = 0;
            while (index < data.length) {
                byte[] byMember = new byte[3];
                System.arraycopy(data, index, byMember, 0, 3);
                String username = String.valueOf(Util.bytesToInt(new byte[]{byMember[1], byMember[2]}));
                if(username.equals(UserUtil.user.getUsername())) {
                    index += 3;
                    continue;
                }
                ClientBase clientBase = new ClientBase();
                switch (byMember[0]){
                    case 0x01:
                        //手持终端
                        clientBase.setClientBaseType(ClientBaseType.PHONE);
                        break;
                    case 0x02:
                        //摄像望远镜
                        clientBase.setClientBaseType(ClientBaseType.TELESCOPE);
                        break;
                    case 0x03:
                        //无人机
                        clientBase.setClientBaseType(ClientBaseType.UAV);
                        break;
                    case 0x04:
                        //便携式录音设备
                        clientBase.setClientBaseType(ClientBaseType.SOUND_RECORDER);
                        break;
                    case 0x05:
                        //便携式录音设备
                        clientBase.setClientBaseType(ClientBaseType.MAIN_SERVER);
                        break;
                }
                if(null != clientBase.getClientBaseType()) {
                    clientBase.setUsername(username);
                    UserUtil.addClientBase(clientBase);
                }

                index += 3;
            }
            if(null != LoginActivity.handler) {
                LoginActivity.handler.obtainMessage(0).sendToTarget();
            }
        }
    }
}
