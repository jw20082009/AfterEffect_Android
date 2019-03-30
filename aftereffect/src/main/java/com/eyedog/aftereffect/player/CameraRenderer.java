
package com.eyedog.aftereffect.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;

import com.eyedog.aftereffect.camera.CameraDev;
import com.eyedog.aftereffect.camera.CameraHandler;
import com.eyedog.aftereffect.filters.SpSwirlFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/9 20:20
 **/
public class CameraRenderer extends OesRenderer {
    private final String TAG = "CameraRenderer";

    protected CameraDev mCameraDev;

    protected CameraHandler mCallback;

    protected SpSwirlFilter mSwirlFilter;

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
        mCameraDev.startCamera(facing, 1080, 1920, 1080, 1920);
    }

    public void stopCamera() {
        synchronized (mLock) {
            mSurfaceTexture = null;
        }
        mCameraDev.stopCamera();
        super.release();
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
        FloatBuffer textureBuffer) {
        int bufferTextureId = super.onDrawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        if (mSwirlFilter != null) {
            bufferTextureId = mSwirlFilter.drawFrameBuffer(
                bufferTextureId == OpenGLUtils.GL_NOT_TEXTURE ? textureId : bufferTextureId,
                vertexBuffer, textureBuffer);
        }
        return bufferTextureId;
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        if (mSwirlFilter != null) {
            mSwirlFilter.onDisplaySizeChanged(width, height);
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
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
        mSwirlFilter = new SpSwirlFilter(mSurfaceView.getContext());
        mSwirlFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
        mSwirlFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
        if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
            mSwirlFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
        }
    }

    @Override
    public void release() {
        super.release();
        mCameraDev.onDestroy();
        if (mSwirlFilter != null) {
            mSwirlFilter.release();
            mSwirlFilter = null;
        }
    }
}
