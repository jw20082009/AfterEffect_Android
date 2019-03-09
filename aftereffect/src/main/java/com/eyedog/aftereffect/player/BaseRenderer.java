package com.eyedog.aftereffect.player;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/9 14:34
 **/
public class BaseRenderer implements GLSurfaceView.Renderer {

    GLSurfaceView mSurfaceView;

    public BaseRenderer(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
