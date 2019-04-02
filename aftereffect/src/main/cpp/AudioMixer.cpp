//
// Created by jw200 on 2019/4/1.
//

#include "AudioMixer.h"

AudioMixer::AudioMixer(const char *audio1, const char *audio2, const char *audioOut) {
    this->audio1 = audio1;
    this->audio2 = audio2;
    this->audioOut = audioOut;
    startMix();
}

int AudioMixer::openInputFile1(int *streamIndex, const char *filename) {
    AVCodec *codec;
    int ret;
    ifmt_ctx1 = NULL;
    if ((ret = avformat_open_input(&ifmt_ctx1, filename, NULL, NULL)) < 0) {
        LOGE("Cannot open input file \n");
        return ret;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx1, NULL)) < 0) {
        LOGE("Cannot find stream information \n");
        return ret;
    }
    (*streamIndex) = av_find_best_stream(ifmt_ctx1, AVMEDIA_TYPE_AUDIO, -1, -1, &codec, 0);
    if ((*streamIndex) < 0) {
        LOGE("Cannot find a audio stream in the input file %s\n", filename);
        return -1;
    }
    AVCodecParameters *pAVCodecParameters = (ifmt_ctx1)->streams[(*streamIndex)]->codecpar;
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

int AudioMixer::openInputFile2(int *streamIndex, const char *filename) {
    AVCodec *codec;
    int ret;
    ifmt_ctx2 = NULL;
    if ((ret = avformat_open_input(&ifmt_ctx2, filename, NULL, NULL)) < 0) {
        LOGE("Cannot open input file \n");
        return ret;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx2, NULL)) < 0) {
        LOGE("Cannot find stream information \n");
        return ret;
    }
    (*streamIndex) = av_find_best_stream(ifmt_ctx2, AVMEDIA_TYPE_AUDIO, -1, -1, &codec, 0);
    if ((*streamIndex) < 0) {
        LOGE("Cannot find a audio stream in the input file %s\n", filename);
        return -1;
    }
    AVCodecParameters *pAVCodecParameters = (ifmt_ctx2)->streams[(*streamIndex)]->codecpar;
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

int AudioMixer::initFilter(const char *filterDesc) {
    int ret = 0;
    char *argsFmt = "time_base=%d/%d:sample_rate=%d:sample_fmt=%s:channel_layout=0x%llu";
    char args_spk[512];
    char argsMic[512];
    char *padName1 = "in0";
    char *padName2 = "in1";

    AVFilter *filterSrc1 = avfilter_get_by_name("abuffer");
    AVFilter *filterSrc2 = avfilter_get_by_name("abuffer");
    AVFilter *filterSink = avfilter_get_by_name("abuffersink");
    AVFilterInOut *filterOut1 = avfilter_inout_alloc();
    AVFilterInOut *filterOut2 = avfilter_inout_alloc();
    AVFilterInOut *filterInput = avfilter_inout_alloc();
    if (filterInput == NULL) {
        ret = -1;
        LOGE("avfilter alloc inputfilter failed");
        return ret;
    }
    _filter_graph = avfilter_graph_alloc();
    snprintf(args_spk, sizeof(args_spk), argsFmt,
             pDecodeCtx1->time_base.num, pDecodeCtx1->time_base.den, pDecodeCtx1->sample_rate,
             av_get_sample_fmt_name(
                     pDecodeCtx1->sample_fmt), pDecodeCtx1->channel_layout);
    snprintf(argsMic, sizeof(argsMic), argsFmt, pDecodeCtx2->time_base.num,
             pDecodeCtx2->time_base.den, pDecodeCtx2->sample_rate,
             av_get_sample_fmt_name(pDecodeCtx2->sample_fmt), pDecodeCtx2->channel_layout);
    if ((ret = avfilter_graph_create_filter(&_filterCtxSrc1, filterSrc1, padName1, args_spk, NULL,
                                            _filter_graph)) < 0) {
        LOGE("Filter:failed to createFilter 1");
        return ret;
    }
    if ((ret = avfilter_graph_create_filter(&_filterCtxSrc2, filterSrc2, padName2, argsMic, NULL,
                                            _filter_graph)) < 0) {
        LOGE("Filter:failed to createFilter 2");
        return ret;
    }
    if ((ret = avfilter_graph_create_filter(&_filterCtxSink, filterSink, "out", NULL, NULL,
                                            _filter_graph)) < 0) {
        LOGE("Filter: failed to createFilter sink");
        return ret;
    }
    AVCodecContext *encodeCtx = pDecodeCtx1;
    if ((ret = av_opt_set_bin(_filterCtxSink, "sample_fmts", (uint8_t *) &encodeCtx->sample_fmt,
                              sizeof(encodeCtx->sample_fmt), AV_OPT_SEARCH_CHILDREN)) < 0) {
        LOGE("Filter: failed to set sampleFormats for sinkCtx");
        return ret;
    }
    if ((ret = av_opt_set_bin(_filterCtxSink, "channel_layouts",
                              (uint8_t *) &encodeCtx->channel_layout,
                              sizeof(encodeCtx->channel_layout), AV_OPT_SEARCH_CHILDREN)) < 0) {
        LOGE("Filter: failed to set channelLayouts for sinkCtx");
        return ret;
    }
    if ((ret = av_opt_set_bin(_filterCtxSink, "sample_rates", (uint8_t *) &encodeCtx->sample_rate,
                              sizeof(encodeCtx->sample_rate), AV_OPT_SEARCH_CHILDREN)) < 0) {
        LOGE("Filter: failed to set sampleRates for sinkCtx");
        return ret;
    }
    filterOut1->name = av_strdup(padName1);
    filterOut1->filter_ctx = _filterCtxSrc1;
    filterOut1->pad_idx = 0;
    filterOut1->next = filterOut2;

    filterOut2->name = av_strdup(padName2);
    filterOut2->filter_ctx = _filterCtxSrc2;
    filterOut2->pad_idx = 0;
    filterOut2->next = NULL;

    filterInput->name = av_strdup("out");
    filterInput->filter_ctx = _filterCtxSink;
    filterInput->pad_idx = 0;
    filterInput->next = NULL;

    AVFilterInOut *filterOutputs[2];
    filterOutputs[0] = filterOut1;
    filterOutputs[1] = filterOut2;

    if ((ret = avfilter_graph_parse_ptr(_filter_graph, filterDesc, &filterInput, filterOutputs,
                                        NULL)) < 0) {
        LOGE("Filter:failed to parsePtr");
        return ret;
    }
    if ((ret = avfilter_graph_config(_filter_graph, NULL)) < 0) {
        LOGE("Filter: failed to config filter");
        return ret;
    }
    if (filterInput != NULL) {
        avfilter_inout_free(&filterInput);
    }
    if (filterOutputs != NULL) {
        avfilter_inout_free(filterOutputs);
    }
    return ret;
}

int AudioMixer::startMix() {
    int ret = -1;
    char *filter_desc = "[in0][in1]amix=inputs=2:duration=first:dropout_transition=0[out]";
    int streamIndex1 = -1;
    int streamIndex2 = -1;
    int readOut1 = 0;
    int readOut2 = 0;
    AVPacket packet1;
    AVPacket packet2;
    AVFrame *pFrame1 = av_frame_alloc();
    AVFrame *pFrame2 = av_frame_alloc();
    AVFrame *pFrameOut = av_frame_alloc();

    av_register_all();
    avfilter_register_all();

    if ((ret = openInputFile1(&streamIndex1, audio1)) < 0) {
        LOGI("open inputfile1 failed %s", audio1);
        goto FINISH;
    }
    if ((ret = openInputFile2(&streamIndex2, audio2)) < 0) {
        LOGI("open inputFile2 failed %s", audio2);
        goto FINISH;
    }
    initFilter(filter_desc);
    av_init_packet(&packet1);
    av_init_packet(&packet2);

    while (true) {
        while (readOut1 <= 0) {
            //解码第一个音频
            packet1.data = NULL;
            packet1.size = 0;
            if ((ret = av_read_frame(ifmt_ctx1, &packet1)) < 0) {
                LOGI("AudioMix ifmt_ctx1 av_read_frame < 0");
                readOut1++;
                continue;
            }
            if (packet1.stream_index == streamIndex1) {
                //解码
                if ((ret = avcodec_send_packet(pDecodeCtx1, &packet1)) < 0) {
                    LOGI("AudioMix ifmt_ctx1 sendPacket error %d", ret);
                    goto FINISH;
                }
            }
            while ((ret = avcodec_receive_frame(pDecodeCtx1, pFrame1)) >= 0) {
                //写入filter first位置
            }
        }
        while (readOut1 <= 0) {
            //解码第二个音频
            if ((ret = av_read_frame(ifmt_ctx2, &packet2)) < 0) {
                LOGI("AudioMix ifmt_ctx2 av_read_frame < 0");
                readOut2++;
                continue;
            }
            if (packet2.stream_index == streamIndex2) {
                //解码
                if ((ret = avcodec_send_packet(pDecodeCtx2, &packet2)) < 0) {
                    LOGI("AudioMix ifmt_ctx1 sendPacket error %d", ret);
                    goto FINISH;
                }
            }
            while ((ret = avcodec_receive_frame(pDecodeCtx2, pFrame2)) >= 0) {
                //写入filter第二位置

            }
        }
        while (true) {
            //读出混合后的音频
//            if ((ret =))
        }
    }
    FINISH:
    av_packet_unref(&packet1);
    av_packet_unref(&packet2);
    if (pFrame1 != NULL) {
        av_frame_free(&pFrame1);
        pFrame1 = NULL;
    }
    if (pFrame2 != NULL) {
        av_frame_free(&pFrame2);
        pFrame2 = NULL;
    }
    if (pFrameOut != NULL) {
        av_frame_free(&pFrameOut);
        pFrameOut = NULL;
    }
    if (pDecodeCtx1 != NULL) {
        avcodec_free_context(&pDecodeCtx1);
        pDecodeCtx1 = NULL;
    }
    if (pDecodeCtx2 != NULL) {
        avcodec_free_context(&pDecodeCtx2);
        pDecodeCtx2 = NULL;
    }
    if (ifmt_ctx1 != NULL) {
        avformat_close_input(&ifmt_ctx1);
        ifmt_ctx1 = NULL;
    }
    if (ifmt_ctx2 != NULL) {
        avformat_close_input(&ifmt_ctx2);
        ifmt_ctx2 = NULL;
    }
    return ret;
}