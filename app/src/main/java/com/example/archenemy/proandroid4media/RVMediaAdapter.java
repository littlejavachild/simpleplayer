package com.example.archenemy.proandroid4media;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by archenemy on 4/1/16.
 */
public class RVMediaAdapter extends RecyclerView.Adapter<RVMediaAdapter.ViewHolder>{
    private Cursor cursor;
    private Context context;
    private OnMediaItemClickListener listener;
    private LruCache<String,Bitmap> cache;
    //----------------------------------------------------------------------------------------------
    public RVMediaAdapter( Cursor cursor, Context context, OnMediaItemClickListener listener){
        this.cursor = cursor;
        this.context = context;
        this.listener = listener;
        cache = new LruCache<>( 4 * 1024 * 1024 ); // 1Mb cache
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View view = inf.inflate(R.layout.item_song,parent,false);
        return new ViewHolder(view);
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // todo: add listener
        holder.title.setText( getTitle(position) );
        holder.artist.setText( getArtist(position) );

        String path = getAlbumArt(position);
        // if the song has no associated album art,
        // the path will be null
        if( path == null ) return;

        Bitmap bmp = cache.get(path);
        if( bmp != null ){
            holder.albumArt.setImageBitmap( bmp );
        }else{
            bmp = BitmapFactory.decodeFile( path );
            cache.put(path,bmp);
            holder.albumArt.setImageBitmap( bmp );
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
    //----------------------------------------------------------------------------------------------
    public void swapCursor( Cursor cursor ){
        this.cursor = cursor;
        notifyDataSetChanged();
    }
    //----------------------------------------------------------------------------------------------
    public String getTitle( int position ){
        cursor.moveToPosition( position );
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE);
        String title = cursor.getString( index );
        return title;
    }
    //----------------------------------------------------------------------------------------------
    public String getArtist( int position ){
        cursor.moveToPosition( position );
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST);
        String artist = cursor.getString(index);
        return artist;
    }
    //----------------------------------------------------------------------------------------------
    public String getAlbumArt( int position ){
        cursor.moveToPosition( position );
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ID);
        int albumId = cursor.getInt(index);
        Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);

        Cursor albumArtCursor = context.getContentResolver().query(
                albumUri,
                null,
                null,
                null,
                null
        );

        String albumArtUri = null;
        if( albumArtCursor.moveToFirst() ){
            albumArtUri = albumArtCursor.getString( albumArtCursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART) );
        }
        albumArtCursor.close();
        return albumArtUri;
    }
    //----------------------------------------------------------------------------------------------
    public String getData( int position ){
        cursor.moveToPosition(position);
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA);
        return cursor.getString(index);
    }
    //----------------------------------------------------------------------------------------------
    public int getDuration( int position ){
        cursor.moveToPosition( position );
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION);
        return cursor.getInt( index );
    }
    //----------------------------------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        public ImageView albumArt;
        public TextView title;
        public TextView artist;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById( R.id.title );
            albumArt = (ImageView) itemView.findViewById( R.id.album_art );
            artist = (TextView) itemView.findViewById( R.id.artist );
            itemView.setOnClickListener( this );
        }

        @Override
        public void onClick(View v) {
            listener.onMediaItemClicked( v,this.getLayoutPosition() );
        }
    }
    //----------------------------------------------------------------------------------------------
    public interface OnMediaItemClickListener{
        void onMediaItemClicked(View view, int position );
    }
    //----------------------------------------------------------------------------------------------
}
