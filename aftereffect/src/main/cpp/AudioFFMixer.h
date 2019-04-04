//
// Created by jw200 on 2019/4/4.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOFFMIXER_H
#define AFTEREFFECT_ANDROID_AUDIOFFMIXER_H

#include <stdio.h>

#include "JniBase.h"
#include "BaseProgresser.h"
#include "AudioTranscoder.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavutil/channel_layout.h"
#include "libavutil/md5.h"
#include "libavutil/opt.h"
#include "libavutil/samplefmt.h"
#include "libavfilter/avfilter.h"
#include "libavfilter/buffersink.h"
#include "libavfilter/buffersrc.h"
#include <libavformat/avformat.h>
#include <libavfilter/avfiltergraph.h>
#include "libavformat/avio.h"
#include "libavutil/audio_fifo.h"
#include "libavutil/avassert.h"
#include "libavutil/avstring.h"
#include "libavutil/frame.h"
#include "libavutil/opt.h"
#include "libswresample/swresample.h"
#include "libavutil/samplefmt.h"
};



/** The output bit rate in kbit/s */
#define OUTPUT_BIT_RATE 44100
/** The audio sample output format */
#define OUTPUT_SAMPLE_FORMAT AV_SAMPLE_FMT_S16
#define SWR_CH_MAX 32

class AudioFFMixer : public BaseProgresser {

private:
    AVFormatContext *output_format_context = NULL;
    AVCodecContext *output_codec_context = NULL;

    AVFormatContext *input_format_context_0 = NULL;
    AVCodecContext *input_codec_context_0 = NULL;
    AVFormatContext *input_format_context_1 = NULL;
    AVCodecContext *input_codec_context_1 = NULL;

    AVFilterGraph *graph;
    AVFilterContext *src0, *src1, *sink;
    const char *audio1;
    const char *audio2;
    const char *audioOut;
    int out_channel_num = 2;

    int open_output_file(const char *filename,
                         AVCodecContext *input_codec_context,
                         AVFormatContext **output_format_context,
                         AVCodecContext **output_codec_context);

    int init_input_frame(AVFrame **frame);

    int decode_audio_frame(AVFrame *frame,
                           AVFormatContext *input_format_context,
                           AVCodecContext *input_codec_context,
                           int *data_present, int *finished);

    void init_packet(AVPacket *packet);

    int process_all();

    int encode_audio_frame(AVFrame *frame,
                           AVFormatContext *output_format_context,
                           AVCodecContext *output_codec_context,
                           int *data_present);

    int open_input_file(const char *filename,
                        AVFormatContext **input_format_context,
                        AVCodecContext **input_codec_context);

    int init_filter_graph(AVFilterGraph **graph, AVFilterContext **src0, AVFilterContext **src1,
                          AVFilterContext **sink);

    int write_output_file_header(AVFormatContext *output_format_context);

    int write_output_file_trailer(AVFormatContext *output_format_context);

public:
    AudioFFMixer(const char *audio1, const char *audio2, const char *audioOut);

    int startMix();
};


#endif //AFTEREFFECT_ANDROID_AUDIOFFMIXER_H
