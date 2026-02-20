package com.namah.feedwithlove;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.google.android.material.textfield.TextInputEditText;

public class ForgetActivity extends AppCompatActivity {

    private ConstraintLayout layoutEmailEntry, layoutVerifyCode;
    private EditText etForgotEmail, etVerificationCode;
    TextInputEditText etNewPassword;
    private Button btnSendCode, btnResetPassword;
    private TextView[] tvCodes;
    private TextView tvResendCode;

    private String userEmail;
    private CognitoUserPool userPool;   // ✅ cache safely

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        // ✅ Initialize UserPool ONCE
        userPool = CognitoManager.getUserPool(this);
        if (userPool == null) {
            Toast.makeText(this, "Auth service unavailable. Restart app.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        layoutEmailEntry = findViewById(R.id.layoutEmailEntry);
        layoutVerifyCode = findViewById(R.id.layoutVerifyCode);
        etForgotEmail = findViewById(R.id.etForgotEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvResendCode = findViewById(R.id.tvResendCode);

        tvCodes = new TextView[]{
                findViewById(R.id.tvCode1),
                findViewById(R.id.tvCode2),
                findViewById(R.id.tvCode3),
                findViewById(R.id.tvCode4),
                findViewById(R.id.tvCode5),
                findViewById(R.id.tvCode6)
        };

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            hideKeyboard();
            if (layoutVerifyCode.getVisibility() == View.VISIBLE) {
                showEmailLayout();
            } else {
                finish();
            }
        });

        setupOtpInput();

        btnSendCode.setOnClickListener(v -> sendCode());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvResendCode.setOnClickListener(v -> sendCode());
    }

    // ---------------- SEND CODE ----------------

    private void sendCode() {
        userEmail = etForgotEmail.getText().toString().trim();

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        CognitoUser user = userPool.getUser(userEmail);
        user.forgotPasswordInBackground(forgotPasswordHandler);

        hideKeyboard();
        showVerifyLayout();
    }

    // ---------------- RESET PASSWORD ----------------

    private void resetPassword() {
        String code = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (code.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Enter code and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        CognitoUser user = userPool.getUser(userEmail);
        user.confirmPasswordInBackground(code, newPassword, forgotPasswordHandler);
    }

    // ---------------- HANDLER ----------------

    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            Toast.makeText(ForgetActivity.this,
                    "Password reset successfully!",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
            finish();
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation continuation) {
            Toast.makeText(ForgetActivity.this,
                    "Reset code sent to your email",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(Exception exception) {
            Toast.makeText(ForgetActivity.this,
                    "Error: " + exception.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    };

    // ---------------- UI HELPERS ----------------

    private void showEmailLayout() {
        layoutEmailEntry.setVisibility(View.VISIBLE);
        layoutVerifyCode.setVisibility(View.GONE);
    }

    private void showVerifyLayout() {
        layoutEmailEntry.setVisibility(View.GONE);
        layoutVerifyCode.setVisibility(View.VISIBLE);
        updateBoxHighlight(0);
    }

    private void setupOtpInput() {
        etVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                for (int i = 0; i < tvCodes.length; i++) {
                    tvCodes[i].setText(i < length ? String.valueOf(s.charAt(i)) : "");
                }
                updateBoxHighlight(length);
                if (length == 6) hideKeyboard();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateBoxHighlight(int currentLength) {
        for (int i = 0; i < tvCodes.length; i++) {
            tvCodes[i].setAlpha(i == currentLength ? 1f : 0.7f);
            tvCodes[i].setBackgroundResource(
                    i == currentLength
                            ? R.drawable.bg_design3_field_focused
                            : R.drawable.bg_design3_field
            );
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
