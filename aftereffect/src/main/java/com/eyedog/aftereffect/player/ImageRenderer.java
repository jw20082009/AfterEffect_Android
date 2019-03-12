
package com.eyedog.aftereffect.player;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.eyedog.aftereffect.filters.GLImageFilter;
import com.eyedog.aftereffect.filters.GLImageGaussianBlurFilter;
import com.eyedog.aftereffect.filters.GLImageInputFilter;
import com.eyedog.aftereffect.filters.StickerFilter;
import com.eyedog.aftereffect.utils.ImageUtils;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import com.eyedog.aftereffect.utils.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageRenderer extends BaseRenderer {
    private final String TAG = "ImageRenderer";

    protected StickerFilter mInputFilter;

    protected GLImageFilter mDisplayFilter;

    // 输入纹理大小
    protected int mTextureWidth;

    protected int mTextureHeight;

    // 控件视图大小
    protected int mViewWidth;

    protected int mViewHeight;

    //无需释放，在filter中release
    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE, mInputTexture2 =
        OpenGLUtils.GL_NOT_TEXTURE;

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;

    private Bitmap mBitmap, mBlurBitmap;

    private boolean mHasTextureChanged = false, mHasSurfaceCreated = false;

    private Object mLock = new Object();

    public ImageRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mHasTextureChanged = false;
    }

    public void setBitmap(final Bitmap bitmap) {
        mBitmap = bitmap;
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int scaledWidth = width;
        int scaledHeight = height;
        if (300 < width) {
            scaledWidth = 300;
            scaledHeight = (int) ((1.0f * 300 / width) * height);
        }
        mTextureWidth = scaledWidth;
        mTextureHeight = scaledHeight;
        mBlurBitmap =
            ImageUtils.blurBitmap(mSurfaceView.getContext(), mBitmap, scaledWidth, scaledHeight,
                20);
        synchronized (mLock) {
            mHasTextureChanged = true;
            if (mHasSurfaceCreated) {
                mSurfaceView.requestRender();
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        GLES30.glClearColor(0.5F, 0.5F, 0F, 1.0F);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            synchronized (mLock) {
                Log.i(TAG, "onSurfaceCreated ");
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
        synchronized (mLock) {
            Log.i(TAG, "onSurfaceChanged " + mHasTextureChanged);
            this.mHasSurfaceCreated = true;
        }
        GLES30.glViewport(0, 0, width, height);
        changeTexture(true);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        //如果onSurfaceChanged回调时bitmap还未被设置，需要在此设置滤镜参数
        changeTexture(false);
        int currentTexture = mInputTexture;
        if (mInputFilter != null) {
            currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer,
                mTextureBuffer);
        }
        mDisplayFilter.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);
    }

    private void changeTexture(boolean mNeedRequestRender) {
        if (mHasTextureChanged && mViewWidth > 0) {
            synchronized (mLock) {
                if (mHasTextureChanged && mViewWidth > 0) {
                    mHasTextureChanged = false;
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        mInputTexture = OpenGLUtils.createTexture(mBitmap, mInputTexture);
                    }
                    if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
                        mInputTexture2 = OpenGLUtils.createTexture(mBlurBitmap, mInputTexture2);
                        mInputFilter.setStickerTextureId(mInputTexture2);
                    }
                    onFilterSizeChanged();
                    if (mNeedRequestRender) {
                        mSurfaceView.requestRender();
                    }
                }
            }
        }
    }

    private void initFilters() {
        if (mInputFilter == null) {
            mInputFilter = new StickerFilter(mSurfaceView.getContext());
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
            //int scaledHeight = (int) (mViewWidth * (1.0f * mTextureHeight / mTextureWidth));
            //int x = 0;
            //int y = (int) ((mViewHeight - scaledHeight) / 2.0f);
            mDisplayFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    private void createTexture() {

    }

    public void release() {
        synchronized (mLock) {
            mHasSurfaceCreated = false;
            mHasTextureChanged = false;
        }
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.release();
            mDisplayFilter = null;
        }
    }
}
