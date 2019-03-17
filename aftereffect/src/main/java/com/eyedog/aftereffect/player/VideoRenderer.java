
package com.eyedog.aftereffect.player;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.eyedog.aftereffect.filters.SpStickerFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;

import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * created by jw200 at 2019/3/13 16:05
 **/
public class VideoRenderer extends OesRenderer implements MediaPlayer.OnVideoSizeChangedListener {
    private static final String TAG = "VideoRenderer";

    private MediaPlayer mediaPlayer;

    protected SpStickerFilter mStickerFilter;

    public VideoRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        super.onSurfaceTextureCreated(surfaceTexture);
        Surface surface = new Surface(surfaceTexture);
        mediaPlayer.setSurface(surface);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = mSurfaceView.getContext().getAssets().openFd("video.mp4");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
            FloatBuffer textureBuffer) {
        int currentTexture = OpenGLUtils.GL_NOT_TEXTURE;
        if (mStickerFilter != null) {
            currentTexture = mStickerFilter.drawFrameBuffer(textureId, mVertexBuffer,
                    mTextureBuffer);
        }
        return currentTexture;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.i(TAG, "onVideoSizeChanged " + width + "*" + height);
        onInputSizeChanged(width, height);
    }

    public void startPlay() {
        initMediaPlayer();
    }

    public void stopPlay() {
        Log.i(TAG, "stopCamera");
        synchronized (mLock) {
            mSurfaceTexture = null;
        }
        mediaPlayer.release();
    }
}
