//
// Created by jakechen on 2016/12/30.
//


#include "VideoClip.h"

JNI(jstring, sayHello)(JNIEnv *env, jclass type, jstring name_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    env->ReleaseStringUTFChars(name_, name);
    char buff[128] = {0};
    sprintf(buff, "%s,this is come from jni", name);
    return env->NewStringUTF(buff);
}

JNI(jint, audioMix)(JNIEnv *env, jclass type, jstring audio1, jstring audio2, jstring audioOut) {
    const char *_audio1 = env->GetStringUTFChars(audio1, 0);
    const char *_audio2 = env->GetStringUTFChars(audio2, 0);
    const char *_audioOut = env->GetStringUTFChars(audioOut, 0);
    int result = audioMix(_audio1, _audio2, _audioOut);
    env->ReleaseStringUTFChars(audio1, _audio1);
    env->ReleaseStringUTFChars(audio2, _audio2);
    env->ReleaseStringUTFChars(audioOut, _audioOut);
    return result;
}
