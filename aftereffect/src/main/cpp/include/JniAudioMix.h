//
// Created by jw200 on 2019/4/2.
//

#ifndef AFTEREFFECT_ANDROID_JNIAUDIOMIX_H
#define AFTEREFFECT_ANDROID_JNIAUDIOMIX_H

#include "JniBase.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libavutil/opt.h"
#include "libavfilter/buffersrc.h"
#include "libavfilter/buffersink.h"

#define JNI(rettype, name) JNIEXPORT rettype JNICALL Java_com_eyedog_aftereffect_audio_AudioMixer_##name
JNI(jint, initAudioMixer)(JNIEnv
                          *env,
                          jclass type, jstring audioInput, jstring audioInput2,
                          jstring audioOutput);
JNI(jlong, getProgress)(JNIEnv *env, jclass type);
JNI(jint, releaseMixer)(JNIEnv *env, jclass type);
}
#endif //AFTEREFFECT_ANDROID_JNIAUDIOMIX_H
