package com.namah.feedwithlove;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    TextView tvToSignup, tvForgotPassword;
    EditText etEmail;
    TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔹 Initialize AWS (REQUIRED)
        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {}

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        // 🔐 If already logged in → ask biometric (NOT auto login)
        if (isUserLoggedIn()) {
            authenticateWithBiometric();
        }

        // Edge-to-Edge UI
        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvToSignup = findViewById(R.id.tvToSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());

        tvToSignup.setOnClickListener(v -> {
            hideKeyboard();
            startActivity(new Intent(this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            hideKeyboard();
            startActivity(new Intent(this, ForgetActivity.class));
        });
    }

    // 🔐 AWS LOGIN
    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        AWSMobileClient.getInstance().signIn(
                email,
                password,
                null,
                new Callback<SignInResult>() {
                    @Override
                    public void onResult(SignInResult signInResult) {
                        runOnUiThread(() -> {
                            saveLoginState(true);

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(
                                    new Intent(
                                            LoginActivity.this,
                                            DashboardActivity.class
                                    )
                            );
                            finish();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("AWS_LOGIN", e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Login Failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // 🔐 BIOMETRIC / PIN AUTH
    private void authenticateWithBiometric() {

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) != BiometricManager.BIOMETRIC_SUCCESS) {
            return; // device not supported
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt =
                new BiometricPrompt(this, executor,
                        new BiometricPrompt.AuthenticationCallback() {

                            @Override
                            public void onAuthenticationSucceeded(
                                    BiometricPrompt.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);

                                startActivity(
                                        new Intent(
                                                LoginActivity.this,
                                                DashboardActivity.class
                                        )
                                );
                                finish();
                            }

                            @Override
                            public void onAuthenticationError(
                                    int errorCode, CharSequence errString) {
                                super.onAuthenticationError(errorCode, errString);
                                Toast.makeText(
                                        LoginActivity.this,
                                        errString,
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Login Required")
                        .setSubtitle("Use fingerprint, face or device PIN")
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // ⌨️ Hide Keyboard
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // 💾 Save login state
    private void saveLoginState(boolean loggedIn) {
        getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", loggedIn)
                .apply();
    }

    // 🔍 Check login state
    private boolean isUserLoggedIn() {
        return getSharedPreferences("auth", MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);
    }
}