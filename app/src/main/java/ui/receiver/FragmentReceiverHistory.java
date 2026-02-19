package ui.receiver;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;
import com.namah.feedwithlove.Status;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FragmentReceiverHistory extends Fragment {

    private RecyclerView rvReceiverHistory;
    private ReceiverHistoryAdapter adapter;
    private ChipGroup chipGroupFilters;

    private final List<ReceiverHistoryItem> allItems = new ArrayList<>();

    private DatabaseReference foodsRef;
    private ValueEventListener historyListener;
    private String userEmail;
    private String currentFilter = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receiver_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReceiverHistory = view.findViewById(R.id.rvReceiverHistory);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);

        rvReceiverHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReceiverHistoryAdapter(new ArrayList<>(), this::showMealDetails);
        rvReceiverHistory.setAdapter(adapter);

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        loadUserDetails();

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            updateChipUI(checkedId);
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentFilter = chip.getText().toString();
                filter(currentFilter);
            }
        });

        if (chipGroupFilters.getChildCount() > 0) {
            Chip defaultChip = (Chip) chipGroupFilters.getChildAt(0);
            defaultChip.setChecked(true);
            updateChipUI(defaultChip.getId());
        }
    }

    /* ---------------- USER DETAILS ---------------- */
    private void loadUserDetails() {
        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(new Callback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> attributes) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                activity.runOnUiThread(() -> {
                    userEmail = attributes.get("email");
                    loadData();
                });
            }

            @Override
            public void onError(Exception e) {
                Activity activity = getActivity();
                if (activity == null || !isAdded()) return;
                activity.runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /* ---------------- FIREBASE LOAD ---------------- */
    private void loadData() {
        if (userEmail == null) return;

        if (historyListener != null) {
            foodsRef.removeEventListener(historyListener);
        }

        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                allItems.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String receiverEmail = snap.child("role/receiver").getValue(String.class);
                    if (receiverEmail == null || !receiverEmail.equalsIgnoreCase(userEmail)) continue;

                    String title = snap.child("basic/title").getValue(String.class);
                    String imageUrl = snap.child("basic/imageUrl").getValue(String.class);
                    String address = snap.child("location/drop/address").getValue(String.class);

                    String donor = snap.child("role/donor").getValue(String.class);
                    String volunteerRole = snap.child("role/volunteer").getValue(String.class);
                    String volunteerStatus = snap.child("status/delivery_valounteer").getValue(String.class);

                    Long time = snap.child("timestamps/createdAt").getValue(Long.class);

                    String status = (volunteerStatus == null || volunteerStatus.equalsIgnoreCase(Status.NULL.name()))
                            ? Status.PENDING.name()
                            : Status.COMPLETED.name();

                    String volunteerText = status.equals(Status.PENDING.name())
                            ? "Volunteer: Pending..."
                            : "Volunteer: " + (volunteerRole == null ? "Assigned" : volunteerRole);

                    allItems.add(new ReceiverHistoryItem(
                            title,
                            formatTime(time),
                            address,
                            status,
                            "Donor: " + (donor == null ? "-" : donor),
                            volunteerText,
                            "Receiver: " + receiverEmail,
                            imageUrl
                    ));
                }

                filter(currentFilter);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        foodsRef.addValueEventListener(historyListener);
    }

    /* ---------------- FILTER ---------------- */
    private void filter(String type) {
        List<ReceiverHistoryItem> filtered = new ArrayList<>();
        for (ReceiverHistoryItem item : allItems) {
            if ("All".equalsIgnoreCase(type) || item.status.equalsIgnoreCase(type)) {
                filtered.add(item);
            }
        }
        adapter.updateList(filtered);
    }

    /* ---------------- CHIP UI ---------------- */
    private void updateChipUI(int selectedChipId) {
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(i);
            if (chip.getId() == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.love_primary);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.love_text_deep));
            }
        }
    }

    private String formatTime(Long t) {
        if (t == null) return "";
        return new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
                .format(new Date(t));
    }

    /* ---------------- BOTTOM SHEET ---------------- */
    private void showMealDetails(ReceiverHistoryItem item) {

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);

        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_donation_details_bottom_sheet, null);

        ImageView ivImage = v.findViewById(R.id.foodimg);

        ((TextView) v.findViewById(R.id.tvDetailFoodName)).setText(item.foodName);
        ((TextView) v.findViewById(R.id.tvDetailLocation))
                .setText("Drop Location: " + item.location);
        ((TextView) v.findViewById(R.id.tvDetailDateTime)).setText(item.dateTime);
        ((TextView) v.findViewById(R.id.tvVolunteerLabel)).setText(item.donorInfo);
        ((TextView) v.findViewById(R.id.tvVolunteerName)).setText(item.volunteerInfo);
        ((TextView) v.findViewById(R.id.tvVolunteerEmail)).setText(item.receiverInfo);
        ((TextView) v.findViewById(R.id.tvDetailStatus)).setText(item.status);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        dialog.setContentView(v);
        dialog.setOnShowListener(d -> {
            FrameLayout sheet =
                    dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                BottomSheetBehavior.from(sheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (foodsRef != null && historyListener != null) {
            foodsRef.removeEventListener(historyListener);
        }
    }

    /* ---------------- MODEL ---------------- */
    static class ReceiverHistoryItem {
        String foodName, dateTime, location, status;
        String donorInfo, volunteerInfo, receiverInfo, imageUrl;

        ReceiverHistoryItem(String foodName, String dateTime, String location,
                            String status, String donorInfo,
                            String volunteerInfo, String receiverInfo,
                            String imageUrl) {

            this.foodName = foodName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.donorInfo = donorInfo;
            this.volunteerInfo = volunteerInfo;
            this.receiverInfo = receiverInfo;
            this.imageUrl = imageUrl;
        }
    }
}
