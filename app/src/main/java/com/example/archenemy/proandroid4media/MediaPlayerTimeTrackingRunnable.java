package com.example.archenemy.proandroid4media;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by archenemy on 4/1/16.
 */
public class MediaPlayerTimeTrackingRunnable implements Runnable {

    private WeakReference<MediaPlayer> mp;
    private TimeTrackingListener listener;
    private Handler handler;
    boolean isCancelled = false;

    public MediaPlayerTimeTrackingRunnable( MediaPlayer player, TimeTrackingListener listener, Handler handler ){
        mp = new WeakReference<>(player);
        this.listener = listener;
        this.handler = handler;
    }

    @Override
    public void run() {
        if(isCancelled) return;
        if( mp.get() == null ) return;
        listener.currentTime( mp.get().getCurrentPosition() );
        handler.postDelayed(this,100);
    }

    public void cancel(){
        isCancelled = true;
    }

    public interface TimeTrackingListener{
        void currentTime( int time );
    }
}
