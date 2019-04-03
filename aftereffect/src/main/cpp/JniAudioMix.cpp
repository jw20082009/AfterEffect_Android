//
// Created by jw200 on 2019/4/2.
//
#include "JniAudioMix.h"
#include "AudioMixer.h"

static AudioMixer *audioMixer = NULL;

JNI(jint, initAudioMixer)(JNIEnv
                          *env,
                          jclass type, jstring audioInput, jstring audioInput2,
                          jstring audioOutput) {
    const char *_audioInput = env->GetStringUTFChars(audioInput, 0);
    const char *_audioInput2 = env->GetStringUTFChars(audioInput2, 0);
    const char *_audioOutput = env->GetStringUTFChars(audioOutput, 0);
    if (audioMixer != NULL) {
        delete (audioMixer);
    }
    audioMixer = new AudioMixer(_audioInput, _audioInput2, _audioOutput);
    env->ReleaseStringUTFChars(audioInput, _audioInput);
    env->ReleaseStringUTFChars(audioInput2, _audioInput2);
    env->ReleaseStringUTFChars(audioOutput, _audioOutput);
    return 0;
}

JNI(jlong, getProgress)(JNIEnv *env, jclass type) {
    if (audioMixer != NULL) {
        return audioMixer->getProgress();
    }
    return -1;
}

JNI(jint, releaseMixer)(JNIEnv *env, jclass type) {
    if (audioMixer != NULL) {
        delete (audioMixer);
        audioMixer = NULL;
    }
    return -1;
}