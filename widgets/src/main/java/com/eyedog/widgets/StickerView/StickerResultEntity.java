package com.eyedog.widgets.stickerview;

import android.os.Parcel;

public class StickerResultEntity extends StickerEntity {

    StickerResultItem resultItem;

    public StickerResultEntity(){}

    protected StickerResultEntity(Parcel in) {
        super(in);
        resultItem = in.readParcelable(StickerResultItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeParcelable(resultItem, flags);
    }

    public StickerResultItem getResultItem() {
        return resultItem;
    }

    public void setResultItem(StickerResultItem resultItem) {
        this.resultItem = resultItem;
    }

    public static final Creator<StickerResultEntity> CREATOR = new Creator<StickerResultEntity>() {
        @Override
        public StickerResultEntity createFromParcel(Parcel in) {
            return new StickerResultEntity(in);
        }

        @Override
        public StickerResultEntity[] newArray(int size) {
            return new StickerResultEntity[size];
        }
    };
}
