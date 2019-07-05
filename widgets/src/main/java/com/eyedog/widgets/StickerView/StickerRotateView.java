package com.eyedog.widgets.StickerView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.eyedog.basic.multitouch.MoveGestureDetector;
import com.eyedog.basic.multitouch.RotateGestureDetector;

public class StickerRotateView extends android.support.v7.widget.AppCompatImageView implements MoveGestureDetector.OnMoveGestureListener, RotateGestureDetector.OnRotateGestureListener {
    private final String TAG = "StickerRotateView";
    MoveGestureDetector mMoveGestureDetector;
    ViewGroup parentView;

    public StickerRotateView(Context context) {
        super(context);
        init();
    }

    public StickerRotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerRotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        mMoveGestureDetector = new MoveGestureDetector(getContext(), this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mMoveGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup parentView = (ViewGroup) getParent();
        if (parentView instanceof IStickerView) {
            this.parentView = parentView;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.parentView = null;
    }

    @Override
    public boolean onMove(MoveGestureDetector detector) {
        float scaleX = detector.getFocusDelta().x;
        float scaleY = detector.getFocusDelta().y;
        Log.i(TAG, "onMove:" + scaleX + ";" + scaleY);
        if (parentView != null) {
//            ViewGroup.LayoutParams params = parentView.getLayoutParams();
//            params.width;
//
//            parentView.setScaleX(scaleX);
//            parentView.setScaleY(scaleY);

        }
        return false;
    }

    @Override
    public boolean onMoveBegin(MoveGestureDetector detector) {
        return this.parentView != null;
    }

    @Override
    public void onMoveEnd(MoveGestureDetector detector) {

    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        float rotate = detector.getRotationDegreesDelta();
        parentView.setRotation(parentView.getRotation() + rotate);
        return false;
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
        return true;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector) {

    }
}
