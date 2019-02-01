package com.eyedog.aftereffect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.eyedog.basic.BaseUIHandlerActivity;
import com.eyedog.basic.utils.StatusBarUtil;
import com.eyedog.widgets.BgmMonitorView;

public class SplashActivity extends BaseUIHandlerActivity {
    private final String TAG = getClass().toString();
    BgmMonitorView mMonitorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMonitorView = findViewById(R.id.monitor_view);
        postUIDelay(enterRunnable, 1500);
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
