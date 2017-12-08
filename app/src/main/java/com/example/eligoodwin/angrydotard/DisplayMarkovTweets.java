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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

public class DisplayMarkovTweets extends AppCompatActivity {
    private final String TAG = DisplayMarkovTweets.class.getSimpleName();
    private ListView listView;
    private UserModel selectedUser;
    private TextView displayUserName;
    //private String username;
    private ImageView userProfilePic;


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuItem menuItem = menu.add("Delete User Model");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //drop the database
        SQLiteDatabase sqLiteDatabase;
        MarkovUserDB  markovUserDB;
        markovUserDB = new MarkovUserDB(this);
        boolean databaseIsNotEmpty = false;

        try {
            //delete the user and test if the database is empty
            sqLiteDatabase = markovUserDB.getReadableDatabase();
            Log.d(TAG, "SQLite info: " + sqLiteDatabase.toString());
            deleteUser(sqLiteDatabase);
            databaseIsNotEmpty = databaseIsNotEmpty(sqLiteDatabase);
            sqLiteDatabase.close();
        }catch(SQLException e3){
            e3.printStackTrace();
        }

        //if empty after delete return to search for user
        if(databaseIsNotEmpty){
            Intent returnToModels = new Intent(DisplayMarkovTweets.this, ViewModels.class);
            returnToModels.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(returnToModels);
        }
        else{
            //return to search for users
            Intent returnToSearchForUsers = new Intent(DisplayMarkovTweets.this, SearchForUsers.class);
            startActivity(returnToSearchForUsers);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_markov_tweets);
        Toast.makeText(this, "Click on a 'tweet' to send it", Toast.LENGTH_LONG).show();
        //Intent getModelData = getIntent();
        displayUserName = (TextView)findViewById(R.id.displayUsername);
        selectedUser = (UserModel)getIntent().getParcelableExtra("userdata");
        userProfilePic = (ImageView)findViewById(R.id.displayUserProfilePic);
        Picasso.with(this).load(selectedUser.getUserProfilePicUrl()).into(userProfilePic);


        displayUserName.setText(selectedUser.getUsername());
        populateTable();
    }

    private void populateTable() {
        listView = (ListView)findViewById(R.id.tweetList);
        TweetViewAdapter adapter = new TweetViewAdapter(
                getApplicationContext(),
                R.layout.generated_tweets,
                selectedUser.getMarkovTweets(),
                selectedUser.getUsername()
        );

        listView.setAdapter(adapter);
    }


    private boolean databaseIsNotEmpty(SQLiteDatabase sqLiteDatabase){
        boolean result = false;
        String count = "SELECT count(*) FROM " + MarkovUserDB.TABLE_NAME_1;
        Cursor userSearch = sqLiteDatabase.rawQuery(count, null);
        userSearch.moveToFirst();
        //more than 1 entry in the db?
        result = userSearch.getInt(0) > 0;
        userSearch.close();
        return result;
    }

    private void deleteUser(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.delete(MarkovUserDB.TABLE_NAME_1, MarkovUserDB.COLUMN_NAME_USER_NAME +"=?",
                new String[]{selectedUser.getUsername()});
    }

}
