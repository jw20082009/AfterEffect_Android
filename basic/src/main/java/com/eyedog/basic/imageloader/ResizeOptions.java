package com.eyedog.basic.imageloader;

/**
 * Created by yuanyang on 17/8/2.
 */

/**
 * 图片裁剪参数
 */
public class ResizeOptions {

    private int resizeWidth;

    private int resizeHeight;

    public ResizeOptions(int resizeWidth, int resizeHeight) {
        this.resizeWidth = resizeWidth;
        this.resizeHeight = resizeHeight;
    }

    public int getResizeWidth() {
        return resizeWidth;
    }

    public void setResizeWidth(int resizeWidth) {
        this.resizeWidth = resizeWidth;
    }

    public int getResizeHeight() {
        return resizeHeight;
    }

    public void setResizeHeight(int resizeHeight) {
        this.resizeHeight = resizeHeight;
    }
}
