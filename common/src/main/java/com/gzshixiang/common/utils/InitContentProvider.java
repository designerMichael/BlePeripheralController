package com.gzshixiang.common.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


public class InitContentProvider extends ContentProvider {

    private static  InitContentProvider mInstance;


    @Override
    public boolean onCreate() {
        mInstance = this;
        return true;
    }

    public static Context getGlobalContext(){
        return mInstance.getContext().getApplicationContext();
    }




    @Override
    public Cursor query( Uri uri,  String[] projection, String selection,  String[] selectionArgs,String sortOrder) {
        return null;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert( Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete( Uri uri,  String selection,  String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update( Uri uri,  ContentValues values,  String selection, String[] selectionArgs) {
        return 0;
    }


}
