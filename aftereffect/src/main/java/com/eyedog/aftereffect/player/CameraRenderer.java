
package com.eyedog.aftereffect.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;

import com.eyedog.aftereffect.camera.CameraDev;
import com.eyedog.aftereffect.camera.CameraHandler;

/**
 * created by jw200 at 2019/3/9 20:20
 **/
public class CameraRenderer extends OesRenderer {
    private final String TAG = "CameraRenderer";

    protected CameraDev mCameraDev;

    protected CameraHandler mCallback;

    private int mPreviewWidth, mPreviewHeight;

    private boolean mHasCameraOpened = false;

    CameraRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        mCallback = new CameraCallback(Looper.getMainLooper());
        mCameraDev = new CameraDev(mCallback);
    }

    public void startCamera(int facing) {
        synchronized (mLock) {
            mHasInputSizeChanged = false;
            mHasCameraOpened = false;
        }
        mCameraDev.startCamera(facing, 1080, 1920);
    }

    public void stopCamera() {
        synchronized (mLock) {
            mSurfaceTexture = null;
        }
        mCameraDev.stopCamera();
        super.release();
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        super.onSurfaceTextureCreated(surfaceTexture);
        if (mHasCameraOpened && surfaceTexture != null) {
            synchronized (mLock) {
                if (mHasCameraOpened && surfaceTexture != null) {
                    mCameraDev.startPreview(mSurfaceTexture);
                }
            }
        }
    }

    class CameraCallback extends CameraHandler {
        public CameraCallback(Looper looper) {
            super(looper);
        }

        @Override
        protected void handleStartFailed(String errorMsg) {
            Log.i(TAG, "handleStartFailed " + errorMsg);
        }

        @Override
        protected void handleStartSuccess(int previewWidth, int previewHeight, int pictureWidth,
                int pictureHeight) {
            synchronized (mLock) {
                if (mPreviewWidth != previewWidth || mPreviewHeight != previewHeight) {
                    if (previewWidth > previewHeight) {
                        mPreviewWidth = previewHeight;
                        mPreviewHeight = previewWidth;
                    } else {
                        mPreviewWidth = previewWidth;
                        mPreviewHeight = previewHeight;
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
            onInputSizeChanged(mPreviewWidth, mPreviewHeight);
        }
    }

    @Override
    public void release() {
        super.release();
        mCameraDev.onDestroy();
    }
}
