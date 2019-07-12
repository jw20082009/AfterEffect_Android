package com.eyedog.basic.imageloader.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eyedog.basic.imageloader.ImageRequest;
import com.eyedog.basic.imageloader.ResizeOptions;
import com.eyedog.basic.imageloader.inter.Decoder;

/**
 * Created by yuanyang on 17/8/2.
 */

public class DefaultDecoder implements Decoder {

    @Override
    public Bitmap decode(ImageRequest request) {
        String imagePath = request.getPath();
        ResizeOptions opts = request.getResizeOptions();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = request.getMutable();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath,options);
        int sampleSize = 1;
        boolean isCropWidth = false;
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        int requestWidth = opts == null?srcWidth:opts.getResizeWidth();
        int requestHeight = opts == null?srcHeight:opts.getResizeHeight();
        if (requestHeight >= srcHeight || requestWidth >= srcWidth) {
            sampleSize = 1;
        }else {
            int widthSampleSize = srcWidth/requestWidth;
            int heightSampleSize = srcHeight/requestHeight;
            isCropWidth = widthSampleSize < heightSampleSize;
            sampleSize = Math.min(widthSampleSize,heightSampleSize);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;//减小内存占用
        Bitmap scaledBitmap = BitmapFactory.decodeFile(imagePath,options);
        if (sampleSize == 1){
            return scaledBitmap;
        }
        Bitmap wantedBitmap;
        if (isCropWidth) {
            int startY = (scaledBitmap.getHeight() / 2 - requestHeight / 2) < 0 ? 0
                    : (scaledBitmap.getHeight() / 2 - requestHeight / 2);
            wantedBitmap =
                    Bitmap.createBitmap(scaledBitmap, 0, startY, requestWidth, requestHeight);
        } else {
            int startX = (scaledBitmap.getWidth() / 2 - requestWidth / 2) < 0 ? 0
                    : (scaledBitmap.getWidth() / 2 - requestWidth / 2);
            wantedBitmap =
                    Bitmap.createBitmap(scaledBitmap, startX, 0, requestWidth, requestHeight);
        }
        scaledBitmap.recycle();
        return wantedBitmap;
    }

}
