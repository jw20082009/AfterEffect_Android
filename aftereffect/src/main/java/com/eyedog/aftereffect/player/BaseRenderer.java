
package com.eyedog.aftereffect.player;

import android.opengl.GLSurfaceView;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/9 14:34
 **/
public class BaseRenderer implements GLSurfaceView.Renderer {

    protected GLSurfaceView mSurfaceView;

    private final LinkedList<Runnable> mRunOnDraw;

    public BaseRenderer(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        mRunOnDraw = new LinkedList<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        runPendingOnDrawTasks();
    }

    /**
     * 添加延时任务
     */
    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    /**
     * 运行延时任务
     */
    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
}
