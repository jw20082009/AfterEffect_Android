//
// Created by jw200 on 2019/3/30.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOMIX_H
#define AFTEREFFECT_ANDROID_AUDIOMIX_H

extern "C" {
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libavutil/opt.h"
#include "libavfilter/buffersrc.h"
#include "libavfilter/buffersink.h"
}

#include "Log.h"
#include "AacEncode.h"

int audioMix(const char *audio1, const char *audio2, const char *audioOut);

#endif //AFTEREFFECT_ANDROID_AUDIOMIX_H
