package com.eyedog.aftereffect;

import android.app.Application;

/**
 * created by jw200 at 2018/12/19 21:22
 **/
public class AEApplication extends Application {

    public static AEApplication mContext;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
    }
}
