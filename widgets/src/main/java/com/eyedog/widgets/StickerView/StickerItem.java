
package com.eyedog.widgets.stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.eyedog.basic.utils.RectUtil;
import com.eyedog.widgets.R;

/**
 * @author panyi
 */
public class StickerItem extends StickerResultItem {

    private boolean DEBUG = false;
    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;
    private static final int BUTTON_WIDTH = Constants.STICKER_BTN_HALF_SIZE;
    public Bitmap bitmap;
    public Rect srcRect;// 原始图片坐标
    private Rect helpToolsRect;
    public Matrix matrix;// 变化矩阵
    boolean isDrawHelpTool = false;
    private Paint helpBoxPaint = new Paint(), bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float initWidth;// 加入屏幕时原始宽度
    private static Bitmap deleteBit;
    private static Bitmap rotateBit;
    private Paint debugPaint = new Paint();
    public StickerEntity stickerEntity;

    public StickerItem(Context context) {
        helpBoxPaint.setColor(Color.BLACK);
        helpBoxPaint.setStyle(Style.STROKE);
        helpBoxPaint.setAntiAlias(true);
        helpBoxPaint.setStrokeWidth(4);
        bitmapPaint.setFilterBitmap(true);
        // 导入工具按钮位图
        if (deleteBit == null) {
            deleteBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sample_sticker_delete);
        }
        if (rotateBit == null) {
            rotateBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sample_sticker_rotate);
        }
        if (DEBUG) {
            debugPaint = new Paint();
            debugPaint.setColor(Color.GREEN);
            debugPaint.setAlpha(120);
        }
    }

    public void init(Bitmap addBit, View parentView) {
        this.bitmap = addBit;
        this.srcRect = new Rect(0, 0, addBit.getWidth(), addBit.getHeight());
        int bitWidth = Math.min(addBit.getWidth(), parentView.getWidth() >> 1);
        int bitHeight = (int) bitWidth * addBit.getHeight() / addBit.getWidth();
        int left = (parentView.getWidth() >> 1) - (bitWidth >> 1);
        int top = (parentView.getHeight() >> 1) - (bitHeight >> 1);
        this.dstRect = new RectF(left, top, left + bitWidth, top + bitHeight);
        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / addBit.getWidth(),
                (float) bitHeight / addBit.getHeight(), this.dstRect.left, this.dstRect.top);
        Log.i("stickerMatrix", "init，postTranslate:" + dstRect.left + "*" + dstRect.top + ";postScale:" + ((float) bitWidth / addBit.getWidth()) + "*" + ((float) bitHeight / addBit.getHeight()) + ";" + matrix.toShortString());
        initWidth = this.dstRect.width();// 记录原始宽度
        this.isDrawHelpTool = false;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();
        helpToolsRect = new Rect(0, 0, deleteBit.getWidth(), deleteBit.getHeight());
        deleteRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.top - BUTTON_WIDTH,
                helpBox.left + BUTTON_WIDTH, helpBox.top + BUTTON_WIDTH);
        rotateRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.bottom - BUTTON_WIDTH,
                helpBox.right + BUTTON_WIDTH, helpBox.bottom + BUTTON_WIDTH);
        detectRotateRect = new RectF(rotateRect);
        detectDeleteRect = new RectF(deleteRect);
    }

    public void replaceBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private void updateHelpBoxRect() {
        this.helpBox.left -= HELP_BOX_PAD;
        this.helpBox.right += HELP_BOX_PAD;
        this.helpBox.top -= HELP_BOX_PAD;
        this.helpBox.bottom += HELP_BOX_PAD;
    }

    /**
     * 位置更新
     *
     * @param dx
     * @param dy
     */
    public void updatePos(final float dx, final float dy) {
        this.matrix.postTranslate(dx, dy);// 记录到矩阵中
        dstRect.offset(dx, dy);
        // 工具按钮随之移动
        helpBox.offset(dx, dy);
        deleteRect.offset(dx, dy);
        rotateRect.offset(dx, dy);
        this.detectRotateRect.offset(dx, dy);
        this.detectDeleteRect.offset(dx, dy);
        Log.i("stickerMatrix", "translate:" + dx + "*" + dy + ";" + matrix.toShortString());
    }

    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float oldx, final float oldy, final float dx,
                                     final float dy) {
        float c_x = dstRect.centerX();
        float c_y = dstRect.centerY();
        float x = this.detectRotateRect.centerX();
        float y = this.detectRotateRect.centerY();
        float n_x = x + dx;
        float n_y = y + dy;
        float xa = x - c_x;
        float ya = y - c_y;
        float xb = n_x - c_x;
        float yb = n_y - c_y;
        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);
        float scale = curLen / srcLen;// 计算缩放比
        float newWidth = dstRect.width() * scale;
        if (newWidth / initWidth < MIN_SCALE) {// 最小缩放值检测
            return;
        }
        scale(scale);
        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        // 定理
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向
        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;
        rotate(angle);
    }

    public void scaleAndRotate(float scale, float angle) {
        if (scale > 0) {
            scale(scale);
        }
        if (angle > 0) {
            rotate(angle);
        }
    }

    public void scale(float scale) {
        float centerX = this.dstRect.centerX();
        float centerY = this.dstRect.centerY();
        this.matrix.postScale(scale, scale, centerX, centerY);// 存入scale矩阵
        RectUtil.scaleRect(this.dstRect, scale);// 缩放目标矩形
        // 重新计算工具箱坐标
        helpBox.set(dstRect);
        updateHelpBoxRect();// 重新计算
        rotateRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom - BUTTON_WIDTH);
        deleteRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top - BUTTON_WIDTH);
        detectRotateRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom - BUTTON_WIDTH);
        detectDeleteRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top - BUTTON_WIDTH);
        Log.i("stickerMatrix", "scale:" + scale + ";centerX:" + centerX + ";centerY:" + centerY + ";" + matrix.toShortString());
    }

    public void rotate(float angle) {
        rotateAngle += angle;
        float centerX = this.dstRect.centerX();
        float centerY = this.dstRect.centerY();
        this.matrix.postRotate(angle, centerX, centerY);
        RectUtil.rotateRect(this.detectRotateRect, this.dstRect.centerX(), this.dstRect.centerY(),
                rotateAngle);
        RectUtil.rotateRect(this.detectDeleteRect, this.dstRect.centerX(), this.dstRect.centerY(),
                rotateAngle);
        Log.i("stickerMatrix", "angle:" + angle + ";centerX:" + centerX + ";centerY:" + centerY + ";" + matrix.toShortString());
    }

    public void initParams(StickerResultItem resultItem) {
        if (resultItem != null) {
            if (resultItem.matrixArray != null) {
                this.matrix.setValues(resultItem.matrixArray);
                Log.i("stickerMatrix", "initParams:" + matrix.toShortString());
            }
            if (resultItem.deleteRect != null) {
                this.deleteRect = resultItem.deleteRect;
            }
            if (resultItem.helpBox != null) {
                this.helpBox = resultItem.helpBox;
            }
            if (resultItem.rotateRect != null) {
                this.rotateRect = resultItem.rotateRect;
            }
            this.rotateAngle = resultItem.rotateAngle;
            if (resultItem.detectRotateRect != null) {
                this.detectRotateRect = resultItem.detectRotateRect;
            }
            if (resultItem.detectDeleteRect != null) {
                this.detectDeleteRect = resultItem.detectDeleteRect;
            }
            if (resultItem.dstRect != null) {
                this.dstRect = resultItem.dstRect;
            }
        }
    }

    public StickerResultItem getResultItem() {
        if (this.matrixArray == null) {
            this.matrixArray = new float[9];
        }
        this.matrix.getValues(this.matrixArray);
        return this;
    }

    public void draw(Canvas canvas) {
        if(this.bitmap != null && !this.bitmap.isRecycled()){
            canvas.drawBitmap(this.bitmap, this.matrix, bitmapPaint);// 贴图元素绘制
            if (this.isDrawHelpTool) {// 绘制辅助工具线
                canvas.save();
                canvas.rotate(rotateAngle, helpBox.centerX(), helpBox.centerY());
                canvas.drawRoundRect(helpBox, 10, 10, helpBoxPaint);
                // 绘制工具按钮
                canvas.drawBitmap(deleteBit, helpToolsRect, deleteRect, bitmapPaint);
                canvas.drawBitmap(rotateBit, helpToolsRect, rotateRect, bitmapPaint);
                canvas.restore();
            }
            if (DEBUG) {
                canvas.drawRect(helpBox, debugPaint);
            }
        }else{
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }
}
