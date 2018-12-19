package com.eyedog.aftereffect.DashLine;

import java.util.List;

/**
 * created by jw200 at 2018/8/3 17:56
 **/
public class DashLineChecker {

    int mOffset = 10;
    private float leftOffset = 0, rightOffset = 0, topOffset = 0, bottomOffset = 0, centerX,
        centerY;

    public DashLineChecker(int offset, float centerX, float centerY) {
        this.mOffset = offset;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void measureCenter(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    private DashResult checkLeft(float leftX, float lineX, float dx, float dy) {
        DashResult dashResult = null;
        if (dx <= 0) {
            if (leftOffset < mOffset * -1) {
                leftOffset = 0;
            }
            float left = leftX + leftOffset + dx;
            if (left <= lineX && left > lineX + mOffset * -1) {
                leftOffset = leftOffset + dx;
                dashResult = new DashResult();
                dashResult.mHType = DashResult.DashType.LEFT;
                dashResult.dashLineX = lineX;
                dashResult.dx = 0;
                dashResult.dy = dy;
            }
        }
        return dashResult;
    }

    private DashResult checkRight(float rightX, float lineX, float dx, float dy) {
        DashResult dashResult = null;
        if (dx >= 0) {
            if (rightOffset > mOffset) {
                rightOffset = 0;
            }
            float right = rightX + rightOffset + dx;
            if (right >= lineX && right < lineX + mOffset) {
                rightOffset = rightOffset + dx;
                dashResult = new DashResult();
                dashResult.mHType = DashResult.DashType.RIGHT;
                dashResult.dashLineX = lineX;
                dashResult.dx = 0;
                dashResult.dy = dy;
            }
        }
        return dashResult;
    }

    private DashResult checkTop(float topY, float lineY, float dx, float dy,
        DashResult dashResult) {
        if (dy <= 0) {
            if (topOffset < -1 * mOffset) {
                topOffset = 0;
            }
            float top = topY + topOffset + dy;
            if (top <= lineY && top > lineY - mOffset) {
                topOffset = topOffset + dy;
                DashResult result = new DashResult();
                result.mVType = DashResult.DashType.TOP;
                result.dashLineY = lineY;
                result.dx = dx;
                result.dy = 0;
                if (dashResult != null) {
                    result.mHType = dashResult.mHType;
                    result.dx = dashResult.dx;
                    result.dashLineX = dashResult.dashLineX;
                }
                return result;
            }
        }
        return dashResult;
    }

    private DashResult checkBottom(float bottomY, float lineY, float dx, float dy,
        DashResult dashResult) {
        if (dy >= 0) {
            if (bottomOffset > mOffset) {
                bottomOffset = 0;
            }
            float bottom = bottomY + bottomOffset + dy;
            if (bottom >= lineY && bottom < lineY + mOffset) {
                bottomOffset = bottomOffset + dy;
                DashResult result = new DashResult();
                result.mVType = DashResult.DashType.BOTTOM;
                result.dashLineY = lineY;
                result.dx = dx;
                result.dy = 0;
                if (dashResult != null) {
                    result.mHType = dashResult.mHType;
                    result.dx = dashResult.dx;
                    result.dashLineX = dashResult.dashLineX;
                }
                return result;
            }
        }
        return dashResult;
    }

    public DashResult checkHorizontal(float leftX, float rightX, float dx, float dy,
        IDashHorizontal horizontal) {
        DashResult dashResult = null;
        if (horizontal == null) {
            return null;
        }
        if (dx > 0) {
            leftOffset = 0;
            List<Integer> rights = horizontal.getRights();
            if (rights != null && rights.size() > 0) {
                for (Integer right : rights) {
                    if (Math.abs(right - centerX) < 4) {
                        dashResult = checkRight((leftX + rightX) / 2.0f, right, dx, dy);
                    } else {
                        dashResult = checkRight(rightX, right, dx, dy);
                    }
                    if (dashResult != null) {
                        break;
                    }
                }
            }
        } else if (dx < 0) {
            rightOffset = 0;
            List<Integer> lefts = horizontal.getLefts();
            if (lefts != null && lefts.size() > 0) {
                for (Integer left : lefts) {
                    if (Math.abs(left - centerX) < 4) {
                        dashResult = checkLeft((leftX + rightX) / 2.0f, left, dx, dy);
                    } else {
                        dashResult = checkLeft(leftX, left, dx, dy);
                    }
                    if (dashResult != null) {
                        break;
                    }
                }
            }
        }
        if (dashResult != null) {
            dashResult.mVType = null;
        }
        return dashResult;
    }

    public DashResult checkVertical(float topY, float bottomY, float dx, float dy,
        DashResult dashResult, IDashVertical vertical) {
        if (dy > 0) {
            topOffset = 0;

            List<Integer> bottoms = vertical.getBottoms();
            if (bottoms != null && bottoms.size() > 0) {
                for (Integer bottom : bottoms) {
                    if (Math.abs(bottom - centerY) < 4) {
                        dashResult =
                            checkBottom((topY + bottomY) / 2.0f, bottom, dx, dy, dashResult);
                    } else {
                        dashResult = checkBottom(bottomY, bottom, dx, dy, dashResult);
                    }
                    if (dashResult != null) {
                        break;
                    }
                }
            }
        } else if (dy < 0) {
            bottomOffset = 0;
            List<Integer> tops = vertical.getTops();
            if (tops != null && tops.size() > 0) {
                for (Integer top : tops) {
                    if (Math.abs(top - centerY) < 4) {
                        dashResult = checkTop((topY + bottomY) / 2.0f, top, dx, dy, dashResult);
                    } else {
                        dashResult = checkTop(topY, top, dx, dy, dashResult);
                    }
                    if (dashResult != null) {
                        break;
                    }
                }
            }
        }
        return dashResult;
    }

    public interface IDashHorizontal {
        List<Integer> getLefts();

        List<Integer> getRights();
    }

    public interface IDashVertical {
        List<Integer> getTops();

        List<Integer> getBottoms();
    }
}
