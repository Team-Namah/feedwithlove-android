package ui.donor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.namah.feedwithlove.R;

import java.util.List;

public class DonationHistoryAdapter
        extends RecyclerView.Adapter<DonationHistoryAdapter.VH> {

    private List<DonationHistoryItemModel> list;

    public DonationHistoryAdapter(List<DonationHistoryItemModel> list) {
        this.list = list;
    }

    public void updateList(List<DonationHistoryItemModel> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        DonationHistoryItemModel item = list.get(position);

        h.tvFoodName.setText(item.getFoodName());
        h.tvDateTime.setText(item.getDateTime());
        h.tvLocation.setText(item.getLocation());

        String status = item.getDeliveryStatus().toUpperCase();
        h.statusText.setText(status);

        if ("PENDING".equals(status)) {
            h.statusCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            h.statusText.setTextColor(Color.parseColor("#F57C00"));
        } else {
            h.statusCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            h.statusText.setTextColor(Color.parseColor("#2E7D32"));
        }

        Glide.with(h.ivFoodImage.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(h.ivFoodImage);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView ivFoodImage;
        TextView tvFoodName, tvDateTime, tvLocation;

        MaterialCardView statusCard;
        TextView statusText;

        VH(@NonNull View v) {
            super(v);

            ivFoodImage = v.findViewById(R.id.ivFoodImage);
            tvFoodName = v.findViewById(R.id.tvFoodName);
            tvDateTime = v.findViewById(R.id.tvDateTime);
            tvLocation = v.findViewById(R.id.tvLocation);

            // IMPORTANT: tvStatus is a CARD
            statusCard = v.findViewById(R.id.tvStatus);

            // Its child TextView (index 0)
            statusText = (TextView) statusCard.getChildAt(0);
        }
    }
}
