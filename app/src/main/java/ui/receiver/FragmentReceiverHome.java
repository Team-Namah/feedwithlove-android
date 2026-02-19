package ui.receiver;

import android.app.Activity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;
import com.namah.feedwithlove.Status;

import java.util.Map;

public class FragmentReceiverHome extends Fragment {

    private TextView tvMealsReceivedCount;
    private TextView tvMealsPendingCount;

    private DatabaseReference foodsRef;
    private ValueEventListener statsListener;
    private String receiverEmail;

    public FragmentReceiverHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receiver_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMealsReceivedCount = view.findViewById(R.id.tvMealsReceivedCount);
        tvMealsPendingCount = view.findViewById(R.id.tvMealsPendingCount);

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        view.findViewById(R.id.cardBrowseFood)
                .setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), ReceiverBrowseFoodActivity.class)));

        view.findViewById(R.id.btnBrowse)
                .setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), ReceiverBrowseFoodActivity.class)));

        loadUserEmail();
    }

    /* ---------------- USER EMAIL ---------------- */
    private void loadUserEmail() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(new Callback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> attributes) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                activity.runOnUiThread(() -> {
                    receiverEmail = attributes.get("email");
                    loadStats();
                });
            }

            @Override
            public void onError(Exception e) { }
        });
    }

    /* ---------------- LOAD STATS ---------------- */
    private void loadStats() {

        if (receiverEmail == null) return;

        if (statsListener != null) {
            foodsRef.removeEventListener(statsListener);
        }

        statsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                int completedCount = 0;
                int pendingCount = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String receiver =
                            snap.child("role/receiver").getValue(String.class);

                    if (receiver == null ||
                            !receiver.equalsIgnoreCase(receiverEmail)) continue;

                    String volunteerStatus =
                            snap.child("status/delivery_valounteer").getValue(String.class);

                    if (volunteerStatus == null ||
                            volunteerStatus.equalsIgnoreCase(Status.NULL.name())) {
                        pendingCount++;
                    } else {
                        completedCount++;
                    }
                }

                tvMealsReceivedCount.setText(String.valueOf(completedCount));
                tvMealsPendingCount.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        foodsRef.addValueEventListener(statsListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (foodsRef != null && statsListener != null) {
            foodsRef.removeEventListener(statsListener);
        }
    }
}
