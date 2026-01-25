package ui.donor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FragmentDonorHistory extends Fragment {

    private RecyclerView rvHistory;
    private ChipGroup chipGroupFilters;
    private DonationHistoryAdapter adapter;
    private final List<DonationHistoryItemModel> allItems = new ArrayList<>();

    private String userEmail = null;
    private String userRole = null;

    private DatabaseReference foodsRef;

    // 🔥 IMPORTANT: track current selected filter
    private String currentFilter = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donor_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rvHistory);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DonationHistoryAdapter(new ArrayList<>());
        rvHistory.setAdapter(adapter);

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

//        loadData();
        loadUserDetails();
        // 🔥 Chip selection listener (STORE current filter)
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {

            updateChipUI(checkedId);

            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentFilter = chip.getText().toString(); // ✅ SAVE
                filter(currentFilter);
            }
        });

        // Default selected chip (All)
        if (chipGroupFilters.getChildCount() > 0) {
            Chip defaultChip = (Chip) chipGroupFilters.getChildAt(0);
            defaultChip.setChecked(true);
            currentFilter = defaultChip.getText().toString();
            updateChipUI(defaultChip.getId());
        }
    }

    private void loadUserDetails() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            userEmail = attributes.get("email");
                            userRole  = attributes.get("custom:role");

                            // ✅ Only load data AFTER email is available
                            loadData();
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


    /* ---------------- FIREBASE LOAD ---------------- */
    private void loadData() {

        if (userEmail == null) return;

        foodsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allItems.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    // 🔥 CHECK DONOR EMAIL
                    String donorEmail =
                            snap.child("role/donor").getValue(String.class);

                    if (donorEmail == null ||
                            !donorEmail.equalsIgnoreCase(userEmail)) {
                        continue; // ❌ skip others
                    }

                    String title = snap.child("basic/title").getValue(String.class);
                    String imageUrl = snap.child("basic/imageUrl").getValue(String.class);

                    String address =
                            snap.child("location/pickup/address").getValue(String.class);

                    String delivery =
                            snap.child("status/delivery").getValue(String.class);

                    Long time =
                            snap.child("timestamps/createdAt").getValue(Long.class);

                    allItems.add(new DonationHistoryItemModel(
                            title,
                            formatTime(time),
                            address,
                            delivery == null ? "PENDING" : delivery.toUpperCase(),
                            imageUrl
                    ));
                }

                // ✅ Apply selected chip filter
                filter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    /* ---------------- CHIP UI COLOR ---------------- */
    private void updateChipUI(int selectedChipId) {

        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {

            Chip chip = (Chip) chipGroupFilters.getChildAt(i);

            if (chip.getId() == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.love_primary);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(
                        getResources().getColor(R.color.love_text_deep)
                );
            }
        }
    }

    /* ---------------- FILTER ---------------- */
    private void filter(String type) {
        List<DonationHistoryItemModel> filtered = new ArrayList<>();

        for (DonationHistoryItemModel item : allItems) {
            if ("All".equalsIgnoreCase(type)) {
                filtered.add(item);
            } else if (item.getDeliveryStatus().equalsIgnoreCase(type)) {
                filtered.add(item);
            }
        }

        adapter.updateList(filtered);
    }

    /* ---------------- TIME FORMAT ---------------- */
    private String formatTime(Long t) {
        if (t == null) return "";
        return new SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(t));
    }
}
