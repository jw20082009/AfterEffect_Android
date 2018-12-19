package com.eyedog.aftereffect;

import android.util.Log;

/**
 * created by jw200 at 2018/12/7 17:11
 **/
public class RotateChecker {

    private static final String TAG = "RotateChecker";
    static final float offset = 8;//必须小于90度
    private static float currentOffset = 0;

    public static float[] getStandardValues() {
        return new float[] { 0, 90, 180, 270 };
    }

    private static int rotation(int rotation) {
        return (rotation % 360 + 360) % 360;
    }

    public static float checkDash(float delta, float rotation) {
        float[] standardValues = getStandardValues();
        int r = rotation((int) rotation);
        float result = 0f;
        if (standardValues != null) {
            for (int i = 0; i < standardValues.length; i++) {
                float value = standardValues[i];
                if (value == 0) {
                    float deltaR = (r + delta);
                    if ((r > 270 && r < 360 && deltaR >= 360 && deltaR <= 360 + offset) || (r == 0
                        && deltaR >= 0
                        && deltaR <= offset && currentOffset <= 0)) {
                        //顺时针跨0度线
                        if (r != 0) {
                            result = 360 - r;
                            currentOffset = deltaR - 360;
                        } else {
                            result = 0;
                            currentOffset = delta;
                        }
                        break;
                    } else if (r == 0
                        && delta > 0
                        && currentOffset > 0
                        && currentOffset <= offset) {
                        //顺时针进入0度缓冲区
                        result = 0;
                        currentOffset = currentOffset + delta;
                        break;
                    } else if ((r > 0 && r < 90 && deltaR < 0 && deltaR > 0 - offset) || (r == 0
                        && deltaR <= 0
                        && deltaR >= 0 - offset && currentOffset >= 0)) {
                        //逆时针跨0度线
                        result = deltaR;
                        currentOffset = result;
                        break;
                    } else if (r == 0 && currentOffset < 0 && currentOffset >= 0 - offset) {
                        //逆时针进入0度缓冲区
                        result = 0;
                        currentOffset = currentOffset + delta;
                        break;
                    } else {
                        result = delta;
                    }
                } else {
                    float deltaR = (r + delta);
                    if (r > (value - 90)
                        && r <= value
                        && deltaR >= value
                        && deltaR <= value + offset && currentOffset <= 0) {
                        result = value - r;
                        currentOffset = deltaR - value;
                        break;
                    } else if (r == value
                        && delta > 0
                        && currentOffset > 0
                        && currentOffset <= offset) {
                        result = 0;
                        currentOffset = currentOffset + delta;
                        break;
                    } else if ((r > value
                        && r < value + 90
                        && deltaR < value
                        && deltaR > value - offset) || (r == value
                        && deltaR <= value
                        && deltaR >= value - offset
                        && currentOffset >= 0)) {
                        result = deltaR - value;
                        currentOffset = result;
                        break;
                    } else if (r == value && currentOffset < 0 && currentOffset >= 0 - offset) {
                        result = 0;
                        currentOffset = currentOffset + delta;
                        break;
                    } else {
                        result = delta;
                    }
                }
            }
        }
        return result;
    }
}
