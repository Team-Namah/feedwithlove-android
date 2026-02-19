package ui.volunteer;

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

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
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
import com.namah.feedwithlove.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AvailableDeliveriesActivity extends AppCompatActivity {

    private RecyclerView rvAvailableDeliveries;
    private DeliveryAdapter adapter;
    private DatabaseReference foodsRef;
    private ValueEventListener deliveriesListener;
    private final List<DeliveryItem> deliveryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_available_deliveries);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main_available_deliveries),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        rvAvailableDeliveries = findViewById(R.id.rvAvailableDeliveries);
        rvAvailableDeliveries.setLayoutManager(new LinearLayoutManager(this));

        foodsRef = FirebaseDatabase.getInstance().getReference("foods");

        loadAvailableDeliveries();
    }

    /* ================= LOAD DATA ================= */

    private void loadAvailableDeliveries() {

        if (deliveriesListener != null) {
            foodsRef.removeEventListener(deliveriesListener);
        }

        deliveriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                deliveryList.clear();

                for (DataSnapshot foodSnap : snapshot.getChildren()) {

                    String deliveryVolunteer =
                            foodSnap.child("status/delivery_valounteer")
                                    .getValue(String.class);

                    // ✅ ONLY FILTER
                    if (deliveryVolunteer == null) continue;
                    if (!deliveryVolunteer.equalsIgnoreCase(Status.NULL.name())) continue;

                    String foodId = foodSnap.getKey();
                    String title = foodSnap.child("basic/title").getValue(String.class);
                    String dropAddress =
                            foodSnap.child("location/drop/address")
                                    .getValue(String.class);
                    String expiry =
                            foodSnap.child("basic/expiryTime")
                                    .getValue(String.class);
                    String donor =
                            foodSnap.child("role/donor")
                                    .getValue(String.class);
                    String imageUrl =
                            foodSnap.child("basic/imageUrl")
                                    .getValue(String.class);

                    deliveryList.add(new DeliveryItem(
                            foodId,
                            title == null ? "Food Item" : title,
                            dropAddress == null ? "Drop location not set" : dropAddress,
                            expiry == null ? "N/A" : "Till " + expiry,
                            donor == null ? "Unknown Donor" : "Donor: " + donor,
                            imageUrl
                    ));
                }

                adapter = new DeliveryAdapter(
                        deliveryList,
                        AvailableDeliveriesActivity.this::showDeliveryDetails
                );
                rvAvailableDeliveries.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        AvailableDeliveriesActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        };
        foodsRef.addValueEventListener(deliveriesListener);
    }

    /* ================= BOTTOM SHEET ================= */

    private void showDeliveryDetails(DeliveryItem item) {

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
        btnAccept.setText("ACCEPT DELIVERY");
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

            // 1️⃣ Update status
            foodsRef.child(item.getFoodId())
                    .child("status")
                    .child("delivery_valounteer")
                    .setValue(Status.PENDING.name());

            // 2️⃣ Assign volunteer email
            assignVolunteerEmail(item.getFoodId());

            Toast.makeText(this,
                    "Delivery accepted successfully!",
                    Toast.LENGTH_LONG).show();

            bottomSheetDialog.dismiss();
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

    /* ================= ASSIGN VOLUNTEER ================= */

    private void assignVolunteerEmail(String foodId) {

        if (!AWSMobileClient.getInstance().isSignedIn()) return;

        AWSMobileClient.getInstance().getUserAttributes(
                new Callback<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> attributes) {

                        String email = attributes.get("email");
                        if (email == null) return;

                        foodsRef.child(foodId)
                                .child("role")
                                .child("volunteer")
                                .setValue(email);
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        AvailableDeliveriesActivity.this,
                                        "Failed to assign volunteer",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (foodsRef != null && deliveriesListener != null) {
            foodsRef.removeEventListener(deliveriesListener);
        }
    }

    /* ================= MODEL ================= */

    public static class DeliveryItem {

        private final String foodId;
        private final String foodName;
        private final String location;
        private final String time;
        private final String donorInfo;
        private final String imageUrl;

        public DeliveryItem(String foodId, String foodName,
                            String location, String time,
                            String donorInfo, String imageUrl) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.location = location;
            this.time = time;
            this.donorInfo = donorInfo;
            this.imageUrl = imageUrl;
        }

        public String getFoodId() { return foodId; }
        public String getFoodName() { return foodName; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getDonorInfo() { return donorInfo; }
        public String getImageUrl() { return imageUrl; }
    }

    /* ================= ADAPTER ================= */

    public static class DeliveryAdapter
            extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {

        private final List<DeliveryItem> items;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(DeliveryItem item);
        }

        public DeliveryAdapter(List<DeliveryItem> items,
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

            DeliveryItem item = items.get(position);

            holder.tvFoodName.setText(item.getFoodName());
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

            TextView tvFoodName, tvLocation;
            com.google.android.material.chip.Chip chipTime;
            ImageView ivFoodThumb;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvLocation = itemView.findViewById(R.id.tvPickupLoc);
                chipTime = itemView.findViewById(R.id.chipTime);
                ivFoodThumb = itemView.findViewById(R.id.ivFoodThumb);
            }
        }
    }
}
