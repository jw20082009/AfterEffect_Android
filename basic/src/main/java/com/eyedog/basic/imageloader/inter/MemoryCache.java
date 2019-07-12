package com.eyedog.basic.imageloader.inter;

import android.graphics.Bitmap;


/**
 * Created by yuanyang on 17/8/2.
 */

public interface MemoryCache {

    Bitmap get(String key);

    void put(String key, Bitmap bitmap);

    void clear();

}
