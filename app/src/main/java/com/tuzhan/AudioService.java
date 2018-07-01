package com.tuzhan;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AudioService extends Service {

    public static MediaPlayer mediaPlayer;

    public int audioPosition;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = MediaPlayer.create(this, R.raw.mean_theme);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void changeAudio(int id, Context context){

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(context, id);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            audioPosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(audioPosition);
            mediaPlayer.start();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            pause();
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
        }
    }
}
