
package com.eyedog.aftereffect.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eyedog.aftereffect.R;

public class ImageGLSurfaceView extends BaseGLSurfaceView {

    public ImageGLSurfaceView(Context context) {
        super(context);
    }

    public ImageGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageUrl("ss");
    }

    public void setImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
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
    public void onPause() {
        super.onPause();
        getRenderer().release();
    }

    @Override
    protected ImageRenderer getRenderer() {
        if (mRenderer == null) {
            mRenderer = new ImageRenderer(this);
        }
        return (ImageRenderer) mRenderer;
    }
}
