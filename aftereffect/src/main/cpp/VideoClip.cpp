//
// Created by jakechen on 2016/12/30.
//


#include "VideoClip.h"

JNI(jstring, sayHello)(JNIEnv *env, jclass type, jstring name_) {
    const char *name = env->GetStringUTFChars(name_, NULL);
    env->ReleaseStringUTFChars(name_, name);
    char buff[128] = {0};
    sprintf(buff, "%s,this is come from jni", name);
    return env->NewStringUTF(buff);
}