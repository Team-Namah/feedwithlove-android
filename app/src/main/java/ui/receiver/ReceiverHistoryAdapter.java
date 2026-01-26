package ui.receiver;

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

public class ReceiverHistoryAdapter
        extends RecyclerView.Adapter<ReceiverHistoryAdapter.ViewHolder> {

    private List<FragmentReceiverHistory.ReceiverHistoryItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FragmentReceiverHistory.ReceiverHistoryItem item);
    }

    public ReceiverHistoryAdapter(List<FragmentReceiverHistory.ReceiverHistoryItem> items,
                                  OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<FragmentReceiverHistory.ReceiverHistoryItem> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_delivery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        FragmentReceiverHistory.ReceiverHistoryItem item = items.get(position);

        h.tvFoodName.setText(item.foodName);
        h.tvDistance.setText(item.dateTime);
        h.tvLocation.setText(item.location);
        h.chipTime.setText(item.status);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(h.ivFoodImage);
        } else {
            h.ivFoodImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFoodName, tvDistance, tvLocation;
        Chip chipTime;
        ImageView ivFoodImage;

        ViewHolder(@NonNull View v) {
            super(v);
            ivFoodImage = v.findViewById(R.id.ivFoodThumb);
            tvFoodName = v.findViewById(R.id.tvFoodName);
            tvDistance = v.findViewById(R.id.tvDistance);
            tvLocation = v.findViewById(R.id.tvPickupLoc);
            chipTime = v.findViewById(R.id.chipTime);
        }
    }
}
