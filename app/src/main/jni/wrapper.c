
#include "wrapper.h"
#define LOG_TAG "LAME ENCODER"
#define LOGD(format, args...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##args);
#define BUFFER_SIZE 8192
#define be_short(s) ((short) ((unsigned short) (s) << 8) | ((unsigned short) (s) >> 8))

lame_t lame;

int read_samples(FILE *input_file, short *input) {
	int nb_read;
	nb_read = fread(input, 1, sizeof(short), input_file) / sizeof(short);

	int i = 0;
	while (i < nb_read) {
		input[i] = be_short(input[i]);
		i++;
	}

	return nb_read;
}

void Java_ensias_readforme_1volunteer_Record_initEncoder(JNIEnv *env,
		jobject jobj, jint in_num_channels, jint in_samplerate, jint in_brate,
		jint in_mode, jint in_quality) {
	lame = lame_init();

	LOGD("Init parameters:");
	lame_set_num_channels(lame, in_num_channels);
	LOGD("Number of channels: %d", in_num_channels);
	lame_set_in_samplerate(lame, in_samplerate);
	LOGD("Sample rate: %d", in_samplerate);
	lame_set_brate(lame, in_brate);
	LOGD("Bitrate: %d", in_brate);
	lame_set_mode(lame, in_mode);
	LOGD("Mode: %d", in_mode);
	lame_set_quality(lame, in_quality);
	LOGD("Quality: %d", in_quality);

	int res = lame_init_params(lame);
	LOGD("Init returned: %d", res);
}

void Java_ensias_readforme_1volunteer_Record_destroyEncoder(
		JNIEnv *env, jobject jobj) {
	int res = lame_close(lame);
	LOGD("Deinit returned: %d", res);
}

void Java_ensias_readforme_1volunteer_Record_encodeFile(JNIEnv *env,
		jobject jobj, jstring in_source_path, jstring in_target_path) {
	const char *source_path, *target_path;
	source_path = (*env)->GetStringUTFChars(env, in_source_path, NULL);
	target_path = (*env)->GetStringUTFChars(env, in_target_path, NULL);

	FILE *input_file, *output_file;
	input_file = fopen(source_path, "rb");
	output_file = fopen(target_path, "wb");

	short input[BUFFER_SIZE];
	char output[BUFFER_SIZE];
	int nb_read = 0;
	int nb_write = 0;
	int nb_total = 0;

	LOGD("Encoding started");
	while (nb_read = read_samples(input_file, input)) {
		nb_write = lame_encode_buffer(lame, input, input, nb_read, output,
				BUFFER_SIZE);
		fwrite(output, nb_write, 1, output_file);
		nb_total += nb_write;
	}
	LOGD("Encoded %d bytes", nb_total);
	__android_log_print(ANDROID_LOG_ERROR, "createWAV","Bytes : %d",nb_total );

	nb_write = lame_encode_flush(lame, output, BUFFER_SIZE);
	fwrite(output, nb_write, 1, output_file);
	LOGD("Flushed %d bytes", nb_write);

	fclose(input_file);
	fclose(output_file);
}
lame_global_flags *glf;

JNIEXPORT void JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_initializeDefault(
        JNIEnv *env, jclass cls) {

    glf = initializeDefault(env);
}

JNIEXPORT void JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_initialize(
        JNIEnv *env, jclass cls, jint inSamplerate, jint outChannel,
        jint outSamplerate, jint outBitrate, jfloat scaleInput, jint mode, jint vbrMode,
        jint quality, jint vbrQuality, jint abrMeanBitrate, jint lowpassFreq, jint highpassFreq,
        jstring id3tagTitle, jstring id3tagArtist, jstring id3tagAlbum,
        jstring id3tagYear, jstring id3tagComment) {

    glf = initialize(env, inSamplerate, outChannel, outSamplerate, outBitrate, scaleInput, mode,
                     vbrMode,
                     quality, vbrQuality, abrMeanBitrate, lowpassFreq, highpassFreq, id3tagTitle,
                     id3tagArtist, id3tagAlbum,
                     id3tagYear,
                     id3tagComment);
}

jint lameEncode(
        JNIEnv *env, jclass cls, jshortArray buffer_l,
        jshortArray buffer_r, jint samples, jbyteArray mp3buf) {
    return encode(env, glf, buffer_l, buffer_r, samples, mp3buf);
}
JNIEXPORT jint JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_lameEncode(
        JNIEnv *env, jclass cls, jshortArray buffer_l,
        jshortArray buffer_r, jint samples, jbyteArray mp3buf) {
    return encode(env, glf, buffer_l, buffer_r, samples, mp3buf);
}

JNIEXPORT jint JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_encodeBufferInterleaved(
        JNIEnv *env, jclass cls, jshortArray pcm,
        jint samples, jbyteArray mp3buf) {
    return encodeBufferInterleaved(env, glf, pcm, samples, mp3buf);
}

JNIEXPORT jint JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_lameFlush(
        JNIEnv *env, jclass cls, jbyteArray mp3buf) {
    return flush(env, glf, mp3buf);
}

JNIEXPORT void JNICALL Java_ensias_readforme_1volunteer_lame_AndroidLame_lameClose(
        JNIEnv *env, jclass cls) {
    close(glf);
}


lame_global_flags *initializeDefault(JNIEnv *env) {
    lame_global_flags *glf = lame_init();
    lame_init_params(glf);
    return glf;
}
int encode_buffer(short int *intOutput, unsigned char* mp3_buffer, int MP3_SIZE, unsigned int maxSamplesDecoded){
    lame_t lame = lame_init();
    lame_set_in_samplerate(lame, 44100);
    lame_set_VBR(lame, vbr_default);
    lame_init_params(lame);
    if (maxSamplesDecoded == 0)
        return  lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
    else
        return lame_encode_buffer_interleaved(lame, intOutput, maxSamplesDecoded, mp3_buffer, MP3_SIZE);
}
lame_global_flags *initialize(
        JNIEnv *env,
        jint inSamplerate, jint outChannel,
        jint outSamplerate, jint outBitrate, jfloat scaleInput, jint mode, jint vbrMode,
        jint quality, jint vbrQuality, jint abrMeanBitrate, jint lowpassFreq, jint highpassFreq,
        jstring id3tagTitle, jstring id3tagArtist, jstring id3tagAlbum,
        jstring id3tagYear, jstring id3tagComment) {

    lame_global_flags *glf = lame_init();
    lame_set_in_samplerate(glf, inSamplerate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSamplerate);
    lame_set_brate(glf, outBitrate);
    lame_set_quality(glf, quality);
    lame_set_scale(glf, scaleInput);
    lame_set_VBR_q(glf, vbrQuality);
    lame_set_VBR_mean_bitrate_kbps(glf, abrMeanBitrate);
    lame_set_lowpassfreq(glf, lowpassFreq);
    lame_set_highpassfreq(glf, highpassFreq);

    switch (mode) {
        case 0:
            lame_set_mode(glf, STEREO);
            break;
        case 1:
            lame_set_mode(glf, JOINT_STEREO);
            break;
        case 3:
            lame_set_mode(glf, MONO);
            break;
        case 4:
            lame_set_mode(glf, NOT_SET);
            break;
        default:
            lame_set_mode(glf, NOT_SET);
            break;
    }

    switch (vbrMode) {
        case 0:
            lame_set_VBR(glf, vbr_off);
            break;
        case 2:
            lame_set_VBR(glf, vbr_rh);
            break;
        case 3:
            lame_set_VBR(glf, vbr_abr);
            break;
        case 4:
            lame_set_VBR(glf, vbr_mtrh);
            break;
        case 6:
            lame_set_VBR(glf, vbr_default);
            break;
        default:
            lame_set_VBR(glf, vbr_off);
            break;

    }


    const jchar *title = NULL;
    const jchar *artist = NULL;
    const jchar *album = NULL;
    const jchar *year = NULL;
    const jchar *comment = NULL;
    if (id3tagTitle) {
        title = (*env)->GetStringChars(env, id3tagTitle, NULL);
    }
    if (id3tagArtist) {
        artist = (*env)->GetStringChars(env, id3tagArtist, NULL);
    }
    if (id3tagAlbum) {
        album = (*env)->GetStringChars(env, id3tagAlbum, NULL);
    }
    if (id3tagYear) {
        year = (*env)->GetStringChars(env, id3tagYear, NULL);
    }
    if (id3tagComment) {
        comment = (*env)->GetStringChars(env, id3tagComment, NULL);
    }

    if (title || artist || album || year || comment) {
        id3tag_init(glf);

        if (title) {
            id3tag_set_title(glf, (const char *) title);
            (*env)->ReleaseStringChars(env, id3tagTitle, title);
        }
        if (artist) {
            id3tag_set_artist(glf, (const char *) artist);
            (*env)->ReleaseStringChars(env, id3tagArtist, artist);
        }
        if (album) {
            id3tag_set_album(glf, (const char *) album);
            (*env)->ReleaseStringChars(env, id3tagAlbum, album);
        }
        if (year) {
            id3tag_set_year(glf, (const char *) year);
            (*env)->ReleaseStringChars(env, id3tagYear, year);
        }
        if (comment) {
            id3tag_set_comment(glf, (const char *) comment);
            (*env)->ReleaseStringChars(env, id3tagComment, comment);
        }
    }

    lame_init_params(glf);


    return glf;
}

jint encode(
        JNIEnv *env, lame_global_flags *glf,
        jshortArray buffer_l, jshortArray buffer_r,
        jint samples, jbyteArray mp3buf) {
    jshort *j_buffer_l = (*env)->GetShortArrayElements(env, buffer_l, NULL);

    jshort *j_buffer_r = (*env)->GetShortArrayElements(env, buffer_r, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);
    jbyte *j_mp3buf = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer(glf, j_buffer_l, j_buffer_r,
                                    samples, j_mp3buf, mp3buf_size);

    (*env)->ReleaseShortArrayElements(env, buffer_l, j_buffer_l, 0);
    (*env)->ReleaseShortArrayElements(env, buffer_r, j_buffer_r, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buf, 0);

    return result;
}

jint encodeBufferInterleaved(
        JNIEnv *env, lame_global_flags *glf,
        jshortArray pcm, jint samples, jbyteArray mp3buf) {
    jshort *j_pcm = (*env)->GetShortArrayElements(env, pcm, NULL);

    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);
    jbyte *j_mp3buf = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_buffer_interleaved(glf, j_pcm,
                                                samples, j_mp3buf, mp3buf_size);

    (*env)->ReleaseShortArrayElements(env, pcm, j_pcm, 0);
    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buf, 0);

    return result;
}

jint flush(
        JNIEnv *env, lame_global_flags *glf,
        jbyteArray mp3buf) {
    const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);
    jbyte *j_mp3buf = (*env)->GetByteArrayElements(env, mp3buf, NULL);

    int result = lame_encode_flush(glf, j_mp3buf, mp3buf_size);

    (*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buf, 0);

    return result;
}

void close(
        lame_global_flags *glf) {
    lame_close(glf);
    glf = NULL;
}
int convert(char *path,char* path1)
{
    int read, write;
    FILE *pcm = fopen(path, "rb");
    FILE *mp3 = fopen(path1, "wb");

    const int PCM_SIZE = 8192*3;
    const int MP3_SIZE = 8192*3;

    short int pcm_buffer[PCM_SIZE*2];
    unsigned char mp3_buffer[MP3_SIZE];

    lame_t lame = lame_init();
    lame_set_in_samplerate(lame, 44100);
    lame_set_VBR(lame, vbr_default);
    lame_init_params(lame);

    do {
        read = fread(pcm_buffer, 2*sizeof(short int), PCM_SIZE, pcm);

        if (read == 0)
            write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
        else
            write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);
            fwrite(mp3_buffer, write, 1, mp3);
    } while (read != 0);
    __android_log_print(ANDROID_LOG_ERROR, "Convert","Fin convert ! ");

    lame_close(lame);
    fclose(mp3);
    fclose(pcm);
    if( remove( path) != 0 )
        perror( "Error deleting file" );
    else
        puts( "File successfully deleted" );
    return 0;
}

JNIEXPORT jint JNICALL Java_ensias_readforme_1volunteer_EditorService_Convert(JNIEnv *env, jobject jobj, jstring in_source_path, jstring in_target_path) {
    const char *source_path, *target_path;
    source_path = (*env)->GetStringUTFChars(env, in_source_path, NULL);
    target_path = (*env)->GetStringUTFChars(env, in_target_path, NULL);
    return convert(source_path,target_path);
}
