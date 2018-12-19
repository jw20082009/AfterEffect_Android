package com.eyedog.aftereffect.DashLine;

/**
 * created by jw200 at 2018/7/30 12:53
 **/
public interface IDashLineChecker {

    DashResult checkDashLine(float leftX, float topY, float rightX, float bottomY, float dx,
        float dy);
}
