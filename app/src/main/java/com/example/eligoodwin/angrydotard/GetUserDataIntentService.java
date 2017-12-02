package com.example.eligoodwin.angrydotard;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by eligoodwin on 12/2/17.
 * Used to crate
 */

public class GetUserDataIntentService extends IntentService {
    private static final String TAG = GetUserDataIntentService.class.getSimpleName();
    private final String magaURL = "https://magabot-183518.appspot.com/tweets/";

    private SQLiteDatabase sqLiteDatabase;
    private MarkovUserDB  markovUserDB;

    public GetUserDataIntentService(){
        super("GetUserDataIntentService");
        setIntentRedelivery(true);


    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //set up the database
        markovUserDB = new MarkovUserDB(this);
        try {
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "SQLite info: " + sqLiteDatabase.toString());

        }catch(SQLException e3){
            e3.printStackTrace();
        }
        //get the user name string
        String username = intent.getStringExtra("username");

        //make the request and add the data to the database
        getGeneratedTweets(username);

    }

    private void getGeneratedTweets(String targetUser) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //prevent timeouts due the nature of the request
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        //make the request
        final Request request = new Request.Builder()
                .header("content-type", "application/json; charset=utf-8")
                .url(magaURL + targetUser)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Something is not right...");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String theResponse = response.body().string();
                    int responseCode = response.code();
                    //let user know that model was created for the user
                    Log.d(TAG, "Response body: " + theResponse + " response code: " + responseCode);
                    JSONObject theTweets = new JSONObject(theResponse);
                    Gson convertResponseToObject = new Gson();
                    //create new model entry
                    UserModel retrievedModel = convertResponseToObject.fromJson(theResponse, UserModel.class);
                    //and previously retrieved data
                    Log.d(TAG, "User model: " + retrievedModel.toString());
                    //inert into database
                    addEntries(retrievedModel);
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

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
}
