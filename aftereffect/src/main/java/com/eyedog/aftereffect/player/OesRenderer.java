
package com.eyedog.aftereffect.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.eyedog.aftereffect.filters.GLImageOESInputFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import com.eyedog.aftereffect.utils.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/14 16:27
 **/
public class OesRenderer extends BaseRenderer implements SurfaceTexture.OnFrameAvailableListener {
    private final String TAG = "OesRenderer";

    protected SurfaceTexture mSurfaceTexture;

    protected final float[] mSTMatrix = new float[16];

    // 用于显示裁剪的纹理顶点缓冲
    protected FloatBuffer mDisplayVertexBuffer;

    protected FloatBuffer mDisplayTextureBuffer;

    protected ScaleType mScaleType = ScaleType.CENTER_CROP;

    protected boolean mHasTextureCreated = false;

    public OesRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        synchronized (mLock) {
            mInputTexture = createInputTexture();
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                mSurfaceTexture = new SurfaceTexture(mInputTexture);
                mHasTextureCreated = true;
                mSurfaceTexture.setOnFrameAvailableListener(this);
                onSurfaceTextureCreated(mSurfaceTexture);
            } else {
                Log.i(TAG, "createInputTexture failed");
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        super.onDrawFrame(gl);
    }

    @Override
    protected void beforeDrawFrame() {
        super.beforeDrawFrame();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        ((GLImageOESInputFilter) mInputFilter).setTextureTransformMatrix(mSTMatrix);
    }

    @Override
    public int createInputTexture() {
        mInputTexture = OpenGLUtils.createOESTexture();
        return mInputTexture;
    }

    @Override
    protected GLImageOESInputFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageOESInputFilter(mSurfaceView.getContext());
        } else {
            mInputFilter.initProgramHandle();
        }
        return (GLImageOESInputFilter) mInputFilter;
    }

    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
    }

    @Override
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
        adjustCoordinateSize();
    }

    protected void adjustCoordinateSize() {
        int mViewWidth = mSurfaceWidth;
        int mViewHeight = mSurfaceHeight;
        int mTextureWidth = mIncommingWidth;
        int mTextureHeight = mIncommingHeight;
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

    protected float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    @Override
    protected void release() {
        super.release();
        synchronized (mLock) {
            mHasTextureCreated = false;
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

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_XY
    }
}
