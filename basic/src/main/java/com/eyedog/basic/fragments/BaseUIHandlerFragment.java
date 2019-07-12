
package com.eyedog.basic.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.eyedog.basic.handler.HandlerCallback;
import com.eyedog.basic.handler.UIHandler;
import com.eyedog.basic.handler.UIHandlerWorker;

public class BaseUIHandlerFragment extends Fragment implements UIHandlerWorker {
    UIHandler uiHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new UIHandler(uiCallback, true);
    }

    HandlerCallback uiCallback = new HandlerCallback() {
        @Override
        public void handleMessage(Message message) {
            handleUIMessage(message);
        }
    };

    protected void handleUIMessage(Message message) {
        uiHandler.handleUIMessage(message);
    }

    @Override
    public Message obtainUIMessage(int what) {
        return uiHandler.obtainUIMessage(what);
    }

    @Override
    public void sendEmptyUIMessage(int what) {
        uiHandler.sendEmptyUIMessage(what);
    }

    @Override
    public void sendUIMessage(Message msg) {
        uiHandler.sendUIMessage(msg);
    }

    @Override
    public void sendUIMessageDelay(Message msg, long timeMillis) {
        uiHandler.sendUIMessageDelay(msg, timeMillis);
    }

    @Override
    public void sendEmptyUIMessageDelay(int what, long timeMillis) {
        uiHandler.sendEmptyUIMessageDelay(what, timeMillis);
    }

    @Override
    public void postUI(Runnable runnable) {
        uiHandler.postUI(runnable);
    }

    @Override
    public void postUIDelay(Runnable runnable, long delay) {
        uiHandler.postUIDelay(runnable, delay);
    }

    @Override
    public void removeUICallbacks(Runnable runnable) {
        uiHandler.removeUICallbacks(runnable);
    }

    @Override
    public void removeUIMessage(int what) {
        uiHandler.removeUIMessage(what);
    }

    @Override
    public Handler obtainUIHandler() {
        return uiHandler.obtainUIHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHandler.onDestroy();
    }
}
