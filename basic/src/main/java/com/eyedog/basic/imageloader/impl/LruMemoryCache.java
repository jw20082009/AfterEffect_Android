package com.eyedog.basic.imageloader.impl;


import android.graphics.Bitmap;
import android.util.LruCache;


/**
 * Created by yuanyang on 17/8/3.
 */

public class LruMemoryCache extends LruCache<String,Bitmap>  {

    private static final int DEFAULT_MEMORY_SIZE = 10;

    public LruMemoryCache(){
        this(DEFAULT_MEMORY_SIZE*1024*1024);
    }

    public LruMemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        if (value == null)return 0;
        return value.getRowBytes() * value.getHeight();
    }

    public void clear() {
        evictAll();
    }
}
