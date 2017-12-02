package com.example.eligoodwin.angrydotard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eligoodwin on 11/14/17.
 */

public class TweetViewAdapter extends ArrayAdapter<String> {
    private final List<String> userTweeets;
    private final Context context;
    private final int resource;
    private final String username;

    public TweetViewAdapter(Context context, int resource, List<String> userTweets, String username){
        super(context, resource, userTweets);
        this.context = context;
        this.resource = resource;
        this.userTweeets = userTweets;
        this.username = username;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.generated_tweets, null, true);
        }

        final String generatedTweet = getItem(position);
        //set teh button up
        TextView individualTweet = (TextView)convertView.findViewById(R.id.markovTweet);
        individualTweet.setText(generatedTweet);
        //make this thing into a button
        individualTweet.setOnClickListener(new View.OnClickListener() {
            //set text view to
            @Override
            public void onClick(View v) {
                //get session data about user
                final TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                //prepare the tweet
                final Intent intent = new ComposerActivity.Builder(context)
                        .session(session)
                        .image(null)
                        .text(generatedTweet)
                        .hashtags("#angryDotard")
                        .createIntent();
                context.startActivity(intent);
                Toast.makeText(context, "Tweet created", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

}
