package com.cyril.speexnoisecancel;

/**
 * Created by xuan on 2017/8/17.
 */
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class UPlayer{

    private final String TAG = UPlayer.class.getName();
    private String path;

    private MediaPlayer mPlayer;
    public UPlayer(String path){
        this.path = path;
    }


    public boolean isAudioExist(){
        if (!TextUtils.isEmpty(path)){
            File file = new File(path);
            return file.exists();
        }
        return false;
    }

    public boolean start() {
        try {
            if(mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("UPlayer","onCompletion");
                        stop();
                    }
                });
            }
            //设置要播放的文件
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            //播放
            mPlayer.start();
        }catch(Exception e){
            Log.e(TAG, "prepare() failed");
        }

        return false;
    }


    public boolean stop() {
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        return false;
    }

}