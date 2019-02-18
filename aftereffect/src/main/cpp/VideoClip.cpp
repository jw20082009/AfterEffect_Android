//
// Created by jakechen on 2016/12/30.
//


#include "VideoClip.h"

JNI(jstring, sayHello)(JNIEnv *env, jclass type, jstring name_) {
    string name = env->GetStringUTFChars(name_, NULL);
    string jni = name + ",this is come from jni";
    env->ReleaseStringUTFChars(name_, name.c_str());
    return env->NewStringUTF(jni.c_str());
}