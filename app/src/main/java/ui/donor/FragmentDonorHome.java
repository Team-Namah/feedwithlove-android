package ui.donor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;

import java.util.Map;

public class FragmentDonorHome extends Fragment {

    private TextView tvMealsShared, tvActivePickups;

    private DatabaseReference foodsRef;

    private String userEmail = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donor_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMealsShared = view.findViewById(R.id.tvMealsShared);
        tvActivePickups = view.findViewById(R.id.tvActivePickups);

        view.findViewById(R.id.cardDonate)
                .setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), DonorFoodUploadActivity.class)));

        view.findViewById(R.id.btnDonate)
                .setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), DonorFoodUploadActivity.class)));

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        loadUserDetails();
    }

    /* ---------------- USER DETAILS ---------------- */
    private void loadUserDetails() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            userEmail = attributes.get("email");
                            loadStats(); // ✅ Load stats after email
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(),
                                "Failed to load user info",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /* ---------------- LOAD STATS ---------------- */
    private void loadStats() {

        if (userEmail == null) return;

        foodsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int mealsShared = 0;
                int activePickups = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String donorEmail =
                            snap.child("role/donor").getValue(String.class);

                    if (donorEmail == null ||
                            !donorEmail.equalsIgnoreCase(userEmail)) {
                        continue;
                    }

                    mealsShared++;

                    String delivery =
                            snap.child("status/delivery").getValue(String.class);

                    if (delivery == null ||
                            delivery.equalsIgnoreCase("PENDING")) {
                        activePickups++;
                    }
                }

                tvMealsShared.setText(String.valueOf(mealsShared));
                tvActivePickups.setText(String.valueOf(activePickups));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
