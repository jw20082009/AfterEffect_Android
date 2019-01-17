package com.eyedog.aftereffect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

/**
 * created by jw200 at 2018/12/21 17:46
 **/
public class HomeLogoImageView extends SVGAImageView {
    private final Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mMaskLogo;
    private final Drawable mNormalMask = new ColorDrawable(0xFFE8E8E8);
    private final Drawable mLightMask = new ColorDrawable(0xFFEC0000);
    private boolean enableSvgAnim = true;
    private float mMaskOffset = 0.0f;

    private boolean mRunning = true;

    public HomeLogoImageView(Context context) {
        this(context, null);
    }

    public HomeLogoImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeLogoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HomeLogoImageView(Context context, AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setCallback(mSvgaCallback);
    }

    public void setRunning(boolean running) {
        this.mRunning = running;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void setEnableSvgAnim(boolean enableSvgAnim) {
        if (this.enableSvgAnim != enableSvgAnim) {
            this.enableSvgAnim = enableSvgAnim;
            if (!enableSvgAnim) {
                stopAnimation();
                stepToFrame(0, false);
            }
            invalidate();
        }
    }

    public void setMaskOffset(float offset) {
        if (offset < 0.0f) offset = 0.0f;
        if (offset > 1.0f) offset = 1.0f;
        if (mMaskOffset != offset) {
            mMaskOffset = offset;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMaskLogo == null) {
            Drawable drawable = getContext().getDrawable(R.drawable.logo_echo);
            if (drawable instanceof BitmapDrawable) {
                drawable.setBounds(0, 0, getWidth(), getHeight());
                mMaskLogo = ((BitmapDrawable) drawable).getBitmap();
                if (mMaskLogo != null) {
                    mMaskLogo.setHasAlpha(true);
                }
            }
        }
        if (mMaskLogo == null) return;
        if (enableSvgAnim) {
            int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            super.onDraw(canvas);
            canvas.drawBitmap(mMaskLogo, 0, 0, mMaskPaint);
            canvas.restoreToCount(sc);
        } else {
            int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            //super.onDraw(canvas);
            final int width = getWidth();
            final int height = getHeight();
            final int lightRight = (int) (width * mMaskOffset);
            mLightMask.setBounds(0, 0, lightRight, height);
            mNormalMask.setBounds(lightRight, 0, width, height);
            mLightMask.draw(canvas);
            mNormalMask.draw(canvas);
            canvas.drawBitmap(mMaskLogo, 0, 0, mMaskPaint);
            canvas.restoreToCount(sc);
        }
    }

    private final SVGACallback mSvgaCallback = new SVGACallback() {
        @Override
        public void onPause() {

        }

        @Override
        public void onFinished() {

        }

        @Override
        public void onRepeat() {
            if (!mRunning) {
                stopAnimation();
                stepToFrame(0, false);
            }
        }

        @Override
        public void onStep(int frame, double percentage) {

        }
    };
}