
package com.eyedog.aftereffect.filters;

import android.content.Context;

import com.eyedog.aftereffect.utils.OpenGLUtils;

/**
 * 加载一张图片，需要倒过来
 */
public class GLImageInputFilter extends GLImageFilter {

    public GLImageInputFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_imagecenter_input.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_image_input.glsl"));
    }

    public GLImageInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
