package com.eyedog.aftereffect.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import com.eyedog.aftereffect.gles.FullFrameRect;
import com.eyedog.aftereffect.gles.Texture2dProgram;
import com.eyedog.basic.handler.ThreadHandler;
import java.util.concurrent.Semaphore;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/2/1 21:13
 **/
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraView";
    protected SurfaceTexture mSurfaceTexture;
    protected FullFrameRect mFullScreen;
    protected int mTextureId;
    protected boolean mIncomingSizeUpdated;
    protected int mIncomingWidth, mIncomingHeight;
    protected int mSurfaceWidth, mSurfaceHeight;
    protected final float[] mSTMatrix = new float[16];
    protected boolean mIsCameraBackForward = true;
    protected ThreadHandler mThreadHandler;
    protected Semaphore mPreviewPermit = new Semaphore(1);

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mIncomingSizeUpdated = false;
        mIncomingWidth = mIncomingHeight = -1;
    }

    public void setCameraPreviewSize(int width, int height) {
        Log.i(TAG, "setCameraPreviewSize");
        mIncomingWidth = width;
        mIncomingHeight = height;
        mIncomingSizeUpdated = true;
    }

    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    private boolean startCamera() {
        int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK
            : Camera.CameraInfo.CAMERA_FACING_FRONT;
        boolean result = cameraInstance().tryOpenCamera(new CameraOpenCallback() {
            @Override
            public void cameraReady() {
                if (!cameraInstance().isPreviewing()) {
                    mIncomingWidth = cameraInstance().previewWidth();
                    mIncomingHeight = cameraInstance().previewHeight();
                    if (mSurfaceTexture != null && mPreviewPermit.tryAcquire()) {
                        cameraInstance().startPreview(mSurfaceTexture);
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                setCameraPreviewSize(mIncomingWidth,
                                    mIncomingHeight);
                            }
                        });
                    }
                }
            }
        }, facing);
        Log.i(TAG, "startCamera finished");
        return result;
    }

    public synchronized void switchCamera() {
        mIsCameraBackForward = !mIsCameraBackForward;
        if (cameraInstance().isCameraOpened()) {
            cameraInstance().stopCameraRealTime();
        }
        int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK
            : Camera.CameraInfo.CAMERA_FACING_FRONT;
        cameraInstance().tryOpenCamera(new CameraOpenCallback() {
            @Override
            public void cameraReady() {
                if (!cameraInstance().isPreviewing()) {
                    mIncomingWidth = cameraInstance().previewWidth();
                    mIncomingHeight = cameraInstance().previewHeight();
                    cameraInstance().startPreview(mSurfaceTexture);
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            setCameraPreviewSize(mIncomingWidth, mIncomingHeight);
                        }
                    });
                }
            }
        }, facing);
        requestRender();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        if (cameraInstance().isCameraOpened()) {
            cameraInstance().stopCamera();
        }
        startCamera();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        cameraInstance().stopCamera();
        mSurfaceTexture.release();
        mSurfaceTexture = null;
        mPreviewPermit.release();
        super.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        mFullScreen = new FullFrameRect(
            new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullScreen.createTextureObject();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        if (mThreadHandler == null) {
            mThreadHandler = new ThreadHandler();
        }
        mSurfaceTexture.setOnFrameAvailableListener(this, mThreadHandler.obtainThreadHandler());
        if (mPreviewPermit.tryAcquire()) {
            cameraInstance().startPreview(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (width > 0 && height > 0) {
            int scaledHeight = (int) (width * 16.0f / 9.0f);
            if (scaledHeight > height) {
                GLES20.glViewport(0, (int) ((height - scaledHeight) / 2.0f), width, scaledHeight);
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG, "onDrawFrame");
        mSurfaceTexture.updateTexImage();
        if (mIncomingSizeUpdated) {
            mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
            mIncomingSizeUpdated = false;
        }
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onFrameAvailable");
        requestRender();
    }
}
