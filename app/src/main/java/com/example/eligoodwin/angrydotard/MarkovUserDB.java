package com.example.eligoodwin.angrydotard;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by eligoodwin on 11/11/17.
 */

class MarkovUserDB extends SQLiteOpenHelper {

    public MarkovUserDB(Context context){
        super(context, DBContract.MarkovContract.DB_Name, null, DBContract.MarkovContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.MarkovContract.SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.MarkovContract.SQL_DROP_MARKOVED_USERS);
        onCreate(db);
    }

}
