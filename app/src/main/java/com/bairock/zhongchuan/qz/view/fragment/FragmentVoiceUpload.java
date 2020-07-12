package com.bairock.zhongchuan.qz.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.view.activity.VideoUploadThirdActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceUploadActivity;
import com.bairock.zhongchuan.qz.view.activity.VoiceUploadThirdActivity;

public class FragmentVoiceUpload extends Fragment {
	private static String TAG = "FragmentVoiceUpload";
	// 发现
	private Activity ctx;
	private View layout;
//	private Button btnLocal;
//	private Button btnRemoteVoice;
	private CardView cardLocal;
	private CardView cardRemoteVoice;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (layout == null) {
			ctx = this.getActivity();
			layout = ctx.getLayoutInflater().inflate(R.layout.fragment_voice_upload_choose,
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
//		btnRemoteVoice = layout.findViewById(R.id.btnRemoteVoice);
		cardLocal = layout.findViewById(R.id.cardLocal);
		cardRemoteVoice = layout.findViewById(R.id.cardRemoteVoice);
	}

	private void setListener() {
		cardLocal.setOnClickListener(onClickListener);
		cardRemoteVoice.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent;
			if (v.getId() == R.id.cardRemoteVoice) {
				intent = new Intent(ctx, VoiceUploadThirdActivity.class);
			} else {
				intent = new Intent(ctx, VoiceUploadActivity.class);
			}
			startActivity(intent);
		}
	};
}
