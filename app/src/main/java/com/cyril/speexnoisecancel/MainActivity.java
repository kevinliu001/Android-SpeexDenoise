package com.cyril.speexnoisecancel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cyril.speexnoisecancel.permission.IPermissionSuccess;
import com.cyril.speexnoisecancel.permission.PermissionManager;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Speex mSpeex;

    TextView hintTV;

    Button startRecordBtn;
    Button startRecordBtn1;
    Button stopRecordBtn;
    Button startPlayBtn;


    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    boolean isRecording = false;


    private AudioRecord audioRecord;

    UPlayer mPlayer;
    //RawAudioName裸音频数据文件
    private static final String RawAudioName = "/sdcard/record_speex.raw";
    //WaveAudioName可播放的音频文件
    private static final String WaveAudioName = "/sdcard/record_speex.wav";


    boolean isDenoiseMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpeex = new Speex();

        hintTV = (TextView) findViewById(R.id.hint_tv);

        startRecordBtn = (Button) findViewById(R.id.start_record);
        startRecordBtn1 = (Button) findViewById(R.id.start_record1);

        stopRecordBtn = (Button) findViewById(R.id.stop_record);
        startPlayBtn = (Button) findViewById(R.id.start_play);

        startRecordBtn.setOnClickListener(this);
        startRecordBtn1.setOnClickListener(this);

        stopRecordBtn.setOnClickListener(this);
        startPlayBtn.setOnClickListener(this);

        mPlayer = new UPlayer(WaveAudioName);

        creatAudioRecord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                PermissionManager.grantPermission(this, new IPermissionSuccess() {
                    @Override
                    public void onSuccess() {
                        if(!isRecording) {
                            isDenoiseMode = false;
                            startRecord();
                        }
                    }
                }
                ,Permission.RECORD_AUDIO
                ,Permission.WRITE_EXTERNAL_STORAGE
                ,Permission.READ_EXTERNAL_STORAGE);
                break;
            case R.id.start_record1:

                PermissionManager.grantPermission(this, new IPermissionSuccess() {
                    @Override
                    public void onSuccess() {
                        if(!isRecording) {
                            isDenoiseMode = true;
                            startRecord();
                        }
                    }
                }
                ,Permission.RECORD_AUDIO
                ,Permission.WRITE_EXTERNAL_STORAGE
                ,Permission.READ_EXTERNAL_STORAGE);

                break;
            case R.id.stop_record:
                stopRecord();
                break;
            case R.id.start_play:
                if (mPlayer.isAudioExist()) {
                    startPlay();
                }
                break;
        }
    }


    private void creatAudioRecord() {

        PermissionManager.grantPermission(this, new IPermissionSuccess() {
                    @Override
                    public void onSuccess() {
                        // 获得缓冲区字节大小
                        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                                channelConfig, audioFormat);
                        mSpeex.CancelNoiseInit(bufferSizeInBytes,sampleRateInHz);
                        // 创建AudioRecord对象
                        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                                channelConfig, audioFormat, bufferSizeInBytes);
                    }
                }
                ,Permission.RECORD_AUDIO
                ,Permission.WRITE_EXTERNAL_STORAGE
                ,Permission.READ_EXTERNAL_STORAGE);

    }


    private void startRecord() {

        if (isDenoiseMode){
            hintTV.setText("正在录制，并降噪，请说话……");
        }
        else{
            hintTV.setText("正在录制，请说话……");
        }


        if(audioRecord == null){
            creatAudioRecord();
        }

        audioRecord.startRecording();
        // 让录制状态为true
        isRecording = true;
        // 开启音频文件写入线程  
        new Thread(new AudioRecordThread()).start();
    }

    private void stopRecord() {
        //停止文件写入
        isRecording = false;
    }

    private void close() {
        mSpeex.CancelNoiseDestroy();
        if (audioRecord != null) {
            System.out.println("stopRecord");
            audioRecord.stop();
            audioRecord.release();//释放资源  
            audioRecord = null;
        }
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();//往文件中写入裸数据
            close();
            //给裸数据加上头文件
            AudioFileManager.WriteWav(RawAudioName, WaveAudioName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hintTV.setText("录制结束，可点开始播放");
                }
            });
        }
    }


    /**
     * 读取音频数据写入文件
     */
    private void writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(RawAudioName);
            if (file.exists()) {
                file.delete();
            }
            // 建立一个可存取字节的文件
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecording == true) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);

            if(isDenoiseMode) {
                mSpeex.CancelNoisePreprocess(audiodata);
            }

            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        isRecording = false;
        close();
        mPlayer.stop();
        super.onDestroy();
    }

    private void startPlay(){
        hintTV.setText("正在播放");
        mPlayer.start();
    }
}
