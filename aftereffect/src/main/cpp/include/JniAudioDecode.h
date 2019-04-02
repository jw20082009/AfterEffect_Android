//
// Created by jw200 on 2019/4/2.
//

#ifndef AFTEREFFECT_ANDROID_JNIAUDIODECODE_H
#define AFTEREFFECT_ANDROID_JNIAUDIODECODE_H

#include "JniBase.h"

extern "C"
{
#define JNI(rettype, name) JNIEXPORT rettype JNICALL Java_com_eyedog_aftereffect_audio_AudioDecoder_##name
JNI(jint, initAudioDecoder)(JNIEnv
                            *env,
                            jclass type, jstring audioInput, jstring audioOutput);
JNI(jlong, getDecodeProgress)(JNIEnv *env, jclass type);
JNI(jint, releaseDecoder)(JNIEnv *env, jclass type);
}
#endif //AFTEREFFECT_ANDROID_JNIAUDIODECODE_H
