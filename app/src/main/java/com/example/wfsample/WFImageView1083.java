package com.example.wfsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.utility.IStreamJpeg;
import com.utility.Mjpeg2Jpeg;

public class WFImageView1083 extends androidx.appcompat.widget.AppCompatImageView implements IStreamJpeg
{
	private static final int WHAT_UPDATE_MJPEG = 2;
	
	private Context mContext = null;
	private Bitmap  mLastFrame=null;
	private Bitmap  mCanvasBitmap=null;
	private Paint   mVideoPaint  =new Paint();
	private int     mWidth=-1, mHeight=-1;
	private Canvas  mCanvas= null;
	private Paint   mPaint = new Paint();
	private GestureDetector mGestureDetector=null;
	
	public WFImageView1083(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mVideoPaint.setARGB(255, 200, 200, 200);
		mGestureDetector=new GestureDetector(new OnDoubleClick());
		this.setLongClickable(true);
		//this.setOnTouchListener(this);
	}

	public void regAVObj(Mjpeg2Jpeg objAV){
		objAV.regAPIListener(this);
	}
	
	public void unregAVObj(Mjpeg2Jpeg objAV){
		objAV.unregAPIListener(this);
	}
	
	public synchronized Bitmap getLastFrame() {
		return mLastFrame;
	}

	public int getImageWidth() { return mWidth;		}
	public int getImageHeight(){ return mHeight;	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    return mGestureDetector.onTouchEvent(event);
	}

	class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {
	        return false;
	    }
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
				case WHAT_UPDATE_MJPEG: {
					Bundle bundle = msg.getData();
					Bitmap bmp = (Bitmap) bundle.getParcelable("mjpeg_bmp");
					mLastFrame = bmp;
					if(bmp==null) break;
					if(mCanvas == null && mCanvasBitmap == null) {
						mCanvasBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
						mCanvas = new Canvas(mCanvasBitmap);
						setImageBitmap(mCanvasBitmap);
					}
					mCanvas.drawBitmap(mLastFrame, 0, 0, mPaint);
					WFImageView1083.this.invalidate();
					}
					break;

				default:
					break;
			}
		}
	};

	protected void updateMJpeg(Bitmap bmp) {
		Message message = new Message();
		message.what = WHAT_UPDATE_MJPEG;
		Bundle data = new Bundle();
		data.putParcelable("mjpeg_bmp", bmp);
		message.setData(data);
		handler.sendMessage(message);
	}

	@Override
	public int OnCallbackStream(byte[] pData, int nDataSize, int pUserData) {
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeByteArray(pData, 0, nDataSize);
			System.out.println("WFImageView1083, bmp="+bmp);
			if(bmp!=null){
				mWidth=bmp.getWidth();
				mHeight=bmp.getHeight();
				updateMJpeg(bmp);
			}
		}catch(Exception e) { e.printStackTrace(); }
		
		return 0;
	}
}