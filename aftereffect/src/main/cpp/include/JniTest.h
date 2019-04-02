//
// Created by jw200 on 2019/4/2.
//

#ifndef AFTEREFFECT_ANDROID_JNITEST_H
#define AFTEREFFECT_ANDROID_JNITEST_H

#include "JniBase.h"

extern "C"
{
#define JNI(rettype, name) JNIEXPORT rettype JNICALL Java_com_eyedog_aftereffect_JniTest_##name

JNI(jstring, sayHello)(JNIEnv
                       *env,
                       jclass type, jstring
                       name_);
}

#endif //AFTEREFFECT_ANDROID_JNITEST_H
