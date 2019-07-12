
package com.eyedog.widgets.stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.eyedog.basic.imageloader.ImageRequest;
import com.eyedog.basic.imageloader.LocalImageLoader;
import com.eyedog.basic.utils.DensityUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnimateStickerView extends StickerView {
    final String TAG = "AnimateStickerView";

    int frameRate = 25;

    AnimateHandler mHandler;

    boolean isRunning = false;

    List<StickerEntity> stickerList = new ArrayList<>();

    StickerChangedListener listener;

    MediaPlayer mediaPlayer;

    TextPaint textPaint;

    int textPadding;

    HashMap<String, List<String>> splitText;

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public AnimateStickerView(Context context) {
        super(context);
        init();
    }

    public AnimateStickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimateStickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHandler = new AnimateHandler(this);
        textPadding = DensityUtil.dip2px(getContext(), 10);
    }

    private TextPaint getTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(DensityUtil.dip2px(getContext(), 20));
            textPaint.setFilterBitmap(true);
        }
        return textPaint;
    }

    public void addStickerImage(final StickerEntity stickerEntity) {
        if (stickerEntity != null) {
            stickerList.add(stickerEntity);
        }
        if (!isRunning) {
            startRunning();
        }
    }

    public void changeSelectedTime(int id, int startTime, int duration) {
        if (stickerList != null) {
            for (StickerEntity stickerEntity : stickerList) {
                if (stickerEntity.getId() == id) {
                    stickerEntity.setStartTime(startTime);
                    stickerEntity.setDurationTime(duration);
                    break;
                }
            }
        }
    }

    public void setStickerImages(List<StickerEntity> stickerEntities) {
        if (stickerEntities == null || stickerEntities.size() <= 0) {
            clear();
        } else {
            if (stickerList != null) {
                stickerList.clear();
                bank.clear();
            }
            for (StickerEntity stickerEntity : stickerEntities) {
                addStickerImage(stickerEntity);
            }
        }
    }

    public List<StickerEntity> getResult() {
        if (bank != null) {
            List<StickerEntity> stickerEntities = new ArrayList<>();
            for (Integer id : bank.keySet()) {
                StickerItem item = bank.get(id);
                StickerEntity stickerEntity = item.stickerEntity;
                if (stickerEntity instanceof StickerResultEntity) {
                    StickerResultItem resultItem = item.getResultItem();
                    ((StickerResultEntity) stickerEntity).setResultItem(resultItem);
                    stickerEntity.setMatrixArray(resultItem.matrixArray);
                }
                stickerEntity.setContainerWidth(getMeasuredWidth());
                stickerEntity.setContainerHeight(getMeasuredHeight());
                stickerEntity.setId(0);
                stickerEntities.add(stickerEntity);
            } // end for each
            return stickerEntities;
        }
        return null;
    }

    public void startRunning() {
        isRunning = true;
        mHandler.removeMessages(MSG_UI_REFRESH_BITMAP);
        mHandler.removeMessages(MSG_UI_STOP_RUNNING);
        mHandler.sendEmptyMessage(MSG_UI_REFRESH_BITMAP);
    }

    public void stopRunning() {
        mHandler.removeMessages(MSG_UI_REFRESH_BITMAP);
        mHandler.removeMessages(MSG_UI_STOP_RUNNING);
        mHandler.sendEmptyMessage(MSG_UI_STOP_RUNNING);
        isRunning = false;
    }

    private final int MSG_UI_REFRESH_BITMAP = 0x01;

    private final int MSG_UI_STOP_RUNNING = 0x02;

    public void handleMessage(Message msg) {
        long currentTime = 0;
        try {
            currentTime = mediaPlayer.getCurrentPosition();
        } catch (Exception e) {
        }
        switch (msg.what) {
            case MSG_UI_STOP_RUNNING:
                invalidate();
                break;
            case MSG_UI_REFRESH_BITMAP:
                Log.i(TAG, "currentTime:" + currentTime);
                if (stickerList != null && stickerList.size() > 0) {
                    for (final StickerEntity stickerEntity : stickerList) {
                        loadBitmap(currentTime, stickerEntity);
                    }
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(MSG_UI_REFRESH_BITMAP,
                            (long) (1.0f * 1000 / frameRate));
                } else {
                    isRunning = false;
                }
                break;
        }
    }

    private void loadBitmap(long currentTime, final StickerEntity stickerEntity) {
        List<String> stickers = stickerEntity.getStickers();
        if (stickers != null && stickers.size() > 0) {
            String path = stickerEntity.getCurrentStickerImage(currentTime);
            final int id = stickerEntity.getId();
            if (path != null) {
                ImageRequest request = new ImageRequest.ImageRequestBuilder().path(path).inMutable().build();
                LocalImageLoader.getInstance(getContext()).loadImageBitmap(request,
                        new LocalImageLoader.ILocalBitmapListener() {
                            @Override
                            public void onLoadResult(Bitmap bitmap) {
                                drawText(bitmap, stickerEntity);
                                drawBitmap(bitmap, stickerEntity);
                            }
                        });
            } else {
                if (id > 0) {
                    StickerItem item = bank.get(id);
                    item.replaceBitmap(null);
                }
            }
        } else if (!TextUtils.isEmpty(stickerEntity.getText())) {
            final int id = stickerEntity.getId();
            if (currentTime >= stickerEntity.getStartTime() && currentTime < (stickerEntity.getStartTime() + stickerEntity.getDurationTime())) {
                Bitmap bitmap = null;
                if (id > 0) {
                    StickerItem item = bank.get(stickerEntity.getId());
                    bitmap = item.bitmap;
                }
                if (bitmap == null || bitmap.isRecycled()) {
                    TextPaint paint = getTextPaint();
                    paint.setTextSize(stickerEntity.getTextSize());
                    Paint.FontMetrics metrics = paint.getFontMetrics();
                    float height = textPadding * 2 + metrics.bottom - metrics.top;
                    float width = textPadding * 2 + paint.measureText(stickerEntity.getText());
                    bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
                }
                drawText(bitmap, stickerEntity);
                drawBitmap(bitmap, stickerEntity);
            } else {
                if (id > 0) {
                    StickerItem item = bank.get(id);
                    item.replaceBitmap(null);
                }
            }
        } else {
            // 无效的实体
        }
    }

    private void drawText(Bitmap bitmap, StickerEntity entity) {
        if (bitmap != null && !bitmap.isRecycled() && entity != null
                && !TextUtils.isEmpty(entity.getText())) {
            String text = entity.getText();
            Canvas canvas = new Canvas(bitmap);
            TextPaint paint = getTextPaint();
            paint.setColor(entity.getTextColor());
            paint.setTextSize(entity.getTextSize());
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float top = metrics.top;
            float bottom = metrics.bottom;
            int lines = (int) Math.ceil(1.0f * text.length() / entity.getMaxWordsPerLine());
            if (lines > entity.getMaxLines()) {
                lines = entity.getMaxLines();
            }
            List<String> splits = new ArrayList<>();
            for (int i = 0; i < lines; i++) {
                int start = i * entity.getMaxWordsPerLine();
                int end = start + entity.getMaxWordsPerLine();
                if (i == lines - 1) {
                    int length = text.length() % entity.getMaxWordsPerLine();
                    if (length == 0) {
                        end = start + text.length();
                    } else {
                        end = start + length;
                    }
                }
                splits.add(text.substring(start, end));
            }
            if (splits.size() <= 0) {
                return;
            }
            int lineHeight = (int) (1.0f * bitmap.getHeight() / splits.size());
            for (int i = 0; i < splits.size(); i++) {
                String str = splits.get(i);
                int baseLineY = (int) (lineHeight / 2.0f + i * lineHeight - top / 2 - bottom / 2);
                canvas.drawText(str, textPadding, baseLineY, textPaint);
            }
        }
    }

    private void drawBitmap(Bitmap bitmap, StickerEntity stickerEntity) {
        StickerItem item = null;
        if (stickerEntity.getId() > 0) {
            item = bank.get(stickerEntity.getId());
            item.replaceBitmap(bitmap);
        } else {
            item = new StickerItem(getContext());
            item.init(bitmap, AnimateStickerView.this);
            if (stickerEntity instanceof StickerResultEntity
                    && ((StickerResultEntity) stickerEntity).getResultItem() != null) {
                item.initParams(((StickerResultEntity) stickerEntity).getResultItem());
            }
            if (currentItem != null) {
                currentItem.isDrawHelpTool = false;
            }
            bank.put(++imageCount, item);
            stickerEntity.setId(imageCount);
            item.stickerEntity = stickerEntity;
            notifyAdd(imageCount, stickerEntity);
        }
    }

    static class AnimateHandler extends Handler {

        final SoftReference<AnimateStickerView> reference;

        public AnimateHandler(AnimateStickerView stickerView) {
            reference = new SoftReference<>(stickerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AnimateStickerView stickerView = reference.get();
            if (stickerView != null) {
                stickerView.handleMessage(msg);
            }
        }
    }

    @Override
    protected void beforeRemoveBitmap(int id) {
        super.beforeRemoveBitmap(id);
        if (stickerList != null) {
            int size = stickerList.size();
            if (size > 0) {
                int index = -1;
                for (int i = 0; i < size; i++) {
                    StickerEntity stickerEntity = stickerList.get(i);
                    if (stickerEntity.getId() == id) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    notifyRemove(id, stickerList.remove(index));
                }
            }
        }
    }

    @Override
    public void clear() {
        mediaPlayer = null;
        if (stickerList != null)
            stickerList.clear();
        super.clear();
    }

    private void notifyRemove(int id, StickerEntity stickerEntity) {
        if (this.listener != null) {
            this.listener.onRemove(id, stickerEntity);
        }
    }

    private void notifyAdd(int id, StickerEntity stickerEntity) {
        if (this.listener != null) {
            this.listener.onAdd(id, stickerEntity);
        }
    }

    public void setStickerChangeListener(StickerChangedListener listener) {
        this.listener = listener;
    }

    public interface StickerChangedListener {
        void onAdd(int id, StickerEntity stickerEntity);

        void onRemove(int id, StickerEntity stickerEntity);
    }

    class SplitText {

        public float textSize;

        public String text;

        public List<String> splitText;

        public SplitText(String text, float textSize, List<String> splitText) {
            this.text = text;
            this.textSize = textSize;
            this.splitText = splitText;
        }
    }
}
