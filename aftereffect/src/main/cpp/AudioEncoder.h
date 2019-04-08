//
// Created by jw200 on 2019/4/3.
//

#ifndef AFTEREFFECT_ANDROID_AUDIOENCODER_H
#define AFTEREFFECT_ANDROID_AUDIOENCODER_H

#include "JniBase.h"
#include <stdlib.h>
#include <aacenc_lib.h>

class AudioEncoder {
private:
    HANDLE_AACENCODER handle;
    AACENC_InfoStruct info = {0};
    FILE *out;
    int16_t *convert_buf;
    int channels = -1;

    int audioEncoding2(uint8_t *data, int in_buffer_size);

public:
    int audioEncodeInit(const char *outfile, int channels, int bitrate, int sampleRate);

    int audioEncoding(uint8_t *data, int in_buffer_size);

    int audioEncodeEnd();
};


#endif //AFTEREFFECT_ANDROID_AUDIOENCODER_H
