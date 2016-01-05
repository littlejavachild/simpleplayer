package com.example.archenemy.proandroid4media;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import es.claucookie.miniequalizerlibrary.EqualizerView;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MediaPlayerTimeTrackingRunnable.TimeTrackingListener, RVMediaAdapter.OnMediaItemClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener{

    private static final int MP3_LOADER = 1;
    private RecyclerView list;
    private ViewGroup simplePlayer;
    private EqualizerView equ;
    private TextView title;
    private MediaPlayer mp;
    private SeekBar seek;
    private Handler handler;
    private RVMediaAdapter adapter;
    private MediaPlayerTimeTrackingRunnable tracker;
    private ImageButton play;
    private boolean isSeeking = false;

    String path;
    final String KEY_SHOULD_RESUME = "should_resume";
    final String KEY_SEEK_TO = "seek_to";
    final String KEY_MEDIA_PLAYER_VISIBILITY = "media_player_visibility";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        list = (RecyclerView) findViewById(R.id.list);
        simplePlayer = (ViewGroup) findViewById(R.id.simple_player);
        equ = (EqualizerView) findViewById(R.id.equalizer_view);
        title = (TextView) findViewById( R.id.title );
        seek = (SeekBar) findViewById(R.id.seek);
        play = (ImageButton) findViewById(R.id.play);
        handler = new Handler();

        mp = new MediaPlayer();
        tracker = new MediaPlayerTimeTrackingRunnable(mp,this,handler);

        getLoaderManager().initLoader(MP3_LOADER, null, MainActivity.this);
        seek.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener( this );

        if( savedInstanceState != null ){
            // if the user's player was showing, we must re-show the player
            if( savedInstanceState.getInt(KEY_MEDIA_PLAYER_VISIBILITY) == View.VISIBLE ){
                int seekTo = savedInstanceState.getInt( KEY_SEEK_TO );
                path = savedInstanceState.getString(MediaStore.Audio.AudioColumns.DATA);
                simplePlayer.setVisibility(View.VISIBLE);
                title.setText(savedInstanceState.getString(MediaStore.Audio.AudioColumns.TITLE));
                // if the user's song was playing when they rotated the device
                if( savedInstanceState.getBoolean(KEY_SHOULD_RESUME) ){
                    // we should continue from where they left off
                    play(path, seekTo);
                    // and show the appropriate button
                    play.setImageResource( R.drawable.ic_action_playback_pause );
                    // and start animating the bars
                    equ.animateBars();
                }else{
                    // else the user's song was paused
                    // so we play the song and pause it immediately
                    play(path,seekTo);
                    mp.pause();
                    // and show the appropriate button
                    play.setImageResource( R.drawable.ic_action_playback_play );
                    // and stop animating bars
                    equ.stopBars();
                }
                // in any case, we must set the seekbar to proper values
                seek.setMax( mp.getDuration() );
                seek.setProgress( seekTo );
                // and start tracking the song progress
                handler.postDelayed( tracker,100 );
            }
        }

        play.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               togglePlayback();
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    public void onResume(){
        super.onResume();
        mp = mp == null? new MediaPlayer() : mp;
        tracker =  tracker == null ? new MediaPlayerTimeTrackingRunnable(mp,this,handler) : tracker;
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState){
        // Save the state only if the user was playing a song
        if( simplePlayer.getVisibility() == View.VISIBLE ){
            boolean shouldResumePlayback = mp.isPlaying();
            outState.putString(MediaStore.Audio.AudioColumns.DATA, path);
            outState.putBoolean(KEY_SHOULD_RESUME, shouldResumePlayback);
            outState.putInt(KEY_MEDIA_PLAYER_VISIBILITY, simplePlayer.getVisibility());
            outState.putInt(KEY_SEEK_TO, mp.getCurrentPosition());
            outState.putString( MediaStore.Audio.AudioColumns.TITLE, title.getText().toString() );
        }
    }
    //----------------------------------------------------------------------------------------------
    public void onStop(){
        super.onStop();
        mp.release();
        tracker.cancel();
        handler.removeCallbacks( tracker );
    }
    //----------------------------------------------------------------------------------------------
    private void play( String path,int seekTo ){
        this.path = path;
        try{
            mp.stop();
            mp.reset();

            mp.setDataSource(path);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC); // play sound from main speaker
            mp.prepare();
            mp.seekTo( seekTo );
            mp.start();

            seek.setMax(mp.getDuration());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------
    private void togglePlayback(){
        if( mp.isPlaying() ){
            mp.pause();
            equ.stopBars();
            play.setImageResource( R.drawable.ic_action_playback_play );
        }else{
            mp.start();
            equ.animateBars();
            play.setImageResource( R.drawable.ic_action_playback_pause );
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where =  MediaStore.Audio.AudioColumns.IS_MUSIC + " = ? ";
        String[] whereArgs = new String[]{ "1" };
        if( id == MP3_LOADER ){
            return new CursorLoader(
                    this,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    where,
                    whereArgs,
                    null
            );
        }
        return null;
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if( adapter == null ){
            adapter = new RVMediaAdapter(data,this,this);
            list.setLayoutManager( new LinearLayoutManager(this) );
            list.setAdapter(adapter);
            list.addItemDecoration( new DividerItemDecoration(this,LinearLayoutManager.VERTICAL) );
        }else{
            adapter.swapCursor( data );
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void currentTime(int time) {
        if(!isSeeking) seek.setProgress( time );
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMediaItemClicked(View view,int position) {
        simplePlayer.setVisibility(View.VISIBLE);
        play( adapter.getData(position),0 );
        title.setText(adapter.getTitle(position));
        equ.animateBars();
        play.setImageResource(R.drawable.ic_action_playback_pause);
        // http://stackoverflow.com/questions/17168215/seekbar-and-media-player-in-android
        handler.postDelayed(tracker, 100);
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // nothing
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeeking = true;
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int current = seekBar.getProgress();
        mp.seekTo(current);
        isSeeking = false;
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onCompletion(MediaPlayer mp) {
        equ.stopBars();
        seek.setProgress( 0 );
        play.setImageResource( R.drawable.ic_action_playback_play );
    }
    //----------------------------------------------------------------------------------------------
}
