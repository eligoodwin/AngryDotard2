package com.example.eligoodwin.angrydotard;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by eligoodwin on 11/7/17.
 */

public class MyTwitterApiClient extends TwitterApiClient {

    public MyTwitterApiClient(TwitterSession session){
        super(session);
    }

    public GetUsersShowAPICustomService getCustomService() {
        return getService(GetUsersShowAPICustomService.class);
    }
}

interface GetUsersShowAPICustomService{
    @GET("1.1/users/show.json")
    Call<User> show(@Query("screen_name") String username);
}

