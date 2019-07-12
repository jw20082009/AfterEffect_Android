
package com.eyedog.widgets.videocutview;

import android.graphics.Bitmap;

public interface IMetadataRetriever {
    void setDataSource(String filepath);

    int getVideoWidth();

    int getVideoHeight();

    int getVideoRotation();

    int getVideoDuration();

    Bitmap getScaledFrameAtTime(long timeUs, int width, int height);

    void release();
}
