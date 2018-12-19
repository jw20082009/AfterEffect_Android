package com.eyedog.aftereffect.DashLine;

/**
 * created by jw200 at 2018/7/30 12:02
 **/
public interface IDashLineView extends IDashLineChecker {

    void registerDashChecker(IDashLineChecker checker);

    void removeDashChecker(IDashLineChecker checker);

    void resetDashLine(IDashLineChecker checker);
}
