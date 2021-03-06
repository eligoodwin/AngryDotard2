package com.example.eligoodwin.angrydotard;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
 * Used to create requests for generated tweets
 */

public class GetUserDataIntentService extends IntentService {
    private static final String TAG = GetUserDataIntentService.class.getSimpleName();
    private final String magaURL = "https://magabot-183518.appspot.com/tweets/";
    private String username;

    public GetUserDataIntentService(){
        super("GetUserDataIntentService");
        setIntentRedelivery(true);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //get the user name string
        username = intent.getStringExtra("username");

        //make the request and add the data to the database
        getGeneratedTweets();

    }

    private void getGeneratedTweets() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS) //prevent timeouts due the nature of the request
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        //make the request
        final Request request = new Request.Builder()
                .header("content-type", "application/json; charset=utf-8")
                .url(magaURL + username)
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
                    Log.d(TAG, "Response body: " + theResponse + " response code: " + responseCode);
                    JSONObject theTweets = new JSONObject(theResponse);
                    Gson convertResponseToObject = new Gson();
                    //create new model entry
                    UserModel retrievedModel = convertResponseToObject.fromJson(theResponse, UserModel.class);
                    //and previously retrieved data
                    Log.d(TAG, "User model: " + retrievedModel.toString());
                    //inert into database
                    addEntries(retrievedModel);
                    sendNotification();

                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    //adds users to the database--need to refactor after modfying the database
    private void addEntries(UserModel userToAdd){
        //add the username
        ContentValues vals = new ContentValues();
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB  markovUserDB;

        markovUserDB = new MarkovUserDB(this);
        try {
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "Attempting to add new entry: " + sqLiteDatabase.toString());
            //add user to database
            vals.put(MarkovUserDB.COLUMN_NAME_USER_NAME, userToAdd.getUsername());
            vals.put(MarkovUserDB.COLUMN_NAME_PROFILE_URL, userToAdd.getUserProfilePicUrl());
            sqLiteDatabase.insert(MarkovUserDB.TABLE_NAME_1, null, vals);
            vals.clear();

            //add their tweets to the database
            List<String> tweetsToAdd = userToAdd.getMarkovTweets();
            Cursor userCursor = sqLiteDatabase.query(
                    MarkovUserDB.TABLE_NAME_1,
                    new String[]{MarkovUserDB.MARKOVED_USER_ID},
                    MarkovUserDB.COLUMN_NAME_USER_NAME + "=?",
                    new String[]{userToAdd.getUsername()},
                    null,
                    null,
                    null,
                    null
            );

            userCursor.moveToFirst();
            String userModelID = userCursor.getString(userCursor.getColumnIndex(MarkovUserDB.MARKOVED_USER_ID));
            userCursor.close();

            for(int i = 0; i < tweetsToAdd.size(); ++i){
                vals.put(MarkovUserDB.COLUMN_NAME_TWEET, tweetsToAdd.get(i));
                vals.put(MarkovUserDB.MARKOVED_USER_ID, userModelID);
                sqLiteDatabase.insert(MarkovUserDB.TABLE_NAME_2, null, vals);
            }
            sqLiteDatabase.close();

        }catch(SQLException e3){
            Log.d(TAG, "Entry to add failed");
            e3.printStackTrace();
        }
    }

    private void sendNotification() {
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.angry_idiot)
                .setContentTitle("User model was added")
                .setContentText(username + " is now in the database!");
        Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
