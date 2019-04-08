//
// Created by jw200 on 2019/4/4.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOTRANSCODER_H
#define AFTEREFFECT_ANDROID_AUDIOTRANSCODER_H

extern "C" {
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
}
#define SWR_CH_MAX 32

class AudioTranscoder {

private:
    SwrContext *pSwrCtx = NULL;
    int64_t out_ch_layout = av_get_default_channel_layout(2);
    enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16P;
    int out_sample_rate = 44100;

    void setup_array(uint8_t *out[SWR_CH_MAX], AVFrame *inframe, int format, int samples);

public:
    void initSwr(int64_t in_ch_layout, enum AVSampleFormat in_sample_fmt, int in_sample_rate);

    void deInitSwr();

    int transSample(AVFrame *inframe, AVFrame *outframe, enum AVSampleFormat samplefmt);
};


#endif //AFTEREFFECT_ANDROID_AUDIOTRANSCODER_H
