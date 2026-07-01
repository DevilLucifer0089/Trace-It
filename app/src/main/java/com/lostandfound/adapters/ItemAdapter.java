package com.lostandfound.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.lostandfound.R;
import com.lostandfound.databinding.ItemRowLayoutBinding;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import java.util.List;

/**
 * RecyclerView adapter for item cards.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private final Context             context;
    private final List<Item>          items;
    private final OnItemClickListener clickListener;

    public ItemAdapter(Context context, List<Item> items, OnItemClickListener clickListener) {
        this.context       = context;
        this.items         = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRowLayoutBinding binding = ItemRowLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ItemRowLayoutBinding binding;

        public ItemViewHolder(@NonNull ItemRowLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Item item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvLocation.setText(item.getLocationName());
            binding.tvDate.setText(item.getDateSpotted());
            binding.chipCategory.setText(item.getCategory());

            boolean isLost = Constants.STATUS_LOST.equals(item.getStatus());
            binding.tvStatus.setText(item.getStatus());

            int textColor = ContextCompat.getColor(context,
                    isLost ? R.color.status_lost_text : R.color.status_found_text);
            int bgColor = ContextCompat.getColor(context,
                    isLost ? R.color.status_lost_bg : R.color.status_found_bg);

            binding.tvStatus.setTextColor(textColor);
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));

            binding.tvResolved.setVisibility(item.isResolved() ? View.VISIBLE : View.GONE);

            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .centerCrop()
                    .into(binding.ivItemImage);

            Glide.with(context)
                    .load(item.getPosterAvatarUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivPosterAvatar);

            binding.ivPosterAvatar.setOnClickListener(v -> {
                if (context instanceof com.lostandfound.activities.MainActivity) {
                    ((com.lostandfound.activities.MainActivity) context).navigateToProfile();
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(item);
            });
        }
    }

    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}
