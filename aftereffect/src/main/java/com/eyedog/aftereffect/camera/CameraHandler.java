
package com.eyedog.aftereffect.camera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * created by jw200 at 2019/3/9 15:00 通过handler处理camera回调，保证线程模型正确
 **/
public abstract class CameraHandler extends Handler {

    public static final int MSG_CAMERA_START_SUCCESS = 0X01;

    public static final int MSG_CAMERA_START_FAILED = 0x02;

    public static final int MSG_CAMERA_START_PREVIEW = 0x03;

    public CameraHandler(Looper looper) {
        super(looper);
    }

    public void startSuccess(int previewWidth, int previewHeight, int pictureWidth,
            int pictureHeight) {
        Bundle data = new Bundle();
        data.putInt("previewWidth", previewWidth);
        data.putInt("previewHeight", previewHeight);
        data.putInt("pictureWidth", pictureWidth);
        data.putInt("pictureHeight", pictureHeight);
        Message msg = obtainMessage(MSG_CAMERA_START_SUCCESS);
        msg.setData(data);
        sendMessage(msg);
    }

    public void startFailed(String errorMsg) {
        sendMessage(Message.obtain(this, MSG_CAMERA_START_FAILED, errorMsg));
    }

    public void startPreview() {
        sendEmptyMessage(MSG_CAMERA_START_PREVIEW);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_CAMERA_START_SUCCESS: {
                Bundle data = msg.getData();
                int previewWidth = data.getInt("previewWidth");
                int previewHeight = data.getInt("previewHeight");
                int pictureWidth = data.getInt("pictureWidth");
                int pictureHeight = data.getInt("pictureHeight");
                handleStartSuccess(previewWidth, previewHeight, pictureWidth, pictureHeight);
            }
                break;
            case MSG_CAMERA_START_FAILED: {
                String errorMsg = null;
                if (msg.obj != null) {
                    errorMsg = (String) msg.obj;
                }
                handleStartFailed(errorMsg);
            }
                break;
            case MSG_CAMERA_START_PREVIEW:
                handleStartPreview();
                break;
        }
    }

    protected abstract void handleStartFailed(String errorMsg);

    protected abstract void handleStartSuccess(int previewWidth, int previewHeight,
            int pictureWidth, int pictureHeight);

    protected abstract void handleStartPreview();
}
