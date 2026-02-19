package ui.volunteer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

public class FragmentVolunteerHistory extends Fragment {

    private RecyclerView rvVolunteerHistory;
    private ChipGroup chipGroupFilters;
    private VolunteerHistoryAdapter adapter;

    private final List<VolunteerHistoryItem> allItems = new ArrayList<>();
    private String currentFilter = "All";
    private String userEmail;

    // 🔹 Volunteer info
    private String volunteerName;
    private String volunteerAvatar;

    private DatabaseReference foodsRef;
    private ValueEventListener historyListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        rvVolunteerHistory = view.findViewById(R.id.rvVolunteerHistory);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);

        rvVolunteerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new VolunteerHistoryAdapter(new ArrayList<>(), item -> {
            if (Status.COMPLETED.name().equalsIgnoreCase(item.getStatus())) {
                showDeliveryDetails(item);
            } else {
                openDeliveryActionActivity(item);
            }
        });

        rvVolunteerHistory.setAdapter(adapter);

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        loadVolunteerProfile(); // 👈 load Cognito profile
        setupFilters();
    }

    /* ================= LOAD VOLUNTEER ================= */
    private void loadVolunteerProfile() {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {
                        Activity activity = getActivity();
                        if (activity == null || !isAdded()) return;
                        activity.runOnUiThread(() -> {
                            volunteerName = attributes.get("name");
                            userEmail = attributes.get("email");

                            volunteerAvatar = attributes.get("picture");
                            if (volunteerAvatar == null) {
                                volunteerAvatar = attributes.get("custom:profileImage");
                            }

                            loadData();
                        });
                    }

                    @Override
                    public void onError(Exception e) { }
                }
        );

    }

    /* ================= FIREBASE ================= */
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

                    String volunteer = snap.child("role/volunteer").getValue(String.class);

                    if (volunteer == null || !volunteer.equalsIgnoreCase(userEmail))
                        continue;

                    String foodId = snap.getKey();
                    String title = snap.child("basic/title").getValue(String.class);
                    String imageUrl = snap.child("basic/imageUrl").getValue(String.class);
                    String address = snap.child("location/pickup/address").getValue(String.class);
                    String donor = snap.child("role/donor").getValue(String.class);
                    String status = snap.child("status/delivery_valounteer").getValue(String.class);
                    Long time = snap.child("timestamps/createdAt").getValue(Long.class);

                    if (status == null || status.equalsIgnoreCase(Status.PENDING.name())) {
                        status = Status.PENDING.name();
                    } else if (!status.equalsIgnoreCase(Status.COMPLETED.name())) {
                         // Default to pending if it's something else like "ASSIGNED"
                         status = Status.PENDING.name();
                    }

                    allItems.add(new VolunteerHistoryItem(
                            foodId,
                            title == null ? "" : title,
                            formatTime(time),
                            address == null ? "N/A" : address,
                            status,
                            "Donor: " + (donor == null ? "-" : donor),
                            imageUrl
                    ));
                }

                applyFilter();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        foodsRef.addValueEventListener(historyListener);
    }

    /* ================= FILTER ================= */
    private void setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentFilter = chip.getText().toString();
                updateChipUI(checkedId);
                applyFilter();
            }
        });

        if (chipGroupFilters.getChildCount() > 0) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(0);
            chip.setChecked(true);
            updateChipUI(chip.getId());
        }
    }

    private void applyFilter() {
        List<VolunteerHistoryItem> filtered = new ArrayList<>();

        for (VolunteerHistoryItem item : allItems) {
            if ("All".equalsIgnoreCase(currentFilter)
                    || item.getStatus().equalsIgnoreCase(currentFilter)) {
                filtered.add(item);
            }
        }
        adapter.updateList(filtered);
    }

    private void updateChipUI(int selectedId) {
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupFilters.getChildAt(i);
            if (chip.getId() == selectedId) {
                chip.setChipBackgroundColorResource(R.color.love_primary);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.love_text_deep));
            }
        }
    }

    /* ================= BOTTOM SHEET ================= */
    private void showDeliveryDetails(VolunteerHistoryItem item) {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.layout_donation_details_bottom_sheet, null);

        ImageView ivVolunteerAvatar = v.findViewById(R.id.ivVolunteerAvatar);
        TextView tvVolunteerName = v.findViewById(R.id.tvVolunteerName);
        TextView tvVolunteerEmail = v.findViewById(R.id.tvVolunteerEmail);

        tvVolunteerName.setText(volunteerName != null ? volunteerName.toUpperCase() : "VOLUNTEER");
        tvVolunteerEmail.setText(userEmail != null ? userEmail : "");

        if (volunteerAvatar != null && !volunteerAvatar.isEmpty()) {
            Picasso.get().load(volunteerAvatar).placeholder(R.drawable.bg_splash).into(ivVolunteerAvatar);
        }

        ((TextView) v.findViewById(R.id.tvDetailFoodName)).setText(item.getFoodName());
        ((TextView) v.findViewById(R.id.tvDetailLocation)).setText(item.getLocation());
        ((TextView) v.findViewById(R.id.tvDetailDateTime)).setText(item.getDateTime());
        ((TextView) v.findViewById(R.id.tvDetailStatus)).setText(item.getStatus());

        ImageView img = v.findViewById(R.id.foodimg);
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(img);
        }

        dialog.setContentView(v);
        dialog.setOnShowListener(d -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                BottomSheetBehavior.from(sheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dialog.show();
    }

    /* ================= HELPERS ================= */
    private String formatTime(Long t) {
        if (t == null) return "";
        return new SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(new Date(t));
    }

    private void openDeliveryActionActivity(VolunteerHistoryItem item) {
        Intent intent = new Intent(requireContext(), VolunteerDeliveryActionActivity.class);
        intent.putExtra("food_id", item.getFoodId());
        intent.putExtra("food_name", item.getFoodName());
        intent.putExtra("donor_info", item.getDonorInfo());
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (foodsRef != null && historyListener != null) {
            foodsRef.removeEventListener(historyListener);
        }
    }

    /* ================= MODEL ================= */
    public static class VolunteerHistoryItem {

        private final String foodId, foodName, dateTime, location, status, donorInfo, imageUrl;

        public VolunteerHistoryItem(String foodId, String foodName, String dateTime, String location, String status, String donorInfo, String imageUrl) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.donorInfo = donorInfo;
            this.imageUrl = imageUrl;
        }

        public String getFoodId() { return foodId; }
        public String getFoodName() { return foodName; }
        public String getDateTime() { return dateTime; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getDonorInfo() { return donorInfo; }
        public String getImageUrl() { return imageUrl; }
    }
}
