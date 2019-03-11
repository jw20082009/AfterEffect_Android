
package com.eyedog.aftereffect.player;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.eyedog.aftereffect.filters.GLImageFilter;
import com.eyedog.aftereffect.filters.GLImageInputFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import com.eyedog.aftereffect.utils.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageRenderer extends BaseRenderer {
    private final String TAG = "ImageRenderer";

    protected GLImageInputFilter mInputFilter;

    protected GLImageFilter mDisplayFilter;

    // 输入纹理大小
    protected int mTextureWidth;

    protected int mTextureHeight;

    // 控件视图大小
    protected int mViewWidth;

    protected int mViewHeight;

    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;

    private Bitmap mBitmap;

    private boolean mHasTextureChanged = false;

    private Object mLock = new Object();

    public ImageRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mHasTextureChanged = false;
    }

    public void setBitmap(final Bitmap bitmap) {
        synchronized (mLock) {
            Log.i(TAG, "setBitmap");
            mBitmap = bitmap;
            mTextureWidth = mBitmap.getWidth();
            mTextureHeight = mBitmap.getHeight();
            mHasTextureChanged = true;
            mSurfaceView.requestRender();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        Log.i(TAG, "onSurfaceCreated");
        if (mBitmap != null && !mBitmap.isRecycled()) {
            synchronized (mLock) {
                mHasTextureChanged = true;
            }
        }
        initFilters();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        this.mViewWidth = width;
        this.mViewHeight = height;
        GLES30.glViewport(0, 0, width, height);
        Log.i(TAG, "onSurfaceChanged0 : " + this.mViewWidth + " * " + this.mViewHeight + ";"
                + mHasTextureChanged);
        if (mHasTextureChanged && mViewWidth > 0) {
            synchronized (mLock) {
                Log.i(TAG, "onSurfaceChanged1 : " + this.mViewWidth + " * " + this.mViewHeight + ";"
                        + mHasTextureChanged);
                if (mHasTextureChanged && mViewWidth > 0) {
                    mHasTextureChanged = false;
                    createTexture();
                    // Note: 如果此时显示输出滤镜对象为空，则表示调用了onPause方法销毁了所有GL对象资源，需要重新初始化滤镜
                    onFilterSizeChanged();
                    mSurfaceView.requestRender();
                }
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        Log.i(TAG, "onDrawFrame");
        if (mHasTextureChanged && mViewWidth > 0) {
            mHasTextureChanged = false;
            createTexture();
            // Note: 如果此时显示输出滤镜对象为空，则表示调用了onPause方法销毁了所有GL对象资源，需要重新初始化滤镜
            onFilterSizeChanged();
        }
        int currentTexture = mInputTexture;
        if (mInputFilter != null) {
            currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer,
                    mTextureBuffer);
        }
        mDisplayFilter.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);
    }

    private void initFilters() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageInputFilter(mSurfaceView.getContext());
        } else {
            mInputFilter.initProgramHandle();
        }
        if (mDisplayFilter == null) {
            mDisplayFilter = new GLImageFilter(mSurfaceView.getContext());
        } else {
            mDisplayFilter.initProgramHandle();
        }
    }

    private void onFilterSizeChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDisplayFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    private void createTexture() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            synchronized (mLock) {
                if (mBitmap != null && !mBitmap.isRecycled())
                    mInputTexture = OpenGLUtils.createTexture(mBitmap, mInputTexture);
            }
        }
    }

    public void release() {
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.release();
            mDisplayFilter = null;
        }
        if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.deleteTexture(mInputTexture);
            mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
    }
}
