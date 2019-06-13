package com.huya.svkit.libcamera.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.hw.videoprocessor.widgets.videocutview.IMetadataRetriever;

import java.lang.ref.SoftReference;
import java.util.List;

public class VideoProgressView extends View {
    protected IMetadataRetriever mRetriever;
    //每屏能展示的时长
    protected int maxDisplayTime = 10 * 1000;
    //每屏展示的帧数
    protected int maxDisplayFrames = 8;
    protected int frameTime = (int) (1.0f * maxDisplayTime / maxDisplayFrames);
    //每帧最小一秒
    protected int minFrameTime = 1000;
    protected int currentFrameStart = 0;
    protected int currentTimeStart = 0;
    protected int currentTimeDuration = 0;

    protected String filePath;
    protected int videoDuration;

    protected HandlerThread mHandlerThread;
    protected VideoProgressHandler mBackHandler, mUiHandler;
    protected int mMeasuredWidth, mMeasuredHeight;
    protected List<VideoFrameNode> videoFrameNodes;
    protected VideoFrameNode lastNode;
    protected VideoFrameNode firstNode;
    protected Bitmap mCurrentBitmap;

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
        mHandlerThread = new HandlerThread("VideoProgressThread");
        mHandlerThread.start();
        mUiHandler = new VideoProgressHandler(Looper.getMainLooper(), this, 0);
        mBackHandler = new VideoProgressHandler(mHandlerThread.getLooper(), this, 1);
    }

    public void setDataSource(String filePath) {
        mRetriever.setDataSource(filePath);
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

    protected final int MSG_THREAD_INIT_FRAME = 0x02;

    protected void handleThreadMessage(Message msg) {
        switch (msg.what) {
            case MSG_THREAD_INIT_RETRIVER: {
                mRetriever = getMetadataRetriever();
                mRetriever.setDataSource((String) msg.obj);
                videoDuration = mRetriever.getVideoDuration();
            }
            break;
            case MSG_THREAD_INIT_FRAME:
            {
                int maxDuration = maxDisplayTime;
                if (maxDisplayTime > videoDuration) {
                    maxDuration = videoDuration;
                }
                int currentFrameTime =
                while((currentTimeStart + currentTimeDuration) < maxDuration){
                    currentTimeDuration += frameTime;
                }
            }
                break;
        }
    }

    protected void handleUIMessage(Message msg) {

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
}
