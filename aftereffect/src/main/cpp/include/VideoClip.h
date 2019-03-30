//
// Created by jakechen on 2016/12/30.
//


#include <jni.h>
#include <string>
#include <iostream>
#include "AudioMix.h"

using namespace std;
extern "C"
{
#define JNI(rettype, name) JNIEXPORT rettype JNICALL Java_com_eyedog_aftereffect_VideoClipJni_##name
JNI(jstring, sayHello)(JNIEnv *env, jclass type, jstring name_);

JNI(jint, audioMix)(JNIEnv *env, jclass type, jstring audio1, jstring audio2, jstring audioOut);
}