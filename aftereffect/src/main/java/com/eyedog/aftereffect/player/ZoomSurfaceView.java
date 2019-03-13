package com.eyedog.aftereffect.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.eyedog.basic.multitouch.VersionedGestureDetector;

/**
 * created by jw200 at 2019/3/13 16:12
 **/
public class ZoomSurfaceView extends ImageGLSurfaceView
    implements VersionedGestureDetector.OnGestureListener {
    private final String TAG = "ZoomSurfaceView";
    private VersionedGestureDetector mScaleDragDetector;

    public ZoomSurfaceView(Context context) {
        this(context, null);
    }

    public ZoomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDragDetector = VersionedGestureDetector.newInstance(context, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (null != mScaleDragDetector && mScaleDragDetector.onTouchEvent(event)) {
            handled = true;
        }
        return handled;
    }

    @Override
    public void onDrag(float dx, float dy) {
        Log.i(TAG, "onDrag:" + dx + "*" + dy);
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        Log.i(TAG, "onFling:"
            + startX
            + ";"
            + startY
            + ";velocityX:"
            + velocityX
            + ";velocityY:"
            + velocityY);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY, float previousSpan,
        float currentSpan) {
        Log.i(TAG, "onScale:"
            + scaleFactor
            + ";"
            + focusX
            + ";"
            + focusY
            + ";"
            + previousSpan
            + ";"
            + currentSpan);
        scaleSize(scaleFactor);
        requestRender();
    }

    @Override
    public void onScaleEnd() {
        Log.i(TAG, "onScaleEnd");
    }
}
