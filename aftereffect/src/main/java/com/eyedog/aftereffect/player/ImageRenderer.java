
package com.eyedog.aftereffect.player;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import com.eyedog.aftereffect.filters.SpStickerFilter;
import com.eyedog.aftereffect.utils.ImageUtils;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import javax.microedition.khronos.opengles.GL10;

public class ImageRenderer extends BaseRenderer {
    private final String TAG = "ImageRenderer";

    private final float LARGE_RATIO = 0.7F;

    private final float SMALL_RATIO = 0.4F;

    private float mCurrentRatio = LARGE_RATIO;

    private int mScaleMode = 0;//0:宽缩放；1:高缩放

    //无需释放，在filter中release
    protected int mInputTexture2 = OpenGLUtils.GL_NOT_TEXTURE;

    private Bitmap mBitmap, mBlurBitmap;

    public ImageRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
    }

    public void setBitmap(final Bitmap bitmap) {
        mBitmap = bitmap;
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int scaledWidth = width;
        int scaledHeight = height;
        onInputSizeChanged(1080, 1920);
        if (160 < width) {
            scaledWidth = 200;
            scaledHeight = (int) (1.0f * scaledWidth / (1.0f * mIncommingWidth / mIncommingHeight));
        }
        mBlurBitmap =
            ImageUtils.blurBitmap(mSurfaceView.getContext(), mBitmap, scaledWidth, scaledHeight,
                25);
        new Palette.Builder(mBitmap).generate(
            new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@NonNull Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    int rgb = swatch.getRgb();
                    int red = Color.red(rgb);
                    int green = Color.green(rgb);
                    int blue = Color.blue(rgb);
                    ((SpStickerFilter) mInputFilter).setBlendColor(
                        new float[] { red / 255f, green / 255f, blue / 255f, 0.4f });
                    //if (mHasInputSizeChanged) {
                        mSurfaceView.requestRender();
                    //}
                }
            });
        synchronized (mLock) {
            if (mHasSurfaceChanged) {
                mSurfaceView.requestRender();
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    @Override
    public int createInputTexture() {
        mInputTexture = OpenGLUtils.createTexture(mBlurBitmap, mInputTexture);
        return mInputTexture;
    }

    @Override
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
        if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
            mInputTexture = OpenGLUtils.createTexture(mBlurBitmap, mInputTexture);
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mInputTexture2 = OpenGLUtils.createTexture(mBitmap, mInputTexture2);
            ((SpStickerFilter) mInputFilter).setStickerTextureId(mInputTexture2);
            initSize();
        }
    }

    private void initSize() {
        if (mBitmap != null && !mBitmap.isRecycled() && mSurfaceWidth > 0 && mSurfaceHeight > 0) {
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            float w = mSurfaceWidth * LARGE_RATIO;
            float h = mSurfaceHeight * LARGE_RATIO;
            float scaledW = w;
            float scaledH = height / (width / w);
            mScaleMode = 0;
            mCurrentRatio = LARGE_RATIO;
            if (scaledH > h) {
                mScaleMode = 1;
                scaledH = h;
                scaledW = width / (height / h);
            }
            ((SpStickerFilter) mInputFilter).setSize(
                new SpStickerFilter.Vec2(scaledW / mSurfaceWidth, scaledH / mSurfaceHeight));
        }
    }

    public void scaleSize(float scale) {
        if (mBitmap != null
            && !mBitmap.isRecycled()
            && mSurfaceWidth > 0
            && mSurfaceHeight > 0
            && mCurrentRatio != 0) {
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            float scaleRatio = mCurrentRatio * scale;
            if (scaleRatio > 3.0f) {
                scaleRatio = 3.0f;
            } else if (scaleRatio < SMALL_RATIO) {
                scaleRatio = SMALL_RATIO;
            }
            float w = mSurfaceWidth * scaleRatio;
            float h = mSurfaceHeight * scaleRatio;
            float scaledW = w;
            float scaledH = h;
            if (mScaleMode == 0) {
                scaledW = w;
                scaledH = height / (width / w);
            } else {
                scaledH = h;
                scaledW = width / (height / h);
            }
            mCurrentRatio = scaleRatio;
            ((SpStickerFilter) mInputFilter).setSize(
                new SpStickerFilter.Vec2(scaledW / mSurfaceWidth, scaledH / mSurfaceHeight));
        }
    }

    @Override
    protected SpStickerFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new SpStickerFilter(mSurfaceView.getContext());
        }
        return (SpStickerFilter) mInputFilter;
    }
}
