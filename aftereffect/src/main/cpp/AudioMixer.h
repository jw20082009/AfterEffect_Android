//
// Created by jw200 on 2019/4/1.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOMIXER_H
#define AFTEREFFECT_ANDROID_AUDIOMIXER_H
extern "C" {
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libavutil/opt.h"
}

#include "JniBase.h"
#include "BaseProgresser.h"

class AudioMixer : public BaseProgresser {
private:
    const char *audio1;
    const char *audio2;
    const char *audioOut;
    AVFormatContext *ifmt_ctx1 = NULL;
    AVFormatContext *ifmt_ctx2 = NULL;
    AVCodecContext *pDecodeCtx1 = NULL;
    AVCodecContext *pDecodeCtx2 = NULL;
    AVFilterGraph *_filter_graph = NULL;
    AVFilterContext *_filterCtxSrc1 = NULL;
    AVFilterContext *_filterCtxSrc2 = NULL;
    AVFilterContext *_filterCtxSink = NULL;

    int startMix();

    int openInputFile1(int *streamIndex, const char *filename);

    int openInputFile2(int *streamIndex, const char *filename);

    int initFilter(const char *filterDesc);

public:
    AudioMixer(const char *audio1, const char *audio2, const char *audioOut);

    ~AudioMixer();
};


#endif //AFTEREFFECT_ANDROID_AUDIOMIXER_H
