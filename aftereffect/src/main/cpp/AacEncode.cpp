//
// Created by jw200 on 2019/3/30.
//
#include "AacEncode.h"

static HANDLE_AACENCODER handle;
static AACENC_InfoStruct info = {0};
static FILE *out;
static int16_t *convert_buf;

int audio_encode_init(const char *out_file, int channels, int bitrate, int sample_rate) {
    int aot = 2;
    int afterburner = 1;
    int vbr = 1;
    CHANNEL_MODE mode;
    out = fopen(out_file, "wb+");
    switch (channels) {
        case 1:
            mode = MODE_1;
            break;
        case 2:
            mode = MODE_2;
            break;
        case 3:
            mode = MODE_1_2;
            break;
        case 4:
            mode = MODE_1_2_1;
            break;
        case 5:
            mode = MODE_1_2_2;
            break;
        case 6:
            mode = MODE_1_2_2_1;
            break;
        default:
            LOGI("Unsupported WAV channels %d\n", channels);
            return 1;
    }
    if (aacEncOpen(&handle, 0x01, channels) != AACENC_OK) {
        LOGI("Unable to open encoder\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_AOT, aot) != AACENC_OK) {
        LOGI("Unable to set the AOT\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_SAMPLERATE, sample_rate) != AACENC_OK) {
        LOGI("Unable to set the AOT\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_CHANNELMODE, mode) != AACENC_OK) {
        LOGI("Unable to set the channel mode\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_CHANNELORDER, 0) != AACENC_OK) {
        LOGI("Unable to set the wav channel order\n");
        return -1;
    }
    if (vbr) {
        if (aacEncoder_SetParam(handle, AACENC_BITRATEMODE, vbr) != AACENC_OK) {
            LOGI("Unable to set the VBR bitrate mode\n");
            return -1;
        }
    } else {
        if (aacEncoder_SetParam(handle, AACENC_BITRATE, bitrate) != AACENC_OK) {
            LOGI("Unable to set the bitrate\n");
            return -1;
        }
    }
    if (aacEncoder_SetParam(handle, AACENC_HEADER_PERIOD, 100) != AACENC_OK) {
        LOGI("Unable to set AACENC_HEADER_PERIOD\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_TRANSMUX, 2) != AACENC_OK) {
        LOGI("Unable to set the ADTS transmux\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_AFTERBURNER, afterburner) != AACENC_OK) {
        LOGI("Unable to set the afterburner mode\n");
        return -1;
    }
    if (aacEncEncode(handle, NULL, NULL, NULL, NULL) != AACENC_OK) {
        LOGI("Unable to initialize the encoder\n");
        return -1;
    }
    if (aacEncInfo(handle, &info) != AACENC_OK) {
        LOGI("Unable to get the encoder info\n");
        return -1;
    }
    LOGI("audio_encode_init");
    return 0;
}

int audio_encoding2(uint8_t *data, int in_buffer_size) {
    convert_buf = (int16_t *) malloc(in_buffer_size);
    AACENC_BufDesc in_buf = {0}, out_buf = {0};
    int i;
    AACENC_ERROR err;
    void *in_ptr, *out_ptr;
    int in_identifier = IN_AUDIO_DATA;
    int in_size = in_buffer_size;
    int in_elem_size = 2;
    for (i = 0; i < in_buffer_size / 2; i++) {
        const uint8_t *in = &data[2 * i];
        convert_buf[i] = in[0] | (in[1] << 8);
    }
    in_ptr = convert_buf;
//    in_ptr = data;
    in_buf.numBufs = 1;
    in_buf.bufs = &in_ptr;
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &in_size;
    in_buf.bufElSizes = &in_elem_size;
    AACENC_InArgs in_args = {0};
    in_args.numInSamples = in_buffer_size / 2;
    AACENC_OutArgs out_args = {0};

    uint8_t outbuf[512];
    int out_size;
    int out_elem_size;
    int out_identifier = OUT_BITSTREAM_DATA;

    out_ptr = outbuf;
    out_size = sizeof(outbuf);
    out_elem_size = 1;
    out_buf.numBufs = 1;
    out_buf.bufs = &out_ptr;
    out_buf.bufferIdentifiers = &out_identifier;
    out_buf.bufSizes = &out_size;
    out_buf.bufElSizes = &out_elem_size;
    if ((err = aacEncEncode(handle, &in_buf, &out_buf, &in_args, &out_args)) != AACENC_OK) {
        char *msg = (char *) malloc(128);
//        free(data);
        if (err == AACENC_ENCODE_EOF) {
            sprintf(msg, "AACENC_ENCODE_EOF");
            return 0;
        } else if (err == AACENC_INVALID_HANDLE) {
            sprintf(msg, "AACENC_INVALID_HANDLE");
        } else if (err == AACENC_INVALID_CONFIG) {
            sprintf(msg, "AACENC_INVALID_CONFIG");
        } else if (err == AACENC_INIT_META_ERROR) {
            sprintf(msg, "AACENC_INIT_META_ERROR");
        } else if (err == AACENC_ENCODE_ERROR) {
            sprintf(msg, "AACENC_ENCODE_ERROR");
        } else {
            sprintf(msg, "unknow err");
        }
        LOGE("Encoding failed %s\n", msg);
        free(msg);
        return -1;
    }
    if (out_args.numOutBytes == 0) {
//        free(data);
        return 0;
    }
    fwrite(outbuf, 1, out_args.numOutBytes, out);
    LOGI("write frame success %d", out_args.numOutBytes);
//    free(data);
    return 0;
}

int audio_encoding(uint8_t *data, int in_buffer_size) {
    int length = info.frameLength;
    uint8_t *tempData;
    if (in_buffer_size > length) {
        int i = 0;
        for (i = 0; i < (in_buffer_size / length); i++) {
            tempData = data + length * i;
            audio_encoding2(tempData, length);
        }
    } else {
        audio_encoding2(data, in_buffer_size);
    }
    return 0;
}

int audio_encode_end() {
    free(convert_buf);
    aacEncClose(&handle);
    fclose(out);
    return 0;
}
