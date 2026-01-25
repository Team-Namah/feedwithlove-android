package com.namah.feedwithlove;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    EditText etName, etEmail;
    TextInputEditText etPassword;
    Button btnSignup;
    TextView tvSelectedRole;

    private String selectedRole = "";
    private final String[] roles = {"DONOR", "VOLUNTEER", "RECEIVER"};

    BottomSheetDialog dialog;
    View sheetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvSelectedRole = findViewById(R.id.tvRoleSelected);

        dialog = new BottomSheetDialog(this);
        sheetView = getLayoutInflater().inflate(R.layout.layout_role_picker_sheet, null);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        findViewById(R.id.rlRolePicker).setOnClickListener(v -> showRolesTypePicker());
        btnSignup.setOnClickListener(v -> signup());
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void signup() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

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
        attrs.addAttribute("custom:role", selectedRole);

        pool.signUpInBackground(email, password, attrs, null, new SignUpHandler() {

            @Override
            public void onSuccess(CognitoUser user, SignUpResult signUpResult) {

                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("foods")
                        .child("role")
                        .child(selectedRole.toLowerCase())
                        .push();   // 🔥 AUTO {uid}

                Map<String, Object> data = new HashMap<>();
                data.put("email", email);
                data.put("name", name);

                ref.setValue(data);

                Toast.makeText(SignupActivity.this,
                        "Verification code sent to email",
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

    private void showRolesTypePicker() {

        dialog.setContentView(sheetView);
        View parent = (View) sheetView.getParent();
        if (parent != null) parent.setBackgroundColor(Color.TRANSPARENT);

        RecyclerView rv = sheetView.findViewById(R.id.rvBloodTypes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RolesAdapter());

        dialog.show();
    }

    class RolesAdapter extends RecyclerView.Adapter<RolesViewHolder> {

        @NonNull
        @Override
        public RolesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_roles, parent, false);
            return new RolesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RolesViewHolder holder, int position) {

            String role = roles[position];
            holder.tvName.setText(role);

            holder.itemView.setOnClickListener(v -> {
                selectedRole = role;
                tvSelectedRole.setText(role);
                dialog.dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return roles.length;
        }
    }

    static class RolesViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        RolesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRolesName);
        }
    }
}
