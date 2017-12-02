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
    private String targetUserprofilePic;
    private final String magaURL = "https://magabot-183518.appspot.com/tweets/";

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
        //delete all entries in table
        sqLiteDatabase.execSQL("DELETE FROM " + DBContract.MarkovContract.TABLE_NAME);
        sqLiteDatabase.close();
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
//                    //make an http request and then print the result to the dialog will add results to database
//                    OkHttpClient client = new OkHttpClient.Builder()
//                            .connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS) //prevent timeouts due the nature of the request
//                            .readTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
//                            .build();
//
//                    //make the request
//                    final Request request = new Request.Builder()
//                            .header("content-type", "application/json; charset=utf-8")
//                            .url(magaURL + targetuser)
//                            .build();
//                    client.newCall(request).enqueue(new okhttp3.Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            Log.d(TAG, "Something is not right...");
//                            e.printStackTrace();
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            try{
//                                String theResponse = response.body().string();
//                                int responseCode = response.code();
//                                //let user know that model was created for the user
//                                Log.d(TAG, "Response body: " + theResponse + " response code: " + responseCode);
//                                JSONObject theTweets = new JSONObject(theResponse);
//                                Gson convertResponseToObject = new Gson();
//                                //create new model entry
//                                UserModel retrievedModel = convertResponseToObject.fromJson(theResponse, UserModel.class);
//                                //and previously retrieved data
//                                Log.d(TAG, "User model: " + retrievedModel.toString());
//                                //inert into database
//                                addEntries(retrievedModel);
//                            }
//                            catch(JSONException e2){
//                                e2.printStackTrace();
//                            }
//                        }
//                    });
                    //send off user name to service
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
                        targetUserprofilePic =result.data.profileImageUrlHttps;
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

    //add entries to the database
    public void addEntries(UserModel userToAdd){
        //add the username
        ContentValues vals = new ContentValues();

        //add the list values
        List<String> tweetsToAdd = userToAdd.getMarkovTweets();
        for(int i = 0; i < tweetsToAdd.size(); ++i){
            vals.put(DBContract.MarkovContract.COLUMN_NAME_USER_NAME, userToAdd.getUsername());
            //add the profile pic url
            vals.put(DBContract.MarkovContract.COLUMN_NAME_PROFILE_URL, userToAdd.getUserProfilePicUrl());
            vals.put(DBContract.MarkovContract.COLUMN_NAME_TWEET, tweetsToAdd.get(i));
            sqLiteDatabase.insert(DBContract.MarkovContract.TABLE_NAME, null, vals);
        }
    }

    //test if username is in database
    private boolean userIsInDatabase(String username){
        boolean result = false;
        if(!databaseIsNotEmpty()){
            return false;
        }
        Cursor userSearch = sqLiteDatabase.query(
                DBContract.MarkovContract.TABLE_NAME,
                new String[]{DBContract.MarkovContract.COLUMN_NAME_USER_NAME},
                DBContract.MarkovContract.COLUMN_NAME_USER_NAME + "=?",
                new String[]{username},
                null,
                null,
                null);

        userSearch.moveToFirst();
        //if count is < 1 user is not in database
        result = userSearch.getCount() > 0;
        userSearch.close();

        return result;
    }

    //is the database empty
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
