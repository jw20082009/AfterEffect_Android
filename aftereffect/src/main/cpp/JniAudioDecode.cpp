//
// Created by jw200 on 2019/4/2.
//
#include "JniAudioDecode.h"
#include "AudioDecoder.h"

static AudioDecoder *decoder = NULL;

JNI(jint, initAudioDecoder)(JNIEnv
                            *env,
                            jclass type, jstring audioInput, jstring audioOutput) {
    const char *_audioInput = env->GetStringUTFChars(audioInput, 0);
    const char *_audioOutput = env->GetStringUTFChars(audioOutput, 0);
    decoder = new AudioDecoder(_audioInput, _audioOutput);
    env->ReleaseStringUTFChars(audioInput, _audioInput);
    env->ReleaseStringUTFChars(audioOutput, _audioOutput);
    return 0;
}

JNI(jlong, getDecodeProgress)(JNIEnv *env, jclass type) {
    if (decoder != NULL) {
        return decoder->getProgress();
    } else {
        return -1;
    }
}

JNI(jint, releaseDecoder)(JNIEnv *env, jclass type) {
    if (decoder != NULL) {
        delete (decoder);
        decoder = NULL;
        return 0;
    }
    return -1;
}