package com.bairock.zhongchuan.qz.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.view.activity.VideoUploadActivity;

public class FragmentVideoUpload extends Fragment {

	private Activity ctx;
	private View layout;
	private Button btnLocal;
	private Button btnTelescope;
	private Button btnDrone;

//	private String username = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		username = savedInstanceState.getString("username");
		if (layout == null) {
			ctx = this.getActivity();
			layout = ctx.getLayoutInflater().inflate(R.layout.fragment_video_upload_choose,
					null);
			findViews();
			setListener();
		} else {
			ViewGroup parent = (ViewGroup) layout.getParent();
			if (parent != null) {
				parent.removeView(layout);
			}
		}
		return layout;
	}

	private void findViews() {
		btnLocal = layout.findViewById(R.id.btnLocal);
		btnTelescope = layout.findViewById(R.id.btnTelescope);
		btnDrone = layout.findViewById(R.id.btnDrone);
	}

	private void setListener() {
		btnLocal.setOnClickListener(onClickListener);
		btnTelescope.setOnClickListener(onClickListener);
		btnDrone.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btnLocal:
					break;
				case R.id.btnTelescope:
					break;
				case R.id.btnDrone:
					break;
			}
//			Intent intent = new Intent(ctx, VideoUploadActivity.class);
//			intent.putExtra("username", username);
			startActivity(new Intent(ctx, VideoUploadActivity.class));
		}
	};
}