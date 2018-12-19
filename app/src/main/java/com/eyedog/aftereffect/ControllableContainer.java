package com.eyedog.aftereffect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import com.eyedog.aftereffect.DashLine.DashLineView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangtian on 2018/8/8.
 * E-Mail Address: 443275705@qq.com
 */
public class ControllableContainer extends FrameLayout {

    public static final String TAG = "ControllableContainer";
    //控件是否能平移
    private boolean mCanTranslate = false;
    //控件是否能旋转
    private boolean mCanRotate = false;
    //控件是否能缩放
    private boolean mCanScale = false;

    private boolean mCanClick = false;

    private boolean mCanLongClick = false;

    //上一次单点触控的坐标
    private PointF mLastSinglePoint = new PointF();
    //手指touchDown坐标
    private PointF mTouchDownPoint = new PointF();
    //记录上一次两只手指构成的一个向量
    private PointF mLastVector = new PointF();
    //记录上一次两只手指之间的距离
    private float mLastDist;

    private View target = null;

    private View extendEventView = null;

    private List<OnControllerListener> onControllerListeners = new ArrayList<>();
    private RectF rect;
    private boolean isHide;
    private boolean isTouchUp;
    private int touchSlop;
    private OnClickListener mOnClickListener;
    private List<Integer> mLefts, mTops, mRights, mBottoms;
    private int mMeasureWidth, mMeasureHeight;
    private final long mLongClickTime = 400L;
    private int mTopOffset = -1;
    private boolean mMultiTouch;
    private float mDownX, mDownY;
    private long mDownTime;

    private boolean mIsDragging;
    private float mLastDragX, mLastDragY;
    private OnGestureListener mGestureListener;

    private boolean enableLongClickFlag = false;
    private boolean mHasDashLine;
    private float mDashRotation;
    RotateChecker mRotateChecher;
    Runnable longClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (target != null) {
                mCanLongClick = true;
                mCanTranslate = false;
                notifyLongClick();
            }
        }
    };
    private DashLineView mDashLineView;

    public ControllableContainer(@NonNull Context context) {
        super(context);
        init();
    }

    public ControllableContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mRotateChecher = new RotateChecker();
    }

    public void setExtendEventView(View extendEventView) {
        this.extendEventView = extendEventView;
    }

    public void setDashLineView(DashLineView dashLineView) {
        this.mDashLineView = dashLineView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
    }

    /**
     * 当单点触控的时候可以进行平移操作
     * 当多点触控的时候：可以进行图片的缩放、旋转
     * ACTION_DOWN：标记能平移、不能旋转、不能缩放
     * ACTION_POINTER_DOWN：如果手指个数为2,标记不能平移、能旋转、能缩放
     * 记录平移开始时两手指的中点、两只手指形成的向量、两只手指间的距离
     * ACTION_MOVE：进行平移、旋转、缩放的操作。
     * ACTION_POINTER_UP：有一只手指抬起的时候，设置图片不能旋转、不能缩放，可以平移
     *
     * @param event 点击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            //单点触控，设置图片可以平移、不能旋转和缩放
            case MotionEvent.ACTION_DOWN:
                target = null;
                isTouchUp = false;
                mCanTranslate = true;
                mCanRotate = false;
                mCanScale = false;
                mCanClick = true;
                mMultiTouch = false;
                mIsDragging = false;
                mDownX = event.getX();
                mDownY = event.getY();
                mLastDragX = mDownX;
                mLastDragY = mDownY;
                if (mGestureListener != null) {
                    mGestureListener.onDragBegin();
                }
                mDownTime = SystemClock.elapsedRealtime();
                //记录单点触控的上一个单点的坐标
                mLastSinglePoint.set(event.getRawX(), event.getRawY());
                //记录touchDown坐标
                mTouchDownPoint.set(event.getRawX(), event.getRawY());
                target = getSinglePointView(event);
                if (enableLongClickFlag) {
                    removeCallbacks(longClickRunnable);
                    postDelayed(longClickRunnable, mLongClickTime);
                }
                if (target != null) {
                    bringChildToFront(target);
                    notifyOnTouchDown(target, true);
                }
                dispatchEvent(event);
                refreshDashLineCheckers();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                target = null;
                //多点触控，设置图片不能平移
                mCanTranslate = false;
                //多点触控，设置图片不能翻转
                mCanClick = false;
                mMultiTouch = true;
                //当手指个数为两个的时候，设置图片能够旋转和缩放
                if (event.getPointerCount() == 2) {
                    mCanRotate = true;
                    mCanScale = true;
                    //记录开始滑动前两个手指之间的距离
                    mLastDist = distance(event);
                    //设置向量，以便于计算角度
                    mLastVector.set(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
                    target = getMultiPointView(event);
                }
                if (target != null) {
                    bringChildToFront(target);
                    notifyOnTouchDown(target, false);
                } else {
                    dispatchEvent(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mDownX) > touchSlop
                    || Math.abs(event.getY() - mDownY) > touchSlop) {
                    mCanClick = false;
                    if (enableLongClickFlag) {
                        removeCallbacks(longClickRunnable);
                    }
                }

                if (target != null) {
                    //判断能否旋转操作
                    if (mCanRotate) {
                        //当前两只手指构成的向量
                        PointF vector = new PointF(event.getX(0) - event.getX(1),
                            event.getY(0) - event.getY(1));
                        //计算本次向量和上一次向量之间的夹角
                        float rotateDelta = calculateDeltaDegree(mLastVector, vector);
                        float rotation = target.getRotation();
                        rotateDelta = mRotateChecher.checkDash(rotateDelta, rotation);
                        float r = ((rotation % 360 + 360) % 360);
                        if (mDashLineView != null && rotateDelta == 0) {
                            if ((r > 360 - 45 || r <= 0 + 45) || (r > 180 - 45 && r <= 180 + 45)) {
                                DashLineView.DashDrawEntity dashDrawEntity =
                                    new DashLineView.DashDrawEntity();
                                dashDrawEntity.type = DashLineView.DashDrawEntity.TYPE_HORIZONTAL;
                                dashDrawEntity.value =
                                    (int) (target.getTranslationY()
                                        + target.getMeasuredHeight() / 2.0f);
                                mDashLineView.drawDashLine(dashDrawEntity);
                            } else if ((r > 90 - 45 && r <= 90 + 45) || (r > 270 - 45
                                && r <= 270 + 45)) {
                                DashLineView.DashDrawEntity dashDrawEntity =
                                    new DashLineView.DashDrawEntity();
                                dashDrawEntity.type = DashLineView.DashDrawEntity.TYPE_VERTICAL;
                                dashDrawEntity.value =
                                    (int) (target.getTranslationX()
                                        + target.getMeasuredWidth() / 2.0f);
                                mDashLineView.drawDashLine(dashDrawEntity);
                            }
                        }
                        rotation(target, rotateDelta);
                        //更新mLastVector,以便下次旋转计算旋转过的角度
                        mLastVector.set(vector.x, vector.y);
                    }

                    //判断能否平移操作
                    if (mCanTranslate) {
                        float dx = event.getRawX() - mLastSinglePoint.x;
                        float dy = event.getRawY() - mLastSinglePoint.y;
                        //平移操作
                        float offX = dx;
                        float offY = dy;
                        translation(target, offX, offY);
                        mLastSinglePoint.set(event.getRawX(), event.getRawY());
                    }
                    //判断能否缩放操作
                    if (mCanScale) {
                        float scaleFactor = distance(event) / mLastDist;
                        scale(target, scaleFactor);
                        mLastDist = distance(event);
                    }
                } else {

                    if (mGestureListener != null && !mMultiTouch) {
                        final float x = event.getX();
                        final float y = event.getY();
                        final float dx = x - mLastDragX, dy = y - mLastDragY;

                        if (!mIsDragging) {
                            mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= touchSlop;
                        }

                        if (mIsDragging) {
                            mGestureListener.onDrag(dx, dy);
                            mLastDragX = x;
                            mLastDragY = y;
                        }
                    } else {
                        dispatchEvent(event);
                    }
                }
                if (calDistance(mTouchDownPoint, new PointF(event.getRawX(), event.getRawY()))
                    > touchSlop) {
                    if (mMultiTouch) {
                        notifyOnTouchMove(target, false);
                    } else {
                        notifyOnTouchMove(target, true);
                    }
                } else {
                    notifyOnTouchMove(target, false);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (target == null) {
                    dispatchEvent(event);
                }
                //当两只手指有一只抬起的时候，设置图片不能缩放和选择，能够进行平移
                if (event.getPointerCount() == 2) {
                    mCanScale = false;
                    mCanRotate = false;
                    //重置两只手指的距离
                    mLastDist = 0f;
                    //重置两只手指形成的向量
                    mLastVector.set(0f, 0f);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouchUp = true;
                if (mRotateChecher != null) {
                    mRotateChecher.clearOffset();
                }
                if (target == null) {
                    if (mIsDragging && mGestureListener != null && !mMultiTouch) {
                        mGestureListener.onDragEnd();
                    } else {
                        dispatchEvent(event);
                    }
                }
                float dx = event.getRawX() - mTouchDownPoint.x;
                float dy = event.getRawY() - mTouchDownPoint.y;
                if (target != null) {
                    if (target instanceof TestTextView) {
                        ((TestTextView) target).setTestText();
                    }
                    if (mCanClick) { //判定为点击
                    }
                }
                if (!mCanLongClick && mCanClick) {
                    //if (SystemClock.elapsedRealtime() - mDownTime < mLongClickTime) {
                    //    mCanClick = false;
                    //    notifyClick(target);
                    //} else {
                    notifyClick(target);
                    //}
                }
                if (enableLongClickFlag) {
                    removeCallbacks(longClickRunnable);
                }
                doRemove(target);
                mTouchDownPoint.set(0f, 0f);
                mLastSinglePoint.set(0f, 0f);
                notifyOnTouchUp(target);
                mCanLongClick = false;
                mCanClick = false;
                mCanScale = false;
                mCanRotate = false;
                break;
        }
        return true;
    }

    private void notifyClick(View target) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(target);
        }
    }

    private void doRemove(View target) {
        if (isHide && mCanTranslate && isTouchUp) {
            removeView(target);
            notifyOnViewRemoved(target);
            isHide = false;
        }
    }

    private void dispatchEvent(MotionEvent event) {
        if (extendEventView != null) {
            extendEventView.onTouchEvent(event);
        }
    }

    private View getMultiPointView(MotionEvent event) {
        int smallX = (int) Math.min(event.getX(0), event.getX(1));
        int bigX = (int) Math.max(event.getX(0), event.getX(1));
        int centerX = smallX + (bigX - smallX) / 2;

        int smallY = (int) Math.min(event.getY(0), event.getY(1));
        int bigY = (int) Math.max(event.getY(0), event.getY(1));
        int centerY = smallY + (bigY - smallY) / 2;

        return getViewByPoint(centerX, centerY);
    }

    private View getSinglePointView(MotionEvent event) {
        return getViewByPoint((int) event.getX(), (int) event.getY());
    }

    private View getViewByPoint(int x, int y) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View target = getChildAt(i);
            RectF out =
                new RectF(target.getLeft(), target.getTop(), target.getRight(), target.getBottom());
            float centerX = out.centerX();
            float centerY = out.centerY();
            float scale = target.getScaleX();
            int realWidth = (int) Math.max(out.width() * scale, 100);
            int realHeight = (int) Math.max(out.height() * scale, 100);

            out.left = centerX - realWidth / 2;
            out.right = out.left + realWidth;
            out.top = centerY - realHeight / 2;
            out.bottom = out.top + realHeight;

            out.left += target.getTranslationX();
            out.right += target.getTranslationX();
            out.top += target.getTranslationY();
            out.bottom += target.getTranslationY();

            Matrix matrix = new Matrix();
            matrix.setRotate(target.getRotation(), out.centerX(), out.centerY());
            matrix.mapRect(out);

            if (out.contains(x, y)) {
                return target;
            }
        }
        return null;
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        Log.v(TAG, "distance:" + (float) Math.sqrt(x * x + y * y));
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }

    /**
     * 图像平移操作
     *
     * @param dx x方向的位移
     * @param dy y方向的位移
     */
    protected void translation(View target, float dx, float dy) {
        target.setTranslationX(target.getTranslationX() + dx);
        target.setTranslationY(target.getTranslationY() + dy);
    }

    /**
     * 图像缩放操作
     *
     * @param scaleFactor 缩放比例因子
     */
    protected void scale(View target, float scaleFactor) {
        target.setScaleX(target.getScaleX() * scaleFactor);
        target.setScaleY(target.getScaleY() * scaleFactor);
    }

    /**
     * 旋转操作
     *
     * @param degree 旋转角度
     */
    protected void rotation(View target, float degree) {
        if (target.getRotationY() == 180) {
            target.setRotation((target.getRotation() - degree) % 360);
        } else {
            target.setRotation((target.getRotation() + degree) % 360);
        }
    }

    protected void mirror(View target) {
        if (target.getRotationY() == 180) {
            target.setRotationY(0);
        } else {
            target.setRotationY(180);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.mOnClickListener = l;
    }

    public void setEnableLongClickFlag(boolean enableLongClickFlag) {
        this.enableLongClickFlag = enableLongClickFlag;
    }

    /**
     * 计算两个向量之间的夹角
     *
     * @param lastVector 上一次两只手指形成的向量
     * @param vector 本次两只手指形成的向量
     * @return 返回手指旋转过的角度
     */
    private float calculateDeltaDegree(PointF lastVector, PointF vector) {
        float lastDegree = (float) Math.atan2(lastVector.y, lastVector.x);
        float degree = (float) Math.atan2(vector.y, vector.x);
        float deltaDegree = degree - lastDegree;
        return (float) Math.toDegrees(deltaDegree);
    }

    private float tmpCurScale; //用于删除动画使用
    private boolean willHide;

    private void animatorHide() {
        if (target == null) {
            return;
        }
        if (isHide) {
            return;
        }
        willHide = true;
        AnimatorSet animSet = new AnimatorSet();
        tmpCurScale = target.getScaleX();
        float scale = tmpCurScale > 0.5f ? 0.5f : tmpCurScale;
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(target, "scaleX", tmpCurScale, scale);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(target, "scaleY", tmpCurScale, scale);
        ObjectAnimator transXAnim = ObjectAnimator.ofFloat(target, "translationX",
            target.getTranslationX(), rect.centerX() - (target.getLeft() + target.getWidth() / 2));
        ObjectAnimator transYAnim = ObjectAnimator.ofFloat(target, "translationY",
            target.getTranslationY(),
            rect.centerY() - (target.getTop() + target.getHeight() / 2) - getOffsetTop());
        animSet.setDuration(100).playTogether(scaleXAnim, scaleYAnim, transXAnim, transYAnim);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "onAnimationEnd hide");
                isHide = true;
                doRemove(target);
            }
        });
        animSet.start();
        notifyOnTrashAnim(false);
    }

    private void animatorShow() {
        if (target == null) {
            return;
        }
        if (!isHide) {
            return;
        }
        float scale = tmpCurScale < 0.5f ? tmpCurScale : 0.5f;
        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(target, "scaleX", scale, tmpCurScale);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(target, "scaleY", scale, tmpCurScale);
        animSet.setDuration(100).playTogether(scaleXAnim, scaleYAnim);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isHide = false;
            }
        });
        animSet.start();
        notifyOnTrashAnim(true);
    }

    private void notifyOnTouchDown(View target, boolean single) {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onTouchDown(target, single);
        }
    }

    private void notifyOnTouchMove(View target, boolean showTrash) {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onTouchMove(target, showTrash);
        }
    }

    private void notifyOnTouchUp(View target) {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onTouchUp(target);
        }
    }

    private void notifyOnViewRemoved(View target) {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onViewRemoved(target);
        }
    }

    private void notifyOnTrashAnim(boolean anim) {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onTrashAnim(anim);
        }
    }

    private void notifyLongClick() {
        for (OnControllerListener onControllerListener : onControllerListeners) {
            onControllerListener.onLongClick(target);
        }
    }

    public void addOnControllerListener(OnControllerListener onControllerListener) {
        onControllerListeners.add(onControllerListener);
    }

    public void setFocusRawRect(RectF rect) {
        //rect.top += getOffsetTop();
        //rect.bottom += getOffsetTop();
        this.rect = rect;
    }

    private void refreshDashLineCheckers() {
        if (mLefts == null) {
            mLefts = new ArrayList<>();
        } else {
            mLefts.clear();
        }
        if (mTops == null) {
            mTops = new ArrayList<>();
        } else {
            mTops.clear();
        }
        if (mRights == null) {
            mRights = new ArrayList<>();
        } else {
            mRights.clear();
        }
        if (mBottoms == null) {
            mBottoms = new ArrayList<>();
        } else {
            mBottoms.clear();
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {

        }
    }

    private int getOffsetTop() {
        if (mTopOffset == -1) {
            int outLocation[] = new int[2];
            getLocationInWindow(outLocation);
            mTopOffset = outLocation[1];
        }
        return mTopOffset;
    }

    private float calDistance(PointF point1, PointF point2) {
        return (float) Math.sqrt(
            Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    public void setGestureListener(OnGestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
    }

    public interface OnControllerListener {
        void onTouchDown(View target, boolean single);

        void onTouchMove(View target, boolean showTrash);

        void onTouchUp(View target);

        void onLongClick(View target);

        void onViewRemoved(View target);

        void onTrashAnim(boolean show);
    }

    public interface OnGestureListener {
        void onDrag(float dx, float dy);

        void onDragEnd();

        void onDragBegin();
    }
}
