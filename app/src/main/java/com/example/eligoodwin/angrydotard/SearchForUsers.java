package com.example.eligoodwin.angrydotard;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchForUsers extends AppCompatActivity {
    private final String TAG = SearchForUsers.class.getSimpleName();
    //set up views
    EditText userToSearchFor;
    Button submitUserForSearch;
    Button makeModel;
    Button showModels;

    //set up variables
    private String targetUsername;
    private TwitterSession currentUserSession;

    //set up db
    private SQLiteDatabase sqLiteDatabase;
    private MarkovUserDB  markovUserDB;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuItem menuItem = menu.add("Sign out");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB  markovUserDB;
        markovUserDB = new MarkovUserDB(this);
        try {
            //delete all entries in table
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "SQLite info: " + sqLiteDatabase.toString());
            sqLiteDatabase.execSQL("DELETE FROM " + MarkovUserDB.TABLE_NAME_1);
            sqLiteDatabase.execSQL("DELETE FROM " + MarkovUserDB.TABLE_NAME_2);
            sqLiteDatabase.close();
        }catch(SQLException e3){
            e3.printStackTrace();
        }

        //sign out and delete session data
        signOut(SearchForUsers.this);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_users);
        //init database
        markovUserDB = new MarkovUserDB(this);
        try {
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "SQLite info: " + sqLiteDatabase.toString());

        }catch(SQLException e3){
            e3.printStackTrace();
        }
        //get current user name
        currentUserSession = TwitterCore.getInstance()
                .getSessionManager()
                .getActiveSession();

        //set up views
        userToSearchFor = (EditText)findViewById(R.id.usertext);
        submitUserForSearch = (Button)findViewById(R.id.userSearch);
        makeModel = (Button)findViewById(R.id.makeModel);
        showModels = (Button)findViewById(R.id.viewModels);

        submitUserForSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the user name
                targetUsername = userToSearchFor.getText().toString();
                //submit username
                loadTwitterAPI(targetUsername);
            }
        });

        makeModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is not in the data base
                if(targetUsername == null){
                    Toast.makeText(SearchForUsers.this, "Need to search for a user first", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!userIsInDatabase(targetUsername)){
                    Toast.makeText(SearchForUsers.this, "User does not exist in database, adding", Toast.LENGTH_SHORT).show();
                    Intent createMarkovTweets = new Intent(SearchForUsers.this, GetUserDataIntentService.class);
                    createMarkovTweets.putExtra("username", targetUsername);
                    startService(createMarkovTweets);
                }
                else{
                    Toast.makeText(SearchForUsers.this, "User already exists in database", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //view the created models
        showModels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(databaseIsNotEmpty()){
                    Intent viewModels = new Intent(SearchForUsers.this, ViewModels.class);
                    startActivity(viewModels);
                }
                else{
                    Toast.makeText(SearchForUsers.this, "Model Database is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //retrieve user data from twitter
    private void loadTwitterAPI(final String targetedUsername){
        new MyTwitterApiClient(currentUserSession).getCustomService().show(targetedUsername)
                .enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        targetUsername = result.data.screenName;
                        //make list view
                        ListView feedView = (ListView)findViewById(R.id.userFeed);
                        UserTimeline userTimeline = new UserTimeline.Builder()
                                .screenName(targetUsername)
                                .build();
                        TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(SearchForUsers.this)
                                .setTimeline(userTimeline)
                                .build();
                        feedView.setAdapter(adapter);
                    }
                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TAG, "Could not get the current twitter session to load");
                        Toast.makeText(SearchForUsers.this, "User does not Exist", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //test if username is in database
    private boolean userIsInDatabase(String username){
        boolean result = false;
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB  markovUserDB;
        try {
            markovUserDB = new MarkovUserDB(this);
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Cursor userSearch = sqLiteDatabase.query(
                    MarkovUserDB.TABLE_NAME_1,
                    new String[]{MarkovUserDB.COLUMN_NAME_USER_NAME},
                    MarkovUserDB.COLUMN_NAME_USER_NAME + "=?",
                    new String[]{username},
                    null,
                    null,
                    null);

            userSearch.moveToFirst();
            //if count is < 1 user is not in database
            result = userSearch.getCount() > 0;
            userSearch.close();
            sqLiteDatabase.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return result;
    }

    //is the database empty
    private boolean databaseIsNotEmpty(){
        boolean result = false;
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB  markovUserDB;
        try {
            markovUserDB = new MarkovUserDB(this);
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            String count = "SELECT count(*) FROM " + MarkovUserDB.TABLE_NAME_1;
            Cursor userSearch = sqLiteDatabase.rawQuery(count, null);
            userSearch.moveToFirst();
            //more than 1 entry in the db?
            result = userSearch.getInt(0) > 0;
            userSearch.close();
            sqLiteDatabase.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return result;
    }


    void signOut(Context context){
        //delete the session
        TwitterCore.getInstance()
                .getSessionManager()
                .clearActiveSession();
        //return to the logon screen
        Intent backToLogon = new Intent(context, Logon.class);

        //start the login process
        context.startActivity(backToLogon);
    }
}
