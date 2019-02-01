package com.eyedog.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * created by jw200 at 2019/1/24 21:27
 **/
public class ColorFulTextView extends AppCompatTextView {
    Paint mColorFulPaint;
    int mStartColor = -1, mEndColor = -1;
    boolean mNeedRefreshColor = false;

    public ColorFulTextView(Context context) {
        this(context, null);
    }

    public ColorFulTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFulTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setColorFulColor(int start, int end) {
        mStartColor = start;
        mEndColor = end;
        if (getWidth() > 0) {
            refreshColor(start, end);
            mNeedRefreshColor = false;
        } else {
            mNeedRefreshColor = true;
        }
    }

    private void refreshColor(int startColor, int endColor) {
        setBackgroundColor(Color.WHITE);
        setTextColor(Color.BLACK);
        mColorFulPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorFulPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        mColorFulPaint.setColor(Color.BLACK);
        mColorFulPaint.setShader(
            new LinearGradient(0, getHeight() / 2.0f, getWidth(), getHeight() / 2.0f,
                new int[] { startColor, endColor }, null,
                LinearGradient.TileMode.CLAMP));
        invalidate();
    }

    public void clearColorFulColor() {
        mStartColor = -1;
        mEndColor = -1;
        mNeedRefreshColor = false;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mNeedRefreshColor) {
            mNeedRefreshColor = false;
            refreshColor(mStartColor, mEndColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mStartColor != -1 && mEndColor != -1 && mColorFulPaint != null) {
            canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), mColorFulPaint);
        }
    }
}
