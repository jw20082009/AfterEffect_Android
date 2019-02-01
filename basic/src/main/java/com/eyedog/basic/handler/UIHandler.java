
package com.eyedog.basic.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by walljiang on 2018/5/7.
 */

public class UIHandler implements UIHandlerWorker {

    private HandlerCallback uiCallback;

    public UIHandler() {
        this(null, true);
    }

    public UIHandler(HandlerCallback callback, boolean needUIHandler) {
        if (needUIHandler() || needUIHandler) {
            startUIHandler(callback);
        }
    }

    public void startUIHandler(HandlerCallback callback) {
        if (callback == null) {
            uiCallback = new HandlerCallback() {
                @Override
                public void handleMessage(Message message) {
                    handleUIMessage(message);
                }
            };
            HandlerManager.getInstance().addHandler(Looper.getMainLooper(), uiCallback);
        } else {
            Handler handler = HandlerManager.getInstance().obtain(callback);
            if (handler == null) {
                uiCallback = callback;
                HandlerManager.getInstance().addHandler(Looper.getMainLooper(), uiCallback);
            } else {
                // 同一个callback无需重新启动
            }
        }
    }

    protected boolean needUIHandler() {
        return false;
    }

    public void handleUIMessage(Message msg) {
    }


    public void onDestroy() {
        if (uiCallback != null) {
            HandlerManager.getInstance().removeHandler(uiCallback);
        }
    }

    @Override
    public Message obtainUIMessage(int what) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                return handler.obtainMessage(what);
            }
        }
        return null;
    }

    @Override
    public void sendEmptyUIMessage(int what) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.obtainMessage(what).sendToTarget();
            }
        }
    }

    @Override
    public void sendUIMessage(Message msg) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }

    @Override
    public void sendUIMessageDelay(Message msg, long timeMillis) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.sendMessageDelayed(msg, timeMillis);
            }
        }
    }

    @Override
    public void sendEmptyUIMessageDelay(int what, long timeMillis) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.sendEmptyMessageDelayed(what, timeMillis);
            }
        }
    }

    @Override
    public void postUI(Runnable runnable) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }

    @Override
    public void postUIDelay(Runnable runnable, long delay) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.postDelayed(runnable, delay);
            }
        }
    }

    @Override
    public void removeUICallbacks(Runnable runnable) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
        }
    }

    @Override
    public void removeUIMessage(int what) {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                handler.removeMessages(what);
            }
        }
    }

    @Override
    public Handler obtainUIHandler() {
        if (uiCallback != null) {
            Handler handler = HandlerManager.getInstance().obtain(uiCallback);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
}
