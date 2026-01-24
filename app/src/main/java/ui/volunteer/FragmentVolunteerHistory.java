package ui.volunteer;

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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.namah.feedwithlove.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FragmentVolunteerHistory extends Fragment {

    private RecyclerView rvVolunteerHistory;
    private VolunteerHistoryAdapter adapter;
    private List<VolunteerHistoryItem> allSamples = new ArrayList<>();
    private ChipGroup chipGroupFilters;

    public FragmentVolunteerHistory() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVolunteerHistory = view.findViewById(R.id.rvVolunteerHistory);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        rvVolunteerHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSampleData();
        setupFilters();
    }

    private void setupSampleData() {
        allSamples.clear();
        allSamples.add(new VolunteerHistoryItem("Warm Pasta Meal", "Oct 24, 2023", "Downtown Restaurant", "COMPLETED", "Donor: Mario's Kitchen", true));
        allSamples.add(new VolunteerHistoryItem("Fresh Fruit Basket", "Oct 25, 2023", "Green Valley Mart", "PENDING", "Donor: Sarah Green", false));
        allSamples.add(new VolunteerHistoryItem("Baked Goodies", "Oct 26, 2023", "Central Bakery", "COMPLETED", "Donor: Old Town Bakes", true));
        allSamples.add(new VolunteerHistoryItem("Family Curry Pack", "Oct 27, 2023", "Spice Garden", "PENDING", "Donor: Raj's Catering", false));

        updateList(allSamples);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPending) {
                filterList("PENDING");
            } else if (checkedId == R.id.chipCompleted) {
                filterList("COMPLETED");
            } else {
                updateList(allSamples);
            }
        });
    }

    private void filterList(String status) {
        List<VolunteerHistoryItem> filtered = allSamples.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        updateList(filtered);
    }

    private void updateList(List<VolunteerHistoryItem> list) {
        adapter = new VolunteerHistoryAdapter(list, item -> {
            if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                showDeliveryDetails(item);
            } else {
                openDeliveryActionActivity(item);
            }
        });
        rvVolunteerHistory.setAdapter(adapter);
    }

    private void openDeliveryActionActivity(VolunteerHistoryItem item) {
        Intent intent = new Intent(getContext(), VolunteerDeliveryActionActivity.class);
        intent.putExtra("food_name", item.getFoodName());
        intent.putExtra("donor_info", item.getDonorInfo());
        startActivity(intent);
    }

    private void showDeliveryDetails(VolunteerHistoryItem item) {
        if (getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_donation_details_bottom_sheet,
                null
        );

        TextView tvTitle = bottomSheetView.findViewById(R.id.tvDetailFoodName);
        TextView tvLocation = bottomSheetView.findViewById(R.id.tvDetailLocation);
        TextView tvDateTime = bottomSheetView.findViewById(R.id.tvDetailDateTime);
        TextView tvLabel = bottomSheetView.findViewById(R.id.tvVolunteerLabel);
        TextView tvVolunteerName = bottomSheetView.findViewById(R.id.tvVolunteerName);
        TextView tvStatus = bottomSheetView.findViewById(R.id.tvDetailStatus);
        
        // Delivery Proof views
        TextView tvProofLabel = bottomSheetView.findViewById(R.id.tvDeliveryProofLabel);
        View cardProof = bottomSheetView.findViewById(R.id.cardDeliveryProof);

        tvTitle.setText(item.getFoodName());
        tvLocation.setText(item.getLocation());
        tvDateTime.setText(item.getDateTime());
        tvLabel.setText("Donor Details");
        tvVolunteerName.setText(item.getDonorInfo());
        tvStatus.setText(item.getStatus());

        if (item.hasProof()) {
            tvProofLabel.setVisibility(View.VISIBLE);
            cardProof.setVisibility(View.VISIBLE);
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomSheetDialog.show();
    }

    // --- Static Inner Classes ---
    public static class VolunteerHistoryItem {
        private String foodName, dateTime, location, status, donorInfo;
        private boolean hasProof;
        public VolunteerHistoryItem(String foodName, String dateTime, String location, String status, String donorInfo, boolean hasProof) {
            this.foodName = foodName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.donorInfo = donorInfo;
            this.hasProof = hasProof;
        }
        public String getFoodName() { return foodName; }
        public String getDateTime() { return dateTime; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getDonorInfo() { return donorInfo; }
        public boolean hasProof() { return hasProof; }
    }

    public static class VolunteerHistoryAdapter extends RecyclerView.Adapter<VolunteerHistoryAdapter.ViewHolder> {
        private List<VolunteerHistoryItem> items;
        private OnItemClickListener listener;

        public interface OnItemClickListener { void onItemClick(VolunteerHistoryItem item); }

        public VolunteerHistoryAdapter(List<VolunteerHistoryItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_delivery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VolunteerHistoryItem item = items.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvDistance.setText(item.getDateTime());
            holder.tvLocation.setText(item.getLocation());
            holder.chipTime.setText(item.getStatus());
            
            if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                holder.chipTime.setChipBackgroundColorResource(R.color.love_bg_warm);
                holder.chipTime.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.love_primary));
            } else {
                holder.chipTime.setChipBackgroundColorResource(android.R.color.holo_green_light);
                holder.chipTime.setTextColor(Color.WHITE);
            }
            
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFoodName, tvDistance, tvLocation;
            com.google.android.material.chip.Chip chipTime;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvDistance = itemView.findViewById(R.id.tvDistance);
                tvLocation = itemView.findViewById(R.id.tvPickupLoc);
                chipTime = itemView.findViewById(R.id.chipTime);
            }
        }
    }
}