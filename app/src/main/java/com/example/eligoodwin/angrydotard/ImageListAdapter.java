package com.example.eligoodwin.angrydotard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by eligoodwin on 11/12/17.
 */

public class ImageListAdapter extends ArrayAdapter<UserModel> {

    List<UserModel> usersInDB;
    Context context;
    int resource;

    public ImageListAdapter(Context context, int resource, List<UserModel> theUsers){
        super(context, resource, theUsers);
        this.usersInDB = theUsers;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView , ViewGroup parent){
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.user_model_display, null, true);
        }

        final UserModel user = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.userProfilePic);
        //Button userSelection = (Button)convertView.findViewById(R.id.viewtweets);

        Picasso.with(context).load(user.getUserProfilePicUrl()).into(imageView);
        TextView userNameDisplay = (TextView) convertView.findViewById(R.id.finalUsername);
        userNameDisplay.setText(user.getUsername());

        //make individual click listeners to the button
        userNameDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showMarkovTweets = new Intent(context, DisplayMarkovTweets.class);
                //cast to parcel
                showMarkovTweets.putExtra("userdata", (Parcelable)user);
                context.startActivity(showMarkovTweets);
            }
        });

        return convertView;
    }
}
