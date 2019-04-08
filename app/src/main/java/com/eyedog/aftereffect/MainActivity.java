
package com.eyedog.aftereffect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.eyedog.aftereffect.audio.AudioDecoder;
import com.eyedog.aftereffect.audio.AudioMixer;
import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
