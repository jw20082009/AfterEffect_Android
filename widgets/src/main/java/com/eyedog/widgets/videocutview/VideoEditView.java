package com.eyedog.widgets.videocutview;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.AttributeSet;

public class VideoEditView extends VideoCutViewBar {

    MediaMetadataRetriever retriever;

    public VideoEditView(Context context) {
        super(context);
        init();
    }

    public VideoEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        retriever = new MediaMetadataRetriever();
    }

    @Override
    public IMetadataRetriever getMetadataRetriever() {
        if (mMetadataRetriever != null)
            return mMetadataRetriever;
        return new IMetadataRetriever() {
            @Override
            public void setDataSource(String filepath) {
                if (retriever == null) {
                    retriever = new MediaMetadataRetriever();
                }
                retriever.setDataSource(filepath);
            }

            @Override
            public int getVideoWidth() {
                if (retriever == null) {
                    return 0;
                }
                return Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            }

            @Override
            public int getVideoHeight() {
                if (retriever == null) {
                    return 0;
                }
                return Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            }

            @Override
            public int getVideoRotation() {
                if (retriever == null) {
                    return 0;
                }
                return Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            }

            @Override
            public int getVideoDuration() {
                if (retriever == null) {
                    return 0;
                }
                return Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            }

            @Override
            public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    return retriever.getScaledFrameAtTime(timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
                } else {
                    Bitmap bitmap = retriever.getFrameAtTime(timeUs,
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
                if (retriever != null) {
                    retriever.release();
                    retriever = null;
                }
            }
        };
    }

    public int getVideoWidth() {
        return getMetadataRetriever().getVideoWidth();
    }

    public int getVideoHeight() {
        return getMetadataRetriever().getVideoHeight();
    }

    public int getVideoRotation() {
        return getMetadataRetriever().getVideoRotation();
    }

    public int getVideoDuration() {
        return getMetadataRetriever().getVideoDuration();
    }
}
