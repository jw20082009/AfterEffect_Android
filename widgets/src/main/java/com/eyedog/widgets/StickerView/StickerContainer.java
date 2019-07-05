package com.eyedog.widgets.StickerView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.eyedog.basic.multitouch.MoveGestureDetector;
import com.eyedog.widgets.R;

public class StickerContainer extends FrameLayout implements IStickerView, MoveGestureDetector.OnMoveGestureListener {
    final String TAG = "StickerContainer";
    boolean isCurrentSelected = false;
    MoveGestureDetector moveGestureDetector;
    PointF centerPoint;

    public StickerContainer(Context context) {
        super(context);
        init();
    }

    public StickerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public StickerContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setClickable(true);
        LayoutInflater.from(getContext()).inflate(R.layout.view_sticker, this, true);
        moveGestureDetector = new MoveGestureDetector(getContext(), this);
        setBackgroundColor(Color.GRAY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return moveGestureDetector.onTouchEvent(event);
    }

    public void addStickerView(View view) {
        if (view == null)
            return;
        addView(view);
        setClickable(true);
    }

    @Override
    public boolean isCurrentSticker() {
        return isCurrentSelected;
    }

    @Override
    public PointF getCenterPoint() {
        return null;
    }

    @Override
    public boolean onMove(MoveGestureDetector detector) {
        PointF focusDelta = detector.getFocusDelta();
        Log.i(TAG, "onMove" + focusDelta.x + ";" + focusDelta.y + ";" + detector.getFocusX() + ";" + detector.getFocusY());
        setTranslationX(focusDelta.x + getTranslationX());
        setTranslationY(focusDelta.y + getTranslationY());
        isCurrentSelected = true;
        return false;
    }

    @Override
    public boolean onMoveBegin(MoveGestureDetector detector) {
        return true;
    }

    @Override
    public void onMoveEnd(MoveGestureDetector detector) {

    }
}
