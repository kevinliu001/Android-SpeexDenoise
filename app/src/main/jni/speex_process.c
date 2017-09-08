#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <stdio.h>
#include <jni.h>
#include <string.h>
#include "speex/speex_preprocess.h"
#include <stdio.h>

#include <android/log.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "speex", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "speex", __VA_ARGS__))


SpeexPreprocessState *st;

jint Java_com_cyril_speexnoisecancel_Speex_CancelNoiseInit(JNIEnv* env,jobject this,jint frame_size, jint sample_rate)
{

   int i;
   int count=0;
   float f;

   st = speex_preprocess_state_init(frame_size/2, sample_rate);
   i=1;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DENOISE, &i);
   i=0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC, &i);
   i=8000;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC_LEVEL, &i);
   i=0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
   f=.0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
   f=.0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);

   return 1;
}

jint Java_com_cyril_speexnoisecancel_Speex_CancelNoisePreprocess(JNIEnv* env,jobject this,jbyteArray buffer)
{
    char * inbuffer = (*env)->GetByteArrayElements(env,buffer, 0);

    short *in = inbuffer;
   
    int vad = speex_preprocess_run(st, in);

    (*env)->ReleaseByteArrayElements(env,buffer, (jbyte *)inbuffer, 0);

    return vad;
}

jint Java_com_cyril_speexnoisecancel_Speex_CancelNoiseDestroy(JNIEnv* env,jobject this)
{
   if(st != NULL)
       speex_preprocess_state_destroy(st);
   st = NULL;
   return 1;
}

