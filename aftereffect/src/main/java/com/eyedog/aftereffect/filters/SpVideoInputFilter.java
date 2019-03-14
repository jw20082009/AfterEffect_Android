package com.eyedog.aftereffect.filters;

import android.content.Context;
import android.opengl.GLES30;
import com.eyedog.aftereffect.utils.OpenGLUtils;

/**
 * created by jw200 at 2019/3/14 16:12
 **/
public class SpVideoInputFilter extends GLImageFilter {

    protected int uMatrix;

    public SpVideoInputFilter(Context context) {
        this(context,
            OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_video_input.glsl"),
            OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_oes_input.glsl"));
    }

    public SpVideoInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            uMatrix = GLES30.glGetUniformLocation(mProgramHandle, "uMatrix");
        }
    }
}
