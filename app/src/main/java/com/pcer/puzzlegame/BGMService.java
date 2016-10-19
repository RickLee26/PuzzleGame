package com.pcer.puzzlegame;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;


/**
 * 播放背景音乐的service，optional
 */
public class BGMService extends Service {
    private MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        try{
            mPlayer = MediaPlayer.create(BGMService.this, R.raw.a_little_story);
            mPlayer.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try{
                    mediaPlayer.start();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                try{
                    mediaPlayer.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });

        boolean isOn = intent.getBooleanExtra("ORDER", true);
        if(isOn){
            mPlayer.start();
        } else {
            mPlayer.pause();
        }

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
