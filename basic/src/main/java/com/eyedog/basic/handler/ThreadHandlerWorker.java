package com.eyedog.basic.handler;

import android.os.Handler;
import android.os.Message;

/**
 * created by jw200 at 2018/6/2 12:13
 **/
public interface ThreadHandlerWorker {
    Message obtainThreadMessage(int what);

    void sendEmptyThreadMessage(int what);

    void sendThreadMessage(Message msg);

    void sendThreadMessageDelay(Message msg, long timeMillis);

    void sendEmptyThreadMessageDelay(int what, long timeMillis);

    void postThread(Runnable runnable);

    void postThreadDelay(Runnable runnable, long delay);

    void removeThreadCallbacks(Runnable runnable);

    void removeThreadMessage(int what);

    Handler obtainThreadHandler();
}
