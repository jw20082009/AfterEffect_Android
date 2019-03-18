package com.eyedog.aftereffect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.eyedog.aftereffect.player.CameraGLSurfaceView;
import com.eyedog.aftereffect.player.VideoGLSurfaceView;

public class GLPlayerActivity extends AppCompatActivity {

    VideoGLSurfaceView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glplayer);
        mCameraView = findViewById(R.id.camera_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.release();
    }
}
