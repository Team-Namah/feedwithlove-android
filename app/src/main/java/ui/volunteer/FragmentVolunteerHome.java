package ui.volunteer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;
import com.namah.feedwithlove.Status;

import java.util.Map;

public class FragmentVolunteerHome extends Fragment {

    private TextView tvTotalDelivery, tvLevel, tvLevelTitle;
    private MaterialCardView cardLevel;

    private DatabaseReference foodsRef;
    private ValueEventListener deliveryListener;
    private String userEmail;
    private int totalDeliveries = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tvTotalDelivery = view.findViewById(R.id.total_delivery);
        tvLevel = view.findViewById(R.id.level);
        tvLevelTitle = view.findViewById(R.id.level_title);
        cardLevel = view.findViewById(R.id.cardLevel);
        view.findViewById(R.id.cardFindDeliveries).setOnClickListener(v -> startActivity(new Intent(requireContext(), AvailableDeliveriesActivity.class)));
        view.findViewById(R.id.btnFindDeliveries).setOnClickListener(v -> startActivity(new Intent(requireContext(), AvailableDeliveriesActivity.class)));
        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        loadUser();
        cardLevel.setOnClickListener(v -> showLevelDialog());
    }

    /* ================= USER ================= */
    private void loadUser() {
        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attrs) {
                        Activity activity = getActivity();
                        if (activity == null || !isAdded()) return;
                        activity.runOnUiThread(() -> {
                            userEmail = attrs.get("email");
                            loadDeliveries();
                        });
                    }
                    @Override public void onError(Exception e) { }
                }
        );
    }

    /* ================= DELIVERIES ================= */
    private void loadDeliveries() {
        if (userEmail == null) return;

        if (deliveryListener != null) {
            foodsRef.removeEventListener(deliveryListener);
        }

        deliveryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                totalDeliveries = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String volunteer =
                            snap.child("role/volunteer").getValue(String.class);

                    String status =
                            snap.child("status/delivery_valounteer")
                                    .getValue(String.class);

                    if (userEmail != null &&
                            userEmail.equalsIgnoreCase(volunteer) &&
                            status != null &&
                            status.equalsIgnoreCase(Status.COMPLETED.name())) {

                        totalDeliveries++;
                    }
                }

                updateUI();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        foodsRef.addValueEventListener(deliveryListener);
    }

    /* ================= LEVEL LOGIC ================= */
    private void updateUI() {

        tvTotalDelivery.setText(String.valueOf(totalDeliveries));

        LevelInfo level = getLevelInfo(totalDeliveries);

        tvLevel.setText("Level " + level.level);
        tvLevelTitle.setText(level.title);
    }

    private LevelInfo getLevelInfo(int deliveries) {

        if (deliveries < 5)
            return new LevelInfo(1, "Kind Starter", 5);

        if (deliveries < 15)
            return new LevelInfo(2, "Helping Hand", 15);

        if (deliveries < 30)
            return new LevelInfo(3, "Food Warrior", 30);

        if (deliveries < 50)
            return new LevelInfo(4, "Hunger Hero", 50);

        return new LevelInfo(5, "Super Hero", -1);
    }

    /* ================= LEVEL DIALOG ================= */
    private void showLevelDialog() {

        LevelInfo level = getLevelInfo(totalDeliveries);

        String message;

        if (level.nextTarget == -1) {
            message = "🎉 You reached the highest level!\n\nThank you for fighting hunger ❤️";
        } else {
            int remaining = level.nextTarget - totalDeliveries;
            message =
                    "Current Level: " + level.level + " (" + level.title + ")\n\n" +
                            "Deliveries done: " + totalDeliveries + "\n" +
                            "Next level at: " + level.nextTarget + " deliveries\n\n" +
                            "🚀 Only " + remaining + " more deliveries to level up!";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Your Volunteer Level")
                .setMessage(message)
                .setPositiveButton("Keep Helping 💪", null)
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (foodsRef != null && deliveryListener != null) {
            foodsRef.removeEventListener(deliveryListener);
        }
    }

    /* ================= MODEL ================= */
    private static class LevelInfo {
        int level;
        String title;
        int nextTarget;

        LevelInfo(int level, String title, int nextTarget) {
            this.level = level;
            this.title = title;
            this.nextTarget = nextTarget;
        }
    }
}
