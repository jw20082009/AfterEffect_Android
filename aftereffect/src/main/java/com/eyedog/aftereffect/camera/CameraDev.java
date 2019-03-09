package com.eyedog.aftereffect.camera;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import com.eyedog.aftereffect.utils.CameraSizeUtils;
import com.eyedog.basic.handler.ThreadHandler;
import java.io.IOException;
import java.util.List;

/**
 * created by jw200 at 2019/3/9 14:46
 **/
public class CameraDev extends ThreadHandler {
    private final int MSG_START_CAMERA = 0X01;
    private final int MSG_START_PREVIEW = 0x02;
    private final int MSG_STOP_CAMERA = 0X03;

    private static final int DEFAULT_PREVIEW_RATE = 30;

    private int mPreviewStatus = 0;//0:idle预览未开始，1：previewStarting正在开始预览,2: previewing 预览中
    private int mCameraStatus = 0;//0:idle 关闭状态，1：cameraStarting正在打开中，2：cameraOpened 已打开
    private CameraHandler mCallback;
    private Camera mCameraDevice;
    private int mDefaultCameraID = -1;
    private int mFacing;
    private Camera.Parameters mParams;
    private int mPictureWidth = 1920;
    private int mPictureHeight = 1080;
    private int mPreferPreviewWidth = 1920;
    private int mPreferPreviewHeight = 1080;
    private int mPreviewWidth;
    private int mPreviewHeight;

    public CameraDev(CameraHandler callback) {
        this.mCallback = callback;
    }

    public void startCamera(int facing, int preferPreviewWidth, int preferPreviewHeight) {
        removeThreadMessage(MSG_STOP_CAMERA);
        removeThreadMessage(MSG_START_CAMERA);
        sendThreadMessage(
            Message.obtain(obtainThreadHandler(), MSG_START_CAMERA, preferPreviewWidth,
                preferPreviewHeight, facing));
    }

    public void startPreview(SurfaceTexture texture) {
        sendThreadMessage(Message.obtain(obtainThreadHandler(), MSG_START_PREVIEW, texture));
    }

    public void stopCamera() {
        removeThreadMessage(MSG_STOP_CAMERA);
        removeThreadMessage(MSG_START_CAMERA);
        sendEmptyThreadMessage(MSG_STOP_CAMERA);
    }

    @Override
    public void handleThreadMessage(Message msg) {
        super.handleThreadMessage(msg);
        switch (msg.what) {
            case MSG_START_CAMERA:
                if (mCameraStatus == 0 && msg.obj != null) {
                    mPreferPreviewWidth = msg.arg1;
                    mPreferPreviewHeight = msg.arg2;
                    handleStartCamera((Integer) msg.obj);
                }
                break;
            case MSG_START_PREVIEW: {
                Object tag = msg.obj;
                if (tag != null && mPreviewStatus == 0 && mCameraStatus == 2) {
                    handleStartPreview((SurfaceTexture) msg.obj);
                }
            }
            break;
            case MSG_STOP_CAMERA:
                handleStopCamera();
                break;
        }
    }

    private void handleStartCamera(int facing) {
        if (mCameraStatus == 0) {
            mCameraStatus = 1;
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    int numberOfCameras = Camera.getNumberOfCameras();
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    for (int i = 0; i < numberOfCameras; i++) {
                        Camera.getCameraInfo(i, cameraInfo);
                        if (cameraInfo.facing == facing) {
                            mDefaultCameraID = i;
                            mFacing = facing;
                        }
                    }
                }
                handleStopPreview();
                if (mCameraDevice != null) {
                    mCameraDevice.release();
                    mCameraDevice = null;
                }
                if (mDefaultCameraID >= 0) {
                    mCameraDevice = Camera.open(mDefaultCameraID);
                } else {
                    mCameraDevice = Camera.open();
                    mFacing = Camera.CameraInfo.CAMERA_FACING_BACK; //default: back facing
                }
                mCameraDevice.setDisplayOrientation(90);
            } catch (Exception e) {
                e.printStackTrace();
                mCameraDevice = null;
                mCallback.startFailed("打开摄像头失败：" + e.getMessage());
            }
            if (mCameraDevice != null) {
                try {
                    initCamera(DEFAULT_PREVIEW_RATE);
                    mCameraStatus = 2;
                    mCallback.startSuccess(mPreviewWidth, mPreviewHeight, mPictureWidth,
                        mPictureHeight);
                } catch (Exception e) {
                    mCameraDevice.release();
                    mCameraDevice = null;
                    mCallback.startFailed("设置摄像头参数失败：" + e.getMessage());
                }
            }
        }
    }

    private void handleStartPreview(SurfaceTexture texture) {
        if (mCameraDevice != null && mPreviewStatus == 0) {
            mPreviewStatus = 1;
            handleStopPreview();
            try {
                mCameraDevice.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCameraDevice.startPreview();
            mPreviewStatus = 2;
        }
    }

    private void handleStopCamera() {
        if (mCameraStatus != 0 && mCameraDevice != null) {
            handleStopPreview();
            mCameraDevice.setPreviewCallback(null);
            mCameraDevice.release();
            mCameraStatus = 0;
            mCameraDevice = null;
            onDestroy();
        }
    }

    private void handleStopPreview() {
        if (mPreviewStatus != 0 && mCameraDevice != null) {
            mCameraDevice.stopPreview();
            mPreviewStatus = 0;
        }
    }

    private void initCamera(int previewRate) {
        if (mCameraDevice == null) {
            Log.e("CameraView", "initCamera: Camera is not opened!");
            return;
        }
        mParams = mCameraDevice.getParameters();
        List<Integer> supportedPictureFormats = mParams.getSupportedPictureFormats();
        mParams.setPictureFormat(PixelFormat.JPEG);
        List<Camera.Size> picSizes = mParams.getSupportedPictureSizes();
        Camera.Size picSz =
            CameraSizeUtils.getLargeSize(picSizes, mPictureWidth, mPictureHeight, false);
        List<Camera.Size> prevSizes = mParams.getSupportedPreviewSizes();
        Camera.Size prevSz =
            CameraSizeUtils.getLargeSize(prevSizes, mPreferPreviewWidth, mPreferPreviewHeight,
                true);
        List<Integer> frameRates = mParams.getSupportedPreviewFrameRates();
        int fpsMax = 0;
        for (Integer n : frameRates) {
            if (fpsMax < n) {
                fpsMax = n;
            }
        }
        mParams.setPreviewSize(prevSz.width, prevSz.height);
        mParams.setPictureSize(picSz.width, picSz.height);
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        previewRate = fpsMax;
        mParams.setPreviewFrameRate(previewRate); //设置相机预览帧率
        try {
            mCameraDevice.setParameters(mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mParams = mCameraDevice.getParameters();
        Camera.Size szPic = mParams.getPictureSize();
        Camera.Size szPrev = mParams.getPreviewSize();
        mPreviewWidth = szPrev.width;
        mPreviewHeight = szPrev.height;
        mPictureWidth = szPic.width;
        mPictureHeight = szPic.height;
    }

    @Override
    protected boolean needThreadHandler() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
