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
    LOGI("openInputFile1 %s", filename);
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
    LOGI("openInputFile2 %s", filename);
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
    pDecodeCtx2 = avcodec_alloc_context3(codec);
    if ((ret = avcodec_parameters_to_context(pDecodeCtx2, pAVCodecParameters)) < 0) {
        LOGI("open_decodec_context()无法根据pCodec分配AVCodecContext");
        return ret;
    }
    //打开解码器
    if ((ret = avcodec_open2(pDecodeCtx2, codec, NULL)) < 0) {
        LOGI("open_decodec_context()无法打开编码器");
        return ret;
    }
    return 0;
}

int AudioMixer::initFilter(const char *filterDesc) {
    LOGI("initFilter %s", filterDesc);
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
    LOGI("startMix %s,%s,%s", audio1, audio2, audioOut);
    int ret = -1;
    char *filter_desc = "[in0][in1]amix=inputs=2[out]";
    int streamIndex1 = -1;
    int streamIndex2 = -1;
    int readOut1 = 0;
    int readOut2 = 0;
    AVPacket packet1;
    AVPacket packet2;
    AVFrame *pFrame1 = av_frame_alloc();
    AVFrame *pFrame2 = av_frame_alloc();
    AVFrame *pFrameOut = av_frame_alloc();
    AVFrame *pFrameTrans = av_frame_alloc();

    av_register_all();
    avfilter_register_all();
    audioEncoder = new AudioEncoder();
    audioEncoder->audioEncodeInit(audioOut, av_get_channel_layout_nb_channels(out_ch_layout), 80000,
                                  out_sample_rate);
    if ((ret = openInputFile1(&streamIndex1, audio1)) < 0) {
        LOGI("open inputfile1 failed %s", audio1);
        goto FINISH;
    }
    out = fopen("/sdcard/audio/output.pcm", "wb+");
    setTotalProgress(ifmt_ctx1->streams[streamIndex1]->duration);
    if ((ret = openInputFile2(&streamIndex2, audio2)) < 0) {
        LOGI("open inputFile2 failed %s", audio2);
        goto FINISH;
    }
    initSwr(pDecodeCtx1->channel_layout, pDecodeCtx1->sample_fmt, pDecodeCtx1->sample_rate);
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
                break;
            }
            if (packet1.stream_index == streamIndex1) {
                //解码
                if ((ret = avcodec_send_packet(pDecodeCtx1, &packet1)) < 0) {
                    LOGI("AudioMix ifmt_ctx1 sendPacket error %d", ret);
                    goto FINISH;
                }
            }
            while ((ret = avcodec_receive_frame(pDecodeCtx1, pFrame1)) >= 0) {
                if (readOut2 <= 0) {
                    //写入filter first位置
                    if ((ret = av_buffersrc_add_frame(_filterCtxSrc1, pFrame1)) < 0) {
                        LOGE("add frame to filterCtxSrc1 error");
                        continue;
                    }
                } else {
                    pFrame1->pts = av_frame_get_best_effort_timestamp(pFrame1);
                    transSample(pFrame1, pFrameTrans, pDecodeCtx1->sample_fmt);
                    if (pFrameTrans == NULL || pFrameTrans->data[0] == NULL) {
                        pFrameTrans = pFrame1;
                    }
                    fwrite(pFrameTrans->data[0], 1, pFrameTrans->linesize[0], out);
                    audioEncoder->audioEncoding(pFrameTrans->data[0], pFrameTrans->linesize[0]);
                    setProgress(pFrameTrans->pts);
                    LOGI("fwrite %d,progress: %lld,total: %lld", pFrameTrans->linesize[0],
                         getProgress(),
                         getTotalProgress());
                    pFrameTrans->data[0] = NULL;
                    pFrameTrans->linesize[0] = 0;
                }
            }
            break;
        }
        while (readOut2 <= 0) {
            //解码第二个音频
            if ((ret = av_read_frame(ifmt_ctx2, &packet2)) < 0) {
                LOGI("AudioMix ifmt_ctx2 av_read_frame < 0");
                readOut2++;
                break;
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
                if ((ret = av_buffersrc_add_frame(_filterCtxSrc2, pFrame2)) < 0) {
                    LOGE("add frame to filterCtxSrc1 error");
                    continue;
                }
            }
            break;
        }
        while (readOut1 <= 0 && readOut2 <= 0) {
            //读出混合后的音频
            if ((ret = av_buffersink_get_frame_flags(_filterCtxSink, pFrameOut, 0)) < 0) {
                LOGE("getFrame from filter sink error %d,%d", readOut1, readOut2);
                break;
            }
            pFrameOut->pts = av_frame_get_best_effort_timestamp(pFrame1);
            transSample(pFrameOut, pFrameTrans, pDecodeCtx1->sample_fmt);
            if (pFrameTrans == NULL || pFrameTrans->data[0] == NULL) {
                pFrameTrans = pFrameOut;
            }
            fwrite(pFrameTrans->data[0], 1, pFrameTrans->linesize[0], out);
            audioEncoder->audioEncoding(pFrameTrans->data[0], pFrameTrans->linesize[0]);
            setProgress(pFrameTrans->pts);
            pFrameTrans->data[0] = NULL;
            pFrameTrans->linesize[0] = 0;
        }
        if (readOut1 > 0 && readOut2 > 0) {
            goto FINISH;
        }
    }
    FINISH:
    if (audioEncoder != NULL) {
        audioEncoder->audioEncodeEnd();
        delete (audioEncoder);
        audioEncoder = NULL;
    }
    deInitSwr();
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
    if (pFrameTrans != NULL) {
        av_frame_free(&pFrameTrans);
        pFrameTrans = NULL;
    }
    if (pDecodeCtx1 != NULL) {
        avcodec_free_context(&pDecodeCtx1);
        pDecodeCtx1 = NULL;
    }
    if (pDecodeCtx2 != NULL) {
        avcodec_free_context(&pDecodeCtx2);
        pDecodeCtx2 = NULL;
    }
    if (_filterCtxSrc1 != NULL) {
        avfilter_free(_filterCtxSrc1);
        _filterCtxSrc1 = NULL;
    }
    if (_filterCtxSrc2 != NULL) {
        avfilter_free(_filterCtxSrc2);
        _filterCtxSrc2 = NULL;
    }
    if (_filterCtxSink != NULL) {
        avfilter_free(_filterCtxSink);
        _filterCtxSink = NULL;
    }
    if (_filter_graph != NULL) {
        avfilter_graph_free(&_filter_graph);
        _filter_graph = NULL;
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

void AudioMixer::initSwr(int64_t in_ch_layout, enum AVSampleFormat in_sample_fmt,
                         int in_sample_rate) {
    if (in_ch_layout != out_ch_layout || in_sample_rate != out_sample_rate ||
        in_sample_fmt != out_sample_fmt) {
        if (pSwrCtx == NULL) {
            pSwrCtx = swr_alloc();
        }
        pSwrCtx = swr_alloc_set_opts(pSwrCtx, out_ch_layout, out_sample_fmt, out_sample_rate,
                                     in_ch_layout, in_sample_fmt, in_sample_rate, 0, NULL);
        swr_init(pSwrCtx);
    }
}

void AudioMixer::setup_array(uint8_t **out, AVFrame *inframe, int format, int samples) {
    if (av_sample_fmt_is_planar((AVSampleFormat) format)) {
        int i;
        int plane_size = av_get_bytes_per_sample((AVSampleFormat) (format & 0xFF)) * samples;
        format &= 0xFF;
        for (i = 0; i < inframe->channels; i++) {
            out[i] = inframe->data[i];
        }
    } else {
        out[0] = inframe->data[0];
    }
}

void AudioMixer::deInitSwr() {
    if (pSwrCtx != NULL) {
        swr_close(pSwrCtx);
        swr_free(&pSwrCtx);
        pSwrCtx = NULL;
    }
}

int AudioMixer::transSample(AVFrame *inframe, AVFrame *outframe, enum AVSampleFormat samplefmt) {
    int ret;
    int max_dst_nb_samples = 4096;
    int64_t src_nb_samples = inframe->nb_samples;
    outframe->pts = inframe->pts;
    uint8_t *paudiobuf;
    int decode_size, input_size, len;
    if (pSwrCtx != NULL) {
        outframe->nb_samples = av_rescale_rnd(
                swr_get_delay(pSwrCtx, out_sample_rate) + src_nb_samples, out_sample_rate,
                out_sample_rate, AV_ROUND_UP);
        ret = av_samples_alloc(outframe->data, &outframe->linesize[0],
                               av_get_channel_layout_nb_channels(out_ch_layout),
                               outframe->nb_samples, out_sample_fmt, 0);
        if (ret < 0) {
            return -1;
        }
        max_dst_nb_samples = outframe->nb_samples;
        uint8_t *m_ain[SWR_CH_MAX];
        setup_array(m_ain, inframe, samplefmt, src_nb_samples);
        len = swr_convert(pSwrCtx, outframe->data, outframe->nb_samples, (const uint8_t **) m_ain,
                          src_nb_samples);
        if (len < 0) {
            return -1;
        }
    } else {
        return -1;
    }
    return 0;
}

AudioMixer::~AudioMixer() {
    setTotalProgress(-1);
    setProgress(-1);
}