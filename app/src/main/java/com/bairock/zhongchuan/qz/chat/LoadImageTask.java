package com.bairock.zhongchuan.qz.chat;

import java.io.File;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bairock.zhongchuan.qz.utils.ImageCache;
import com.easemob.util.ImageUtils;

public class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
	private ImageView iv = null;
//	String localFullSizePath = null;
	String thumbnailPath = null;

	@Override
	protected Bitmap doInBackground(Object... args) {
		thumbnailPath = (String) args[0];
//		localFullSizePath = (String) args[1];
		iv = (ImageView) args[1];
		File file = new File(thumbnailPath);
		if (file.exists()) {
			return ImageUtils.decodeScaleImage(thumbnailPath, 260, 260);
		} else {
//			if (message.direct == EMMessage.Direct.SEND) {
//				return ImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
//			} else {
//				return null;
//			}
			return null;
		}
	}

	protected void onPostExecute(Bitmap image) {
		if (image != null) {
			iv.setImageBitmap(image);
			ImageCache.getInstance().put(thumbnailPath, image);
			iv.setClickable(true);
			iv.setTag(thumbnailPath);
		} else {
//			if (message.status == EMMessage.Status.FAIL) {
//				if (CommonUtils.isNetWorkConnected(activity)) {
//					new Thread(new Runnable() {
//
//						@Override
//						public void run() {
//							EMChatManager.getInstance().asyncFetchMessage(
//									message);
//						}
//					}).start();
//				}
//			}

		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
}
