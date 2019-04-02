package com.eyedog.aftereffect.audio;

/**
 * created by jw200 at 2019/4/2 14:23
 **/
public class AudioDecoder {

    public static native int initAudioDecoder(String audioinput, String audioOutput);

    public static native int getDecodeProgress();

    public static native int releaseDecoder();
}
