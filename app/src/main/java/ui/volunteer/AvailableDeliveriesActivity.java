package ui.volunteer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.namah.feedwithlove.R;

import java.util.ArrayList;
import java.util.List;

public class AvailableDeliveriesActivity extends AppCompatActivity {

    private RecyclerView rvAvailableDeliveries;
    private DeliveryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_available_deliveries);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_available_deliveries), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvAvailableDeliveries = findViewById(R.id.rvAvailableDeliveries);
        rvAvailableDeliveries.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        setupSampleData();
    }

    private void setupSampleData() {
        List<DeliveryItem> samples = new ArrayList<>();
        samples.add(new DeliveryItem("Warm Pasta Meal", "1.2 km", "Downtown Restaurant", "Till 08:00 PM", "Donor: Mario's Kitchen"));
        samples.add(new DeliveryItem("Fresh Fruit Basket", "2.5 km", "Green Valley Mart", "Till 06:00 PM", "Donor: Sarah Green"));
        samples.add(new DeliveryItem("Baked Goodies", "0.8 km", "Central Bakery", "Till 09:30 PM", "Donor: Old Town Bakes"));
        samples.add(new DeliveryItem("Family Curry Pack", "3.1 km", "Spice Garden", "Till 10:00 PM", "Donor: Raj's Catering"));

        adapter = new DeliveryAdapter(samples, this::showDeliveryDetails);
        rvAvailableDeliveries.setAdapter(adapter);
    }

//    @SuppressLint("ResourceAsColor")
    private void showDeliveryDetails(DeliveryItem item) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.layout_donation_details_bottom_sheet,
                null
        );

        TextView tvTitle = bottomSheetView.findViewById(R.id.tvDetailFoodName);
        TextView tvLocation = bottomSheetView.findViewById(R.id.tvDetailLocation);
        TextView tvDateTime = bottomSheetView.findViewById(R.id.tvDetailDateTime);
        TextView tvLabel = bottomSheetView.findViewById(R.id.tvVolunteerLabel);
        TextView tvVolunteerName = bottomSheetView.findViewById(R.id.tvVolunteerName);
        
        tvTitle.setText(item.getFoodName());
        tvLocation.setText(item.getLocation());
        tvDateTime.setText(item.getTime());
        tvLabel.setText("Donor Details");
        tvVolunteerName.setText(item.getDonorInfo());

        // We find the internal ConstraintLayout to add the button.
        ConstraintLayout internalContainer = bottomSheetView.findViewById(R.id.dragHandle).getParent() instanceof ConstraintLayout ? 
                (ConstraintLayout) bottomSheetView.findViewById(R.id.dragHandle).getParent() : null;

        if (internalContainer != null) {
            MaterialButton btnAccept = new MaterialButton(this);
            btnAccept.setId(View.generateViewId());
            btnAccept.setText("ACCEPT DELIVERY");
            btnAccept.setBackgroundTintList(getResources().getColorStateList(R.color.love_primary));
            btnAccept.setCornerRadius(20);
            btnAccept.setLetterSpacing(0.03F);

            // Using ConstraintLayout.LayoutParams to align the button at the bottom
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            
            params.topToBottom = R.id.cardVolunteerInfo;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMargins(60, 40, 60, 0);
            
            btnAccept.setLayoutParams(params);
            internalContainer.addView(btnAccept);
            
            btnAccept.setOnClickListener(v -> {
                Toast.makeText(this, "Delivery Accepted! Navigation started.", Toast.LENGTH_LONG).show();
                bottomSheetDialog.dismiss();
                finish();
            });
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

    public static class DeliveryItem {
        private String foodName, distance, location, time, donorInfo;
        public DeliveryItem(String foodName, String distance, String location, String time, String donorInfo) {
            this.foodName = foodName;
            this.distance = distance;
            this.location = location;
            this.time = time;
            this.donorInfo = donorInfo;
        }
        public String getFoodName() { return foodName; }
        public String getDistance() { return distance; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getDonorInfo() { return donorInfo; }
    }

    public static class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {
        private List<DeliveryItem> items;
        private OnItemClickListener listener;

        public interface OnItemClickListener { void onItemClick(DeliveryItem item); }

        public DeliveryAdapter(List<DeliveryItem> items, OnItemClickListener listener) {
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
            DeliveryItem item = items.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvDistance.setText(item.getDistance());
            holder.tvLocation.setText(item.getLocation());
            holder.chipTime.setText(item.getTime());
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