package ui.receiver;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ReceiverBrowseFoodActivity extends AppCompatActivity {

    private RecyclerView rvBrowseFood;
    private FoodBrowseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_receiver_browse_food);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_receiver_browse), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvBrowseFood = findViewById(R.id.rvBrowseFood);
        rvBrowseFood.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        setupSampleData();
    }

    private void setupSampleData() {
        List<FoodItem> samples = new ArrayList<>();
        samples.add(new FoodItem("Warm Pasta Meal", "15 Portions", "Downtown Restaurant", "Till 08:00 PM", "Donor: Mario's Kitchen"));
        samples.add(new FoodItem("Fresh Fruit Basket", "5 Baskets", "Green Valley Mart", "Till 06:00 PM", "Donor: Sarah Green"));
        samples.add(new FoodItem("Baked Goodies", "12 Packs", "Central Bakery", "Till 09:30 PM", "Donor: Old Town Bakes"));
        samples.add(new FoodItem("Family Curry Pack", "8 Boxes", "Spice Garden", "Till 10:00 PM", "Donor: Raj's Catering"));

        adapter = new FoodBrowseAdapter(samples, this::showFoodDetails);
        rvBrowseFood.setAdapter(adapter);
    }

    private void showFoodDetails(FoodItem item) {
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

        ConstraintLayout internalContainer = bottomSheetView.findViewById(R.id.dragHandle).getParent() instanceof ConstraintLayout ? 
                (ConstraintLayout) bottomSheetView.findViewById(R.id.dragHandle).getParent() : null;

        if (internalContainer != null) {
            MaterialButton btnAccept = new MaterialButton(this);
            btnAccept.setId(View.generateViewId());
            btnAccept.setText("ACCEPT FOOD");
            btnAccept.setBackgroundTintList(getResources().getColorStateList(R.color.love_primary));
            btnAccept.setCornerRadius(20);
            
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
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(this, ReceiverOrderDetailsActivity.class);
                intent.putExtra("food_name", item.getFoodName());
                startActivity(intent);
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

    // --- Static Inner Classes ---
    public static class FoodItem {
        private String foodName, quantity, location, time, donorInfo;
        public FoodItem(String foodName, String quantity, String location, String time, String donorInfo) {
            this.foodName = foodName;
            this.quantity = quantity;
            this.location = location;
            this.time = time;
            this.donorInfo = donorInfo;
        }
        public String getFoodName() { return foodName; }
        public String getQuantity() { return quantity; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getDonorInfo() { return donorInfo; }
    }

    public static class FoodBrowseAdapter extends RecyclerView.Adapter<FoodBrowseAdapter.ViewHolder> {
        private List<FoodItem> items;
        private OnItemClickListener listener;

        public interface OnItemClickListener { void onItemClick(FoodItem item); }

        public FoodBrowseAdapter(List<FoodItem> items, OnItemClickListener listener) {
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
            FoodItem item = items.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvQuantity.setText(item.getQuantity());
            holder.tvLocation.setText(item.getLocation());
            holder.chipTime.setText(item.getTime());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFoodName, tvQuantity, tvLocation;
            com.google.android.material.chip.Chip chipTime;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvQuantity = itemView.findViewById(R.id.tvDistance); // Reusing distance field for quantity
                tvLocation = itemView.findViewById(R.id.tvPickupLoc);
                chipTime = itemView.findViewById(R.id.chipTime);
            }
        }
    }
}