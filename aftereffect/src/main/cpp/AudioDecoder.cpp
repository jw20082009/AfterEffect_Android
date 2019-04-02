//
// Created by jw200 on 2019/4/1.
//

#include "AudioDecoder.h"

AudioDecoder::AudioDecoder(const char *srcFile, const char *outFile) {
    this->srcpath = srcFile;
    this->outpath = outFile;
    startDecode();
}

AudioDecoder::~AudioDecoder() {
    LOGI("~AudioDecoder %s,%s", this->srcpath, this->outpath);
    this->progress = -1;
}

int AudioDecoder::openInputFile(int *audioStream, const char *filename) {
    AVCodec *codec = NULL;
    int ret;
    ifmt_ctx = NULL;
    if ((ret = avformat_open_input(&ifmt_ctx, filename, NULL, NULL))) {
        LOGE("Cannot open input file\n");
        return ret;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) {
        LOGE("Cannot find stream information\n");
        return ret;
    }
    (*audioStream) = av_find_best_stream(ifmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, &codec, 0);
    if ((*audioStream) < 0) {
        LOGE("Cannot find a audio stream in the input file %s\n", filename);
        return -1;
    }
    AVCodecParameters *pAVCodecParameters = (ifmt_ctx)->streams[(*audioStream)]->codecpar;
    decode_ctx = avcodec_alloc_context3(codec);
    if ((ret = avcodec_parameters_to_context(decode_ctx, pAVCodecParameters)) < 0) {
        LOGI("open_decodec_context()无法根据pCodec分配AVCodecContext");
        return ret;
    }
    //打开解码器
    if ((ret = avcodec_open2(decode_ctx, codec, NULL)) < 0) {
        LOGI("open_decodec_context()无法打开编码器");
        return ret;
    }
    return 0;
}

int AudioDecoder::startDecode() {
    LOGI("startDecode %s,%s", this->srcpath, this->outpath);
    AVPacket packet;
    AVFrame *pFrame = av_frame_alloc();
    int streamIndex = -1;
    int ret = -1;
    FILE *outFile;
    av_register_all();
    if ((ret = openInputFile(&streamIndex, srcpath))) {
        LOGI("open_input_file1 failed %s", srcpath);
        goto FINISH;
    }
    totalProgress = ifmt_ctx->streams[streamIndex]->duration;
    outFile = fopen(this->outpath, "wb+");
    av_init_packet(&packet);
    while (true) {
        packet.data = NULL;
        packet.size = 0;
        if ((ret = av_read_frame(ifmt_ctx, &packet)) < 0) {
            LOGI("av_read_frame < 0,%d", ret);
            goto FINISH;
        }
        if (packet.stream_index == streamIndex) {
            ret = avcodec_send_packet(decode_ctx, &packet);
            if (ret < 0) {
                LOGI("向解码器发送数据失败 %d\n", ret);
            }
        }
        while ((ret = avcodec_receive_frame(decode_ctx, pFrame)) >= 0) {
            //write to file
            fwrite(pFrame->data[0], 1, pFrame->linesize[0], outFile);
            progress = pFrame->pts;
            LOGI("fwrite %d,progress: %lld,total: %lld", pFrame->linesize[0], progress,
                 totalProgress);
        }
    }
    FINISH:
    av_packet_unref(&packet);
    if (pFrame != NULL) {
        av_frame_free(&pFrame);
    }
    return ret;
}

int AudioDecoder::getProgress() {
    this->progress++;
    LOGI("getProgress %d", this->progress);
    return this->progress;
}