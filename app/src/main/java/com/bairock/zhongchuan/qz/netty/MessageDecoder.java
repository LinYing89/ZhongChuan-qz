package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.util.Log;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.enums.ClientBaseType;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.activity.LoginActivity;
import java.util.List;

import io.netty.buffer.ByteBuf;
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
        Log.e(TAG, "bytes" + Util.bytesToHexString(req));

        int memberNumber = Util.bytesToInt(new byte[]{req[0], req[1]});
        Log.e(TAG, "memberNumber: " + memberNumber);
        //过滤掉自己发的信息
        if(memberNumber == Integer.parseInt(UserUtil.user.getUsername())){
            return;
        }
        byte factionCode = req[2];
        byte errCode = req[3];
        Log.e(TAG, "factionCode: " + factionCode);
        int length = Util.bytesToInt(new byte[]{req[4], req[5]});
        byte[] data = new byte[length];
        if(length > 0){
            System.arraycopy(req, 6, data, 0, length);
        }
        switch (factionCode){
            case UdpMessageHelper.HEART:
                //自己的心跳已在上面过滤掉了
                int ip1 = data[0] & 0xff;
                int ip2 = data[1] & 0xff;
                int ip3 = data[2] & 0xff;
                int ip4 = data[3] & 0xff;
                String ip = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                int lng = UdpMessageHelper.bytesToInt(new byte[]{data[4], data[5], data[6], data[7]});
                int lat = UdpMessageHelper.bytesToInt(new byte[]{data[8], data[9], data[10], data[11]});
                double dlng = lng / 10000000d;
                double dlat = lat / 10000000d;
                UserUtil.setHeartInfo(String.valueOf(memberNumber), ip, new Location(dlng, dlat));
                break;
            case UdpMessageHelper.LOGIN:
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
                i1.putExtra(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VOICE);
                i1.putExtra(Constants.NAME, memberNumber);
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
                i3.putExtra(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
                i3.putExtra(Constants.NAME, memberNumber);
                App.getInstance().sendOrderedBroadcast(i3, ConversationUtil.CHAT_BROADCAST_PERMISSION);
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
