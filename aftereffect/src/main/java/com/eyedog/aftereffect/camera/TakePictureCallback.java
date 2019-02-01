package com.eyedog.aftereffect.camera;

import android.graphics.Bitmap;

/**
 * created by jw200 at 2018/6/14 14:02
 **/
public interface TakePictureCallback {
    void takePictureOK(Bitmap bmp, String filePath);
}
