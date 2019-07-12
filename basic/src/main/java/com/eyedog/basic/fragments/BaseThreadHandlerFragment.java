
package com.eyedog.basic.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.eyedog.basic.handler.HandlerCallback;
import com.eyedog.basic.handler.ThreadHandler;
import com.eyedog.basic.handler.ThreadHandlerWorker;

public class BaseThreadHandlerFragment extends BaseUIHandlerFragment
        implements ThreadHandlerWorker {
    ThreadHandler threadHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        threadHandler = new ThreadHandler(threadCallback, true);
    }

    HandlerCallback threadCallback = new HandlerCallback() {
        @Override
        public void handleMessage(Message message) {
            handleThreadMessage(message);
        }
    };

    protected void handleThreadMessage(Message message) {
        threadHandler.handleThreadMessage(message);
    }

    @Override
    public Message obtainThreadMessage(int what) {
        return threadHandler.obtainThreadMessage(what);
    }

    @Override
    public void sendEmptyThreadMessage(int what) {
        threadHandler.sendEmptyThreadMessage(what);
    }

    @Override
    public void sendThreadMessage(Message msg) {
        threadHandler.sendThreadMessage(msg);
    }

    @Override
    public void sendThreadMessageDelay(Message msg, long timeMillis) {
        threadHandler.sendThreadMessageDelay(msg, timeMillis);
    }

    @Override
    public void sendEmptyThreadMessageDelay(int what, long timeMillis) {
        threadHandler.sendEmptyThreadMessageDelay(what, timeMillis);
    }

    @Override
    public void postThread(Runnable runnable) {
        threadHandler.postThread(runnable);
    }

    @Override
    public void postThreadDelay(Runnable runnable, long delay) {
        threadHandler.postThreadDelay(runnable, delay);
    }

    @Override
    public void removeThreadCallbacks(Runnable runnable) {
        threadHandler.removeThreadCallbacks(runnable);
    }

    @Override
    public void removeThreadMessage(int what) {
        threadHandler.removeThreadMessage(what);
    }

    @Override
    public Handler obtainThreadHandler() {
        return threadHandler.obtainThreadHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        threadHandler.onDestroy();
    }
}
