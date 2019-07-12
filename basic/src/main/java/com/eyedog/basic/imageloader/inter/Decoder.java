package com.eyedog.basic.imageloader.inter;

import android.graphics.Bitmap;

import com.eyedog.basic.imageloader.ImageRequest;


/**
 * Created by yuanyang on 17/8/2.
 */

public interface Decoder {

    Bitmap decode(ImageRequest request);
}
