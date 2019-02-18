package com.eyedog.aftereffect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.eyedog.aftereffect.camera.CameraView;
import com.eyedog.aftereffect.display.DisplayActivity;
import com.eyedog.basic.BaseUIHandlerActivity;
import com.eyedog.widgets.RecordButton;

public class CameraActivity extends BaseUIHandlerActivity implements View.OnClickListener {
    private final String TAG = getClass().getName();
    private RecordButton mRecordButton;
    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mRecordButton = findViewById(R.id.record_button);
        mRecordButton.setOnRecordListener(recordListener);
        mRecordButton.setOnClickListener(this);
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
    public void onClick(View v) {
    }

    private void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    RecordButton.IRecordListener recordListener = new RecordButton.IRecordListener() {
        @Override
        public void onRecordPreStart() {
            Log.i(TAG, "onRecordPreStart");
        }

        @Override
        public void onRecordStarted() {
            Log.i(TAG, "onRecordStart");
            mRecordButton.startRecord();
        }

        @Override
        public void onRecordPreEnd() {
            Log.i(TAG, "onRecordPreEnd");
        }

        @Override
        public void onRecordEnded() {
            Log.i(TAG, "onRecordEnded");
            startActivity(DisplayActivity.class);
        }

        @Override
        public void onProgressChanged(float progress) {

        }
    };
}
