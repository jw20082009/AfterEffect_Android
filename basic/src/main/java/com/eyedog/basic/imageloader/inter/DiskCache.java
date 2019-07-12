package com.eyedog.basic.imageloader.inter;

import android.graphics.Bitmap;

import java.io.IOException;


/**
 * Created by yuanyang on 17/8/2.
 */

public interface DiskCache {

    Bitmap get(String key);

    void put(String key, Bitmap bitmap) throws IOException;

    void clear();

    String generateFileName(String key);
}
