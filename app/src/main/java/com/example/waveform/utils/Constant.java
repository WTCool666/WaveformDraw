package com.example.waveform.utils;

import android.media.AudioFormat;

public class Constant {

    public static final int INDEX_TIMES=100;

    public static final String SINGLE_FILE="singleFile.pcm";
    public static final String DOUBLE_FILE="doubleFile.pcm";

    public static final int SINGLE_CHANNEL_SAMPLEER_RATE=44100;
    public static final int SINGLE_CAHNNEL_FORMAT=AudioFormat.ENCODING_PCM_16BIT;
    public static final int SINGLE_CHANNLE_BIT_WIDTH=SINGLE_CAHNNEL_FORMAT==AudioFormat.ENCODING_PCM_16BIT?16:8;
    public static final int SINGLE_CHANNEL_CONFIG=AudioFormat.CHANNEL_IN_MONO;
    public static final int SINGE_CHANNEL_COUNT=SINGLE_CHANNEL_CONFIG==AudioFormat.CHANNEL_IN_STEREO?2:1;

    public static final int DOUBLE_CHANNEL_SAMPLEER_RATE=16000;
    public static final int DOUBLE_CAHNNEL_FORMAT=AudioFormat.ENCODING_PCM_16BIT;
    public static final int DOUBLE_CHANNLE_BIT_WIDTH=DOUBLE_CAHNNEL_FORMAT==AudioFormat.ENCODING_PCM_16BIT?16:8;
    public static final int DOUBLE_CHANNEL_CONFIG=AudioFormat.CHANNEL_IN_STEREO;
    public static final int DOUBLE_CHANNEL_COUNT=DOUBLE_CHANNEL_CONFIG==AudioFormat.CHANNEL_IN_STEREO?2:1;




}
