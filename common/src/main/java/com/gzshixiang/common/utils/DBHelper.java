package com.gzshixiang.common.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Copyright ©  Shixiang. All Rights Reserved.
 *
 * @author zhongjr
 * @description
 * @date 2020/8/15
 * @email 2751358839@qq.com
 */

public class DBHelper extends SQLiteOpenHelper {
    // 数据库名
    private static final String DATABASE_NAME = "dofservice.db";

    // 表名
    public static final String USER_TABLE_NAME = "user";

    private static final int DATABASE_VERSION = 1;

    //数据库版本号
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + " name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
