package com.example.eligoodwin.angrydotard;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ViewModels extends AppCompatActivity {
    //sql stuff
    private SQLiteDatabase sqLiteDatabase;
    private MarkovUserDB markovUserDB;
    private final String TAG = ViewModels.class.getSimpleName();
    ListView listView;
    List<UserModel> userModelList;


    @Override
    protected void onResume() {
        super.onResume();
        //test if db is empty
        if(databaseIsNotEmpty()){
            userModelList = getModels();
            populateTable();
        }
        //go back to searching for users
        else{
            Intent searchForUser= new Intent(this, SearchForUsers.class);
            startActivity(searchForUser);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_models);
        Toast.makeText(this, "Select a modeled user to display their generated tweets by clicking their username", Toast.LENGTH_LONG).show();

        markovUserDB = new MarkovUserDB(this);
        try {
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "SQLite info: " + sqLiteDatabase.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //show the results of the table
        populateTable();
    }


    private void populateTable() {
        userModelList = getModels();
        listView = (ListView)findViewById(R.id.dbList);
        ImageListAdapter adapter = new ImageListAdapter(
                getApplicationContext(), R.layout.user_model_display,
                userModelList
        );

        listView.setAdapter(adapter);
    }


    //generate user models from the database
    private List<UserModel> getModels(){
        List<UserModel> userModelList = new ArrayList<>();

        //make query of all entries of db for the unique user names
        Cursor cursor = sqLiteDatabase.query(true,
                DBContract.MarkovContract.TABLE_NAME,
                new String[]{DBContract.MarkovContract._ID,
                        DBContract.MarkovContract.COLUMN_NAME_USER_NAME},
                null,
                null,
                DBContract.MarkovContract.COLUMN_NAME_USER_NAME,
                null,
                null,
                null);

        //move cursor to beginning
        cursor.moveToFirst();
        //build the new model fromm the database
        while(!cursor.isAfterLast()){
            //get the current user name
            String username = cursor.getString(cursor.getColumnIndex(DBContract.MarkovContract.COLUMN_NAME_USER_NAME)); //<--make a method
            //get the profile pic
            String userProfilePicUrl = getUserPicUrl(username);
            //make a new model, retrieve the tweets first
            UserModel tempModel = new UserModel(username, getTweets(username));
            //set the pic turl for the new object
            tempModel.setUserProiflePicUrl(userProfilePicUrl);
            //add to the model
            userModelList.add(tempModel);
            cursor.moveToNext();
        }
        cursor.close();
        return userModelList;
    }

    private String getUserPicUrl(String username){
        String userPicUrl;
        Cursor cursor = sqLiteDatabase.query(true,
                DBContract.MarkovContract.TABLE_NAME,
                new String[]{DBContract.MarkovContract._ID,
                DBContract.MarkovContract.COLUMN_NAME_PROFILE_URL},
                DBContract.MarkovContract.COLUMN_NAME_USER_NAME + " =?",
                new String[]{username},
                DBContract.MarkovContract.COLUMN_NAME_PROFILE_URL,
                null,
                null,
                null);

        cursor.moveToFirst();
        userPicUrl = cursor.getString(cursor.getColumnIndex(DBContract.MarkovContract.COLUMN_NAME_PROFILE_URL));
        cursor.close();
        return userPicUrl;
    }


    private List<String> getTweets(String username){
        List<String> userTweets = new ArrayList<>();
        //get all the tweeets by a user
        Cursor cursor = sqLiteDatabase.query(
                DBContract.MarkovContract.TABLE_NAME,
                new String[]{DBContract.MarkovContract._ID,
                        DBContract.MarkovContract.COLUMN_NAME_TWEET},
                DBContract.MarkovContract.COLUMN_NAME_USER_NAME + " =?",
                new String[]{username},
                null,
                null,
                null);
        cursor.moveToFirst();
        //populate list with tweets
        while(!cursor.isAfterLast()) {
            userTweets.add(cursor.getString(cursor.getColumnIndex(DBContract.MarkovContract.COLUMN_NAME_TWEET)));
            cursor.moveToNext();
        }
        cursor.close();
        return userTweets;
    }

    private boolean databaseIsNotEmpty(){
        boolean result = false;
        String count = "SELECT count(*) FROM " + DBContract.MarkovContract.TABLE_NAME;
        Cursor userSearch = sqLiteDatabase.rawQuery(count, null);
        userSearch.moveToFirst();

        //more than 1 entry in the db?
        result = userSearch.getInt(0) > 0;
        userSearch.close();
        return result;
    }

}

