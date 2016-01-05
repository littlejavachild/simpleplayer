package com.example.archenemy.proandroid4media;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

/**
 * Created by archenemy on 6/1/16.
 */
public class PrefUtils {
    private static WeakReference<Context> con;
    public static final String PREF_NAME = "pref";

    public static void init(Context context){
        con = new WeakReference<>(context);
    }
    //----------------------------------------------------------------------------------------------
    public static void putString( String key,String value ){
        SharedPreferences.Editor editor = editor();
        editor.putString( key,value );
        editor.commit();
    }
    //----------------------------------------------------------------------------------------------
    public static String getString( String key, String defVal ){
        SharedPreferences pref = con.get().getSharedPreferences(PREF_NAME, 0);
        return pref.getString(key,defVal);
    }
    //----------------------------------------------------------------------------------------------
    public static void putInt( String key,int value ){
        SharedPreferences.Editor editor = editor();
        editor.putInt(key,value);
        editor.commit();
    }
    //----------------------------------------------------------------------------------------------
    public static int getInt( String key,int defVal){
        SharedPreferences pref = con.get().getSharedPreferences(PREF_NAME, 0);
        return pref.getInt(key, defVal);
    }
    //----------------------------------------------------------------------------------------------
    public static void putBoolean( String key,boolean value ){
        SharedPreferences.Editor editor = editor();
        editor.putBoolean(key, value);
        editor.commit();
    }
    //----------------------------------------------------------------------------------------------
    public static boolean getBoolean( String key,boolean defVal){
        SharedPreferences pref = con.get().getSharedPreferences(PREF_NAME, 0);
        return pref.getBoolean(key, defVal);
    }
    //----------------------------------------------------------------------------------------------
    private static SharedPreferences.Editor editor(){
        SharedPreferences prefs = con.get().getSharedPreferences(PREF_NAME,0);
        return prefs.edit();
    }
    //----------------------------------------------------------------------------------------------
}
