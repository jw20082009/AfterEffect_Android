package com.eyedog.basic.handler;

import android.os.Handler;
import android.os.Message;

/**
 * created by jw200 at 2018/6/2 12:10
 **/
public interface UIHandlerWorker {
    Message obtainUIMessage(int what);

    void sendEmptyUIMessage(int what);

    void sendUIMessage(Message msg);

    void sendUIMessageDelay(Message msg, long timeMillis);

    void sendEmptyUIMessageDelay(int what, long timeMillis);

    void postUI(Runnable runnable);

    void postUIDelay(Runnable runnable, long delay);

    void removeUICallbacks(Runnable runnable);

    void removeUIMessage(int what);

    Handler obtainUIHandler();
}
