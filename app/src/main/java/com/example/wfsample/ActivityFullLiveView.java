package com.example.wfsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bairock.zhongchuan.qz.R;

public class ActivityFullLiveView extends AppCompatActivity {

    private WFRender myRender= null;
    private WFSurfaceView myGlSurfaceView = null;
    private GestureDetector mGestureDetector=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_full_live_view);
        //仅去掉标题栏，系统状态栏还是会显示
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        mGestureDetector=new GestureDetector(this,new OnDoubleClick());

        myGlSurfaceView = findViewById(R.id.my_gl_surfaceview);
        myRender = new WFRender(myGlSurfaceView);
        myGlSurfaceView.setRenderer(myRender);
        if(myRender!=null) TelescopeVideoUploadActivity.m_objAV.API_SetRender(myRender);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Intent resultIntent = new Intent();
            ActivityFullLiveView.this.setResult(RESULT_OK, resultIntent);
            finish();
            return false;
        }
//	    @Override
//	    public boolean onDoubleTapEvent(MotionEvent e) {
//	        return super.onDoubleTapEvent(e);
//	    }
    }
}