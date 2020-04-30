package com.bairock.zhongchuan.qz.chat;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.bairock.zhongchuan.qz.utils.ImageCache;
import com.easemob.util.ImageUtils;

public class LoadVideoImageTask extends AsyncTask<Object, Void, Bitmap> {

	private ImageView iv = null;
	String thumbnailPath = null;
	private Activity activity;

	@Override
	protected Bitmap doInBackground(Object... params) {
		thumbnailPath = (String) params[0];
		iv = (ImageView) params[1];
		activity = (Activity) params[2];
		if (new File(thumbnailPath).exists()) {
			Bitmap bitmap = VideoThumbUtils.getVideoThumbnail(thumbnailPath, 260, 260);
			return bitmap;
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (result != null) {
			iv.setImageBitmap(result);
			ImageCache.getInstance().put(thumbnailPath, result);
			iv.setClickable(true);
			iv.setTag(thumbnailPath);
			iv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (thumbnailPath != null) {
						File videoFile = new File(thumbnailPath);
						Uri uri;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							String authority = "com.bairock.zhongchuan.qz.fileprovider"; //【清单文件中provider的authorities属性的值】
							uri = FileProvider.getUriForFile(activity, authority, videoFile);
						} else {
							uri = Uri.fromFile(videoFile);
						}
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						intent.setDataAndType(uri, "video/*");
						activity.startActivity(intent);

						// VideoMessageBody videoBody = (VideoMessageBody)
						// message
						// .getBody();
						// Intent intent = new Intent(activity,
						// ShowVideoActivity.class);
						// intent.putExtra("localpath",
						// videoBody.getLocalUrl());
						// intent.putExtra("secret", videoBody.getSecret());
						// intent.putExtra("remotepath",
						// videoBody.getRemoteUrl());
						// if (message != null
						// && message.direct == EMMessage.Direct.RECEIVE
						// && !message.isAcked) {
						// message.isAcked = true;
						// try {
						// EMChatManager.getInstance().ackMessageRead(
						// message.getFrom(), message.getMsgId());
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						// }
						// activity.startActivity(intent);

					}
				}
			});

		} else {
//			if (message.status == EMMessage.Status.FAIL
//					|| message.direct == EMMessage.Direct.RECEIVE) {
//				if (CommonUtils.isNetWorkConnected(activity)) {
//					new AsyncTask<Void, Void, Void>() {
//
//						@Override
//						protected Void doInBackground(Void... params) {
//							EMChatManager.getInstance().asyncFetchMessage(
//									message);
//							return null;
//						}
//
//						@Override
//						protected void onPostExecute(Void result) {
//							adapter.notifyDataSetChanged();
//						};
//					}.execute();
//				}
//			}

		}
	}

}
