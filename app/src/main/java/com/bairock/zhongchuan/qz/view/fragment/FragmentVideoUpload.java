package com.bairock.zhongchuan.qz.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.view.activity.VideoUploadActivity;
import com.bairock.zhongchuan.qz.view.activity.VideoUploadThirdActivity;
import com.example.wfsample.TelescopeActivity;

public class FragmentVideoUpload extends Fragment {

	public static final String SOURCE_LOCAL = "sourceLocal";
	public static final String SOURCE_TELESCOPE = "sourceTelescope";
	public static final String SOURCE_DRONE = "sourceDrone";

	private Activity ctx;
	private View layout;
//	private Button btnLocal;
//	private Button btnTelescope;
//	private Button btnDrone;
	private CardView cardLocal;
	private CardView cardTelescope;
	private CardView cardUav;

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
//		btnLocal = layout.findViewById(R.id.btnLocal);
//		btnTelescope = layout.findViewById(R.id.btnTelescope);
//		btnDrone = layout.findViewById(R.id.btnDrone);
		cardLocal = layout.findViewById(R.id.cardLocal);
		cardTelescope = layout.findViewById(R.id.cardTelescope);
		cardUav = layout.findViewById(R.id.cardUav);
	}

	private void setListener() {
		cardLocal.setOnClickListener(onClickListener);
		cardTelescope.setOnClickListener(onClickListener);
		cardUav.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String source;
			Intent intent;
			switch (v.getId()) {
				case R.id.cardTelescope:
					source = SOURCE_TELESCOPE;
					intent = new Intent(ctx, TelescopeActivity.class);
					break;
				case R.id.cardUav:
					source = SOURCE_DRONE;
					intent = new Intent(ctx, VideoUploadThirdActivity.class);
					break;
				default:
					source = SOURCE_LOCAL;
					intent = new Intent(ctx, VideoUploadActivity.class);
					break;
			}
			intent.putExtra("source", source);
			startActivity(intent);
		}
	};
}