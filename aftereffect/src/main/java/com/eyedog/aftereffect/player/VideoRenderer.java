package com.eyedog.aftereffect.player;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import java.io.IOException;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/13 16:05
 **/
public class VideoRenderer extends OesRenderer implements SurfaceTexture.OnFrameAvailableListener,
    MediaPlayer.OnVideoSizeChangedListener {
    private static final String TAG = "VideoRenderer";
    private MediaPlayer mediaPlayer;

    public VideoRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
        initMediaPlayer();
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        super.onSurfaceTextureCreated(surfaceTexture);
        Surface surface = new Surface(surfaceTexture);
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd =
                mSurfaceView.getContext().getAssets().openFd("camera_test.mp4");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        mSurfaceView.requestRender();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setIncomingSize(width, height);
    }
}
