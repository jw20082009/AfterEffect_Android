//
// Created by jw200 on 2019/3/30.
//
#include "AudioMix.h"

static AVFormatContext *ifmt_first_ctx = NULL;
static AVCodecContext *pDecodeCtx1;
static int64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
static enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;
static int out_sample_rate = 44100;
static int swrInitFlag = 0;

static int open_input_file1(int *streamIndex, const char *filename) {
    LOGI("filename:%s", filename);
    AVCodec *codec;
    int ret;
    ifmt_first_ctx = NULL;
    if ((ret = avformat_open_input(&ifmt_first_ctx, filename, NULL, NULL)) < 0) {
        LOGE("Cannot open input file\n");
        return ret;
    }
    if ((ret = avformat_find_stream_info(ifmt_first_ctx, NULL)) < 0) {
        LOGE("Cannot find stream information\n");
        return ret;
    }
    (*streamIndex) = av_find_best_stream(ifmt_first_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, &codec, 0);
    if ((*streamIndex) < 0) {
        LOGE("Cannot find a audio stream in the input file %s\n", filename);
        return -1;
    }
    AVCodecParameters *pAVCodecParameters = (ifmt_first_ctx)->streams[(*streamIndex)]->codecpar;
    pDecodeCtx1 = avcodec_alloc_context3(codec);
    if ((ret = avcodec_parameters_to_context(pDecodeCtx1, pAVCodecParameters)) < 0) {
        LOGI("open_decodec_context()无法根据pCodec分配AVCodecContext");
        return ret;
    }
    //打开解码器
    if ((ret = avcodec_open2(pDecodeCtx1, codec, NULL)) < 0) {
        LOGI("open_decodec_context()无法打开编码器");
        return ret;
    }
    return 0;
}

int audioMix(const char *audio1, const char *audio2, const char *audioOut) {
    AVPacket packet2;
    AVPacket packet1;
    AVFrame *pFrame1 = av_frame_alloc();
    AVFrame *pFrame2 = av_frame_alloc();
    AVFrame *pFrame_out = av_frame_alloc();
    int ret = -1;
    int streamIndex1 = -1;
    av_register_all();
    avfilter_register_all();

    if ((ret = open_input_file1(&streamIndex1, audio1)) < 0) {
        LOGI("open_input_file1 failed %s", audio1);
        goto FINISH;
    }
    LOGI("open_input_file1 %s,%d", audio1, streamIndex1);
    audio_encode_init(audioOut, 1,
                      44100,
                      44100);
    av_init_packet(&packet1);
    while (true) {
        packet1.data = NULL;
        packet1.size = 0;
        if ((ret = av_read_frame(ifmt_first_ctx, &packet1)) < 0) {
            LOGI("av_read_frame < 0");
            goto FINISH;
        }
        if (packet1.stream_index == streamIndex1) {
            ret = avcodec_send_packet(pDecodeCtx1, &packet1);
            if (ret < 0) {
                LOGI("向解码器发送数据失败%d\n", ret);
            }
        }
        while ((ret = avcodec_receive_frame(pDecodeCtx1, pFrame1)) >= 0) {
            audio_encoding(pFrame1->data[0], pFrame1->linesize[0]);
        }
    }
    FINISH:
    av_packet_unref(&packet1);
    if (pFrame_out != NULL) {
        av_frame_free(&pFrame_out);
    }
    if (pFrame1 != NULL) {
        av_frame_free(&pFrame1);
    }
    if (pFrame2 != NULL) {
        av_frame_free(&pFrame2);
    }
    if (pDecodeCtx1 != NULL) {
        avcodec_free_context(&pDecodeCtx1);
    }
    if (ifmt_first_ctx != NULL) {
        avformat_close_input(&ifmt_first_ctx);
    }

    return ret;
}