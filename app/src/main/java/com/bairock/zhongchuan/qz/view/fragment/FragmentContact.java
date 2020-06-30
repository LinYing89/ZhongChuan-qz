package com.bairock.zhongchuan.qz.view.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.bairock.zhongchuan.qz.Constants;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.bean.ClientBase;
import com.bairock.zhongchuan.qz.bean.Location;
import com.bairock.zhongchuan.qz.bean.Telescope;
import com.bairock.zhongchuan.qz.bean.UnmannedAerialVehicle;
import com.bairock.zhongchuan.qz.bean.User;
import com.bairock.zhongchuan.qz.bean.ZCConversation;
import com.bairock.zhongchuan.qz.dialog.ActionItem;
import com.bairock.zhongchuan.qz.dialog.TitlePopup;
import com.bairock.zhongchuan.qz.enums.ClientBaseType;
import com.bairock.zhongchuan.qz.utils.ConversationUtil;
import com.bairock.zhongchuan.qz.utils.SendUdpThread;
import com.bairock.zhongchuan.qz.utils.UserUtil;
import com.bairock.zhongchuan.qz.view.ChatActivity;
import com.bairock.zhongchuan.qz.view.activity.MainServerChatActivity;
import com.bairock.zhongchuan.qz.view.activity.SettingsActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceCallActivity;

//通讯录

public class FragmentContact extends Fragment {

	private MapView mapView = null;
	private AMap aMap;
	private LinearLayout layoutGroupMember;
	private View layout;
	private ImageView imgHead;
	private TextView txtName;
	private TextView txtMember;
	private TitlePopup groupPopup;
	private UserAddBroadcastReceiver receiver;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (layout == null) {
			layout = this.getLayoutInflater().inflate(R.layout.fragment_contact,
					null);
			findViews();
			setListener();
			txtName.setText(UserUtil.user.getUsername());
			txtMember.setText(UserUtil.user.getUsername());
			initPopWindow();
			mapView.onCreate(savedInstanceState);

			aMap = mapView.getMap();

			aMap.getUiSettings().setCompassEnabled(true);
			aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
			// 如果要设置定位的默认状态，可以在此处进行设置
			MyLocationStyle myLocationStyle = new MyLocationStyle();
			myLocationStyle.interval(2000);
			myLocationStyle.showMyLocation(true);
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
			//aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
			myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
			aMap.setMyLocationStyle(myLocationStyle);

			new UpdateMapThread().start();
		} else {
			ViewGroup parent = (ViewGroup) layout.getParent();
			if (parent != null) {
				parent.removeView(layout);
			}
		}
		receiver = new UserAddBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(ConversationUtil.USER_ADD);
		this.getActivity().registerReceiver(receiver, intentFilter);
		return layout;
	}

	private void findViews() {
		mapView = layout.findViewById(R.id.map);
		layoutGroupMember = layout.findViewById(R.id.layoutGroupMember);
		imgHead = layout.findViewById(R.id.imgHead);
		txtName = layout.findViewById(R.id.txtName);
		txtMember = layout.findViewById(R.id.txtMember);
	}

	@Override
	public void onDestroy() {
		try {
			this.getActivity().unregisterReceiver(receiver);
			receiver = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		mapView.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	private void setListener(){
		layoutGroupMember.setOnClickListener(onClickListener);
		imgHead.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.layoutGroupMember:
					groupPopup.show(layoutGroupMember);
					break;
				case R.id.imgHead:
					startActivity(new Intent(getActivity(), SettingsActivity.class));
					break;
				default:
					break;
			}
		}
	};

	private void initPopWindow() {
		// 实例化标题栏弹窗
		Activity activity = this.getActivity();
		assert activity != null;
		groupPopup = new TitlePopup(activity, ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		groupPopup.setItemOnClickListener(onitemClick);
		// 给标题栏弹窗添加子类
		for(ClientBase user : UserUtil.findPhoneUser()){
//			if(!user.getUsername().equals(UserUtil.user.getUsername())) {
			String name = user.getUsername();
			if(user.getClientBaseType() == ClientBaseType.MAIN_SERVER){
				name = name + "(终端)";
			}
			groupPopup.addAction(new ActionItem(activity, name, user));
//			}
		}
	}

	private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {

		@Override
		public void onItemClick(ActionItem item, int position) {
			String title = item.mTitle.toString();
			ClientBaseType clientBaseType = item.getClientBase().getClientBaseType();
			String number = item.getClientBase().getUsername();
//			if(clientBaseType == ClientBaseType.MAIN_SERVER){
//				number = title.substring(0, title.indexOf("("));
//			}
			ZCConversation conversation = ConversationUtil.activeConversation(number);
			if (null == conversation) {
				conversation = new ZCConversation(number);
				ConversationUtil.addConversation(conversation);
			}
			if(clientBaseType == ClientBaseType.MAIN_SERVER){
				Intent intent = new Intent(getActivity(), MainServerChatActivity.class);
				intent.putExtra(Constants.NAME, title);// 设置昵称
				intent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_SINGLE);
				intent.putExtra(Constants.User_ID, conversation.getUsername());
				getActivity().startActivity(intent);
			}else {
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				intent.putExtra(Constants.NAME, conversation.getUsername());// 设置昵称
				intent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_SINGLE);
				intent.putExtra(Constants.User_ID, conversation.getUsername());
				getActivity().startActivity(intent);
			}
		}
	};

	private class UpdateMapThread extends Thread{
		@Override
		public void run() {
			while (!interrupted()) {
				for (ClientBase user : UserUtil.findClientBases()) {
					Location location = user.getLocation();
					if (user.getMarker() == null) {
						if (location != null) {
							LatLng latLng = new LatLng(location.getLat(), location.getLng());
							MarkerOptions markerOption = new MarkerOptions();
							markerOption.position(latLng);
							markerOption.title(user.getUsername()).snippet(user.getUsername());
							if(user.getClientBaseType() == ClientBaseType.PHONE) {
								markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
										.decodeResource(getResources(), R.drawable.jingyuan_green)));
							}else if(user.getClientBaseType() == ClientBaseType.UAV){
								markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
										.decodeResource(getResources(), R.drawable.wurenjicaitu)));
							}else if(user.getClientBaseType() == ClientBaseType.TELESCOPE){
								markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
										.decodeResource(getResources(), R.drawable.wangyuanjing)));
							}
							// 将Marker设置为贴地显示，可以双指下拉地图查看效果
							markerOption.setFlat(true);//设置marker平贴地图效果
							final Marker marker = aMap.addMarker(markerOption);
							user.setMarker(marker);
						}
					} else {
						if (location != null) {
							user.getMarker().setPosition(new LatLng(location.getLat(), location.getLng()));
						}
					}
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class UserAddBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String username = intent.getStringExtra(Constants.NAME);
			for(ClientBase user : UserUtil.findPhoneUser()){
				String name = user.getUsername();
				if(username.equals(name)) {
					if(user.getClientBaseType() == ClientBaseType.MAIN_SERVER){
						name = name + "(终端)";
					}
					groupPopup.addAction(new ActionItem(context, name, user));
					break;
				}
			}
		}
	}
}
