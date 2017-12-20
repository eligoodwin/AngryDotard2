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
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB markovUserDB;
        try {
            markovUserDB = new MarkovUserDB(this);
            sqLiteDatabase = markovUserDB.getReadableDatabase();

            //get all user names into the cursor
            Cursor userCursor = sqLiteDatabase.query(true,
                    MarkovUserDB.TABLE_NAME_1,
                    new String[]{MarkovUserDB.MARKOVED_USER_ID,
                            MarkovUserDB.COLUMN_NAME_USER_NAME,
                            MarkovUserDB.COLUMN_NAME_PROFILE_URL
                    },
                    null,
                    null,
                    MarkovUserDB.COLUMN_NAME_USER_NAME,
                    null,
                    null,
                    null);
            userCursor.moveToFirst();

            //build the new model fromm the database
            List<String> userTweets = new ArrayList<>();
            while (!userCursor.isAfterLast()) {
                //get model id
                String userModelID = userCursor.getString(userCursor.getColumnIndex(MarkovUserDB.MARKOVED_USER_ID));
                //get the current user name
                String username = userCursor.getString(userCursor.getColumnIndex(MarkovUserDB.COLUMN_NAME_USER_NAME));
                //get the profile pic
                String userProfilePicUrl = userCursor.getString(userCursor.getColumnIndex(MarkovUserDB.COLUMN_NAME_PROFILE_URL));
                //make a new model, retrieve the tweets first
                userTweets = getTweets(userModelID);
                UserModel tempModel = new UserModel(username, userProfilePicUrl, userTweets);
                //add to the model
                userModelList.add(tempModel);
                userCursor.moveToNext();
            }
            userCursor.close();
        }
        catch(SQLException e){
            Log.d(TAG, "couldn't make model");
            e.printStackTrace();
        }

        return userModelList;
    }



    private List<String> getTweets(String userModelId){
        List<String> userTweets = new ArrayList<>();
        //get all the tweeets by a user ID
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB markovUserDB;

        markovUserDB = new MarkovUserDB(this);
        try {
            sqLiteDatabase = markovUserDB.getReadableDatabase();

            Cursor tweetCursor = sqLiteDatabase.query(
                    MarkovUserDB.TABLE_NAME_2,
                    new String[]{MarkovUserDB.COLUMN_NAME_TWEET},
                    MarkovUserDB.MARKOVED_USER_ID + "=?",
                    new String[]{userModelId},
                    null,
                    null,
                    null,
                    null);
            tweetCursor.moveToFirst();

            while(!tweetCursor.isAfterLast()){
                //add the tweet for the target user
                userTweets.add(tweetCursor.getString(tweetCursor.getColumnIndex(MarkovUserDB.COLUMN_NAME_TWEET)));
                tweetCursor.moveToNext();
            }

            tweetCursor.close();
            sqLiteDatabase.close();
        } catch(SQLException e){
            Log.d(TAG, "failure to get tweets");
            e.printStackTrace();
        }

        return userTweets;
    }

    private boolean databaseIsNotEmpty(){
        boolean result = false;
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB markovUserDB;
        markovUserDB = new MarkovUserDB(this);
        try {
            String count = "SELECT count(*) FROM " + MarkovUserDB.TABLE_NAME_1;
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Cursor userSearch = sqLiteDatabase.rawQuery(count, null);
            userSearch.moveToFirst();

            //more than 1 entry in the db?
            result = userSearch.getInt(0) > 0;
            userSearch.close();
            sqLiteDatabase.close();
        }catch (SQLException e){
            Log.d(TAG, "Could not query if database was empty");
            e.printStackTrace();
        }
        return result;
    }

}

