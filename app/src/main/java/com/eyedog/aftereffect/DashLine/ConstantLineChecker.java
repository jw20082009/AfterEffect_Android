package com.eyedog.aftereffect.DashLine;

import android.content.Context;
import com.eyedog.aftereffect.utils.DensityUtil;
import java.util.Arrays;
import java.util.List;

/**
 * created by jw200 at 2018/8/3 13:57
 **/
public class ConstantLineChecker implements IDashLineChecker {
    private String TAG = "ConstantLineChecker";
    private Context mContext;
    private int dp_5, dp_10, dp_20, dp_50;
    int mMeasureWidth, mMeasureHeight;
    int /**constant_left, constant_top, constant_right, constant_bottom,*/
        constant_horizontal,
        constant_vertical;
    private DashLineChecker mChecker;

    public ConstantLineChecker(Context context) {
        if (context == null) {
            throw new RuntimeException("null context cannot check dashLineX");
        }
        this.mContext = context;
        dp_5 = DensityUtil.dip2px(mContext, 5);
        dp_10 = DensityUtil.dip2px(mContext, 10);
        dp_20 = DensityUtil.dip2px(mContext, 20);
        dp_50 = DensityUtil.dip2px(mContext, 50);
        mChecker = new DashLineChecker(dp_5, mMeasureWidth, mMeasureHeight);
    }

    public void onMeasure(int measureWidth, int measureHeight) {
        mMeasureWidth = measureWidth;
        mMeasureHeight = measureHeight;
        /**constant_left = dp_10;
         constant_top = dp_50;
         constant_right = mMeasureWidth - dp_10;
         constant_bottom = mMeasureHeight - dp_20;*/
        constant_horizontal = (int) (mMeasureHeight / 2.0f);
        constant_vertical = (int) (mMeasureWidth / 2.0f);
        mChecker.measureCenter(constant_vertical, constant_horizontal);
    }

    @Override
    public DashResult checkDashLine(float leftX, float topY, float rightX, float bottomY, float dx,
        float dy) {
        return mChecker.checkVertical(topY, bottomY, dx, dy,
            mChecker.checkHorizontal(leftX, rightX, dx, dy,
                new DashLineChecker.IDashHorizontal() {
                    @Override
                    public List<Integer> getLefts() {
                        //return Arrays.asList(constant_vertical, constant_left);
                        return Arrays.asList(constant_vertical);
                    }

                    @Override
                    public List<Integer> getRights() {
                        //return Arrays.asList(constant_vertical, constant_right);
                        return Arrays.asList(constant_vertical);
                    }
                }), new DashLineChecker.IDashVertical() {
                @Override
                public List<Integer> getTops() {
                    //return Arrays.asList(constant_horizontal, constant_top);
                    return Arrays.asList(constant_horizontal);
                }

                @Override
                public List<Integer> getBottoms() {
                    //return Arrays.asList(constant_horizontal, constant_bottom);
                    return Arrays.asList(constant_horizontal);
                }
            });
    }
}
