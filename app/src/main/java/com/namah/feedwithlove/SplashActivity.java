package com.namah.feedwithlove;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;

//import com.google.firebase.messaging.FirebaseMessaging;
import com.ncorti.slidetoact.SlideToActView;

import org.json.JSONObject;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "FCM_REGISTER";

    private SlideToActView slider;

    // 🔴 REPLACE with your real API Gateway URL
    private static final String REGISTER_URL =
            "https://0wpttnfsql.execute-api.ap-south-1.amazonaws.com/prod/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_splash);

        slider = findViewById(R.id.welcomeSlider);
        slider.setText("Swipe to continue");

        slider.setOnSlideCompleteListener(view -> {

//            FirebaseMessaging.getInstance().getToken()
//                    .addOnSuccessListener(token -> {
//                        Log.d(TAG, "FCM TOKEN: " + token);
//                        registerDevice(token);
//                    })
//                    .addOnFailureListener(e ->
//                            Log.e(TAG, "Token fetch failed", e)
//                    );

            startActivity(new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            ));
            finish();
        });
    }

//    private void registerDevice(String token) {
//
//        OkHttpClient client = new OkHttpClient();
//
//        JSONObject json = new JSONObject();
//        try {
//            json.put("token", token);
//        } catch (Exception e) {
//            Log.e(TAG, "JSON error", e);
//        }
//
//        RequestBody body = RequestBody.create(
//                MediaType.parse("application/json"),
//                json.toString()
//        );
//
//        Request request = new Request.Builder()
//                .url(REGISTER_URL)
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "Registration FAILED", e);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                Log.d(TAG, "Registration SUCCESS, code=" + response.code());
//            }
//        });
//    }
}