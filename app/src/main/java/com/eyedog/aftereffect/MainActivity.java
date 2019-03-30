
package com.eyedog.aftereffect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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
                VideoClipJni.audioMix("/sdcard/audio/1553863788514.aac", "",
                    "/sdcard/audio/audioOut.aac");
            }
        }).start();
    }
}
