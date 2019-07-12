package com.eyedog.widgets.stickerview;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

public class StickerResultItem implements Parcelable {
    protected float[] matrixArray;
    public RectF deleteRect;// 删除按钮位置
    public RectF helpBox;
    public RectF rotateRect;// 旋转按钮位置
    public float rotateAngle = 0;
    public RectF detectRotateRect;
    public RectF detectDeleteRect;
    public RectF dstRect;// 绘制目标坐标

    public StickerResultItem() {
    }

    public StickerResultItem(float[] matrix, RectF deleteRect, RectF helpBox, RectF rotateRect, float rotateAngle, RectF detectRotateRect, RectF detectDeleteRect, RectF dstRect) {
        this.matrixArray = matrix;
        this.deleteRect = deleteRect;
        this.helpBox = helpBox;
        this.rotateRect = rotateRect;
        this.rotateAngle = rotateAngle;
        this.detectRotateRect = detectRotateRect;
        this.detectDeleteRect = detectDeleteRect;
        this.dstRect = dstRect;
    }

    protected StickerResultItem(Parcel in) {
        matrixArray = in.createFloatArray();
        deleteRect = in.readParcelable(RectF.class.getClassLoader());
        helpBox = in.readParcelable(RectF.class.getClassLoader());
        rotateRect = in.readParcelable(RectF.class.getClassLoader());
        rotateAngle = in.readFloat();
        detectRotateRect = in.readParcelable(RectF.class.getClassLoader());
        detectDeleteRect = in.readParcelable(RectF.class.getClassLoader());
        dstRect = in.readParcelable(RectF.class.getClassLoader());
    }

    public static final Creator<StickerResultItem> CREATOR = new Creator<StickerResultItem>() {
        @Override
        public StickerResultItem createFromParcel(Parcel in) {
            return new StickerResultItem(in);
        }

        @Override
        public StickerResultItem[] newArray(int size) {
            return new StickerResultItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(matrixArray);
        dest.writeParcelable(deleteRect, flags);
        dest.writeParcelable(helpBox, flags);
        dest.writeParcelable(rotateRect, flags);
        dest.writeFloat(rotateAngle);
        dest.writeParcelable(detectRotateRect, flags);
        dest.writeParcelable(detectDeleteRect, flags);
        dest.writeParcelable(dstRect, flags);
    }
}
