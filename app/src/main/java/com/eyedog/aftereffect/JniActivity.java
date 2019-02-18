package com.eyedog.aftereffect;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.eyedog.basic.BaseThreadHandlerActivity;

public class JniActivity extends BaseThreadHandlerActivity {
    private final String TAG = getClass().toString();
    private TextView mTvJni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni);
        mTvJni = findViewById(R.id.tv_jni);
    }

    private final int MSG_THREAD_JNI_TEST = 0x01;

    @Override
    protected void handleThreadMessage(Message message) {
        super.handleThreadMessage(message);
        switch (message.what) {
            case MSG_THREAD_JNI_TEST:
                String jniStr = VideoClipJni.sayHello(TAG);
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

    public void muxVideo(View view) {

    }
}
