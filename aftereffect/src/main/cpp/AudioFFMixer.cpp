//
// Created by jw200 on 2019/4/4.
//

#include "AudioFFMixer.h"

AudioFFMixer::AudioFFMixer(const char *audio1, const char *audio2, const char *audioOut) {
    this->audio1 = audio1;
    this->audio2 = audio2;
    this->audioOut = audioOut;
    startMix();
}

int AudioFFMixer::open_output_file(const char *filename, AVCodecContext *input_codec_context,
                                   AVFormatContext **output_format_context,
                                   AVCodecContext **output_codec_context) {
    AVIOContext *output_io_context = NULL;
    AVStream *stream = NULL;
    AVCodec *output_codec = NULL;
    int error;

    /** Open the output file to write to it. */
    if ((error = avio_open(&output_io_context, filename,
                           AVIO_FLAG_WRITE)) < 0) {
        LOGE("Could not open output file '%s' (error '%d')\n", filename, error);
        return error;
    }

    /** Create a new format context for the output container format. */
    if (!(*output_format_context = avformat_alloc_context())) {
        LOGE("Could not allocate output format context\n");
        return AVERROR(ENOMEM);
    }

    /** Associate the output file (pointer) with the container format context. */
    (*output_format_context)->pb = output_io_context;

    /** Guess the desired container format based on the file extension. */
    if (!((*output_format_context)->oformat = av_guess_format(NULL, filename,
                                                              NULL))) {
        LOGE("Could not find output file format\n");
        goto cleanup;
    }

    av_strlcpy((*output_format_context)->filename, filename,
               sizeof((*output_format_context)->filename));

    /** Find the encoder to be used by its name. */
    if (!(output_codec = avcodec_find_encoder(AV_CODEC_ID_PCM_S16LE))) {
        LOGE("Could not find an PCM encoder.\n");
        goto cleanup;
    }

    /** Create a new audio stream in the output file container. */
    if (!(stream = avformat_new_stream(*output_format_context, output_codec))) {
        LOGE("Could not create new stream\n");
        error = AVERROR(ENOMEM);
        goto cleanup;
    }

    /** Save the encoder context for easiert access later. */
    *output_codec_context = stream->codec;

    /**
     * Set the basic encoder parameters.
     */
    (*output_codec_context)->channels = out_channel_num;
    (*output_codec_context)->channel_layout = av_get_default_channel_layout(out_channel_num);
    (*output_codec_context)->sample_rate = input_codec_context->sample_rate;
    (*output_codec_context)->sample_fmt = AV_SAMPLE_FMT_S16;
    //(*output_codec_context)->bit_rate       = input_codec_context->bit_rate;

    LOGI("output bitrate %d\n", (*output_codec_context)->bit_rate);

    /**
     * Some container formats (like MP4) require global headers to be present
     * Mark the encoder so that it behaves accordingly.
     */
    if ((*output_format_context)->oformat->flags & AVFMT_GLOBALHEADER)
        (*output_codec_context)->flags |= CODEC_FLAG_GLOBAL_HEADER;

    /** Open the encoder for the audio stream to use it later. */
    if ((error = avcodec_open2(*output_codec_context, output_codec, NULL)) < 0) {
        LOGE("Could not open output codec (error '%d')\n", error);
        goto cleanup;
    }

    return 0;

    cleanup:
    avio_close((*output_format_context)->pb);
    avformat_free_context(*output_format_context);
    *output_format_context = NULL;
    return error < 0 ? error : AVERROR_EXIT;
}

int AudioFFMixer::init_input_frame(AVFrame **frame) {
    if (!(*frame = av_frame_alloc())) {
        LOGE("Could not allocate input frame\n");
        return AVERROR(ENOMEM);
    }
    return 0;
}

void AudioFFMixer::init_packet(AVPacket *packet) {
    av_init_packet(packet);
    /** Set the packet data and size so that it is recognized as being empty. */
    packet->data = NULL;
    packet->size = 0;
}

int AudioFFMixer::decode_audio_frame(AVFrame *frame, AVFormatContext *input_format_context,
                                     AVCodecContext *input_codec_context, int *data_present,
                                     int *finished) {
    /** Packet used for temporary storage. */
    AVPacket input_packet;
    int error;
    init_packet(&input_packet);
    /** Read one audio frame from the input file into a temporary packet. */
    if ((error = av_read_frame(input_format_context, &input_packet)) < 0) {
        /** If we are the the end of the file, flush the decoder below. */
        if (error == AVERROR_EOF)
            *finished = 1;
        else {
            LOGE("Could not read frame (error '%d')\n", error);
            return error;
        }
    }

    /**
     * Decode the audio frame stored in the temporary packet.
     * The input audio stream decoder is used to do this.
     * If we are at the end of the file, pass an empty packet to the decoder
     * to flush it.
     */
    if ((error = avcodec_decode_audio4(input_codec_context, frame,
                                       data_present, &input_packet)) < 0) {
        LOGE("Could not decode frame (error '%d')\n", error);
        av_free_packet(&input_packet);
        return error;
    }

    /**
     * If the decoder has not been flushed completely, we are not finished,
     * so that this function has to be called again.
     */
    if (*finished && *data_present)
        *finished = 0;
    av_free_packet(&input_packet);
    return 0;
}

int AudioFFMixer::encode_audio_frame(AVFrame *frame, AVFormatContext *output_format_context,
                                     AVCodecContext *output_codec_context, int *data_present) {
/** Packet used for temporary storage. */
    AVPacket output_packet;
    int error;
    init_packet(&output_packet);
    /**
     * Encode the audio frame and store it in the temporary packet.
     * The output audio stream encoder is used to do this.
     */
    if ((error = avcodec_encode_audio2(output_codec_context, &output_packet,
                                       frame, data_present)) < 0) {
        LOGE("Could not encode frame (error '%d')\n", error);
        av_free_packet(&output_packet);
        return error;
    }

    /** Write one audio frame from the temporary packet to the output file. */
    if (*data_present) {
        if ((error = av_write_frame(output_format_context, &output_packet)) < 0) {
            LOGE("Could not write frame (error '%d')\n", error);
            av_free_packet(&output_packet);
            return error;
        }

        av_free_packet(&output_packet);
    }
    return 0;
}

int AudioFFMixer::process_all() {
    int ret = 0;
    int data_present = 0;
    int finished = 0;
    int nb_inputs = 2;
    AudioTranscoder *transcoder1 = NULL;
    AudioTranscoder *transcoder2 = NULL;
    AVFormatContext *input_format_contexts[2];
    AVCodecContext *input_codec_contexts[2];
    AVFrame *transFrame = av_frame_alloc();
    input_format_contexts[0] = input_format_context_0;
    input_format_contexts[1] = input_format_context_1;
    input_codec_contexts[0] = input_codec_context_0;
    input_codec_contexts[1] = input_codec_context_1;
    if (input_codec_context_0->channel_layout == AV_CH_LAYOUT_MONO) {
        transcoder1 = new AudioTranscoder();
        transcoder1->initSwr(input_codec_context_0->channel_layout,
                             input_codec_context_0->sample_fmt, input_codec_context_0->sample_rate);
    }
    if (input_codec_context_1->channel_layout == AV_CH_LAYOUT_MONO) {
        transcoder2 = new AudioTranscoder();
        transcoder2->initSwr(input_codec_context_1->channel_layout,
                             input_codec_context_1->sample_fmt, input_codec_context_1->sample_rate);
    }
    AVFilterContext *buffer_contexts[2];
    buffer_contexts[0] = src0;
    buffer_contexts[1] = src1;

    int input_finished[2];
    input_finished[0] = 0;
    input_finished[1] = 0;

    int input_to_read[2];
    input_to_read[0] = 1;
    input_to_read[1] = 1;

    int total_samples[2];
    total_samples[0] = 0;
    total_samples[1] = 0;

    int total_out_samples = 0;

    int nb_finished = 0;


    while (nb_finished < nb_inputs) {
        int data_present_in_graph = 0;

        for (int i = 0; i < nb_inputs; i++) {
            if (input_finished[i] || input_to_read[i] == 0) {
                continue;
            }

            input_to_read[i] = 0;

            AVFrame *frame = NULL;

            if (init_input_frame(&frame) > 0) {
                goto end;
            }

            /** Decode one frame worth of audio samples. */
            if ((ret = decode_audio_frame(frame, input_format_contexts[i], input_codec_contexts[i],
                                          &data_present, &finished))) {
                goto end;
            }
            /**
             * If we are at the end of the file and there are no more samples
             * in the decoder which are delayed, we are actually finished.
             * This must not be treated as an error.
             */
            if (finished && !data_present) {
                input_finished[i] = 1;
                nb_finished++;
                ret = 0;
                LOGI("Input n°%d finished. Write NULL frame \n", i);
                ret = av_buffersrc_write_frame(buffer_contexts[i], NULL);
                if (ret < 0) {
                    LOGE("Error writing EOF null frame for input %d\n", i);
                    goto end;
                }
            } else if (data_present) { /** If there is decoded data, convert and store it */
                /* push the audio data from decoded frame into the filtergraph */
                transFrame->data[0] = NULL;
                if (i == 0 && transcoder1 != NULL) {
                    transcoder1->transSample(frame, transFrame, input_codec_context_0->sample_fmt);
                } else if (i == 1 && transcoder2 != NULL) {
                    transcoder2->transSample(frame, transFrame, input_codec_context_1->sample_fmt);
                }
                if (transFrame == NULL || transFrame->data[0] == NULL) {
                    transFrame = frame;
                }
                ret = av_buffersrc_write_frame(buffer_contexts[i], transFrame);
                if (ret < 0) {
                    LOGE("Error while feeding the audio filtergraph\n");
                    goto end;
                }

//                LOGE("add %d samples on input %d (%d Hz, time=%f, ttime=%f)\n",
//                     frame->nb_samples, i, input_codec_contexts[i]->sample_rate,
//                     (double) frame->nb_samples / input_codec_contexts[i]->sample_rate,
//                     (double) (total_samples[i] += frame->nb_samples) /
//                     input_codec_contexts[i]->sample_rate);

            }

            av_frame_free(&frame);

            data_present_in_graph = data_present | data_present_in_graph;
        }

        if (data_present_in_graph) {
            AVFrame *filt_frame = av_frame_alloc();

            /* pull filtered audio from the filtergraph */
            while (1) {
                ret = av_buffersink_get_frame(sink, filt_frame);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                    for (int i = 0; i < nb_inputs; i++) {
                        if (av_buffersrc_get_nb_failed_requests(buffer_contexts[i]) > 0) {
                            input_to_read[i] = 1;
//                            LOGE("Need to read input %d\n", i);
                        }
                    }

                    break;
                }
                if (ret < 0)
                    goto end;

//                LOGE("remove %d samples from sink (%d Hz, time=%f, ttime=%f)\n",
//                     filt_frame->nb_samples, output_codec_context->sample_rate,
//                     (double) filt_frame->nb_samples / output_codec_context->sample_rate,
//                     (double) (total_out_samples += filt_frame->nb_samples) /
//                     output_codec_context->sample_rate);
//                transFrame->data[0] = NULL;
//                transSample(filt_frame, transFrame, output_codec_context->sample_fmt);
//                if (transFrame == NULL || transFrame->data[0] == NULL) {
//                    transFrame = filt_frame;
//                }
                ret = encode_audio_frame(filt_frame, output_format_context, output_codec_context,
                                         &data_present);
                if (ret < 0)
                    goto end;
                av_frame_unref(filt_frame);
            }

            av_frame_free(&filt_frame);
        } else {
            LOGI("No data in graph\n");
            for (int i = 0; i < nb_inputs; i++) {
                input_to_read[i] = 1;
            }
        }

    }

    end:
    av_frame_unref(transFrame);
    av_frame_free(&transFrame);
    if (transcoder1 != NULL) {
        transcoder1->deInitSwr();
        delete (transcoder1);
        transcoder1 = NULL;
    }
    if (transcoder2 != NULL) {
        transcoder2->deInitSwr();
        delete (transcoder2);
        transcoder2 = NULL;
    }
//    deInitSwr();
//    av_frame_unref(transFrame);
    //    avcodec_close(input_codec_context);
    //    avformat_close_input(&input_format_context);
    //    av_frame_free(&frame);
    //    av_frame_free(&filt_frame);

    if (ret < 0 && ret != AVERROR_EOF) {
        LOGE("Error occurred: %s\n", av_err2str(ret));
        return 1;
    }

    return 0;
}

int AudioFFMixer::open_input_file(const char *filename, AVFormatContext **input_format_context,
                                  AVCodecContext **input_codec_context) {
    AVCodec *input_codec;
    int error;

    /** Open the input file to read from it. */
    if ((error = avformat_open_input(input_format_context, filename, NULL,
                                     NULL)) < 0) {
        LOGE("Could not open input file '%s' (error '%d')\n", filename, error);
        *input_format_context = NULL;
        return error;
    }

    /** Get information on the input file (number of streams etc.). */
    if ((error = avformat_find_stream_info(*input_format_context, NULL)) < 0) {
        LOGE("Could not open find stream info (error '%d')\n", error);
        avformat_close_input(input_format_context);
        return error;
    }

    /** Make sure that there is only one stream in the input file. */
    if ((*input_format_context)->nb_streams != 1) {
        LOGE("Expected one audio input stream, but found %d\n",
             (*input_format_context)->nb_streams);
        avformat_close_input(input_format_context);
        return AVERROR_EXIT;
    }

    /** Find a decoder for the audio stream. */
    if (!(input_codec = avcodec_find_decoder(
            (*input_format_context)->streams[0]->codec->codec_id))) {
        LOGE("Could not find input codec\n");
        avformat_close_input(input_format_context);
        return AVERROR_EXIT;
    }

    /** Open the decoder for the audio stream to use it later. */
    if ((error = avcodec_open2((*input_format_context)->streams[0]->codec,
                               input_codec, NULL)) < 0) {
        LOGE("Could not open input codec (error '%d')\n", error);
        avformat_close_input(input_format_context);
        return error;
    }

    /** Save the decoder context for easier access later. */
    *input_codec_context = (*input_format_context)->streams[0]->codec;

    return 0;
}

int AudioFFMixer::init_filter_graph(AVFilterGraph **graph, AVFilterContext **src0,
                                    AVFilterContext **src1, AVFilterContext **sink) {
    AVFilterGraph *filter_graph;
    AVFilterContext *abuffer1_ctx;
    AVFilter *abuffer1;
    AVFilterContext *abuffer0_ctx;
    AVFilter *abuffer0;
    AVFilterContext *mix_ctx;
    AVFilter *mix_filter;
    AVFilterContext *abuffersink_ctx;
    AVFilter *abuffersink;

    char args[512];

    int err;

    /* Create a new filtergraph, which will contain all the filters. */
    filter_graph = avfilter_graph_alloc();
    if (!filter_graph) {
        LOGE("Unable to create filter graph.\n");
        return AVERROR(ENOMEM);
    }

    /****** abuffer 0 ********/

    /* Create the abuffer filter;
     * it will be used for feeding the data into the graph. */
    abuffer0 = avfilter_get_by_name("abuffer");
    if (!abuffer0) {
        LOGE("Could not find the abuffer filter.\n");
        return AVERROR_FILTER_NOT_FOUND;
    }

    /* buffer audio source: the decoded frames from the decoder will be inserted here. */
    if (!input_codec_context_0->channel_layout)
        input_codec_context_0->channel_layout = av_get_default_channel_layout(
                input_codec_context_0->channels);
    snprintf(args, sizeof(args),
             "sample_rate=%d:sample_fmt=%s:channel_layout=0x%" PRIx64,
             input_codec_context_0->sample_rate,
             av_get_sample_fmt_name(input_codec_context_0->sample_fmt),
             input_codec_context_0->channel_layout);


    err = avfilter_graph_create_filter(&abuffer0_ctx, abuffer0, "src0",
                                       args, NULL, filter_graph);
    if (err < 0) {
        LOGE("Cannot create audio buffer source\n");
        return err;
    }

    /****** abuffer 1 ******* */

    /* Create the abuffer filter;
     * it will be used for feeding the data into the graph. */
    abuffer1 = avfilter_get_by_name("abuffer");
    if (!abuffer1) {
        LOGE("Could not find the abuffer filter.\n");
        return AVERROR_FILTER_NOT_FOUND;
    }

    /* buffer audio source: the decoded frames from the decoder will be inserted here. */
    if (!input_codec_context_1->channel_layout)
        input_codec_context_1->channel_layout = av_get_default_channel_layout(
                input_codec_context_1->channels);
    snprintf(args, sizeof(args),
             "sample_rate=%d:sample_fmt=%s:channel_layout=0x%" PRIx64,
             input_codec_context_1->sample_rate,
             av_get_sample_fmt_name(input_codec_context_1->sample_fmt),
             input_codec_context_1->channel_layout);


    err = avfilter_graph_create_filter(&abuffer1_ctx, abuffer1, "src1",
                                       args, NULL, filter_graph);
    if (err < 0) {
        LOGE("Cannot create audio buffer source\n");
        return err;
    }

    /****** amix ******* */
    /* Create mix filter. */
    mix_filter = avfilter_get_by_name("amix");
    if (!mix_filter) {
        LOGE("Could not find the mix filter.\n");
        return AVERROR_FILTER_NOT_FOUND;
    }

    snprintf(args, sizeof(args), "inputs=2");

    err = avfilter_graph_create_filter(&mix_ctx, mix_filter, "amix",
                                       args, NULL, filter_graph);

    if (err < 0) {
        LOGE("Cannot create audio amix filter\n");
        return err;
    }

    /* Finally create the abuffersink filter;
     * it will be used to get the filtered data out of the graph. */
    abuffersink = avfilter_get_by_name("abuffersink");
    if (!abuffersink) {
        LOGE("Could not find the abuffersink filter.\n");
        return AVERROR_FILTER_NOT_FOUND;
    }

    abuffersink_ctx = avfilter_graph_alloc_filter(filter_graph, abuffersink, "sink");
    if (!abuffersink_ctx) {
        LOGE("Could not allocate the abuffersink instance.\n");
        return AVERROR(ENOMEM);
    }

    /* Same sample fmts as the output file. */
    err = av_opt_set_int_list(abuffersink_ctx, "sample_fmts",
                              ((int[]) {AV_SAMPLE_FMT_S16, AV_SAMPLE_FMT_NONE}),
                              AV_SAMPLE_FMT_NONE, AV_OPT_SEARCH_CHILDREN);

    char ch_layout[64];
    av_get_channel_layout_string(ch_layout, sizeof(ch_layout), 0,
                                 out_channel_num);
    LOGI("channel_layout %s", ch_layout);
    av_opt_set(abuffersink_ctx, "channel_layout", ch_layout,
               AV_OPT_SEARCH_CHILDREN);

    if (err < 0) {
        LOGE("Could set options to the abuffersink instance.\n");
        return err;
    }

    err = avfilter_init_str(abuffersink_ctx, NULL);
    if (err < 0) {
        LOGE("Could not initialize the abuffersink instance.\n");
        return err;
    }


    /* Connect the filters; */

    err = avfilter_link(abuffer0_ctx, 0, mix_ctx, 0);
    if (err >= 0)
        err = avfilter_link(abuffer1_ctx, 0, mix_ctx, 1);
    if (err >= 0)
        err = avfilter_link(mix_ctx, 0, abuffersink_ctx, 0);
    if (err < 0) {
        LOGE("Error connecting filters\n");
        return err;
    }

    /* Configure the graph. */
    err = avfilter_graph_config(filter_graph, NULL);
    if (err < 0) {
        LOGE("Error while configuring graph : %d\n", err);
        return err;
    }
    char *dump = avfilter_graph_dump(filter_graph, NULL);

    LOGE("Graph :\n%s\n", dump);
    av_free(dump);
    *graph = filter_graph;
    *src0 = abuffer0_ctx;
    *src1 = abuffer1_ctx;
    *sink = abuffersink_ctx;

    return 0;
}

int AudioFFMixer::write_output_file_trailer(AVFormatContext *output_format_context) {
    int error;
    if ((error = av_write_trailer(output_format_context)) < 0) {
        LOGE("Could not write output file trailer (error '%d')\n", error);
        return error;
    }
    return 0;
}

int AudioFFMixer::write_output_file_header(AVFormatContext *output_format_context) {
    int error;
    if ((error = avformat_write_header(output_format_context, NULL)) < 0) {
        LOGE("Could not write output file header (error '%d')\n", error);
        return error;
    }
    return 0;
}

int AudioFFMixer::startMix() {
    int err;

    av_register_all();
    avfilter_register_all();

    const char *audio1Path = audio1;

    if (open_input_file(audio1Path, &input_format_context_0, &input_codec_context_0) < 0) {
        LOGE("Error while opening file 1\n");
        return 1;
    }
    av_dump_format(input_format_context_0, 0, audio1Path, 0);

    const char *audio2Path = audio2;

    if (open_input_file(audio2Path, &input_format_context_1, &input_codec_context_1) < 0) {
        LOGE("Error while opening file 2\n");
        return 1;
    }
    av_dump_format(input_format_context_1, 0, audio2Path, 0);

    /* Set up the filtergraph. */
    err = init_filter_graph(&graph, &src0, &src1, &sink);
    LOGI("Init err = %d\n", err);

    const char *outputFile = audioOut;
    remove(outputFile);

    LOGI("Output file : %s\n", outputFile);

    err = open_output_file(outputFile, input_codec_context_0, &output_format_context,
                           &output_codec_context);

    LOGI("open output file err : %d\n", err);
    av_dump_format(output_format_context, 0, outputFile, 1);


    if (write_output_file_header(output_format_context) < 0) {
        LOGE("Error while writing header outputfile\n");
        return 1;
    }

    process_all();

    if (write_output_file_trailer(output_format_context) < 0) {
        LOGE("Error while writing header outputfile\n");
        return 1;
    }

    LOGI("FINISHED\n");

    return 0;
}