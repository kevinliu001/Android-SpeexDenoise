package com.cyril.speexnoisecancel;

class Speex {

	Speex() {
	}

	static {
		System.loadLibrary("speexdsp");
	}

	native void CancelNoiseInit(int frame_size,int sample_rate);
	native void CancelNoisePreprocess(byte[] inbuffer);
	native void CancelNoiseDestroy();
}