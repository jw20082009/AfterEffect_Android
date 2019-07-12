package com.eyedog.basic.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.eyedog.basic.imageloader.manager.KeyManager;

import java.lang.ref.WeakReference;

/**
 * Created by yuanyang on 17/8/2.
 */

public class ImageDisplayTask implements Runnable {

    private WeakReference<Bitmap> mBitmapRef;

    private TargetWrapper<ImageView> mWrapper;

    private String mkey;

    public ImageDisplayTask(Bitmap bitmap, ImageViewWrapper wrapper, String key) {
        mBitmapRef = new WeakReference<>(bitmap);
        mWrapper = wrapper;
        mkey = key;
    }

    @Override
    public void run() {
        ImageView iv = mWrapper.get();
        if (iv == null) return;
        int hash = iv.hashCode();
        String key = KeyManager.getInstance().getKey(hash);
        if (!mkey.equals(key)) return;
        Bitmap b = mBitmapRef == null ? null : mBitmapRef.get();
        if (b == null) return;
        iv.setImageBitmap(b);
        KeyManager.getInstance().removeKey(hash);
    }
}
