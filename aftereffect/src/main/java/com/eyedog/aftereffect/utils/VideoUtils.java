package com.eyedog.aftereffect.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

/**
 * created by jw200 at 2019/3/15 14:27
 **/
public class VideoUtils {

    public static Bitmap getVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int timeInt = Integer.valueOf(time);
            if (timeInt >= 1000) {
                bitmap =
                    retriever.getFrameAtTime(1200, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
            } else {
                bitmap =
                    retriever.getFrameAtTime(timeInt, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
