package com.ltz.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Explorer on 2016/6/8.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static DBHelper dbHelper = null;

    private static final String DB_NAME = "thread_info";
    private static final int DB_VERSION = 1;
    private static final String DB_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start integer,end integer,finished integer)";
    private static final String DB_DROP = "drop table if exists thread_info";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 使用单例模式保证多线程操作数据库的安全
     */
    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }

        return dbHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DB_DROP);
        db.execSQL(DB_CREATE);
    }
}
