package com.bairock.zhongchuan.qz.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.bairock.zhongchuan.qz.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mcontext;
//    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public MyCrashHandler(Context context){
        mcontext = context;
//        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //在这里处理异常信息

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Toast.makeText(mcontext, "很抱歉，程序出现异常，请联系管理员, 即将退出", Toast.LENGTH_SHORT).show();
//                Looper.loop();
//            }
//        }).start();
        saveCrashInfoToFile(e);
//        mDefaultHandler.uncaughtException(t, e);
    }

    private void saveCrashInfoToFile(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable exCause = ex.getCause();
        while (exCause != null) {
            exCause.printStackTrace(printWriter);
            exCause =exCause.getCause();
        }
        printWriter.close();

        long timeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINESE);
        String name = simpleDateFormat.format(new Date());
        //错误日志文件名称
        String fileName = "crash-" + name + ".log";
        String logPath = FileUtil.getLogPath();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(logPath + fileName);
            fileOutputStream.write(writer.toString().getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //退出程序
        Intent i3 = new Intent(ConversationUtil.LOGOUT_ACTION);
        i3.putExtra("exit", 1);
        App.getInstance().sendOrderedBroadcast(i3, ConversationUtil.CHAT_BROADCAST_PERMISSION);
        App.getInstance2().exit();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(0);
//
//        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            //文件存储位置
//            String path = Environment.getExternalStorageDirectory().getPath() + "/crash_logInfo/";
//            File fl = new File(path);
//            //创建文件夹
//            if(!fl.exists()) {
//                fl.mkdirs();
//            }
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
//                fileOutputStream.write(writer.toString().getBytes());
//                fileOutputStream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}