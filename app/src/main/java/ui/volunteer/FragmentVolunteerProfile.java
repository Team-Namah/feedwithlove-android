package ui.volunteer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.namah.feedwithlove.LoginActivity;
import com.namah.feedwithlove.R;
import com.ncorti.slidetoact.SlideToActView;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class FragmentVolunteerProfile extends Fragment {

    private TextView tvProfileName, tvEmail, tvRoleType;
    private SlideToActView swipeLogout;
    private ImageView ivProfileAvatar;
    private View layoutSafetyRulesOverlay;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_volunteer_profile, container, false);

        // Views
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRoleType = view.findViewById(R.id.tvRoleType);
        swipeLogout = view.findViewById(R.id.swipeLogout);
        ivProfileAvatar = view.findViewById(R.id.ivUserAvatar);
        layoutSafetyRulesOverlay = view.findViewById(R.id.layoutSafetyRulesOverlay);

        // Edit Profile
        view.findViewById(R.id.editProfileInfo).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), EditVolunteerProfileActivity.class))
        );

        // Safety Rules Show/Hide
        view.findViewById(R.id.chipSafetyRules).setOnClickListener(v -> {
            layoutSafetyRulesOverlay.setVisibility(View.VISIBLE);
            layoutSafetyRulesOverlay.setAlpha(0f);
            layoutSafetyRulesOverlay.animate().alpha(1f).setDuration(300).start();
        });

        view.findViewById(R.id.btnCloseSafety).setOnClickListener(v -> {
            layoutSafetyRulesOverlay.animate().alpha(0f).setDuration(300).withEndAction(() ->
                    layoutSafetyRulesOverlay.setVisibility(View.GONE)
            ).start();
        });

        setupLogout();
        loadUserDetails();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserDetails(); // 🔁 refresh profile every time
    }


    // ================= LOAD USER DETAILS =================

    private void loadUserDetails() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {

                            String name = attributes.get("name");
                            String email = attributes.get("email");
                            String profileImageUrl = attributes.get("picture");
                            String role = attributes.get("custom:role");

                            if (name != null) tvProfileName.setText(name.toUpperCase());
                            if (email != null) tvEmail.setText(email);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.bg_splash)
                                        .error(R.drawable.bg_splash)
                                        .fit()
                                        .centerCrop()
                                        .into(ivProfileAvatar);
                            } else {
                                ivProfileAvatar.setImageResource(R.drawable.bg_splash);
                            }

                            if (role != null) tvRoleType.setText(role.toUpperCase());
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(),
                                        "Failed to load profile",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }

    // ================= LOGOUT =================

    private void setupLogout() {
        swipeLogout.setOnSlideCompleteListener(view -> {

            // 🔒 Safe sign out
            if (AWSMobileClient.getInstance().isSignedIn()) {
                AWSMobileClient.getInstance().signOut();
            }

            // Clear login state
            SharedPreferences prefs =
                    requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();

            // Redirect to Login
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
