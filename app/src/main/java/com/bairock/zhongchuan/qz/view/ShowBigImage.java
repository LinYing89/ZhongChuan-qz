package com.bairock.zhongchuan.qz.view;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

import com.bairock.zhongchuan.qz.R;
import com.bairock.zhongchuan.qz.utils.ImageCache;
import com.bairock.zhongchuan.qz.widght.TouchImageView.TouchImageView;

/**
 * 下载显示大图
 * 
 */
public class ShowBigImage extends BaseActivity {

	private ProgressDialog pd;
	private TouchImageView image;
	private int default_res = R.drawable.default_image;
	private String localFilePath;
	private Bitmap bitmap;
	private boolean isDownloaded;
	private ProgressBar loadLocalPb;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_show_big_image);
		super.onCreate(savedInstanceState);

		image = (TouchImageView) findViewById(R.id.image);
		loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
		default_res = getIntent().getIntExtra("default_image", R.drawable.head);
		Uri uri = getIntent().getParcelableExtra("uri");

		// 本地存在，直接显示本地的图片
		if (uri != null && new File(uri.getPath()).exists()) {
			System.err.println("showbigimage file exists. directly show it");
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// int screenWidth = metrics.widthPixels;
			// int screenHeight =metrics.heightPixels;
			bitmap = ImageCache.getInstance().get(uri.getPath());
			if (bitmap != null) {
				image.setImageBitmap(bitmap);
			}
		} else {
			image.setImageResource(default_res);
		}

		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void initControl() {

	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {

	}

	@Override
	protected void setListener() {

	}

	/**
	 * 通过远程URL，确定下本地下载后的localurl
	 * 
	 * @param remoteUrl
	 * @return
	 */
	public String getLocalFilePath(String remoteUrl) {
		String localPath = "";
		return localPath;
	}

	@Override
	public void onBackPressed() {
		if (isDownloaded)
			setResult(RESULT_OK);
		finish();
	}
}
