package com.example.eligoodwin.angrydotard;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.core.models.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by eligoodwin on 11/10/17.
 */

public class UserModel implements Serializable, Parcelable{
    //use for making parcels
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public UserModel createFromParcel(Parcel in){
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    //members
    @SerializedName("username")
    private final String username;

    @SerializedName("theTweets")
    private final List<String> markovTweets;

    @SerializedName("userProfilePic")
    private String userProfilePicUrl;

    public String getUsername() {
        return username;
    }

    public UserModel(String username, String userProfilePicUrl, List<String>tweets){
        this.userProfilePicUrl = userProfilePicUrl;
        this.username = username;
        this.markovTweets = tweets;
    }

    public UserModel(Parcel in){
        this.username = in.readString();
        this.userProfilePicUrl = in.readString();
        this.markovTweets = in.createStringArrayList();
    }

    public void setUserProiflePicUrl(String userProfilePicUrl){
        this.userProfilePicUrl = userProfilePicUrl;
    }
    public String getUserProfilePicUrl(){
        return this.userProfilePicUrl;
    }

    public List<String> getMarkovTweets() {
        return markovTweets;
    }

    @Override
    public String toString(){
        return this.username + " " + this.userProfilePicUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.userProfilePicUrl);
        dest.writeStringList(this.markovTweets);

    }
}
