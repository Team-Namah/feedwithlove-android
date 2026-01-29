package ui.volunteer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.namah.feedwithlove.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VolunteerHistoryAdapter
        extends RecyclerView.Adapter<VolunteerHistoryAdapter.ViewHolder> {

    private List<FragmentVolunteerHistory.VolunteerHistoryItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FragmentVolunteerHistory.VolunteerHistoryItem item);
    }

    public VolunteerHistoryAdapter(
            List<FragmentVolunteerHistory.VolunteerHistoryItem> items,
            OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<FragmentVolunteerHistory.VolunteerHistoryItem> list) {
        this.items = list;
        notifyDataSetChanged();
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

        FragmentVolunteerHistory.VolunteerHistoryItem item = items.get(position);

        holder.tvFoodName.setText(item.getFoodName());
        holder.tvDistance.setText(item.getDateTime());
        holder.tvLocation.setText(item.getLocation());
        holder.chipTime.setText(item.getStatus());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.ivFoodImage);
        }

        if ("PENDING".equalsIgnoreCase(item.getStatus())) {
            holder.chipTime.setChipBackgroundColorResource(R.color.love_bg_warm);
            holder.chipTime.setTextColor(
                    holder.itemView.getContext()
                            .getResources().getColor(R.color.love_primary));
        } else {
            holder.chipTime.setChipBackgroundColorResource(
                    android.R.color.holo_green_light);
            holder.chipTime.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvDistance, tvLocation;
        Chip chipTime;
        ImageView ivFoodImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvLocation = itemView.findViewById(R.id.tvPickupLoc);
            chipTime = itemView.findViewById(R.id.chipTime);
            ivFoodImage = itemView.findViewById(R.id.ivFoodThumb);
        }
    }
}
