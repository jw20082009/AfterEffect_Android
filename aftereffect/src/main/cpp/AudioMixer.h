//
// Created by jw200 on 2019/4/1.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOMIXER_H
#define AFTEREFFECT_ANDROID_AUDIOMIXER_H
extern "C" {
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libavutil/opt.h"
#include "libavfilter/buffersrc.h"
#include "libavfilter/buffersink.h"
#include "libswresample/swresample.h"
#include "libavutil/samplefmt.h"
}

#include "JniBase.h"
#include "BaseProgresser.h"
#include "AudioEncoder.h"

#define BUF_SIZE_20K 20480000
#define BUF_SIZE_1K 1024000
#define SWR_CH_MAX 32

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
    SwrContext *pSwrCtx = NULL;
    int64_t out_ch_layout = AV_CH_LAYOUT_MONO;
    FILE *out;
    enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;
    int out_sample_rate = 44100;
    AudioEncoder *audioEncoder;

    int startMix();

    int openInputFile1(int *streamIndex, const char *filename);

    int openInputFile2(int *streamIndex, const char *filename);

    int initFilter(const char *filterDesc);

    void initSwr(int64_t in_ch_layout, enum AVSampleFormat in_sample_fmt, int in_sample_rate);

    void deInitSwr();

    void setup_array(uint8_t *out[SWR_CH_MAX], AVFrame *inframe, int format, int samples);

    int transSample(AVFrame *inframe, AVFrame *outframe, enum AVSampleFormat samplefmt);

public:
    AudioMixer(const char *audio1, const char *audio2, const char *audioOut);

    ~AudioMixer();
};


#endif //AFTEREFFECT_ANDROID_AUDIOMIXER_H
