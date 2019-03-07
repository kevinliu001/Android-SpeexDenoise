#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "speex/speex_preprocess.h"
#include <stdio.h>

#define NN 1024


jint Java_com_newband_myspeexdemo_Speex_CancelNoise(JNIEnv* env,jobject this,jstring input_file,jstring output_file)
{
    const char* inputfile_path = (*env)->GetStringUTFChars(env,input_file, 0);
    const char* outputfile_path = (*env)->GetStringUTFChars(env,output_file, 0);

    FILE *inFile = fopen(inputfile_path, "rb");
    FILE *outFile = fopen(outputfile_path, "wb");


   short in[NN];
   int i;
   SpeexPreprocessState *st;
   int count=0;
   float f;

   st = speex_preprocess_state_init(NN, 44100);
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
   while (1)
   {
      int vad;
      fread(in, sizeof(short), NN, inFile);
      if (feof(stdin))
         break;
      vad = speex_preprocess_run(st, in);
      /*fprintf (stderr, "%d\n", vad);*/
      fwrite(in, sizeof(short), NN, outFile);
      count++;
   }

   fclose(inFile);
   fclose(outFile);

   speex_preprocess_state_destroy(st);

   (*env)->ReleaseStringUTFChars(env,input_file, inputfile_path);
       (*env)->ReleaseStringUTFChars(env,output_file, outputfile_path);

   return 0;
}
