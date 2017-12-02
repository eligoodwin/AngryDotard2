package com.example.eligoodwin.angrydotard;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class Logon extends AppCompatActivity {
    private TwitterLoginButton loginButton; //login button provided by twitter
    private final String TAG = "Logon Screen";
    private TextView userDisplay;
    private TwitterSession session;
    private String username;

    //use to show login screen for 5 seconds if user has already been authroized
    private Handler pauseIntent = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_logon);
        boolean result = alreadyAuthorized();
        setUpViews(result);
    }

    private void setUpViews(boolean alreadySignedIn){
        loginButton = (TwitterLoginButton) findViewById(R.id.loginButton);
        userDisplay = (TextView) findViewById(R.id.finalUsername);

        if(alreadyAuthorized()){
            //set user name
            String displayText = "Welcome back, " + session.getUserName();
            userDisplay.setText(displayText);
            //start next activity
            pauseIntent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent displayFollowers = new Intent(Logon.this, SearchForUsers.class);
                    //prevent the user from coming back to this splash page
                    startActivity(displayFollowers);
                    finish();
                }
            }, 1500);
        }

        //get authorization
        else {
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    username = result.data.getUserName();
                    //sign on and proceed to the next screen
                    Toast.makeText(Logon.this, "logged in as " + username, Toast.LENGTH_SHORT).show();
                    Intent displayFollowers = new Intent(Logon.this, SearchForUsers.class);
                    //displayFollowers.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(displayFollowers);
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {
                    //make a toast
                    Toast.makeText(Logon.this, "Unable to start logon: connect to a network or install Twitter on your phone.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //what happens when we get back from the login page?
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "Request code: " + requestCode + " resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    //user has already been logged in and session has persisted
    private boolean alreadyAuthorized(){
        session = TwitterCore.getInstance()
                .getSessionManager()
                .getActiveSession();
        //do we have an active session

        return (session != null);
    }

}
