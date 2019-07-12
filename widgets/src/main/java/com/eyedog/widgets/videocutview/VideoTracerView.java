
package com.eyedog.widgets.videocutview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.eyedog.basic.utils.DensityUtil;
import com.eyedog.widgets.R;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VideoTracerView extends View {
    private final String TAG = "VideoTracerView";

    protected IMetadataRetriever mRetriever;

    protected final int touchWidth = 20;

    // 每屏能展示的时长
    protected int maxDisplayDuration = 10 * 1000;

    // 每屏展示的帧数
    protected int maxDisplayFrames = 10;

    protected int frameTime = (int) (1.0f * maxDisplayDuration / maxDisplayFrames);

    protected int currentTime = 0;

    protected int videoDuration;

    protected HandlerThread mHandlerThread;

    protected VideoProgressHandler mBackHandler, mUiHandler;

    protected int mMeasuredWidth, mMeasuredHeight, mFrameWidth, mScreenWidth, mScreenHeight,
            mDefaultFrameWidth, mDefaultFrameHeight;

    protected ConcurrentLinkedQueue<VideoFrame> frameQueue = new ConcurrentLinkedQueue<VideoFrame>();

    protected Bitmap mCurrentBitmap;

    protected int mCurrentTotalWidth = 0;

    protected Paint mBitmapPaint, mRectPaint, mIndicatePaint;

    protected Rect mMaxDisplayRect;

    protected int initTranslate = 0;

    protected int translate = 0;

    protected int verticalStroken = 20;

    protected int horizontalStroken = 6;

    protected MediaPlayer mediaPlayer;

    protected List<SelectedArea> selectedAreas = new ArrayList<>();

    protected IVideoTracerListener listener;

    protected TouchEdgeResult touchEdgeResult;

    /**
     * 选中区域是否可触摸更改
     */
    protected boolean canChangeSelectedArea = true;

    public void setVideoTracerListener(IVideoTracerListener listener) {
        this.listener = listener;
    }

    public void startAutoSeek(MediaPlayer player) {
        this.mediaPlayer = player;
        mUiHandler.removeMessages(MSG_UI_AUTO_SEEK);
        mUiHandler.removeMessages(MSG_UI_AUTO_REVERSE_SEEK);
        mUiHandler.sendEmptyMessage(MSG_UI_AUTO_SEEK);
    }

    public void stopAutoSeek() {
        mUiHandler.removeMessages(MSG_UI_AUTO_SEEK);
    }

    public void startAutoReverseSeek(MediaPlayer player) {
        this.mediaPlayer = player;
        mUiHandler.removeMessages(MSG_UI_AUTO_SEEK);
        mUiHandler.removeMessages(MSG_UI_AUTO_REVERSE_SEEK);
        mUiHandler.sendEmptyMessage(MSG_UI_AUTO_REVERSE_SEEK);
    }

    public void stopAutoReverseSeek() {
        mUiHandler.removeMessages(MSG_UI_AUTO_REVERSE_SEEK);
    }

    public VideoTracerView(Context context) {
        super(context);
        init();
    }

    public VideoTracerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTracerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mDefaultFrameWidth = (int) (1.0f * mScreenWidth / 12);
        mDefaultFrameHeight = DensityUtil.dip2px(getContext(), 30);
        mHandlerThread = new HandlerThread("VideoProgressThread");
        mHandlerThread.start();
        mUiHandler = new VideoProgressHandler(Looper.getMainLooper(), this, 0);
        mBackHandler = new VideoProgressHandler(mHandlerThread.getLooper(), this, 1);
        setClickable(true);
        initPaint();
    }

    protected void initPaint() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setFilterBitmap(true);

        mRectPaint = new Paint();
        mRectPaint.setColor(Color.WHITE);

        mIndicatePaint = new Paint();
        mIndicatePaint.setStrokeWidth(6);
        mIndicatePaint.setColor(Color.RED);
    }

    public void setDataSource(String filePath, int maxDisplayDuration, int maxDisplayFrames) {
        if (mCurrentBitmap != null)
            return;
        this.maxDisplayDuration = maxDisplayDuration;
        this.maxDisplayFrames = maxDisplayFrames;
        this.frameTime = (int) (1.0f * maxDisplayDuration / maxDisplayFrames);
        Message msg = mBackHandler.obtainMessage(MSG_THREAD_INIT_RETRIVER);
        msg.obj = filePath;
        msg.sendToTarget();
    }

    protected IMetadataRetriever getMetadataRetriever() {
        return new CustomMediaRetriver();
    }

    class CustomMediaRetriver implements IMetadataRetriever {
        MediaMetadataRetriever mediaMetadataRetriever;

        public CustomMediaRetriver() {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }

        @Override
        public void setDataSource(String filepath) {
            mediaMetadataRetriever.setDataSource(filepath);
        }

        @Override
        public int getVideoWidth() {
            return Integer.parseInt(mediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        }

        @Override
        public int getVideoHeight() {
            return Integer.parseInt(mediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }

        @Override
        public int getVideoRotation() {
            return Integer.parseInt(mediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        }

        @Override
        public int getVideoDuration() {
            return Integer.parseInt(mediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        }

        @Override
        public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return mediaMetadataRetriever.getScaledFrameAtTime(timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
            } else {
                Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                Bitmap result = bitmap;
                if (bitmap.getWidth() > width) {
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    float scaledHeight = 1.0f * width * h / w;
                    result = Bitmap.createScaledBitmap(bitmap, width, (int) scaledHeight, false);
                }
                return result;
            }
        }

        @Override
        public void release() {
            mediaMetadataRetriever.release();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMeasuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        initTranslate = (int) (mMeasuredWidth / 2.0f);
    }

    private TouchEdgeResult checkEdgeTouch(float x) {
        if (canChangeSelectedArea && selectedAreas != null && selectedAreas.size() > 0
                && mCurrentBitmap != null) {
            int totalWidth = mCurrentBitmap.getWidth();
            if (totalWidth > 0) {
                for (SelectedArea selectedArea : selectedAreas) {
                    int left = (int) (1.0f * totalWidth * selectedArea.startTime / videoDuration);
                    int width = (int) (1.0f * selectedArea.durationTime * totalWidth
                            / videoDuration);
                    if (Math.abs(x - translate - left - width) < touchWidth) {
                        // 右侧边界
                        if (touchEdgeResult == null) {
                            touchEdgeResult = new TouchEdgeResult();
                        }
                        touchEdgeResult.touchArea = 1;
                        touchEdgeResult.selectedArea = selectedArea;
                        return touchEdgeResult;
                    } else if (Math.abs(x - translate - left) < touchWidth) {
                        // 左侧边界
                        if (touchEdgeResult == null) {
                            touchEdgeResult = new TouchEdgeResult();
                        }
                        touchEdgeResult.touchArea = 0;
                        touchEdgeResult.selectedArea = selectedArea;
                        return touchEdgeResult;
                    }
                }
            }
        }
        return null;
    }

    float lastMoveX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveX = event.getX();
                stopAutoSeek();
                stopAutoReverseSeek();
                if (listener != null) {
                    listener.onTouchDown(currentTime);
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                float currentMoveX = event.getX();
                float deltaX = currentMoveX - lastMoveX;
                TouchEdgeResult result = checkEdgeTouch(currentMoveX);
                if (result != null) {
                    Log.i(TAG, "touchEdgeResult:" + result.toString());
                    if (result.touchArea == 0) {
                        // 左边
                        float deltaTime = 1.0f * videoDuration * deltaX / mCurrentBitmap.getWidth();
                        result.selectedArea.startTime = (int) (result.selectedArea.startTime
                                + deltaTime);
                        notifySelectedAreaChanged(result.selectedArea);
                        invalidate();
                    } else if (result.touchArea == 1) {
                        // 右边
                        float deltaTime = 1.0f * videoDuration * deltaX / mCurrentBitmap.getWidth();
                        result.selectedArea.durationTime = (int) (result.selectedArea.durationTime
                                + deltaTime);
                        notifySelectedAreaChanged(result.selectedArea);
                        invalidate();
                    }
                } else {
                    move(currentMoveX - lastMoveX);
                }
                lastMoveX = currentMoveX;
            }
            break;
            case MotionEvent.ACTION_UP: {
                float currentMoveX = event.getX();
                move(currentMoveX - lastMoveX);
                lastMoveX = currentMoveX;
                if (listener != null) {
                    listener.onTouchUp(currentTime);
                }
            }
            break;
        }
        return super.onTouchEvent(event);
    }

    protected final int MSG_THREAD_INIT_RETRIVER = 0x01;

    protected final int MSG_THREAD_REFRESH_FRAME = 0x02;

    protected void handleThreadMessage(Message msg) {
        switch (msg.what) {
            case MSG_THREAD_INIT_RETRIVER: {
                mRetriever = getMetadataRetriever();
                mRetriever.setDataSource((String) msg.obj);
                videoDuration = mRetriever.getVideoDuration();
                int currentTime = 0;
                Canvas canvas = null;
                while (currentTime < videoDuration && mBackHandler.isValid) {
                    Bitmap bitmap = mRetriever.getScaledFrameAtTime(currentTime * 1000,
                            mDefaultFrameWidth, mDefaultFrameHeight);
                    VideoFrame videoFrame = new VideoFrame();
                    videoFrame.bitmap = bitmap;
                    videoFrame.frameTime = currentTime;
                    frameQueue.offer(videoFrame);
                    if (mMeasuredWidth > 0 && mMeasuredHeight > 0) {
                        if (mCurrentBitmap == null) {
                            mFrameWidth = (int) (1.0f * mMeasuredWidth / maxDisplayFrames);
                            mMaxDisplayRect = new Rect(0, 0, mFrameWidth * maxDisplayFrames,
                                    mMeasuredHeight);
                            int bitmapHeight = mMeasuredHeight;
                            int bitmapWidth = (int) (1.0F * mFrameWidth * videoDuration
                                    / frameTime);
                            mCurrentBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                                    Bitmap.Config.RGB_565);
                            canvas = new Canvas(mCurrentBitmap);
                            canvas.drawColor(Color.parseColor("#292929"));
                        }
                        drawToBitmap(canvas);
                    }
                    currentTime += frameTime;
                }
            }
            break;
            case MSG_THREAD_REFRESH_FRAME: {

            }
            break;
        }
    }

    private void drawToBitmap(Canvas canvas) {
        VideoFrame frame = null;
        while ((frame = frameQueue.poll()) != null) {
            int frameLeft = (int) (1.0f * mFrameWidth * frame.frameTime / frameTime);
            Bitmap bitmap = frame.bitmap;
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            Rect destRect = new Rect(frameLeft, 0, frameLeft + mFrameWidth, mMeasuredHeight);
            int srcHeight = (int) (bitmapWidth * (1.0f * destRect.height() / destRect.width()));
            Rect srcRect = null;
            if (srcHeight <= bitmapHeight) {
                int top = (int) ((bitmapHeight - srcHeight) / 2.0f);
                int bottom = top + srcHeight;
                srcRect = new Rect(0, top, bitmapWidth, bottom);
            } else {
                srcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
            }
            canvas.drawBitmap(bitmap, srcRect, destRect, mBitmapPaint);
            bitmap.recycle();
            mUiHandler.sendEmptyMessage(MSG_UI_DRAW_FRAME);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentBitmap != null) {
            canvas.save();
            canvas.translate(translate, 0);
            canvas.drawBitmap(mCurrentBitmap, 0, 0, mBitmapPaint);
            // drawRect(canvas);
            for (SelectedArea a : selectedAreas) {
                drawSelectedArea(canvas, a);
            }
            canvas.restore();
        }
        canvas.drawLine(mMeasuredWidth / 2.0f, 0, mMeasuredWidth / 2.0f, mMeasuredHeight,
                mIndicatePaint);
    }

    private void drawSelectedArea(Canvas canvas, SelectedArea selectedArea) {
        int totalWidth = mCurrentBitmap.getWidth();
        int left = 0;
        int right = totalWidth;
        if (selectedArea.durationTime > 0 && selectedArea.startTime >= 0
                && selectedArea.startTime < videoDuration) {
            left = (int) (1.0f * totalWidth * selectedArea.startTime / videoDuration);
            int width = (int) (1.0f * selectedArea.durationTime * totalWidth / videoDuration);
            right = (left + width) <= totalWidth ? (left + width) : totalWidth;
            Rect rect = new Rect(left, 0, right, mMeasuredHeight);
            mRectPaint.setColor(selectedArea.color);
            canvas.drawRect(rect, mRectPaint);
        } else if (selectedArea.durationTime == -1 && selectedArea.startTime == -1) {
            // -1时代表全部区域
            Rect rect = new Rect(left, 0, right, mMeasuredHeight);
            mRectPaint.setColor(selectedArea.color);
            canvas.drawRect(rect, mRectPaint);
        }
    }

    /**
     * 绘制默认大小
     *
     * @param canvas
     */
    private void drawRect(Canvas canvas) {
        mRectPaint.setStrokeWidth(verticalStroken);
        canvas.drawLine(mMaxDisplayRect.left, mMaxDisplayRect.top, mMaxDisplayRect.left,
                mMaxDisplayRect.bottom, mRectPaint);
        canvas.drawLine(mMaxDisplayRect.right, mMaxDisplayRect.top, mMaxDisplayRect.right,
                mMaxDisplayRect.bottom, mRectPaint);
        mRectPaint.setStrokeWidth(horizontalStroken);
        canvas.drawLine(mMaxDisplayRect.left, mMaxDisplayRect.top, mMaxDisplayRect.right,
                mMaxDisplayRect.top, mRectPaint);
        canvas.drawLine(mMaxDisplayRect.left, mMaxDisplayRect.bottom, mMaxDisplayRect.right,
                mMaxDisplayRect.bottom, mRectPaint);
    }

    private void seek(int time) {
        if (videoDuration > 0 && mCurrentBitmap != null) {
            if (time <= 0) {
                time = 0;
            } else if (time >= videoDuration) {
                time = videoDuration;
            }
            currentTime = time;
            if (listener != null) {
                listener.onAutoSeek(currentTime);
            }
            int length = (int) (1.0f * mCurrentBitmap.getWidth() * time / videoDuration);
            translate = (int) (initTranslate - length);
            invalidate();
        }
    }

    private void move(float dx) {
        if (videoDuration > 0 && mCurrentBitmap != null) {
            translate = (int) (translate + dx);
            int totalwidth = mCurrentBitmap.getWidth();
            if (translate < initTranslate - totalwidth) {
                translate = initTranslate - totalwidth;
            } else if (translate > (initTranslate)) {
                translate = initTranslate;
            }
            currentTime = (int) (1.0f * videoDuration * (initTranslate - translate) / totalwidth);
            if (listener != null) {
                listener.onDrag(currentTime);
            }
            invalidate();
        }
    }

    private final int MSG_UI_DRAW_FRAME = 0x01;

    private final int MSG_UI_TRANSLATE = 0x02;

    private final int MSG_UI_AUTO_SEEK = 0x03;

    private final int MSG_UI_AUTO_REVERSE_SEEK = 0x04;

    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MSG_UI_DRAW_FRAME:
                invalidate();
                break;
            case MSG_UI_TRANSLATE:
                if (mCurrentBitmap != null) {
                    if (mCurrentBitmap.getWidth() > (translate + mMeasuredWidth)) {
                        translate = translate + 4;
                    } else {
                        translate = 0;
                    }
                    invalidate();
                }
                break;
            case MSG_UI_AUTO_SEEK:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seek(mediaPlayer.getCurrentPosition());
                }
                mUiHandler.sendEmptyMessageDelayed(MSG_UI_AUTO_SEEK, 30);
                break;
            case MSG_UI_AUTO_REVERSE_SEEK:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seek(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition());
                }
                mUiHandler.sendEmptyMessageDelayed(MSG_UI_AUTO_REVERSE_SEEK, 30);
                break;
        }
    }

    static class VideoProgressHandler extends Handler {
        public boolean isValid = false;

        private SoftReference<VideoTracerView> reference;

        private int type = 0;

        public VideoProgressHandler(Looper looper, VideoTracerView videoProgressView, int type) {
            super(looper);
            reference = new SoftReference<>(videoProgressView);
            this.type = type;
            isValid = true;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VideoTracerView view = reference.get();
            if (view != null) {
                if (type == 0) {
                    view.handleUIMessage(msg);
                } else {
                    view.handleThreadMessage(msg);
                }
            }
        }

        public void invalidate() {
            reference.clear();
            isValid = false;
        }
    }

    public void onDestory() {
        mBackHandler.removeCallbacks(null);
        mUiHandler.removeCallbacks(null);
        mBackHandler.invalidate();
        mHandlerThread.getLooper().quit();
        if (mRetriever != null) {
            mRetriever.release();
            mRetriever = null;
        }
        mUiHandler.invalidate();
        if (mCurrentBitmap != null && !mCurrentBitmap.isRecycled()) {
            mCurrentBitmap.recycle();
            mCurrentBitmap = null;
        }
    }

    public class VideoFrame {

        public int frameTime;

        public Bitmap bitmap;
    }

    public interface IVideoTracerListener {
        void onTouchDown(int startTime);

        void onTouchUp(int startTime);

        void onAutoSeek(int startTime);

        void onDrag(int startTime);

        void onSelectedAreaChanged(SelectedArea selectedArea);
    }

    private void notifySelectedAreaChanged(SelectedArea selectedArea) {
        if (listener != null) {
            listener.onSelectedAreaChanged(selectedArea);
        }
    }

    public static class SelectedArea {
        public int startTime;

        public int durationTime;

        public int color;

        public String name;

        public SelectedArea(int color, int startTime, int durationTime, String name) {
            this.color = color;
            this.startTime = startTime;
            this.durationTime = durationTime;
            this.name = name;
        }
    }

    private class TouchEdgeResult {
        int touchArea = 1;// 0:left,1;right;

        SelectedArea selectedArea;

        public TouchEdgeResult() {
        }

        public TouchEdgeResult(int touchArea, SelectedArea selectedArea) {
            this.touchArea = touchArea;
            this.selectedArea = selectedArea;
        }

        @Override
        public String toString() {
            return "touchArea:" + touchArea;
        }
    }

    public void setCanChangeSelectedArea(boolean canChangeSelectedArea) {
        this.canChangeSelectedArea = canChangeSelectedArea;
    }

    public void addSelectedArea(SelectedArea selectedArea) {
        if (selectedAreas != null && selectedArea != null)
            selectedAreas.add(selectedArea);
    }

    public void removeSelectedArea(String name) {
        if (!TextUtils.isEmpty(name) && selectedAreas != null) {
            int removeIndex = -1;
            for (int i = 0; i < selectedAreas.size(); i++) {
                SelectedArea selectedArea = selectedAreas.get(i);
                if (TextUtils.equals(selectedArea.name, name)) {
                    removeIndex = i;
                    break;
                }
            }
            if (removeIndex >= 0) {
                selectedAreas.remove(removeIndex);
                invalidate();
            }
        }
    }

    public void setSelectedAreas(List<SelectedArea> selectedAreas) {
        this.selectedAreas = selectedAreas;
    }
}
