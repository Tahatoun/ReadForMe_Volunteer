/*
 * Copyright (C) 2016 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package ensias.readforme_volunteer.lame;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidLame {

    static {
        System.loadLibrary("wrapper");
    }

    public AndroidLame() {
        initializeDefault();
    }

    public AndroidLame(LameBuilder builder) {
        initialize(builder);
    }

    private void initialize(LameBuilder builder) {
        initialize(builder.inSampleRate, builder.outChannel, builder.outSampleRate,
                builder.outBitrate, builder.scaleInput, getIntForMode(builder.mode), getIntForVbrMode(builder.vbrMode), builder.quality, builder.vbrQuality, builder.abrMeanBitrate,
                builder.lowpassFreq, builder.highpassFreq, builder.id3tagTitle, builder.id3tagArtist,
                builder.id3tagAlbum, builder.id3tagYear, builder.id3tagComment);
    }

    public int encode(short[] buffer_l, short[] buffer_r,
                      int samples, byte[] mp3buf) {

        return lameEncode(buffer_l, buffer_r, samples, mp3buf);
    }

    public int encodeBufferInterLeaved(short[] pcm, int samples,
                                       byte[] mp3buf) {
        return encodeBufferInterleaved(pcm, samples, mp3buf);
    }

    public int flush(byte[] mp3buf) {
        return lameFlush(mp3buf);
    }

    public void close() {
        lameClose();
    }


    ///////////NATIVE
    private static native void initializeDefault();

    private static native void initialize(int inSamplerate, int outChannel,
                                          int outSamplerate, int outBitrate, float scaleInput, int mode, int vbrMode,
                                          int quality, int vbrQuality, int abrMeanBitrate, int lowpassFreq, int highpassFreq, String id3tagTitle,
                                          String id3tagArtist, String id3tagAlbum, String id3tagYear,
                                          String id3tagComment);

    private native static int lameEncode(short[] buffer_l, short[] buffer_r,
                                         int samples, byte[] mp3buf);


    private native static int encodeBufferInterleaved(short[] pcm, int samples,
                                                      byte[] mp3buf);


    private native static int lameFlush(byte[] mp3buf);


    private native static void lameClose();


    ////UTILS
    private static int getIntForMode(LameBuilder.Mode mode) {
        switch (mode) {
            case STEREO:
                return 0;
            case JSTEREO:
                return 1;
            case MONO:
                return 3;
            case DEFAULT:
                return 4;
        }
        return -1;
    }

    private static int getIntForVbrMode(LameBuilder.VbrMode mode) {
        switch (mode) {
            case VBR_OFF:
                return 0;
            case VBR_RH:
                return 2;
            case VBR_ABR:
                return 3;
            case VBR_MTRH:
                return 4;
            case VBR_DEFAUT:
                return 6;
        }
        return -1;
    }

    public static void encode(File file,File file2) {
        int OUTPUT_STREAM_BUFFER = 8192;
        BufferedOutputStream outputStream = null;
        File input = file;
        final File output = file2;

        int CHUNK_SIZE = 8192;

        //addLog("Initialising wav reader");
        WaveReader waveReader = new WaveReader(input);

        try {
            waveReader.openWave();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //addLog("Intitialising encoder");
        AndroidLame androidLame = new LameBuilder()
                .setInSampleRate(waveReader.getSampleRate())
                .setOutChannels(waveReader.getChannels())
                .setOutBitrate(128)
                .setOutSampleRate(waveReader.getSampleRate())
                .setQuality(5)
                .build();
        try {
             outputStream = new BufferedOutputStream(new FileOutputStream(output), OUTPUT_STREAM_BUFFER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int bytesRead = 0;

        short[] buffer_l = new short[CHUNK_SIZE];
        short[] buffer_r = new short[CHUNK_SIZE];
        byte[] mp3Buf = new byte[CHUNK_SIZE];

        int channels = waveReader.getChannels();

        //addLog("started encoding");
        while (true) {
            try {
                if (channels == 2) {

                    bytesRead = waveReader.read(buffer_l, buffer_r, CHUNK_SIZE);
                    //addLog("bytes read=" + bytesRead);

                    if (bytesRead > 0) {

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        //addLog("bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                //addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                } else {

                    bytesRead = waveReader.read(buffer_l, CHUNK_SIZE);
                    //addLog("bytes read=" + bytesRead);

                    if (bytesRead > 0) {
                        int bytesEncoded = 0;

                        bytesEncoded = androidLame.encode(buffer_l, buffer_l, bytesRead, mp3Buf);
                        //addLog("bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                //addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //addLog("flushing final mp3buffer");
        int outputMp3buf = androidLame.flush(mp3Buf);
        //addLog("flushed " + outputMp3buf + " bytes");

        if (outputMp3buf > 0) {
            try {
                //addLog("writing final mp3buffer to outputstream");
                outputStream.write(mp3Buf, 0, outputMp3buf);
                //addLog("closing output stream");
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
