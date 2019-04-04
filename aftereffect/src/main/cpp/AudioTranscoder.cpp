//
// Created by jw200 on 2019/4/4.
//

#include "AudioTranscoder.h"

void AudioTranscoder::initSwr(int64_t in_ch_layout, enum AVSampleFormat in_sample_fmt,
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

void AudioTranscoder::setup_array(uint8_t **out, AVFrame *inframe, int format, int samples) {
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

void AudioTranscoder::deInitSwr() {
    if (pSwrCtx != NULL) {
        swr_close(pSwrCtx);
        swr_free(&pSwrCtx);
        pSwrCtx = NULL;
    }
}

int
AudioTranscoder::transSample(AVFrame *inframe, AVFrame *outframe, enum AVSampleFormat samplefmt) {
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