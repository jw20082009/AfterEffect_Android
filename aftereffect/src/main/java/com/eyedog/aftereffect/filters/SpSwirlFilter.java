package com.eyedog.aftereffect.filters;

import android.content.Context;
import android.opengl.GLES30;
import com.eyedog.aftereffect.utils.OpenGLUtils;

/**
 * created by jw200 at 2019/3/28 14:40
 **/
public class SpSwirlFilter extends GLImageFilter {

    protected int center;
    protected int radius;
    protected int angle;

    protected float[] mCenter = new float[] { 0.5f, 0.5f };
    protected float mRadius = 0.2f;
    protected float mAngle = 1.0f;

    public SpSwirlFilter(Context context) {
        this(context, VERTEX_SHADER,
            OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_swirl.glsl"));
    }

    public SpSwirlFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            center = GLES30.glGetUniformLocation(mProgramHandle, "center");
            radius = GLES30.glGetUniformLocation(mProgramHandle, "radius");
            angle = GLES30.glGetUniformLocation(mProgramHandle, "angle");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mCenter != null) {
            GLES30.glUniform2f(center, mCenter[0], mCenter[1]);
        }
        GLES30.glUniform1f(radius, mRadius);
        GLES30.glUniform1f(angle, mAngle);
    }

    public void setCenter(float[] center) {
        this.mCenter = center;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    public void setAngle(float angle) {
        this.mAngle = angle;
    }
}
