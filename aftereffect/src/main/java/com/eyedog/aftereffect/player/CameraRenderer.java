
package com.eyedog.aftereffect.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;
import com.eyedog.aftereffect.camera.CameraDev;
import com.eyedog.aftereffect.camera.CameraHandler;
import com.eyedog.aftereffect.utils.OpenGLUtils;

/**
 * created by jw200 at 2019/3/9 20:20
 **/
public class CameraRenderer extends OesRenderer {
    private final String TAG = "CameraRenderer";

    protected CameraDev mCameraDev;

    protected CameraHandler mCallback;

    CameraRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        mCallback = new CameraCallback(Looper.getMainLooper());
        mCameraDev = new CameraDev(mCallback);
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        super.onSurfaceTextureCreated(surfaceTexture);
    }

    public void startCamera(int facing) {
        Log.i(TAG, "startCamera");
        mIncomingSizeUpdated = false;
        mCameraDev.startCamera(facing, 1080, 1920);
    }

    public void stopCamera() {
        Log.i(TAG, "stopCamera");
        synchronized (lock) {
            mSurfaceTexture = null;
        }
        mCameraDev.stopCamera();
        super.release();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        super.onFrameAvailable(surfaceTexture);
        mSurfaceView.requestRender();
    }

    class CameraCallback extends CameraHandler {
        public CameraCallback(Looper looper) {
            super(looper);
        }

        @Override
        protected void handleStartFailed(String errorMsg) {
        }

        @Override
        protected void handleStartSuccess(int previewWidth, int previewHeight, int pictureWidth,
            int pictureHeight) {
            Log.i(TAG, "handleStartSuccess");
            synchronized (lock) {
                if (mIncomingWidth != previewWidth || mIncomingHeight != previewHeight) {
                    if (previewWidth > previewHeight) {
                        mIncomingWidth = previewHeight;
                        mIncomingHeight = previewWidth;
                    } else {
                        mIncomingWidth = previewWidth;
                        mIncomingHeight = previewHeight;
                    }
                }
                if (mSurfaceTexture != null) {
                    mCameraDev.startPreview(mSurfaceTexture);
                } else {
                    Log.i(TAG, "handleStartSuccess mSurfaceTexture null");
                }
            }
        }

        @Override
        protected void handleStartPreview() {
            Log.i(TAG, "handleStartPreview");
            setIncomingSize(mIncomingWidth, mIncomingHeight);
        }
    }

    @Override
    public void release() {
        super.release();
        mCameraDev.onDestroy();
    }

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_XY
    }
}
