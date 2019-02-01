
package com.eyedog.basic.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by walljiang on 2018/5/7.
 */

public class ThreadHandler implements ThreadHandlerWorker {

    private HandlerCallback threadCallback;

    public ThreadHandler() {
        this(null, true);
    }

    public ThreadHandler(HandlerCallback callback, boolean needThreadHandler) {
        if (needThreadHandler() || needThreadHandler) {
            startThreadHandler(callback);
        }
    }

    public void startThreadHandler(HandlerCallback callback) {
        if (callback == null) {
            HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
            handlerThread.start();
            threadCallback = new HandlerCallback() {
                @Override
                public void handleMessage(Message message) {
                    handleThreadMessage(message);
                }
            };
            HandlerManager.getInstance().addHandler(handlerThread.getLooper(), threadCallback);
        } else {
            Handler handler = HandlerManager.getInstance().obtain(callback);
            if (handler == null) {
                HandlerThread handlerThread = new HandlerThread(this.getClass().getName());
                handlerThread.start();
                threadCallback = callback;
                HandlerManager.getInstance().addHandler(handlerThread.getLooper(), threadCallback);
            } else {
                // 同一个callback无需重新启动
            }
        }
    }

    public void handleThreadMessage(Message msg) {
    }

    protected boolean needThreadHandler() {
        return false;
    }


    public void onDestroy() {
        if (threadCallback != null) {
            HandlerManager.getInstance().removeHandler(threadCallback);
        }
    }

    @Override
    public Message obtainThreadMessage(int what) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                return handler.obtainMessage(what);
            }
        }
        return null;
    }

    @Override
    public void sendEmptyThreadMessage(int what) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.obtainMessage(what).sendToTarget();
            }
        }
    }

    @Override
    public void sendThreadMessage(Message msg) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    @Override
    public void sendThreadMessageDelay(Message msg, long timeMillis) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.sendMessageDelayed(msg, timeMillis);
            }
        }
    }

    @Override
    public void sendEmptyThreadMessageDelay(int what, long timeMillis) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.sendEmptyMessageDelayed(what, timeMillis);
            }
        }
    }

    @Override
    public void postThread(Runnable runnable) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }

    @Override
    public void postThreadDelay(Runnable runnable, long delay) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.postDelayed(runnable, delay);
            }
        }
    }

    @Override
    public void removeThreadCallbacks(Runnable runnable) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
        }
    }

    @Override
    public void removeThreadMessage(int what) {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                handler.removeMessages(what);
            }
        }
    }

    public Handler obtainThreadHandler() {
        if (threadCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(threadCallback);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
}
