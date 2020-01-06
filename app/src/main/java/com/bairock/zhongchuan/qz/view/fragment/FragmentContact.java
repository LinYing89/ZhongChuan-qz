package com.bairock.zhongchuan.qz.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.MapView;
import com.bairock.zhongchuan.qz.GloableParams;
import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.bean.User;
import com.bairock.zhongchuan.qz.dialog.ActionItem;
import com.bairock.zhongchuan.qz.dialog.TitlePopup;
import com.bairock.zhongchuan.qz.view.activity.SettingsActivity;

//通讯录

public class FragmentContact extends Fragment {

	private MapView mapView = null;
	private LinearLayout layoutGroupMember;
	private View layout;
	private ImageView imgHead;
	private TitlePopup groupPopup;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (layout == null) {
			layout = this.getLayoutInflater().inflate(R.layout.fragment_contact,
					null);
			findViews();
			setListener();
			initPopWindow();
			mapView.onCreate(savedInstanceState);
		} else {
			ViewGroup parent = (ViewGroup) layout.getParent();
			if (parent != null) {
				parent.removeView(layout);
			}
		}
		return layout;
	}

	private void findViews() {
		mapView = layout.findViewById(R.id.map);
		layoutGroupMember = layout.findViewById(R.id.layoutGroupMember);
		imgHead = layout.findViewById(R.id.imgHead);
	}

	@Override
	public void onDestroy() {
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
		groupPopup.addAction(new ActionItem(activity, "成员1"));
		groupPopup.addAction(new ActionItem(activity, "成员2"));
		groupPopup.addAction(new ActionItem(activity, "成员3"));
		groupPopup.addAction(new ActionItem(activity, "成员4"));
	}

	private TitlePopup.OnItemOnClickListener onitemClick = new TitlePopup.OnItemOnClickListener() {

		@Override
		public void onItemClick(ActionItem item, int position) {

		}
	};
}
