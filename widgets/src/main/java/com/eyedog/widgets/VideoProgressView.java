
package com.eyedog.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.eyedog.basic.utils.DensityUtil;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VideoProgressView extends View {
    protected IMetadataRetriever mRetriever;

    // 每屏能展示的时长
    protected int maxDisplayDuration = 10 * 1000;

    // 每屏展示的帧数
    protected int maxDisplayFrames = 8;

    protected int frameTime = (int) (1.0f * maxDisplayDuration / maxDisplayFrames);

    protected int videoDuration;

    protected HandlerThread mHandlerThread;

    protected VideoProgressHandler mBackHandler, mUiHandler;

    protected int mMeasuredWidth, mMeasuredHeight, mFrameWidth, mScreenWidth, mScreenHeight,
            mDefaultFrameWidth, mDefaultFrameHeight;

    protected ConcurrentLinkedQueue<VideoFrame> frameQueue = new ConcurrentLinkedQueue<VideoFrame>();

    protected Bitmap mCurrentBitmap;

    protected Paint mBitmapPaint;

    public VideoProgressView(Context context) {
        super(context);
        init();
    }

    public VideoProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mDefaultFrameWidth = (int) (1.0f * mScreenWidth / maxDisplayFrames);
        mDefaultFrameHeight = DensityUtil.dip2px(getContext(), 40);
        mHandlerThread = new HandlerThread("VideoProgressThread");
        mHandlerThread.start();
        mUiHandler = new VideoProgressHandler(Looper.getMainLooper(), this, 0);
        mBackHandler = new VideoProgressHandler(mHandlerThread.getLooper(), this, 1);
        setClickable(true);
        initPaint();
        mUiHandler.sendEmptyMessage(MSG_UI_TRANSLATE);
    }

    protected void initPaint() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setFilterBitmap(true);
    }

    public void setDataSource(String filePath, int maxDisplayDuration, int maxDisplayFrames) {
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
                while (currentTime < videoDuration) {
                    Bitmap bitmap = mRetriever.getScaledFrameAtTime(currentTime * 1000,
                            mDefaultFrameWidth, mDefaultFrameHeight);
                    VideoFrame videoFrame = new VideoFrame();
                    videoFrame.bitmap = bitmap;
                    videoFrame.frameTime = currentTime;
                    frameQueue.offer(videoFrame);
                    if (mMeasuredWidth > 0 && mMeasuredHeight > 0) {
                        if (mCurrentBitmap == null) {
                            mFrameWidth = (int) (1.0f * mMeasuredWidth / maxDisplayFrames);
                            int bitmapHeight = mMeasuredHeight;
                            int bitmapWidth = (int) (1.0F * mFrameWidth * videoDuration
                                    / frameTime);
                            mCurrentBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                                    Bitmap.Config.RGB_565);
                            canvas = new Canvas(mCurrentBitmap);
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
            if(frameLeft == 0){
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStrokeWidth(50);
                canvas.drawLine(25,0,25,mMeasuredHeight,paint);
            }
            bitmap.recycle();
            mUiHandler.sendEmptyMessage(MSG_UI_DRAW_FRAME);
        }
    }

    int translate = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentBitmap != null) {
            canvas.translate(-1 * translate, 0);
            canvas.drawBitmap(mCurrentBitmap, 0, 0, mBitmapPaint);
        }
    }

    private final int MSG_UI_DRAW_FRAME = 0x01;

    private final int MSG_UI_TRANSLATE = 0x02;

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
                mUiHandler.sendEmptyMessageDelayed(MSG_UI_TRANSLATE, 50);
                break;
        }
    }

    static class VideoProgressHandler extends Handler {

        private SoftReference<VideoProgressView> reference;

        private int type = 0;

        public VideoProgressHandler(Looper looper, VideoProgressView videoProgressView, int type) {
            super(looper);
            reference = new SoftReference<>(videoProgressView);
            this.type = type;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VideoProgressView view = reference.get();
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
        }
    }

    public void onDestory() {
        mRetriever.release();
        mBackHandler.invalidate();
        mHandlerThread.getLooper().quit();
        mUiHandler.invalidate();
    }

    public class VideoFrame {

        public int frameTime;

        public Bitmap bitmap;
    }

    public interface IMetadataRetriever {
        void setDataSource(String filePath);

        int getVideoWidth();

        int getVideoHeight();

        int getVideoRotation();

        int getVideoDuration();

        Bitmap getScaledFrameAtTime(long timeUs, int width, int height);

        void release();
    }
}
