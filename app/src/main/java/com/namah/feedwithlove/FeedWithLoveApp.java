package com.namah.feedwithlove;

import android.app.Application;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;

public class FeedWithLoveApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize AWSMobileClient globally
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("AWS", "AWSMobileClient initialized in Application: " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("AWS", "AWSMobileClient initialization error in Application", e);
            }
        });
    }
}
