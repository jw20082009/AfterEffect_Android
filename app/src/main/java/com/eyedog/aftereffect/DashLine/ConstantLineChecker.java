package com.eyedog.aftereffect.DashLine;

import android.content.Context;
import android.util.Log;
import com.eyedog.aftereffect.AEApplication;
import com.eyedog.aftereffect.utils.DensityUtil;
import java.util.Arrays;
import java.util.List;

/**
 * created by jw200 at 2018/8/3 13:57
 **/
public class ConstantLineChecker {
    private static String TAG = "ConstantLineChecker";
    private static float dp_50 = DensityUtil.dip2px(AEApplication.mContext, 50);
    static int offset = 20;
    static float currentOffset;

    public static float checkConstant(float center, float constant, float delta) {
        float deltaX = (center + delta);
        float result = delta;
        Log.i(TAG,"checkConstant,center:"+center+";constant:"+constant+";delta:"+delta+";currentOffset:"+currentOffset);
        if (center < constant
            && center > constant - dp_50
            && deltaX >= constant
            && deltaX <= constant + offset
            && currentOffset <= 0) {
            //从左向右跨过常量线
            result = constant - center;
            currentOffset = deltaX - constant;
        } else if (center == constant && delta > 0 && currentOffset > 0
            && currentOffset <= offset) {
            //从左向右进入缓冲区
            result = 0;
            currentOffset = currentOffset + delta;
        } else if (center > constant
            && center < constant + dp_50
            && deltaX < constant
            && deltaX > constant - offset
            && currentOffset >= 0) {
            result = constant - center;
            currentOffset = deltaX - constant;
        } else if (center == constant
            && delta < 0
            && currentOffset < 0
            && currentOffset >= 0 - offset) {
            result = 0;
            currentOffset = currentOffset + delta;
        } else {
            result = delta;
        }
        return result;
    }
}
