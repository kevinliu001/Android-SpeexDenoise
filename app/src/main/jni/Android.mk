LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := libspeexdsp
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
 
LOCAL_SRC_FILES :=  \
./libspeexdsp/buffer.c \
./libspeexdsp/fftwrap.c \
./libspeexdsp/filterbank.c \
./libspeexdsp/jitter.c \
./libspeexdsp/kiss_fft.c \
./libspeexdsp/kiss_fftr.c \
./libspeexdsp/mdf.c \
./libspeexdsp/preprocess.c \
./libspeexdsp/resample.c \
./libspeexdsp/scal.c \
./libspeexdsp/smallft.c \
./speex_process.c

LOCAL_LDLIBS := -llog -lz

include $(BUILD_SHARED_LIBRARY)