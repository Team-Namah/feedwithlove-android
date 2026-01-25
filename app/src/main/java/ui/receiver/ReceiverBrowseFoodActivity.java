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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.namah.feedwithlove.R;

import java.util.ArrayList;
import java.util.List;

public class ReceiverBrowseFoodActivity extends AppCompatActivity {

    private RecyclerView rvBrowseFood;
    private FoodBrowseAdapter adapter;
    private DatabaseReference foodsRef;
    private final List<FoodItem> foodList = new ArrayList<>();

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

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        rvBrowseFood = findViewById(R.id.rvBrowseFood);
        rvBrowseFood.setLayoutManager(new LinearLayoutManager(this));

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");
        loadAvailableFoods();
    }

    private void loadAvailableFoods() {

        foodsRef.orderByChild("status/state")
                .equalTo("AVAILABLE")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        foodList.clear();

                        for (DataSnapshot foodSnap : snapshot.getChildren()) {

                            String foodId = foodSnap.getKey();

                            String title = foodSnap.child("basic/title").getValue(String.class);
                            String quantity = foodSnap.child("basic/quantity").getValue(String.class);
                            String location = foodSnap.child("location/pickup/address").getValue(String.class);
                            String time = foodSnap.child("basic/expiryTime").getValue(String.class);
                            String imageUrl = foodSnap.child("basic/imageUrl").getValue(String.class);

                            String donorEmail =
                                    foodSnap.child("role/donor").getValue(String.class);

                            if (donorEmail == null) donorEmail = "Unknown Donor";

                            foodList.add(new FoodItem(
                                    foodId,
                                    title,
                                    quantity,
                                    location,
                                    "Till " + time,
                                    "Donor: " + donorEmail,
                                    imageUrl
                            ));
                        }

                        adapter = new FoodBrowseAdapter(
                                foodList,
                                ReceiverBrowseFoodActivity.this::showFoodDetails
                        );

                        rvBrowseFood.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                ReceiverBrowseFoodActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showFoodDetails(FoodItem item) {

        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.layout_donation_details_bottom_sheet, null);
        ImageView foodimg = bottomSheetView.findViewById(R.id.foodimg);
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

        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(foodimg);

        ConstraintLayout container =
                (ConstraintLayout) bottomSheetView
                        .findViewById(R.id.cardVolunteerInfo)
                        .getParent();

        MaterialButton btnAccept = new MaterialButton(this);
        btnAccept.setText("ACCEPT FOOD");
        btnAccept.setBackgroundTintList(
                getResources().getColorStateList(R.color.love_primary)
        );
        btnAccept.setCornerRadius(20);

        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topToBottom = R.id.cardVolunteerInfo;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(60, 40, 60, 0);

        container.addView(btnAccept, params);

        btnAccept.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, ReceiverOrderDetailsActivity.class);
            intent.putExtra("food_id", item.getFoodId());
            startActivity(intent);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setOnShowListener(dialog -> {
            FrameLayout bottomSheet =
                    ((BottomSheetDialog) dialog)
                            .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        bottomSheetDialog.show();
    }

    /* ================= MODEL ================= */

    public static class FoodItem {

        private final String foodId;
        private final String foodName;
        private final String quantity;
        private final String location;
        private final String time;
        private final String donorInfo;
        private final String imageUrl;

        public FoodItem(String foodId, String foodName, String quantity,
                        String location, String time,
                        String donorInfo, String imageUrl) {

            this.foodId = foodId;
            this.foodName = foodName;
            this.quantity = quantity;
            this.location = location;
            this.time = time;
            this.donorInfo = donorInfo;
            this.imageUrl = imageUrl;
        }

        public String getFoodId() { return foodId; }
        public String getFoodName() { return foodName; }
        public String getQuantity() { return quantity; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getDonorInfo() { return donorInfo; }
        public String getImageUrl() { return imageUrl; }
    }

    /* ================= ADAPTER ================= */

    public static class FoodBrowseAdapter
            extends RecyclerView.Adapter<FoodBrowseAdapter.ViewHolder> {

        private final List<FoodItem> items;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(FoodItem item);
        }

        public FoodBrowseAdapter(List<FoodItem> items,
                                 OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_delivery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder, int position) {

            FoodItem item = items.get(position);

            holder.tvFoodName.setText(item.getFoodName());
            holder.tvQuantity.setText(item.getQuantity());
            holder.tvLocation.setText(item.getLocation());
            holder.chipTime.setText(item.getTime());

            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivFoodThumb);

            holder.itemView.setOnClickListener(
                    v -> listener.onItemClick(item)
            );
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvFoodName, tvQuantity, tvLocation;
            com.google.android.material.chip.Chip chipTime;
            ImageView ivFoodThumb;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvQuantity = itemView.findViewById(R.id.tvDistance);
                tvLocation = itemView.findViewById(R.id.tvPickupLoc);
                chipTime = itemView.findViewById(R.id.chipTime);
                ivFoodThumb = itemView.findViewById(R.id.ivFoodThumb);
            }
        }
    }
}
