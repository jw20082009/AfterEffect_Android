package com.eyedog.aftereffect.DashLine;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.eyedog.aftereffect.utils.DashLineHelper;
import java.util.LinkedList;

/**
 * created by jw200 at 2018/7/30 11:54
 **/
public class DashLineView extends View {

    private final String TAG = "DashLineView";

    DashLineHelper mHelper;
    boolean mCandraw = true;
    int mMeasureWidth, mMeasureHeight;
    LinkedList<DashDrawEntity> dashEntityList;

    public DashLineView(Context context) {
        super(context);
        init();
    }

    public DashLineView(Context context,
        @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mHelper = new DashLineHelper(getContext());
        mHelper.init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCandraw = true;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void enableDraw() {
        mCandraw = true;
        invalidate();
    }

    public void disableDraw() {
        mHelper.resetLineParam();
        mCandraw = false;
        invalidate();
    }

    public void resetDashLine() {
        mHelper.resetLineParam();
        invalidate();
    }

    public void drawDashLine(DashDrawEntity entity) {
        if (entity == null) {
            return;
        }
        if (dashEntityList == null) {
            dashEntityList = new LinkedList<>();
        }
        removeCallbacks(resetRunable);
        postDelayed(resetRunable, 300);
        post(drawRunable);
        dashEntityList.add(entity);
    }

    Runnable resetRunable = new Runnable() {
        @Override
        public void run() {
            mHelper.resetLineParam();
            invalidate();
        }
    };

    Runnable drawRunable = new Runnable() {
        @Override
        public void run() {
            if (dashEntityList != null && dashEntityList.size() > 0) {
                for (DashDrawEntity entity : dashEntityList) {
                    if (entity.type == DashDrawEntity.TYPE_HORIZONTAL) {
                        mHelper.drawDashedLine(0, mMeasureWidth, entity.value, entity.value);
                    } else if (entity.type == DashDrawEntity.TYPE_VERTICAL) {
                        mHelper.drawDashedLine(entity.value, entity.value, 0, mMeasureHeight);
                    }
                }
                invalidate();
                dashEntityList.clear();
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCandraw) {
            mHelper.drawLine(canvas);
        }
    }

    public static class DashDrawEntity {
        public static final int TYPE_HORIZONTAL = 1;
        public static final int TYPE_VERTICAL = 2;
        public int type;//1:horizontal，2：vertical
        public int value;

        public DashDrawEntity() {
        }

        public DashDrawEntity(int type, int value) {
            this.type = type;
            this.value = value;
        }
    }
}
