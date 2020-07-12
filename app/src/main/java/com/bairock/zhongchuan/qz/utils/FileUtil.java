package com.bairock.zhongchuan.qz.utils;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    public static byte[] getImageStream(String imageUrl) {
        byte[] buffer = null;
        File file = new File(imageUrl);
        FileInputStream fis;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static void readBin2Image(byte[] byteArray, String targetPath) {
        InputStream in = new ByteArrayInputStream(byteArray);
        File file = new File(targetPath);
        String path = targetPath.substring(0, targetPath.lastIndexOf("/"));
        if (!file.exists()) {
            new File(path).mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString() + File.separator;
        }
        return null;
    }

    public static String getZhongchuanPath(){
        String sdDir = getSDPath();
        String zhiboFilePath = null;
        if(null != sdDir){
            zhiboFilePath = sdDir+"zhongchuan" + File.separator;
        }
        return zhiboFilePath;
    }

    public static String getPolicePath(){
        String sdDir = getZhongchuanPath();
        String policePath = null;
        if(null != sdDir){
            String policeNum;
            if(UserUtil.user.getUsername() != null && !UserUtil.user.getUsername().isEmpty()){
                policeNum = UserUtil.user.getUsername();
            }else{
                policeNum = "0000";
            }
            policePath = sdDir + policeNum + File.separator;
        }
        return policePath;
    }

    public static String getPoliceImagePath(){
        String path = getPolicePath();
        if(null != path){
            return path + "image" + File.separator;
        }
        return null;
    }

    public static String getPoliceVoicePath(){
        String path = getPolicePath();
        if(null != path){
            return path + "voice" + File.separator;
        }
        return null;
    }

    public static String getPoliceVideoPath(){
        String path = getPolicePath();
        if(null != path){
            return path + "video" + File.separator;
        }
        return null;
    }

    public static String getPoliceFileName(){
        String policeNum;
        if(UserUtil.user.getUsername() != null && !UserUtil.user.getUsername().isEmpty()){
            policeNum = UserUtil.user.getUsername();
        }else{
            policeNum = "0000";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        policeNum += "-" + simpleDateFormat.format(new Date()) + "-(" + UserUtil.MY_LOCATION.getLng() + "-" + UserUtil.MY_LOCATION.getLat() + ")";
        return policeNum;
    }

    public static String getLogPath(){
        String path = getZhongchuanPath();
        if(null != path){
            return path + "log" + File.separator;
        }
        return null;
    }

    public static String getLocationPath(){
        String path = getPolicePath();
        if(null != path){
            return path + "轨迹" + File.separator;
        }
        return null;
    }

    public static String getSubPolicePath(){
        String policePath = null;
            String policeNum;
            if(UserUtil.user.getUsername() != null && !UserUtil.user.getUsername().isEmpty()){
                policeNum = UserUtil.user.getUsername();
            }else{
                policeNum = "0000";
            }
            policePath = "zhongchuan" + File.separator + policeNum + File.separator;
        return policePath;
    }

    /**
     * 保存经纬度
     * @param lat 纬度
     * @param lng 经度
     */
    public static void saveLocation(Double lat, Double lng){
        try{
            String path = getLocationPath();
            if(null == path){
                path = "";
            }
            String policeNum;
            if(UserUtil.user.getUsername() != null && !UserUtil.user.getUsername().isEmpty()){
                policeNum = UserUtil.user.getUsername();
            }else{
                policeNum = "0000";
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINESE);
            path = path + policeNum + "-" + simpleDateFormat.format(new Date()) + ".txt";
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
            String content = simpleDateFormat2.format(new Date()) + " " + lng + "-" + lat + "\n";
//            File file =new File(path);
//            if(!file.exists()){
//                file.createNewFile();
//            }
            //使用true，即进行append file
            FileWriter fileWritter = new FileWriter(path,true);
            fileWritter.write(content);
            fileWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void createPolicePath(){
        if(UserUtil.user.getUsername() != null && !UserUtil.user.getUsername().isEmpty()){
            File policeFile = new File(getPolicePath());
            if(!policeFile.exists()){
                policeFile.mkdirs();
            }

            File fileImage = new File(getPoliceImagePath());
            if(!fileImage.exists()){
                fileImage.mkdir();
            }

            File file = new File(getPoliceVoicePath());
            if(!file.exists()){
                file.mkdir();
            }

            File file1 = new File(getPoliceVideoPath());
            if(!file1.exists()){
                file1.mkdir();
            }

            File file2 = new File(getLogPath());
            if(!file2.exists()){
                file2.mkdir();
            }
            File file3 = new File(getLocationPath());
            if(!file3.exists()){
                file3.mkdir();
            }
        }
    }
}
