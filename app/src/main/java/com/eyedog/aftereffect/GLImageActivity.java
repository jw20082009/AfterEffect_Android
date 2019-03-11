
package com.eyedog.aftereffect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eyedog.aftereffect.player.ImageGLSurfaceView;

public class GLImageActivity extends AppCompatActivity {
    ImageGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glimage);
        mSurfaceView = findViewById(R.id.surfaceview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }
}
