package com.eyedog.basic.imageloader.impl;

import android.graphics.Bitmap;

import com.eyedog.basic.imageloader.inter.MemoryCache;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class DefaultMemoryCache implements MemoryCache {

    /**
     * 内存缓存
     */
    private final Map<String,WeakReference<Bitmap>> cache = Collections.synchronizedMap(new HashMap<String, WeakReference<Bitmap>>());

    @Override
    public Bitmap get(String key) {
        WeakReference<Bitmap> ref = cache.get(key);
        if (ref != null){
            return ref.get();
        }
        return null;
    }

    @Override
    public void put(String key, Bitmap bitmap) {
        if (key == null || bitmap == null)return;
        cache.put(key,new WeakReference<>(bitmap));
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
