package com.bairock.zhongchuan.qz.chat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;

import java.io.File;

public class VideoThumbUtils {
    /**
     * 得到视屏的缩略图
     *
     * @param videoPath
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {
        Bitmap result = null;
        Bitmap temp = null;
        if(videoPath==null){
            return null;
        }

        File file = new File(videoPath);
        if (!file.exists()) {
            return null;
        }
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(videoPath);  // 设置视屏资源（还有一种是传url的，这里传的string）
            String time = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long frameTime = Long.parseLong(time) * 1000 / 2;// 视屏前面的帧可能为黑屏，所以取中间的帧
            temp = metadataRetriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (null == temp) {
                temp = metadataRetriever.getFrameAtTime(0);
            }
        } catch (Exception o) {
            o.printStackTrace();
        } finally {
            try {
                metadataRetriever.release();
            } catch (RuntimeException o) {
                o.printStackTrace();
            }
        }
        result = extractThumbnail(temp, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT); //对得到的bitmap压缩
        return result;
    }

    private static Bitmap extractThumbnail(Bitmap source, int width, int height,
                                           int options) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {  //得到压缩比
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap thumbnail = transform(matrix, source, width, height,
                0x1 | options);
        return thumbnail;
    }

    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth,
                                    int targetHeight, int options) {
        boolean scaleUp = (options & 0x1) != 0;
        boolean recycle = (options & ThumbnailUtils.OPTIONS_RECYCLE_INPUT) != 0;

        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In such case the bitmap is smaller, at least in one dimension, than the target. Transform
             * it by placing as much of the image as possible into the target and leaving the top/bottom
             * or left/right (or both) black.
             */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
                    + Math.min(targetWidth, source.getWidth()), deltaYHalf
                    + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
                    - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            c.setBitmap(null);
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                    source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
                targetHeight);

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;
    }
}
