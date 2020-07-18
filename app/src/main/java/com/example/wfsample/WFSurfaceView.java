package com.example.wfsample;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class WFSurfaceView extends GLSurfaceView{
	private GestureDetector mGestureDetector=null;
	
	public WFSurfaceView(Context context) {
		super(context);
	}

	public WFSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEGLContextClientVersion(2);
		
		mGestureDetector= new GestureDetector(context, new OnDoubleClick());
	}
	
	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    return mGestureDetector.onTouchEvent(event);
	}
	class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {
	        return false;
	    }
//	    @Override
//	    public boolean onDoubleTapEvent(MotionEvent e) {
//	        return super.onDoubleTapEvent(e);
//	    }
	}
}
