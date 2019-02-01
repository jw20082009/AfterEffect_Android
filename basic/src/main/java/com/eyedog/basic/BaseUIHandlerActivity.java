package com.eyedog.basic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.eyedog.basic.handler.HandlerCallback;
import com.eyedog.basic.handler.UIHandler;
import com.eyedog.basic.handler.UIHandlerWorker;
import com.eyedog.basic.utils.StatusBarUtil;

/**
 * created by jw200 at 2018/6/2 12:05
 **/
public class BaseUIHandlerActivity extends FragmentActivity implements UIHandlerWorker {

    UIHandler uiHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new UIHandler(uiCallback, true);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initStatusBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initStatusBar();
    }

    private void initStatusBar() {
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.onDestroy();
    }
}
