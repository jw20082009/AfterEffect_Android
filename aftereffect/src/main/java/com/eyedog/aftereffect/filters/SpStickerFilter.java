package com.eyedog.aftereffect.filters;

import android.content.Context;
import android.opengl.GLES30;
import com.eyedog.aftereffect.utils.OpenGLUtils;

/**
 * created by jw200 at 2019/3/12 21:16
 **/
public class SpStickerFilter extends GLImageFilter {

    protected int mInputTexture2Handle;//foreground texture
    protected int size;
    protected int center;
    protected int theta;
    protected int alpha;
    protected int blendMode;
    protected int mirrorMode;
    protected int aspectRatio;
    protected int s;
    protected int c;
    protected int blendColor;

    protected int mStickerTextureId = OpenGLUtils.GL_NOT_TEXTURE;
    protected Vec2 mSize = new Vec2(0.7f, 0.5f);
    protected Vec2 mCenter = new Vec2(0.5f, 0.5f);
    protected float mTheta = 0;
    protected float mAlpha = 1.0F;
    protected int mBlendMode = 1;
    protected int mMirrorMode = 0;
    protected float mAspectRatio = 1.0f;
    protected float mS = 0f;
    protected float mC = 1f;
    protected float[] mBlendColor = new float[] { 0f, 0f, 0f, 0.0f };//r,g,b,a

    public SpStickerFilter(Context context) {
        this(context, VERTEX_SHADER,
            OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_sticker.glsl"));
    }

    public SpStickerFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        setTheta(0);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mInputTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle,
                "inputImageTexture2");
            size = GLES30.glGetUniformLocation(mProgramHandle, "size");
            center = GLES30.glGetUniformLocation(mProgramHandle, "center");
            theta = GLES30.glGetUniformLocation(mProgramHandle, "theta");
            alpha = GLES30.glGetUniformLocation(mProgramHandle, "alpha");
            blendMode = GLES30.glGetUniformLocation(mProgramHandle, "blendMode");
            mirrorMode = GLES30.glGetUniformLocation(mProgramHandle, "mirrorMode");
            aspectRatio = GLES30.glGetUniformLocation(mProgramHandle, "aspectRatio");
            s = GLES30.glGetUniformLocation(mProgramHandle, "s");
            c = GLES30.glGetUniformLocation(mProgramHandle, "c");
            blendColor = GLES30.glGetUniformLocation(mProgramHandle, "blendColor");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mStickerTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mInputTexture2Handle, mStickerTextureId, 1);
        }
        if (mSize != null) {
            GLES30.glUniform2f(size, mSize.wRatio, mSize.hRatio);
        }
        if (mCenter != null) {
            GLES30.glUniform2f(center, mCenter.wRatio, mCenter.hRatio);
        }
        if (mBlendColor != null) {
            GLES30.glUniform4f(blendColor, mBlendColor[0], mBlendColor[1], mBlendColor[2],
                mBlendColor[3]);
        }
        GLES30.glUniform1f(aspectRatio, mAspectRatio);
        GLES30.glUniform1f(alpha, mAlpha);
        GLES30.glUniform1i(blendMode, mBlendMode);
        GLES30.glUniform1i(mirrorMode, mMirrorMode);
        GLES30.glUniform1f(theta, mTheta);
        GLES30.glUniform1f(s, mS);
        GLES30.glUniform1f(c, mC);
    }

    public void setStickerTextureId(int textureId) {
        mStickerTextureId = textureId;
    }

    public void setSize(Vec2 size) {
        if (size == null) {
            return;
        }
        this.mSize = size;
    }

    public void setTheta(float degree) {
        mTheta = degree;
        mS = (float) Math.sin(mTheta);
        mC = (float) Math.cos(mTheta);
    }

    public void setBlendColor(float[] blendColor) {
        mBlendColor = blendColor;
    }

    public static class Vec2 {
        public float wRatio;
        public float hRatio;

        public Vec2(float wRatio, float hRatio) {
            this.wRatio = wRatio;
            this.hRatio = hRatio;
        }
    }

    @Override
    public void release() {
        super.release();
        if (mStickerTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[] { mStickerTextureId }, 0);
            mStickerTextureId = OpenGLUtils.GL_NOT_TEXTURE;
        }
    }
}
