package ui.receiver;

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

public class FragmentReceiverHistory extends Fragment {

    private RecyclerView rvReceiverHistory;
    private ReceiverHistoryAdapter adapter;
    private List<ReceiverHistoryItem> allSamples = new ArrayList<>();
    private ChipGroup chipGroupFilters;

    public FragmentReceiverHistory() {
        // Required empty public constructor
    }

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
        rvReceiverHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSampleData();
        setupFilters();
    }

    private void setupSampleData() {
        allSamples.clear();
        allSamples.add(new ReceiverHistoryItem("Freshly Baked Pasta", "Oct 24, 2023", "123 Green Street", "COMPLETED", "Donor: Mario's Kitchen", "Volunteer: Alex T."));
        allSamples.add(new ReceiverHistoryItem("Healthy Fruit Salad", "Oct 25, 2023", "456 Hope Avenue", "PENDING", "Donor: Fresh Mart", "Volunteer: Pending..."));
        allSamples.add(new ReceiverHistoryItem("Veggie Curry Set", "Oct 26, 2023", "789 Kindness Blvd", "COMPLETED", "Donor: Spice Village", "Volunteer: Sarah J."));
        allSamples.add(new ReceiverHistoryItem("Homemade Bread", "Oct 27, 2023", "321 Love Road", "PENDING", "Donor: Old Bakes", "Volunteer: Pending..."));

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
        List<ReceiverHistoryItem> filtered = allSamples.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        updateList(filtered);
    }

    private void updateList(List<ReceiverHistoryItem> list) {
        adapter = new ReceiverHistoryAdapter(list, this::showMealDetails);
        rvReceiverHistory.setAdapter(adapter);
    }

    private void showMealDetails(ReceiverHistoryItem item) {
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

        tvTitle.setText(item.getFoodName());
        tvLocation.setText("Drop Location: " + item.getLocation());
        tvDateTime.setText("Order Date: " + item.getDateTime());
        tvLabel.setText(item.getDonorInfo());
        tvVolunteerName.setText(item.getVolunteerInfo());
        tvStatus.setText(item.getStatus());

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
    public static class ReceiverHistoryItem {
        private String foodName, dateTime, location, status, donorInfo, volunteerInfo;
        public ReceiverHistoryItem(String foodName, String dateTime, String location, String status, String donorInfo, String volunteerInfo) {
            this.foodName = foodName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.donorInfo = donorInfo;
            this.volunteerInfo = volunteerInfo;
        }
        public String getFoodName() { return foodName; }
        public String getDateTime() { return dateTime; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getDonorInfo() { return donorInfo; }
        public String getVolunteerInfo() { return volunteerInfo; }
    }

    public static class ReceiverHistoryAdapter extends RecyclerView.Adapter<ReceiverHistoryAdapter.ViewHolder> {
        private List<ReceiverHistoryItem> items;
        private OnItemClickListener listener;

        public interface OnItemClickListener { void onItemClick(ReceiverHistoryItem item); }

        public ReceiverHistoryAdapter(List<ReceiverHistoryItem> items, OnItemClickListener listener) {
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
            ReceiverHistoryItem item = items.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvDistance.setText(item.getDateTime());
            holder.tvLocation.setText(item.getLocation());
            holder.chipTime.setText(item.getStatus());
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