package com.eyedog.aftereffect.DashLine;

/**
 * created by jw200 at 2018/7/30 13:26
 **/
public class DashResult {

    public enum DashType {
        LEFT, TOP, RIGHT, BOTTOM
    }

    public float dx, dy, dashLineX, dashLineY;

    public DashType mHType, mVType;
}
