//
// Created by jw200 on 2019/4/1.
//


#ifndef AFTEREFFECT_ANDROID_AUDIODECODER_H
#define AFTEREFFECT_ANDROID_AUDIODECODER_H

#include "Log.h"
#include <iostream>
#include "BaseProgresser.h"

extern "C" {
#include "libavformat/avformat.h"
}
using namespace std;

class AudioDecoder : public BaseProgresser {
private:
    AVFormatContext *ifmt_ctx = NULL;
    AVCodecContext *decode_ctx;
    int64_t out_ch_layout = AV_CH_LAYOUT_STEREO;//双声道
    int out_sample_rate = 44100;
    const char *srcpath;
    const char *outpath;

    int startDecode();

    int openInputFile(int *audioStream, const char *filename);

public:
    AudioDecoder(const char *srcFile, const char *outFile);

    ~AudioDecoder();
};

#endif //AFTEREFFECT_ANDROID_AUDIODECODER_H
