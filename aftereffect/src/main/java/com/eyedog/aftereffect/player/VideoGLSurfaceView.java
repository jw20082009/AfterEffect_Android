
package com.eyedog.aftereffect.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eyedog.aftereffect.R;

/**
 * created by jw200 at 2019/3/14 15:03
 **/
public class VideoGLSurfaceView extends BaseGLSurfaceView {

    public VideoGLSurfaceView(Context context) {
        super(context);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Glide.with(getContext())
            .asBitmap()
            .load(R.drawable.camera_img)
            .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource,
                    @Nullable Transition<? super Bitmap> transition) {
                    getRenderer().setBitmap(resource);
                }
            });
    }

    @Override
    protected VideoRenderer getRenderer() {
        if (mRenderer == null) {
            mRenderer = new VideoRenderer(this);
        }
        return (VideoRenderer) mRenderer;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRenderer().startPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        getRenderer().stopPlay();
        release();
    }

    public void release() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                getRenderer().release();
            }
        });
    }
}
