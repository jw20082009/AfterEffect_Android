
package com.eyedog.aftereffect.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Looper;

import com.eyedog.aftereffect.camera.CameraDev;
import com.eyedog.aftereffect.camera.CameraHandler;
import com.eyedog.aftereffect.filters.GLImageFilter;
import com.eyedog.aftereffect.filters.GLImageOESInputFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import com.eyedog.aftereffect.utils.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/9 20:20
 **/
public class CameraRenderer extends BaseRenderer {
    private final String TAG = "CameraRenderer";

    protected int mTextureId;

    protected SurfaceTexture mSurfaceTexture;

    protected CameraDev mCameraDev;

    protected CameraHandler mCallback;

    protected Object lock = new Object();

    protected int mIncomingWidth, mIncomingHeight, mSurfaceWidth, mSurfaceHeight;

    protected final float[] mSTMatrix = new float[16];

    protected GLImageFilter mOutFilter;

    protected GLImageOESInputFilter mInputFilter;

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;

    // 用于显示裁剪的纹理顶点缓冲
    private FloatBuffer mDisplayVertexBuffer;

    private FloatBuffer mDisplayTextureBuffer;

    private boolean mIncomingSizeUpdated;

    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    CameraRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        mCallback = new CameraCallback(Looper.getMainLooper());
        mCameraDev = new CameraDev(mCallback);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        synchronized (lock) {
            mTextureId = createTextureObject();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(cameraAvaliableListener);
            mCameraDev.startPreview(mSurfaceTexture);
            initBuffers();
            initFilters(mSurfaceView.getContext());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mIncomingWidth > 0 && mIncomingHeight > 0) {
            adjustCoordinateSize();
            onFilterChanged();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        mSurfaceTexture.updateTexImage();
        if (mIncomingSizeUpdated) {
            adjustCoordinateSize();
            onFilterChanged();
            mIncomingSizeUpdated = false;
        }
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mInputFilter.setTextureTransformMatrix(mSTMatrix);
        int currentTextureId = mInputFilter.drawFrameBuffer(mTextureId, mVertexBuffer,
                mTextureBuffer);
        mOutFilter.drawFrame(currentTextureId, mDisplayVertexBuffer, mDisplayTextureBuffer);
    }

    public void startCamera(int facing) {
        mIncomingSizeUpdated = false;
        mCameraDev.startCamera(facing, 1080, 1920);
    }

    public void stopCamera() {
        synchronized (lock) {
            mSurfaceTexture = null;
        }
        mCameraDev.stopCamera();
    }

    protected int createTextureObject() {
        return OpenGLUtils.createOESTexture();
    }

    private void initBuffers() {
        releaseBuffers();
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    private void initFilters(Context context) {
        releaseFilters();
        mInputFilter = new GLImageOESInputFilter(context);
        mOutFilter = new GLImageFilter(context);
    }

    private void releaseFilters() {
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mOutFilter != null) {
            mOutFilter.release();
            mOutFilter = null;
        }
    }

    private void releaseBuffers() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
        if (mDisplayVertexBuffer != null) {
            mDisplayVertexBuffer.clear();
            mDisplayVertexBuffer = null;
        }
        if (mDisplayTextureBuffer != null) {
            mDisplayTextureBuffer.clear();
            mDisplayTextureBuffer = null;
        }
    }

    public void release() {
        releaseBuffers();
        releaseFilters();
    }

    private void onFilterChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mIncomingWidth, mIncomingHeight);
            mInputFilter.initFrameBuffer(mIncomingWidth, mIncomingHeight);
            mInputFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
        }
        if (mOutFilter != null) {
            mOutFilter.onInputSizeChanged(mIncomingWidth, mIncomingHeight);
            mOutFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
        }
    }

    private void adjustCoordinateSize() {
        int mViewWidth = mSurfaceWidth;
        int mViewHeight = mSurfaceHeight;
        int mTextureWidth = mIncomingWidth;
        int mTextureHeight = mIncomingHeight;
        float[] textureCoord = null;
        float[] vertexCoord = null;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float[] vertexVertices = TextureRotationUtils.CubeVertices;
        float ratioMax = Math.max((float) mViewWidth / mTextureWidth,
                (float) mViewHeight / mTextureHeight);
        // 新的宽高
        int imageWidth = Math.round(mTextureWidth * ratioMax);
        int imageHeight = Math.round(mTextureHeight * ratioMax);
        // 获取视图跟texture的宽高比
        float ratioWidth = (float) imageWidth / (float) mViewWidth;
        float ratioHeight = (float) imageHeight / (float) mViewHeight;
        if (mScaleType == ScaleType.CENTER_INSIDE) {
            vertexCoord = new float[] {
                    vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth,
                    vertexVertices[2], vertexVertices[3] / ratioHeight,
                    vertexVertices[4] / ratioWidth, vertexVertices[5],
                    vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth,
                    vertexVertices[8], vertexVertices[9] / ratioHeight,
                    vertexVertices[10] / ratioWidth, vertexVertices[11],
            };
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCoord = new float[] {
                    addDistance(textureVertices[0], distVertical),
                    addDistance(textureVertices[1], distHorizontal),
                    addDistance(textureVertices[2], distVertical),
                    addDistance(textureVertices[3], distHorizontal),
                    addDistance(textureVertices[4], distVertical),
                    addDistance(textureVertices[5], distHorizontal),
                    addDistance(textureVertices[6], distVertical),
                    addDistance(textureVertices[7], distHorizontal),
            };
        }
        if (vertexCoord == null) {
            vertexCoord = vertexVertices;
        }
        if (textureCoord == null) {
            textureCoord = textureVertices;
        }
        // 更新VertexBuffer 和 TextureBuffer
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    SurfaceTexture.OnFrameAvailableListener cameraAvaliableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mSurfaceView.requestRender();
        }
    };

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
                }
            }
        }

        @Override
        protected void handleStartPreview() {
            mIncomingSizeUpdated = true;
        }
    }

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_XY
    }
}
