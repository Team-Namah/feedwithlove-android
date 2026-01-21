package com.namah.feedwithlove;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    EditText etName, etEmail;
    TextInputEditText etPassword;
    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Edge-to-Edge with a light status bar to match design3Background (#F2F2F2)
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        btnSignup.setOnClickListener(v -> signup());
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

    private void signup() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        CognitoUserPool pool = CognitoManager.getUserPool(this);
        if (pool == null) {
            Toast.makeText(this, "Auth service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        CognitoUserAttributes attrs = new CognitoUserAttributes();
        attrs.addAttribute("name", name);
        attrs.addAttribute("email", email);

        pool.signUpInBackground(email, password, attrs, null,
                new SignUpHandler() {
                    @Override
                    public void onSuccess(CognitoUser user, SignUpResult result) {
                        Toast.makeText(SignupActivity.this,
                                "Code has been sent to email",
                                Toast.LENGTH_LONG).show();

                        Intent i = new Intent(SignupActivity.this, VerifyActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SignupActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
