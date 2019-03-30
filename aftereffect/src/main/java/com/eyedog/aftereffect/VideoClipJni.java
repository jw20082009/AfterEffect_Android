package com.eyedog.aftereffect;

/**
 * created by jw200 at 2019/2/18 21:01
 **/
public class VideoClipJni {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String sayHello(String name);

    public static native int audioMix(String audio1, String audio2, String audioOut);
}
