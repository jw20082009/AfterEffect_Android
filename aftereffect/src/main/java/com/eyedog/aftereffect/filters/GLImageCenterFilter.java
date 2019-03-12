package com.eyedog.aftereffect.filters;

import android.content.Context;

/**
 * created by jw200 at 2019/3/12 16:49
 **/
public class GLImageCenterFilter extends GLImageFilter {

    public GLImageCenterFilter(Context context) {
        super(context);
    }

    public GLImageCenterFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
