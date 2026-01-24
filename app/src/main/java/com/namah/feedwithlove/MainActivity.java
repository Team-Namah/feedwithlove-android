package com.namah.feedwithlove;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d("AWS", "AWSMobileClient initialized: " + result.getUserState());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("AWS", "AWS init error", e);
                    }
                }
        );

        // ✅ REQUIRED for S3 TransferUtility
        TransferNetworkLossHandler.getInstance(getApplicationContext());
    }
}
