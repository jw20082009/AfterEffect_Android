package com.eyedog.basic.imageloader;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.eyedog.basic.BuildConfig;
import com.eyedog.basic.imageloader.impl.DefaultDecoder;
import com.eyedog.basic.imageloader.impl.LruMemoryCache;
import com.eyedog.basic.imageloader.inter.Decoder;
import com.eyedog.basic.imageloader.inter.DiskCache;
import com.eyedog.basic.imageloader.inter.MemoryCache;
import com.eyedog.basic.imageloader.manager.RequestManager;
import com.eyedog.basic.imageloader.util.KeyGeneratorUtil;

import java.io.IOException;

/**
 * Created by yuanyang on 17/8/2.
 */

public class ImageLoadTask<T> implements Runnable {


    private final Handler mHandler;

    private ImageRequest request;

    private TargetWrapper<T> wrapper;

    private DiskCache mDiskCache;

    private Decoder mDecoder;

    private MemoryCache mMemoryCache;

    private LruMemoryCache mLruMemoryCache;

    private RequestManager mRequestManager;

    public ImageLoadTask(RequestManager requestManager, ImageRequest request, TargetWrapper<T> wrapper,
                         DiskCache diskCache, MemoryCache memoryCache, LruMemoryCache lruMemoryCache, Handler handler) {
        this.request = request;
        this.wrapper = wrapper;
        this.mDiskCache = diskCache;
        this.mMemoryCache = memoryCache;
        this.mLruMemoryCache = lruMemoryCache;
        this.mRequestManager = requestManager;
        this.mHandler = handler;
        mDecoder = new DefaultDecoder();
    }

    @Override
    public void run() {
        if (checkRequestState()) return;

        String key = KeyGeneratorUtil.generateKey(request);

        long startTime = 0;
        if (BuildConfig.DEBUG) {
            startTime = System.currentTimeMillis();
        }

        Bitmap lruBitmap = mLruMemoryCache.get(key);
        if (lruBitmap != null) {
            if (wrapper instanceof ImageViewWrapper) {
                mHandler.post(new ImageDisplayTask(lruBitmap, (ImageViewWrapper) wrapper, key));
                mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            } else {
                tryNotifyBitmap(lruBitmap);
                mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            }
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit lru memory cache,waste time:" + (System.currentTimeMillis() - startTime));
            }
            return;
        }

        Bitmap memoryBitmap = mMemoryCache.get(key);
        if (memoryBitmap != null) {
            if (wrapper instanceof ImageViewWrapper) {
                mHandler.post(new ImageDisplayTask(memoryBitmap, (ImageViewWrapper) wrapper, key));
                mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            } else {
                tryNotifyBitmap(memoryBitmap);
                mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            }
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit memory cache,waste time:" + (System.currentTimeMillis() - startTime));
            }
            return;
        }

        if (checkRequestState()) return;

        Bitmap diskBitmap = mDiskCache.get(key);
        if (diskBitmap != null) {
            mLruMemoryCache.put(key, diskBitmap);
            mMemoryCache.put(key, diskBitmap);
            mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            if (checkRequestState()) {
                return;
            }
            if (wrapper instanceof ImageViewWrapper) {
                mHandler.post(new ImageDisplayTask(diskBitmap, (ImageViewWrapper) wrapper, key));
            } else {
                tryNotifyBitmap(diskBitmap);
            }
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "hit disk cache,waste time:" + (System.currentTimeMillis() - startTime));
            }
            return;
        }

        if (checkRequestState()) return;

        Bitmap bitmap = mDecoder.decode(request);
        if (bitmap != null) {
            mLruMemoryCache.put(key, bitmap);
            mMemoryCache.put(key, bitmap);
            try {
                mDiskCache.put(key, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
            if (checkRequestState()) {
                return;
            }
            if (wrapper instanceof ImageViewWrapper) {
                mHandler.post(new ImageDisplayTask(bitmap, (ImageViewWrapper) wrapper, key));
            } else {
                tryNotifyBitmap(bitmap);
            }
            if (BuildConfig.DEBUG) {
                Log.i("LocalLoader", "load from local,waste time:" + (System.currentTimeMillis() - startTime));
            }
        }

        mRequestManager.remove(wrapper.get() == null ? -1 : wrapper.get().hashCode());
    }

    private void tryNotifyBitmap(Bitmap bitmap) {
        if (wrapper != null) {
            T t = wrapper.get();
            if (t != null && t instanceof LocalImageLoader.ILocalBitmapListener) {
                LocalImageLoader.ILocalBitmapListener listener = (LocalImageLoader.ILocalBitmapListener) t;
                listener.onLoadResult(bitmap);
            }
        }
    }

    private boolean checkRequestState() {
        if (request.hasCancel()) {
            if (BuildConfig.DEBUG) {
                Log.w("LocalLoader", "cancel request:" + request.getPath());
            }
            return true;
        }
        return false;
    }
}
