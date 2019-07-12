package com.eyedog.basic.utils;

import android.graphics.Point;
import android.graphics.RectF;

public class RectUtil {

    /**
     * 缩放指定矩形
     *
     * @param rect
     * @param scale
     */
    public static void scaleRect(RectF rect, float scale) {
        float w = rect.width();
        float h = rect.height();

        float newW = scale * w;
        float newH = scale * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 矩形绕指定点旋转
     *
     * @param rect
     * @param roatetAngle
     */
    public static void rotateRect(RectF rect, float center_x, float center_y,
                                  float roatetAngle) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(roatetAngle));
        float cosA = (float) Math.cos(Math.toRadians(roatetAngle));
        float newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA;
        float newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA;

        float dx = newX - x;
        float dy = newY - y;

        rect.offset(dx, dy);
    }

    /**
     * 旋转Point点
     * @param p
     * @param center_x
     * @param center_y
     * @param roatetAngle
     */
    public static void rotatePoint(Point p, float center_x, float center_y,
                                   float roatetAngle) {
        float sinA = (float) Math.sin(Math.toRadians(roatetAngle));
        float cosA = (float) Math.cos(Math.toRadians(roatetAngle));
        // calc new point
        float newX = center_x + (p.x - center_x) * cosA - (p.y - center_y) * sinA;
        float newY = center_y + (p.y - center_y) * cosA + (p.x - center_x) * sinA;
        p.set((int)newX , (int)newY);
    }
}
