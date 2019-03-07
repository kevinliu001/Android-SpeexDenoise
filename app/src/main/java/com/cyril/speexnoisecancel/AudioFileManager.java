package com.cyril.speexnoisecancel;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class AudioFileManager {

    /**
     * 写wav文件
     * @param rawFileName
     * @param wavFileName
     */
    public static void  WriteWav (final String rawFileName, final String wavFileName){

        new Thread(new Runnable() {
            @Override
            public void run() {
                int longSampleRate = 44100;
                int channels = 1;// TODO:声道数一定要通过检测得到！！！！！
                long byteRate = 16 * 44100 * channels / 8;

                FileChannel fcin = null;
                FileChannel fcout = null;
                ByteBuffer buffer = null;

                long totalPCMLength = 0;
                long totalDataLength = totalPCMLength + 36;
                int bufferSize;

                bufferSize = AudioRecord.getMinBufferSize(longSampleRate,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                buffer = ByteBuffer.allocateDirect(bufferSize * 2).order(ByteOrder.nativeOrder());

                long startTime = System.currentTimeMillis();
                try {
                    fcin = new RandomAccessFile(rawFileName, "r").getChannel();
                    fcout = new RandomAccessFile(wavFileName, "rws").getChannel();

                    // 写入头信息
                    totalPCMLength = fcin.size();
                    totalDataLength = totalPCMLength + 36;
                    byte[] header = generateWaveFileHeader(totalPCMLength, totalDataLength, longSampleRate, channels,
                            byteRate);
                    buffer.clear();
                    buffer.put(header);
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        fcout.write(buffer);
                    }

                    buffer.clear();
                    // 写入数据部分
                    while (fcin.read(buffer) != -1) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            fcout.write(buffer);
                        }
                        buffer.clear();
                    }

                    try {
                        fcout.force(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    if (fcout != null) {
                        try {
                            fcout.close();// 关闭写入流
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fcout = null;
                    }

                    if (fcin != null) {
                        try {
                            fcin.close();// 关闭写入流
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fcin = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                long stopTime = System.currentTimeMillis();

                if (buffer != null) {
                    buffer.clear();
                    buffer = null;
                }
            }
        }).start();
    }

    /**
     * 生成wav audio 头部
     * @param totalPCMLength
     * @param totalDataLength
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @return
     */
    public static byte[] generateWaveFileHeader(long totalPCMLength, long totalDataLength, long longSampleRate, int channels,
                                          long byteRate) {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // 从下个地址开始到文件尾的总字节数
        header[4] = (byte) (totalDataLength & 0xff);
        header[5] = (byte) ((totalDataLength >> 8) & 0xff);
        header[6] = (byte) ((totalDataLength >> 16) & 0xff);
        header[7] = (byte) ((totalDataLength >> 24) & 0xff);
        // WAV文件标志（WAVE）
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1 // 2 bytes:格式种类（值为1时，表示数据为线性PCM编码）
        header[20] = 1;
        header[21] = 0;
        // 2 bytes:通道数，单声道为1，双声道为2
        header[22] = (byte) channels;
        header[23] = 0;
        // 采样频率
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        // 波形数据传输速率（每秒平均字节数）
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align //DATA数据块长度，字节。
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        // bits per sample //PCM位宽 TODO:根据配置
        header[34] = 16;
        header[35] = 0;
        // 数据标志符（data）
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // DATA总数据长度字节
        header[40] = (byte) (totalPCMLength & 0xff);
        header[41] = (byte) ((totalPCMLength >> 8) & 0xff);
        header[42] = (byte) ((totalPCMLength >> 16) & 0xff);
        header[43] = (byte) ((totalPCMLength >> 24) & 0xff);
        return header;
    }
}
