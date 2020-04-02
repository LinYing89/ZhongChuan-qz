package com.bairock.zhongchuan.qz.netty;

import android.content.Intent;
import android.util.Log;

import com.bairock.zhongchuan.qz.App;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.MessageRoot;
import com.bairock.zhongchuan.qz.bean.MessageRootType;
import com.bairock.zhongchuan.qz.bean.SoundRecorder;
import com.bairock.zhongchuan.qz.bean.Telescope;
import com.bairock.zhongchuan.qz.bean.UnmannedAerialVehicle;
import com.bairock.zhongchuan.qz.bean.User;
import com.bairock.zhongchuan.qz.bean.ZCMessage;
import com.bairock.zhongchuan.qz.common.Utils;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.utils.Util;
import com.bairock.zhongchuan.qz.view.activity.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

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

        int memberNumber = Util.bytesToInt(new byte[]{req[0], req[1]});
        byte factionCode = req[2];
        byte errCode = req[3];
        int length = Util.bytesToInt(new byte[]{req[4], req[5]});
        byte[] data = new byte[length];
        if(length > 0){
            System.arraycopy(req, 6, data, 0, length);
        }
        switch (factionCode){
            case UdpMessageHelper.HEART:
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
                        ClientBase clientBase = null;
                        switch (byMember[0]){
                            case 0x01:
                                //手持终端
                                clientBase = new User();
                                break;
                            case 0x02:
                                //摄像望远镜
                                clientBase = new Telescope();
                                break;
                            case 0x03:
                                //无人机
                                clientBase = new UnmannedAerialVehicle();
                                break;
                            case 0x04:
                                //便携式录音设备
                                clientBase = new SoundRecorder();
                                break;
                        }
                        if(null != clientBase) {
                            clientBase.setUsername(username);
                            UserUtil.addClientBase(clientBase);
                        }

                        index += 3;
                    }
                    LoginActivity.handler.obtainMessage(0);
                }
                break;
            case UdpMessageHelper.VOICE_CALL_ANS:
                //语音通话请求
                Intent i1 = new Intent(ConversationUtil.VOICE_ANS_ACTION);
                i1.putExtra(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VOICE);
                i1.putExtra(Constants.NAME, memberNumber);
                App.getInstance().sendOrderedBroadcast(i1, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VOICE_CALL_ASK :
                // 语音通话应答
                String result = String.valueOf(errCode);
                //发送应答广播
                Intent i2 = new Intent(ConversationUtil.VOICE_ASK_ACTION);
                i2.putExtra("result", result);
                App.getInstance().sendOrderedBroadcast(i2, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_ANS:
                //语音通话请求
                Intent i3 = new Intent(ConversationUtil.VIDEO_ANS_ACTION);
                i3.putExtra(Constants.MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
                i3.putExtra(Constants.NAME, memberNumber);
                App.getInstance().sendOrderedBroadcast(i3, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            case UdpMessageHelper.VIDEO_CALL_ASK :
                // 语音通话应答
                String result1 = String.valueOf(errCode);
                //发送应答广播
                Intent i = new Intent(ConversationUtil.VIDEO_ASK_ACTION);
                i.putExtra("result", result1);
                App.getInstance().sendOrderedBroadcast(i, ConversationUtil.CHAT_BROADCAST_PERMISSION);
                break;
            default: break;
        }
        out.add(byteBuf);
    }
}
