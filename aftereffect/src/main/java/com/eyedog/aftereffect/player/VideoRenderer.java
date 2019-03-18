
package com.eyedog.aftereffect.player;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;
import com.eyedog.aftereffect.filters.SpStickerFilter;
import com.eyedog.aftereffect.utils.ImageUtils;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import java.io.IOException;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/13 16:05
 **/
public class VideoRenderer extends OesRenderer implements MediaPlayer.OnVideoSizeChangedListener {
    private static final String TAG = "VideoRenderer";

    private MediaPlayer mediaPlayer;

    protected SpStickerFilter mStickerFilter;

    private int mBackgroundTextureId = OpenGLUtils.GL_NOT_TEXTURE;

    private Bitmap mBlurBitmap;

    private boolean mHasBackgroundChanged = false, mHasPlayerSurfaceSetted = false,
        mHasPlayerInited = false;

    public VideoRenderer(GLSurfaceView surfaceView) {
        super(surfaceView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
            synchronized (mLock) {
                mHasBackgroundChanged = true;
            }
        }
        mStickerFilter = new SpStickerFilter(mSurfaceView.getContext());
    }

    @Override
    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        super.onSurfaceTextureCreated(surfaceTexture);
        synchronized (mLock) {
            if (mHasPlayerInited && !mHasPlayerSurfaceSetted) {
                mHasPlayerSurfaceSetted = true;
                Surface surface = new Surface(surfaceTexture);
                mediaPlayer.setSurface(surface);
            }
        }
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
        synchronized (mLock) {
            mHasPlayerInited = true;
            if (mHasTextureCreated && !mHasPlayerSurfaceSetted) {
                mHasPlayerSurfaceSetted = true;
                Surface surface = new Surface(mSurfaceTexture);
                mediaPlayer.setSurface(surface);
            }
        }
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
        FloatBuffer textureBuffer) {
        int currentTexture = OpenGLUtils.GL_NOT_TEXTURE;
        if (mStickerFilter != null && mBackgroundTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
            mStickerFilter.setStickerTextureId(textureId);
            mStickerFilter.setSize(
                new SpStickerFilter.Vec2(0.7f, 0.7f));
            currentTexture = mStickerFilter.drawFrameBuffer(mBackgroundTextureId, mVertexBuffer,
                mTextureBuffer);
        } else {
            Log.i(TAG, "onDrawFrameBuffer " + currentTexture);
        }
        return currentTexture;
    }

    public void setBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int scaledWidth = width;
        int scaledHeight = height;
        onInputSizeChanged(1080, 1920);
        if (160 < width) {
            scaledWidth = 200;
            scaledHeight = (int) (1.0f * scaledWidth / (1.0f * mIncommingWidth / mIncommingHeight));
        }
        mBlurBitmap =
            ImageUtils.blurBitmap(mSurfaceView.getContext(), bitmap, scaledWidth, scaledHeight,
                25);
        synchronized (mLock) {
            mHasBackgroundChanged = true;
        }
    }

    @Override
    protected void onChildFilterSizeChanged() {
        if (mHasBackgroundChanged) {
            synchronized (mLock) {
                if (mHasBackgroundChanged) {
                    mHasBackgroundChanged = false;
                    mBackgroundTextureId =
                        OpenGLUtils.createTexture(mBlurBitmap, mBackgroundTextureId);
                }
            }
        }
        super.onChildFilterSizeChanged();
        if (mStickerFilter != null) {
            mStickerFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
            mStickerFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
        }
    }

    @Override
    protected void beforeDrawFrame() {
        super.beforeDrawFrame();
        if (mHasBackgroundChanged) {
            synchronized (mLock) {
                if (mHasBackgroundChanged) {
                    mHasBackgroundChanged = false;
                    mBackgroundTextureId =
                        OpenGLUtils.createTexture(mBlurBitmap, mBackgroundTextureId);
                }
            }
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        onInputSizeChanged(width, height);
    }

    public void startPlay() {
        initMediaPlayer();
    }

    public void stopPlay() {
        synchronized (mLock) {
            mSurfaceTexture = null;
        }
        mediaPlayer.release();
    }

    @Override
    protected void release() {
        super.release();
        synchronized (mLock) {
            mHasPlayerInited = false;
            mHasBackgroundChanged = false;
            mHasPlayerSurfaceSetted = false;
            mBackgroundTextureId = OpenGLUtils.GL_NOT_TEXTURE;
        }
        if (mStickerFilter != null) {
            mStickerFilter.release();
            mStickerFilter = null;
        }
    }
}
