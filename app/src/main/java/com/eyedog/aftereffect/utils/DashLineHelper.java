package com.eyedog.aftereffect.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by hyh on 2018/7/19 14:44
 * E-Mail Address：fjnuhyh122@gmail.com
 **/
public class DashLineHelper {
    private Paint mDashPaint;
    private Path mVerticalPath;
    private Path mHorizontalPath;
    private Context mContext;
    public boolean mIsDrawHorizontal;
    public boolean mIsDrawVertical;

    public DashLineHelper(Context context) {
        this.mContext = context;
    }

    public void init() {
        mDashPaint = new Paint();
        mDashPaint.setColor(Color.parseColor("#FDB709"));
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(DensityUtil.dip2px(mContext, 1));
        mDashPaint.setAntiAlias(true);
        mDashPaint.setPathEffect(
            new DashPathEffect(
                new float[] { DensityUtil.dip2px(mContext, 4), DensityUtil.dip2px(mContext, 3) },
                0));
        mVerticalPath = new Path();
        mHorizontalPath = new Path();
    }

    /**
     * 画虚线
     */
    public void drawDashedLine(int fromX, int toX, int fromY, int toY) {
        if (fromX == toX) {
            resetVerticalLineParam();
            //画竖直虚线
            mVerticalPath.moveTo(fromX, fromY);
            mVerticalPath.lineTo(toX, toY);
            mIsDrawVertical = true;
        }

        if (fromY == toY) {
            resetHorizontalLineParam();
            //画水平虚线
            mHorizontalPath.moveTo(fromX, fromY);
            mHorizontalPath.lineTo(toX, toY);
            mIsDrawHorizontal = true;
        }
    }

    public void resetLineParam() {
        resetHorizontalLineParam();
        resetVerticalLineParam();
    }

    public void resetHorizontalLineParam() {
        mIsDrawHorizontal = false;
        mHorizontalPath.reset();
    }

    public void resetVerticalLineParam() {
        mIsDrawVertical = false;
        mVerticalPath.reset();
    }

    public void drawLine(Canvas canvas) {
        if (mIsDrawHorizontal) {
            canvas.drawPath(mHorizontalPath, mDashPaint);
        }
        if (mIsDrawVertical) {
            canvas.drawPath(mVerticalPath, mDashPaint);
        }
    }
}
