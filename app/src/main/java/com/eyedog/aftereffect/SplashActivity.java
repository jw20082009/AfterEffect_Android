package com.eyedog.aftereffect;

import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.eyedog.basic.BaseThreadHandlerActivity;
import com.eyedog.widgets.BgmMonitorView;

public class SplashActivity extends BaseThreadHandlerActivity {
    private final String TAG = getClass().toString();
    BgmMonitorView mMonitorView;
    TextView mTvJni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMonitorView = findViewById(R.id.monitor_view);
        mTvJni = findViewById(R.id.tv_jni);
        obtainThreadMessage(MSG_THREAD_JNI_TEST).sendToTarget();
        postUIDelay(enterRunnable, 500);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            startAnimate();
        } else {
            cancelAnimate();
        }
    }

    private final int MSG_THREAD_JNI_TEST = 0x01;

    @Override
    protected void handleThreadMessage(Message message) {
        super.handleThreadMessage(message);
        switch (message.what) {
            case MSG_THREAD_JNI_TEST:
                String jniStr = JniTest.sayHello(TAG);
                Message uiMsg = obtainUIMessage(MSG_UI_SHOW_JNI_TEST);
                uiMsg.obj = jniStr;
                uiMsg.sendToTarget();
                break;
        }
    }

    private final int MSG_UI_SHOW_JNI_TEST = 0x01;

    @Override
    protected void handleUIMessage(Message message) {
        super.handleUIMessage(message);
        switch (message.what) {
            case MSG_UI_SHOW_JNI_TEST:
                mTvJni.setText((CharSequence) message.obj);
                break;
        }
    }

    private void startAnimate() {
        removeUICallbacks(startRunnable);
        removeUICallbacks(cancelRunnable);
        postUI(startRunnable);
    }

    private void cancelAnimate() {
        removeUICallbacks(startRunnable);
        removeUICallbacks(cancelRunnable);
        postUI(cancelRunnable);
    }

    Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "startRunnable");
            if (mMonitorView != null) {
                mMonitorView.start();
            }
        }
    };

    Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "cancelRunnable");
            if (mMonitorView != null) {
                mMonitorView.cancel();
            }
        }
    };

    Runnable enterRunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    };
}
