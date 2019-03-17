
package com.eyedog.aftereffect.player;

import android.content.Context;
import android.util.AttributeSet;

/**
 * created by jw200 at 2019/3/14 15:03
 **/
public class VideoGLSurfaceView extends BaseGLSurfaceView {

    public VideoGLSurfaceView(Context context) {
        super(context);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected VideoRenderer getRenderer() {
        if (mRenderer == null) {
            mRenderer = new VideoRenderer(this);
        }
        return (VideoRenderer) mRenderer;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRenderer().startPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        getRenderer().stopPlay();
        release();
    }

    public void release() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                getRenderer().release();
            }
        });
    }
}
