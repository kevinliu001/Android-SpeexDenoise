# Speex DeNoise
这是一个利用Speex库在Android上消除录音噪音的Demo



Demo比较简单，用AudioRecord录制声音，边录制边消除噪音，最后保存成wav文件，speex已经编译成lib库，除噪的代码在jni层c语言编写，代码都可编辑。亲测，效果还可以。
