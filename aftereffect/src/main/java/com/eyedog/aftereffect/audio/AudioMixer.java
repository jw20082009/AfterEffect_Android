package com.eyedog.aftereffect.audio;

/**
 * created by jw200 at 2019/4/2 18:14
 **/
public class AudioMixer {

    public static native int initAudioMixer(String audio1, String audio2, String audioOut);

    public static native long getProgress();

    public static native int releaseMixer();
}
