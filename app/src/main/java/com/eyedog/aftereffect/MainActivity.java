
package com.eyedog.aftereffect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eyedog.aftereffect.audio.AudioDecoder;
import com.eyedog.aftereffect.audio.AudioMixer;
import com.eyedog.widgets.VideoProgressView;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private TextView textTestLand;

    protected VideoProgressView mVideoProgressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTestLand = findViewById(R.id.tv_test);
        mVideoProgressView = findViewById(R.id.videoprogressview);
        mVideoProgressView.setDataSource("/sdcard/DCIM/sp_video_1538722774050.mp4",5000,8);
        textTestLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ori = getResources().getConfiguration().orientation;
                if (ori == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为竖屏
                } else if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制为竖屏
                }
            }
        });
    }

    public void startJni(View view) {

    }

    public void startGLlayer(View view) {
        startActivity(new Intent(this, GLPlayerActivity.class));
    }

    public void startGLImage(View view) {
        startActivity(new Intent(this, GLImageActivity.class));
    }

    public void audioMix(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //JniTest.audioMix("/sdcard/audio/1553863788514.aac", "",
                //    "/sdcard/audio/audioOut.aac");
                AudioMixer.initAudioMixer("/sdcard/audio/input.aac", "/sdcard/audio/music.mp3"
                    , "/sdcard/audio/audioOut.aac");
            }
        }).start();
    }

    public void audioMixProgress(View view) {
        long progress = AudioMixer.getProgress();
        Log.i(TAG, "audioMixProgress " + progress);
    }

    public void audioMixRelease(View view) {
        int result = AudioMixer.releaseMixer();
        Log.i(TAG, "audioMixRelease " + result);
    }

    public void audioDecode(View view) {
        AudioDecoder.initAudioDecoder("/sdcard/audio/music.mp3",
            "/sdcard/audio/audioOut.pcm");
    }

    public void audioProgress(View view) {
        int progress = AudioDecoder.getDecodeProgress();
        Log.i(TAG, "audioProgress " + progress);
    }

    public void audioRelease(View view) {
        int releaseResult = AudioDecoder.releaseDecoder();
        Log.i(TAG, "audioRlease " + releaseResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoProgressView.onDestory();
        Log.i(TAG, "onDestroy main");
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG, "finish main");
    }
}
