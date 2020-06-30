package com.bairock.zhongchuan.qz.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bairock.zhongchuan.qz.bean.ClientBase;

/**
 * 功能描述：弹窗内部子类项（绘制标题和图标）
 */
public class ActionItem {
	// 定义图片对象
	public Drawable mDrawable;
	// 定义文本对象
	public CharSequence mTitle;
	private ClientBase clientBase;

	public ActionItem(Drawable drawable, CharSequence title) {
		this.mDrawable = drawable;
		this.mTitle = title;
	}

	public ActionItem(Context context, int titleId, int drawableId) {
		this.mTitle = context.getResources().getText(titleId);
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}

	public ActionItem(Context context, CharSequence title, int drawableId) {
		this.mTitle = title;
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}

	public ActionItem(Context context, CharSequence title) {
		this.mTitle = title;
		this.mDrawable = null;
	}

	public ActionItem(Context context, CharSequence title, ClientBase clientBase) {
		this.mTitle = title;
		this.mDrawable = null;
		this.clientBase = clientBase;
	}

	public ClientBase getClientBase() {
		return clientBase;
	}

	public void setClientBase(ClientBase clientBase) {
		this.clientBase = clientBase;
	}
}
