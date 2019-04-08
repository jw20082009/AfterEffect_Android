//
// Created by jw200 on 2019/4/3.
//

#include "AudioEncoder.h"

int AudioEncoder::audioEncodeInit(const char *outfile, int channels, int bitrate, int sampleRate) {
    this->channels = channels;
    int aot = 2;
    int afterburner = 1;
    int vbr = 1;
    CHANNEL_MODE mode;
    out = fopen(outfile, "wb+");
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
            return 1;
    }
    if (aacEncOpen(&handle, 0x01, channels) != AACENC_OK) {
        LOGE("Unable to open encoder");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_AOT, aot) != AACENC_OK) {
        LOGE("Unable to set the AOT \n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_SAMPLERATE, sampleRate) != AACENC_OK) {
        LOGE("Unable to set the SampleRate \n");
        return -1;
    }

    if (aacEncoder_SetParam(handle, AACENC_CHANNELMODE, mode) != AACENC_OK) {
        LOGE("Unable to set the channel mode \n");
        return -1;
    }

//    if (aacEncoder_SetParam(handle, AACENC_BITRATEMODE, vbr) != AACENC_OK) {
//        LOGE("Unable to set the VBR bitrate mode\n");
//        return -1;
//    }

    if (aacEncoder_SetParam(handle, AACENC_BITRATE, bitrate) != AACENC_OK) {
        LOGE("Unable to set the Bitrate \n");
        return -1;
    }

    if (aacEncoder_SetParam(handle, AACENC_CHANNELORDER, 0) != AACENC_OK) {
        LOGE("Unable to set wav channel order %d: %s\n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_HEADER_PERIOD, 0xFF) != AACENC_OK) {
        LOGE("Unable to set the header period \n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_TRANSMUX, TT_MP4_ADTS) != AACENC_OK) {
        LOGE("Unable to set the transmux \n");
        return -1;
    }
    if (aacEncoder_SetParam(handle, AACENC_AFTERBURNER, afterburner) != AACENC_OK) {
        LOGE("Unable to set the afterburner mode \n");
        return -1;
    }
    if (aacEncEncode(handle, NULL, NULL, NULL, NULL) != AACENC_OK) {
        LOGE("Unable to initialize the encoder \n");
        return -1;
    }
    if (aacEncInfo(handle, &info) != AACENC_OK) {
        LOGE("Unable to get the encode info \n");
        return -1;
    }
    convert_buf = (int16_t *) malloc(channels * 2 * info.frameLength);
    return 0;
}

int AudioEncoder::audioEncoding2(uint8_t *data, int in_buffer_size) {
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
    in_buf.numBufs = 1;
    in_buf.bufs = &in_ptr;
    in_buf.bufferIdentifiers = &in_identifier;
    in_buf.bufSizes = &in_size;
    in_buf.bufElSizes = &in_elem_size;
    AACENC_InArgs in_args = {0};
    in_args.numInSamples = in_buffer_size <= 0 ? -1 : in_buffer_size / 2;
    AACENC_OutArgs out_args = {0};
    uint8_t outbuf[20480];
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
        if (err == AACENC_ENCODE_EOF) {
            return 0;
        } else {
            return -1;
        }
    }
    if (out_args.numOutBytes == 0) {
        return 0;
    }
    fwrite(outbuf, 1, out_args.numOutBytes, out);

    return 0;
}

int AudioEncoder::audioEncoding(uint8_t *data, int in_buffer_size) {
    int length = channels * 2 * info.frameLength;
    audioEncoding2(data, in_buffer_size);

//    uint8_t *tempData;
//    if (in_buffer_size > length) {
//        int i = 0;
//        int readBytes = length * i;
//        int encodeTimes = in_buffer_size / length;
//        if (encodeTimes * length < in_buffer_size) {
//            encodeTimes++;
//        }
//        for (i = 0; i < encodeTimes; i++) {
//            readBytes = length * i;
//            tempData = data + readBytes;
//            if (readBytes + length > in_buffer_size) {
//                audioEncoding2(tempData, in_buffer_size - readBytes);
//            } else {
//                audioEncoding2(tempData, length);
//            }
//        }
//    } else {
//        audioEncoding2(data, in_buffer_size);
//    }
    return 0;
}

int AudioEncoder::audioEncodeEnd() {
    free(convert_buf);
    aacEncClose(&handle);
    fclose(out);
    return 0;
}