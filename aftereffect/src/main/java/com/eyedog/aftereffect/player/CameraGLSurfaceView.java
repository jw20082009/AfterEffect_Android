
package com.eyedog.aftereffect.player;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

/**
 * created by jw200 at 2019/3/9 20:27
 **/
public class CameraGLSurfaceView extends BaseGLSurfaceView {

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onResume() {
        super.onResume();
        getRenderer().startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    public void onPause() {
        super.onPause();
        getRenderer().stopCamera();
        getRenderer().release();
    }

    @Override
    protected CameraRenderer getRenderer() {
        if (mRenderer == null) {
            mRenderer = new CameraRenderer(this);
        }
        return (CameraRenderer) mRenderer;
    }
}
