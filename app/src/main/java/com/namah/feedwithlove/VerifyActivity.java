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

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

public class VerifyActivity extends AppCompatActivity {

    private EditText etCode;
    private TextView[] tvCodes;
    private Button btnVerify;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Edge-to-Edge with a light status bar to match design3Background (#F2F2F2)
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Email is missing. Cannot verify.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnVerify = findViewById(R.id.btnVerify);
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });
        etCode = findViewById(R.id.etCode);
        tvCodes = new TextView[]{
                findViewById(R.id.tvCode1),
                findViewById(R.id.tvCode2),
                findViewById(R.id.tvCode3),
                findViewById(R.id.tvCode4),
                findViewById(R.id.tvCode5),
                findViewById(R.id.tvCode6)
        };

        updateBoxHighlight(0);

        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                for (int i = 0; i < tvCodes.length; i++) {
                    if (i < length) {
                        tvCodes[i].setText(String.valueOf(s.charAt(i)));
                    } else {
                        tvCodes[i].setText("");
                    }
                }
                updateBoxHighlight(length);
                if (length == 6) {
                    hideKeyboard();
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void updateBoxHighlight(int currentLength) {
        for (int i = 0; i < tvCodes.length; i++) {
            if (i == currentLength) {
                tvCodes[i].setAlpha(1.0f);
                tvCodes[i].setBackgroundResource(R.drawable.bg_design3_field_focused);
            } else {
                tvCodes[i].setAlpha(0.7f);
                tvCodes[i].setBackgroundResource(R.drawable.bg_design3_field);
            }
        }
    }

    private void verifyCode() {
        String otp = etCode.getText().toString().trim();

        if (otp.length() < 6) {
            Toast.makeText(this, "Please enter 6 digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CognitoManager.getUserPool(this) == null) {
            Toast.makeText(this, "Auth service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        CognitoUser user =
                CognitoManager.getUserPool(this).getUser(email);

        user.confirmSignUpInBackground(otp, false,
                new GenericHandler() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(VerifyActivity.this,
                                "Email verified",
                                Toast.LENGTH_LONG).show();

                        startActivity(new Intent(
                                VerifyActivity.this,
                                LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(VerifyActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
