package com.example.eligoodwin.angrydotard;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by eligoodwin on 11/11/17.
 */

class MarkovUserDB extends SQLiteOpenHelper {

    public static final String DB_Name = "markoved_users";
    public static final String TABLE_NAME_1 = "markoved_user";
    public static final String TABLE_NAME_2 = "user_tweet";
    public static final String MARKOVED_USER_ID = "markoved_user_ID";
    public static final String TWEET_ID = "tweet_ID";
    public static final String COLUMN_NAME_USER_NAME = "user_name";
    public static final String COLUMN_NAME_TWEET = "tweet";
    public static final String COLUMN_NAME_PROFILE_URL = "profile_pic";
    public static final int DB_VERSION = 25;

    public static final String SQL_CREATE_TABLE_MARKOVED_USERS = "CREATE TABLE " + TABLE_NAME_1
            + " (" + MARKOVED_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME_USER_NAME + " VARCHAR(255)," +
            COLUMN_NAME_PROFILE_URL + " VARCHAR(255));";
    public static final String SQL_DROP_MARKOVED_USERS = "DROP TABLE IF EXISTS " + TABLE_NAME_1;


    public static final String SQL_CREATE_TABLE_TWEETS = "Create TABLE " + TABLE_NAME_2
            + " (" + TWEET_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MARKOVED_USER_ID + " INTEGER KEY NOT NULL, " +
            COLUMN_NAME_TWEET + " VARCHAR(255), " +
            "FOREIGN KEY (" + MARKOVED_USER_ID+ ") REFERENCES "+ TABLE_NAME_1+
            "(" + MARKOVED_USER_ID + ") ON DELETE CASCADE);";
    public static final String SQL_DROP_TWEETS = "DROP TABLE IF EXISTS " + TABLE_NAME_2;


    public MarkovUserDB(Context context){
        super(context, DBContract.MarkovContract.DB_Name, null, DBContract.MarkovContract.DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=1");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_MARKOVED_USERS);
        db.execSQL(SQL_CREATE_TABLE_TWEETS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TWEETS);
        db.execSQL(SQL_DROP_MARKOVED_USERS);
        db.execSQL(SQL_CREATE_TABLE_TWEETS);
        onCreate(db);
    }

}
