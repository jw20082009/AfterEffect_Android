package com.eyedog.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * created by jw200 at 2019/2/1 13:50
 **/
public class BgmMonitorView extends View {

    private final String TAG = "BgmMonitorView";
    private BitmapShader mWaveShader;
    private Matrix mShaderMatrix;
    private Paint mViewPaint;
    private boolean mShowWave = true;
    private double mDefaultAngularFrequency;
    private float mAmplitudeValue;
    private float mDefaultWaterLevel;
    private float mDefaultWaveLength;
    private static float mAmplitudeRatio = 0f;
    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_WAVE_LENGTH_RATIO = 1.0f;
    private static final float DEFAULT_WAVE_SHIFT_RATIO = 0.0f;
    private float mWaterLevelRatio = DEFAULT_WATER_LEVEL_RATIO;
    private float mWaveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO;
    private AnimatorSet mAnimatorSet;

    public BgmMonitorView(Context context) {
        this(context, null);
    }

    public BgmMonitorView(Context context,
        @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BgmMonitorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BgmMonitorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mShaderMatrix = new Matrix();
        mViewPaint = new Paint();
        mViewPaint.setAntiAlias(true);
        initAnimation();
    }

    private void initAnimation() {
        // horizontal animation.
        // wave waves infinitely.
        ValueAnimator initAnimator = ValueAnimator.ofFloat(0, 0.125f);
        initAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAmplitudeRatio = (float) animation.getAnimatedValue();
                createShader();
                invalidate();
            }
        });
        initAnimator.setDuration(500);

        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
            this, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(waveShiftAnim).after(initAnimator);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createShader();
    }

    public void start() {
        setShowWave(true);
        if (mAnimatorSet != null) {
            mAnimatorSet.start();
        }
    }

    public void cancel() {
        setShowWave(false);
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
        }
    }

    public void setShowWave(boolean showWave) {
        this.mShowWave = showWave;
    }

    public void setWaveShiftRatio(float waveShiftRatio) {
        if (mWaveShiftRatio != waveShiftRatio) {
            mWaveShiftRatio = waveShiftRatio;
            invalidate();
        }
    }

    private void createShader() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        mDefaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / getWidth();
        mAmplitudeValue = getHeight() * mAmplitudeRatio;
        mDefaultWaterLevel = getHeight() * DEFAULT_WATER_LEVEL_RATIO;
        mDefaultWaveLength = getWidth();
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint wavePaint = new Paint();
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);
        wavePaint.setShader(
            new LinearGradient(getWidth() / 2.0f, 0, getWidth() / 2.0f, getHeight(),
                new int[] { 0xFFFFCF40, 0xFFFFBF00 },
                null, Shader.TileMode.REPEAT));
        final int endX = getWidth() + 1;
        final int endY = getHeight() + 1;
        float[] waveY = new float[endX];
        Path path = new Path();
        path.moveTo(0, mDefaultWaterLevel);
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * mDefaultAngularFrequency;
            float beginY = (float) (mDefaultWaterLevel + mAmplitudeValue * Math.sin(wx));
            path.lineTo(beginX, beginY);
            waveY[beginX] = beginY;
        }
        canvas.drawPath(path, wavePaint);
        wavePaint.setShader(null);
        wavePaint.setColor(0x99ffffff);
        final int wave2Shift = (int) (mDefaultWaveLength / 4);
        path = new Path();
        path.moveTo(0, waveY[wave2Shift]);
        for (int beginX = 0; beginX < endX; beginX++) {
            path.lineTo(beginX, waveY[(beginX + wave2Shift) % endX]);
        }
        canvas.drawPath(path, wavePaint);
        mWaveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        mViewPaint.setShader(mWaveShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // modify paint shader according to mShowWave state
        if (mShowWave && mWaveShader != null) {
            if (mViewPaint.getShader() == null) {
                mViewPaint.setShader(mWaveShader);
            }
            mShaderMatrix.setTranslate(mWaveShiftRatio * getWidth(),
                (DEFAULT_WATER_LEVEL_RATIO - mWaterLevelRatio) * getHeight());
            mWaveShader.setLocalMatrix(mShaderMatrix);
            float borderWidth = 0;
            canvas.drawRect(borderWidth, borderWidth, getWidth() - borderWidth,
                getHeight() - borderWidth, mViewPaint);
        } else {
            mViewPaint.setShader(null);
        }
    }
}
