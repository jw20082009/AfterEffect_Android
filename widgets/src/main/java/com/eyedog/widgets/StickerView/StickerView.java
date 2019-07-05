package com.eyedog.widgets.StickerView;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.eyedog.basic.multitouch.MoveGestureDetector;

public class StickerView extends android.support.v7.widget.AppCompatImageView implements MoveGestureDetector.OnMoveGestureListener {
    private final String TAG = "StickerView";
    boolean isCurrentSelected = false;
    MoveGestureDetector moveGestureDetector;

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        moveGestureDetector = new MoveGestureDetector(getContext(), this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return moveGestureDetector.onTouchEvent(event);
    }

    public boolean isCurrentSticker() {
        return isCurrentSelected;
    }

    @Override
    public boolean onMove(MoveGestureDetector detector) {
        PointF focusDelta = detector.getFocusDelta();
        Log.i(TAG, "onMove" + focusDelta.x + ";" + focusDelta.y + ";" + detector.getFocusX() + ";" + detector.getFocusY());
        setTranslationX(focusDelta.x + getTranslationX());
        setTranslationY(focusDelta.y + getTranslationY());
        return false;
    }

    @Override
    public boolean onMoveBegin(MoveGestureDetector detector) {
        Log.i(TAG, "onMoveBegin" + detector.getFocusDelta().x + ";" + detector.getFocusDelta().y + ";" + detector.getFocusX() + ";" + detector.getFocusY());
        return true;
    }

    @Override
    public void onMoveEnd(MoveGestureDetector detector) {

    }
}
