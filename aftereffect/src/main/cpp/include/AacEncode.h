//
// Created by jw200 on 2019/3/30.
//

#ifndef AFTEREFFECT_ANDROID_AACENCODE_H
#define AFTEREFFECT_ANDROID_AACENCODE_H

#include <aacenc_lib.h>
#include <stdio.h>
#include <stdlib.h>
#include "Log.h"

/**
 * 初始化编码器
 * @param out_file
 * @return
 */
int audio_encode_init(const char *out_file, int channels, int bitrate, int sample_rate);

/**
 * 编码音频
 * @param data
 * @return
 */
int audio_encoding(uint8_t *data, int in_buffer_size);

/**
 * 释放资源
 * @return
 */
int audio_encode_end();

#endif //AFTEREFFECT_ANDROID_AACENCODE_H
