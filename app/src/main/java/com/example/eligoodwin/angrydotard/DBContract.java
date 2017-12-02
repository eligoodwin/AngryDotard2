package com.example.eligoodwin.angrydotard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by eligoodwin on 11/11/17.
 */

/*Build a mysql database to store markov'ed user tweets so that they can be displayed
*
* */

public class DBContract {
    private DBContract(){};

    public final class MarkovContract implements BaseColumns{
        public static final String DB_Name = "markoved_users";
        public static final String TABLE_NAME = "available_users";
        public static final String COLUMN_NAME_USER_NAME = "user_name";
        public static final String COLUMN_NAME_TWEET = "tweet";
        public static final String COLUMN_NAME_PROFILE_URL = "profile_pic";

        public static final int DB_VERSION = 13;
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " +
                MarkovContract.TABLE_NAME + "(" + MarkovContract._ID + " INTEGER PRIMARY KEY NOT NULL," +
                MarkovContract.COLUMN_NAME_USER_NAME + " VARCHAR(255)," +
                MarkovContract.COLUMN_NAME_PROFILE_URL + " VARCHAR(255)," +
                MarkovContract.COLUMN_NAME_TWEET + " VARCHAR(255));";
        public static final String SQL_DROP_MARKOVED_USERS = "DROP TABLE IF EXISTS " + MarkovContract.TABLE_NAME;

    }
}
