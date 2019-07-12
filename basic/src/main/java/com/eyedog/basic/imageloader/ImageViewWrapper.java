package com.eyedog.basic.imageloader;

import android.widget.ImageView;

/**
 * Created by yuanyang on 17/8/2.
 */

/**
 * ImageView 包装类，防止内存泄漏
 */
public class ImageViewWrapper extends TargetWrapper<ImageView> {

    public ImageViewWrapper(ImageView target) {
        super(target);
    }
}
