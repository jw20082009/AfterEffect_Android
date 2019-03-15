package com.eyedog.aftereffect.player;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import com.eyedog.aftereffect.filters.SpStickerFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import java.io.IOException;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/13 16:05
 **/
public class VideoRenderer extends OesRenderer implements SurfaceTexture.OnFrameAvailableListener,
    MediaPlayer.OnVideoSizeChangedListener {
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initMediaPlayer();
        super.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
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
            currentTexture =
                mStickerFilter.drawFrameBuffer(textureId, mVertexBuffer, mTextureBuffer);
        }
        return currentTexture;
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        mSurfaceView.requestRender();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        onInputSizeChanged(width, height);
    }

    @Override
    public void release() {
        super.release();
        mediaPlayer.release();
    }
}
