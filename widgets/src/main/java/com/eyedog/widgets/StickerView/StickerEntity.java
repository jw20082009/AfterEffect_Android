
package com.eyedog.widgets.stickerview;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.List;

public class StickerEntity extends BaseEffectEntity implements Parcelable {
    private int id;// 唯一标识在界面上的显示id

    private String name;

    private String stickerId;

    private String stickerFps;

    private List<String> stickers;

    private float[] matrixArray;

    private float textSize;

    private int maxLines;

    private int maxWordsPerLine;

    private int textColor = Color.WHITE;

    private int containerWidth, containerHeight;

    private String text;

    public StickerEntity() {
    }

    protected StickerEntity(Parcel in) {
        super(in);
        name = in.readString();
        stickerId = in.readString();
        stickerFps = in.readString();
        stickers = in.createStringArrayList();
        containerWidth = in.readInt();
        containerHeight = in.readInt();
        text = in.readString();
        textSize = in.readFloat();
        maxLines = in.readInt();
        maxWordsPerLine = in.readInt();
        textColor = in.readInt();
        matrixArray = in.createFloatArray();
    }

    public static final Creator<StickerEntity> CREATOR = new Creator<StickerEntity>() {
        @Override
        public StickerEntity createFromParcel(Parcel in) {
            return new StickerEntity(in);
        }

        @Override
        public StickerEntity[] newArray(int size) {
            return new StickerEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(stickerId);
        dest.writeString(stickerFps);
        dest.writeStringList(stickers);
        dest.writeInt(containerWidth);
        dest.writeInt(containerHeight);
        dest.writeString(text);
        dest.writeFloat(textSize);
        dest.writeInt(maxLines);
        dest.writeInt(maxWordsPerLine);
        dest.writeInt(textColor);
        if (matrixArray != null)
            dest.writeFloatArray(matrixArray);
    }

    @Override
    public String toString() {
        return "name_" + (name == null ? "" : name) + "_stickerId_"
                + (stickerId == null ? "" : stickerId) + "_stickerfps_"
                + (stickerFps == null ? "" : stickerFps) + "_text_" + (text == null ? "" : text);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StickerEntity)) {
            return false;
        }
        StickerEntity stickerEntity = (StickerEntity) obj;
        if (TextUtils.equals(stickerEntity.getStickerId(), getStickerId())
                && TextUtils.equals(name, stickerEntity.getName())) {
            return true;
        }
        return super.equals(obj);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public String getStickerFps() {
        return stickerFps;
    }

    public void setStickerFps(String stickerFps) {
        this.stickerFps = stickerFps;
    }

    public List<String> getStickers() {
        return stickers;
    }

    public void setStickers(List<String> stickers) {
        this.stickers = stickers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getMatrixArray() {
        return matrixArray;
    }

    public void setMatrixArray(float[] matrixArray) {
        this.matrixArray = matrixArray;
    }

    public int getContainerWidth() {
        return containerWidth;
    }

    public void setContainerWidth(int containerWidth) {
        this.containerWidth = containerWidth;
    }

    public int getContainerHeight() {
        return containerHeight;
    }

    public void setContainerHeight(int containerHeight) {
        this.containerHeight = containerHeight;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public int getMaxWordsPerLine() {
        return maxWordsPerLine;
    }

    public void setMaxWordsPerLine(int maxWordsPerLine) {
        this.maxWordsPerLine = maxWordsPerLine;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getCurrentStickerImage(long currentTime) {
        if (stickers != null && stickers.size() > 0) {
            long timeEllapsed = currentTime - startTime;
            if (timeEllapsed >= 0 && timeEllapsed < durationTime) {
                int fps = 25;
                try {
                    int f = Integer.parseInt(getStickerFps());
                    fps = f;
                } catch (Exception e) {
                }
                int frameDuration = (int) (1000f / fps);
                long totalDuration = frameDuration * stickers.size();
                timeEllapsed = timeEllapsed % totalDuration;
                int index = (int) Math.floor(1.0f * timeEllapsed / frameDuration);
                return stickers.get(index);
            } else {
                return null;
            }
        }
        return null;
    }
}
