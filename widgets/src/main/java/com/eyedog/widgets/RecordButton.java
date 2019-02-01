package com.eyedog.widgets;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.eyedog.basic.utils.DensityUtil;

/**
 * created by jw200 at 2019/2/1 17:09
 **/
public class RecordButton extends View {

    private final String TAG = "RecordButton";

    private final int START_COLOR = Color.parseColor("#ffffffff");//默认前景环色值
    private final int END_COLOR = Color.parseColor("#33ffffff");//放大后背景环色值
    private final int BG_COLOR = Color.parseColor("#4cffffff");//背景圆圈色值
    private final int RING_SMALL_SIZE, RING_LARGE_SIZE, RING_BORDER_SIZE, BACKGROUND_LARGE_SIZE;
    private float ringCenterX, ringCenterY;
    private Paint backgroundPaint, ringPaint, progressPaint;
    private final int STATUS_IDLE = 0X00;
    private final int STATUS_ZOOM_OUT = 0X01;
    private final int STATUS_LARGE_IDLE = 0x02;
    private final int STATUS_PROGRESSING = 0X03;
    private final int STATUS_ZOOM_IN = 0X04;
    private int mStatus = STATUS_IDLE, mLastStatus = STATUS_IDLE;
    private final float ZOOM_SCALE = 1.0F / 2;
    private final long MAX_PROGRESS = 15 * 1000;
    private float mCurrentProgress = 0;

    private IRecordListener listener;

    private OnClickListener onClickListener;

    private ValueAnimator smallAnimator, largeAnimator;

    public static final long ONCLICK_TIME = 180l;

    AnimEntity startAnim, endAnim, currentAnim;

    boolean canClick = false, mNeedRecord = false;

    private long mStartProgressTime = 0L;

    private int measureWidth, measureHeight;

    private int touchSlop;

    boolean concernEvent = false;
    float totalProgress;
    float mDownX, mDownY;
    long mDownTime;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        RING_SMALL_SIZE = DensityUtil.dip2px(getContext(), 80);
        RING_LARGE_SIZE = DensityUtil.dip2px(getContext(), 110);
        RING_BORDER_SIZE = DensityUtil.dip2px(getContext(), 6);
        BACKGROUND_LARGE_SIZE = DensityUtil.dip2px(getContext(), 120);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStrokeWidth(RING_BORDER_SIZE);
        backgroundPaint.setColor(BG_COLOR);
        backgroundPaint.setDither(true);
        backgroundPaint.setFilterBitmap(true);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(Color.parseColor("#FDB709"));
        progressPaint.setStrokeWidth(RING_BORDER_SIZE);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(RING_BORDER_SIZE);
        ringPaint.setColor(START_COLOR);

        startAnim = new AnimEntity();
        startAnim.bgRadius = RING_SMALL_SIZE / 2;
        startAnim.foreRadius = (RING_SMALL_SIZE - RING_BORDER_SIZE) / 2;
        startAnim.foreColor = START_COLOR;

        endAnim = new AnimEntity();
        endAnim.bgRadius = RING_LARGE_SIZE / 2;
        endAnim.foreRadius =
            (RING_LARGE_SIZE - RING_BORDER_SIZE) / 2 - DensityUtil.dip2px(getContext(), 2);
        endAnim.foreColor = END_COLOR;
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        initParams();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != this.measureWidth || h != this.measureHeight) {
            this.measureWidth = w;
            this.measureHeight = h;
            ringCenterX = measureWidth / 2.0f;
            ringCenterY = measureHeight - BACKGROUND_LARGE_SIZE / 2.0f;
            progressPaint.setShader(new SweepGradient(ringCenterX, ringCenterY, new int[] {
                Color.parseColor("#FDB709"), Color.parseColor("#E0202D"),
                Color.parseColor("#FDB709")
            }, new float[] { 0.25f, 0.5f, 1.0f }));
        }
    }

    private void initParams() {
        mNeedRecord = false;
        mDownTime = 0;
        mDownX = mDownY = 0;
        mCurrentProgress = 0;
        currentAnim = startAnim;
        concernEvent = false;
    }

    public void startRecord() {
        mNeedRecord = true;
        if (mStatus == STATUS_LARGE_IDLE) {
            startProgressing();
        }
    }

    public void cancelRecord() {
        mNeedRecord = false;
        if (largeAnimator != null) {
            largeAnimator.cancel();
        }
        showZoomIn();
    }

    private void startProgressing() {
        mStartProgressTime = SystemClock.elapsedRealtime();
        setStatus(STATUS_PROGRESSING);
        notifyStatus();
        progressing();
    }

    private void progressing() {
        if (mStartProgressTime > 0) {
            long elapsedTime = SystemClock.elapsedRealtime() - mStartProgressTime;
            if (mStatus == STATUS_PROGRESSING && elapsedTime < MAX_PROGRESS) {
                mCurrentProgress = (1.0f * elapsedTime / MAX_PROGRESS) * 360;
                invalidate();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressing();
                    }
                }, 15);
            } else {
                showZoomIn();
            }
        }
    }

    private boolean isTouchRecord(float downX, float downY) {
        float halfLarge = RING_LARGE_SIZE / 2;
        return downX > ringCenterX - halfLarge && downX < ringCenterX + halfLarge
            && downY > ringCenterY - halfLarge && downY < ringCenterY + halfLarge;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = event.getX();
                mDownY = event.getY();
                mDownTime = SystemClock.elapsedRealtime();
                totalProgress = measureHeight - (measureHeight - mDownY);
                if (isTouchRecord(mDownX, mDownY)) {
                    concernEvent = true;
                    canClick = true;
                    showZoomOut();
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (concernEvent) {
                    if (listener != null) {
                        float moveY = event.getY();
                        if (moveY < mDownY && moveY >= (1 - ZOOM_SCALE) * totalProgress) {
                            listener.onProgressChanged(
                                1.0f * (mDownY - moveY) / (totalProgress
                                    * ZOOM_SCALE));
                        }
                    }
                }
                if ((event.getX() - mDownX) > touchSlop || (event.getY() - mDownY) > touchSlop) {
                    canClick = false;
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (concernEvent) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    concernEvent = false;
                    long offsetTime = SystemClock.elapsedRealtime() - mDownTime;
                    if (offsetTime < ONCLICK_TIME && canClick) {
                        if (onClickListener != null) {
                            onClickListener.onClick(this);
                        }
                    }
                    cancelRecord();
                }
            }
            break;
        }
        return concernEvent || super.onTouchEvent(event);
    }

    private void showZoomIn() {
        if (mStatus != STATUS_ZOOM_IN && mStatus != STATUS_IDLE) {
            setStatus(STATUS_ZOOM_IN);
            notifyStatus();
            if (smallAnimator == null) {
                smallAnimator = ValueAnimator.ofObject(new ZoomOutEvaluator(), currentAnim,
                    startAnim);
                smallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        currentAnim = (AnimEntity) valueAnimator.getAnimatedValue();
                        invalidate();
                    }
                });
                smallAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        initParams();
                        setStatus(STATUS_IDLE);
                        notifyStatus();
                        invalidate();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        initParams();
                        setStatus(STATUS_IDLE);
                        notifyStatus();
                        invalidate();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
            smallAnimator.start();
        } else {
            Log.i(TAG, "showZoomIn failed with status:" + mStatus);
        }
    }

    private void showZoomOut() {
        if (mStatus == STATUS_IDLE) {
            setStatus(STATUS_ZOOM_OUT);
            notifyStatus();
            if (largeAnimator == null) {
                largeAnimator = ValueAnimator.ofObject(new ZoomOutEvaluator(), startAnim, endAnim);
                largeAnimator.setDuration(ONCLICK_TIME);
                largeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        currentAnim = (AnimEntity) valueAnimator.getAnimatedValue();
                        invalidate();
                    }
                });
                largeAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        setStatus(STATUS_LARGE_IDLE);
                        notifyStatus();
                        if (mNeedRecord) {
                            startProgressing();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        setStatus(STATUS_LARGE_IDLE);
                        notifyStatus();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
            largeAnimator.start();
        } else {
            Log.i(TAG, "showZoomOut failed with status:" + mStatus);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(ringCenterX, ringCenterY, currentAnim.bgRadius, backgroundPaint);
        ringPaint.setColor(currentAnim.foreColor);
        canvas.drawCircle(ringCenterX, ringCenterY, currentAnim.foreRadius, ringPaint);
        RectF rectF =
            new RectF(ringCenterX - currentAnim.foreRadius, ringCenterY - currentAnim.foreRadius,
                ringCenterX + currentAnim.foreRadius, ringCenterY + currentAnim.foreRadius);
        canvas.drawArc(rectF, -90, mCurrentProgress, false, progressPaint);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }

    public void setOnRecordListener(IRecordListener listener) {
        this.listener = listener;
    }

    class AnimEntity {

        public float bgRadius = 70f, foreRadius = 70f;

        public int foreColor = Color.WHITE;

        @Override
        public String toString() {
            return "bgRadius:" + bgRadius + ";foreRadius:" + foreRadius + ";foreColor:" + foreColor;
        }
    }

    class ZoomOutEvaluator implements TypeEvaluator<AnimEntity> {

        @Override
        public AnimEntity evaluate(float v, AnimEntity animEntity, AnimEntity t1) {
            AnimEntity startEntity = animEntity;
            AnimEntity endEntity = t1;

            AnimEntity animEntity1 = new AnimEntity();
            animEntity1.bgRadius = startEntity.bgRadius
                + v * (endEntity.bgRadius - startEntity.bgRadius);
            animEntity1.foreRadius = startEntity.foreRadius
                + v * (endEntity.foreRadius - startEntity.foreRadius);
            int startInt = startEntity.foreColor;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = endEntity.foreColor;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            animEntity1.foreColor = (startA + (int) (v * (endA - startA))) << 24
                | (startR + (int) (v * (endR - startR))) << 16
                | (startG + (int) (v * (endG - startG))) << 8
                | (startB + (int) (v * (endB - startB)));
            return animEntity1;
        }
    }

    private void setStatus(int status) {
        mLastStatus = mStatus;
        mStatus = status;
    }

    private void notifyStatus() {
        if (listener != null) {
            switch (mStatus) {
                case STATUS_ZOOM_OUT:
                    listener.onRecordPreStart();
                    break;
                case STATUS_LARGE_IDLE:
                    listener.onRecordStarted();
                    break;
                case STATUS_PROGRESSING:
                    break;
                case STATUS_ZOOM_IN:
                    if (mLastStatus == STATUS_PROGRESSING) {
                        listener.onRecordPreEnd();
                    } else if (mLastStatus == STATUS_LARGE_IDLE) {
                        listener.onRecordStarted();
                        listener.onRecordPreEnd();
                    }
                    break;
                case STATUS_IDLE:
                    if (mLastStatus == STATUS_ZOOM_IN) {
                        listener.onRecordEnded();
                    }
                    break;
            }
        }
    }

    public interface IRecordListener {
        void onRecordPreStart();

        void onRecordStarted();

        void onRecordPreEnd();

        void onRecordEnded();

        void onProgressChanged(float progress);
    }
}
