package com.eyedog.aftereffect;

/**
 * created by jw200 at 2019/2/18 21:01
 **/
public class VideoClipJni {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String sayHello(String name);
}
