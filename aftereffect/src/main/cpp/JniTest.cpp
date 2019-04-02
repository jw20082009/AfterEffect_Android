//
// Created by jw200 on 2019/4/2.
//
#include "JniTest.h"

JNI(jstring, sayHello)(JNIEnv
                       *env,
                       jclass type, jstring
                       name_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    env->ReleaseStringUTFChars(name_, name);
    char buff[128] = {0};
    sprintf(buff, "%s,this is come from jni", name);
    return env->NewStringUTF(buff);
}

