package ui.donor;

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
import com.namah.feedwithlove.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentDonorHistory extends Fragment {

    private RecyclerView rvHistory;
    private DonationHistoryAdapter adapter;

    public FragmentDonorHistory() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donor_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSampleData();
    }

    private void setupSampleData() {
        List<DonationHistoryItemModel> samples = new ArrayList<>();
        samples.add(new DonationHistoryItemModel("Fresh Meal Boxes", "Oct 24, 2023 • 02:30 PM", "123 Heart Street, Community Center", "COMPLETED", "Alex Thompson"));
        samples.add(new DonationHistoryItemModel("Baked Breads", "Oct 25, 2023 • 11:00 AM", "456 Hope Avenue, Shelter A", "PENDING", "Sarah Jenkins"));
        samples.add(new DonationHistoryItemModel("Fruit Baskets", "Oct 26, 2023 • 04:45 PM", "789 Kindness Blvd, Orphanage Home", "COMPLETED", "Michael Ross"));
        samples.add(new DonationHistoryItemModel("Vegetable Soup", "Oct 27, 2023 • 12:15 PM", "321 Love Rd, Senior Living", "COMPLETED", "Emma Watson"));

        adapter = new DonationHistoryAdapter(samples, item -> showDonationDetails(item));
        rvHistory.setAdapter(adapter);
    }

    private void showDonationDetails(DonationHistoryItemModel item) {
        if (getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_donation_details_bottom_sheet,
                null
        );

        TextView tvTitle = bottomSheetView.findViewById(R.id.tvDetailFoodName);
        TextView tvLocation = bottomSheetView.findViewById(R.id.tvDetailLocation);
        TextView tvDateTime = bottomSheetView.findViewById(R.id.tvDetailDateTime);
        TextView tvVolunteer = bottomSheetView.findViewById(R.id.tvVolunteerName);
        TextView tvStatus = bottomSheetView.findViewById(R.id.tvDetailStatus);

        tvTitle.setText(item.getFoodName());
        tvLocation.setText(item.getLocation());
        tvDateTime.setText(item.getDateTime());
        tvVolunteer.setText(item.getVolunteerName());
        tvStatus.setText(item.getStatus());

        if ("PENDING".equalsIgnoreCase(item.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        bottomSheetDialog.setContentView(bottomSheetView);

        // Force the BottomSheet to open fully (STATE_EXPANDED)
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });

        bottomSheetDialog.show();
    }

    // --- Static Inner Model Class ---
    public static class DonationHistoryItemModel {
        private String foodName;
        private String dateTime;
        private String location;
        private String status;
        private String volunteerName;

        public DonationHistoryItemModel(String foodName, String dateTime, String location, String status, String volunteerName) {
            this.foodName = foodName;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.volunteerName = volunteerName;
        }

        public String getFoodName() { return foodName; }
        public String getDateTime() { return dateTime; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getVolunteerName() { return volunteerName; }
    }

    // --- Static Inner Adapter Class ---
    public static class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {

        private List<DonationHistoryItemModel> donationList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(DonationHistoryItemModel item);
        }

        public DonationHistoryAdapter(List<DonationHistoryItemModel> donationList, OnItemClickListener listener) {
            this.donationList = donationList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DonationHistoryItemModel item = donationList.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvDateTime.setText(item.getDateTime());
            holder.tvLocation.setText(item.getLocation());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return donationList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFoodName, tvDateTime, tvLocation;
            ImageView ivFoodImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            }
        }
    }
}