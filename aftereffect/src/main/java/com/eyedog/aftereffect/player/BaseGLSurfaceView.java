package com.eyedog.aftereffect.player;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * created by jw200 at 2019/3/9 14:31
 **/
public abstract class BaseGLSurfaceView extends GLSurfaceView {

    protected BaseRenderer mRenderer;

    public BaseGLSurfaceView(Context context) {
        this(context, null);
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer = getRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    protected abstract BaseRenderer getRenderer();
}
