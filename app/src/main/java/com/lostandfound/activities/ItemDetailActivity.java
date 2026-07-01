package com.lostandfound.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.lostandfound.R;
import com.lostandfound.databinding.ActivityItemDetailBinding;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Item detail screen.
 */
public class ItemDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private ActivityItemDetailBinding binding;
    private FirebaseHelper firebase;
    private String itemId;
    private Item   currentItem;

    public static Intent createIntent(Context context, String itemId) {
        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = FirebaseHelper.getInstance();

        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (itemId == null) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        loadItemDetails();
    }

    private void loadItemDetails() {
        firebase.getDb()
                .collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { finish(); return; }
                    currentItem = doc.toObject(Item.class);
                    if (currentItem != null) populateUI(currentItem);
                })
                .addOnFailureListener(e ->
                        Snackbar.make(binding.getRoot(),
                                getString(R.string.error_network),
                                Snackbar.LENGTH_LONG).show());
    }

    private void populateUI(Item item) {
        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivItemImageDetail);

        binding.tvDetailTitle.setText(item.getTitle());
        binding.tvDetailLocation.setText(item.getLocationName());
        binding.tvDetailDate.setText(item.getDateSpotted());
        binding.tvDescription.setText(item.getDescription());

        boolean isLost = Constants.STATUS_LOST.equals(item.getStatus());
        binding.tvDetailStatus.setText(item.isResolved()
                ? getString(R.string.resolved_badge)
                : item.getStatus());
        int textColor = ContextCompat.getColor(this,
                item.isResolved() ? R.color.status_found_text
                        : isLost ? R.color.status_lost_text : R.color.status_found_text);
        int bgColor = ContextCompat.getColor(this,
                item.isResolved() ? R.color.status_found_bg
                        : isLost ? R.color.status_lost_bg : R.color.status_found_bg);
        binding.tvDetailStatus.setTextColor(textColor);
        binding.tvDetailStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));

        binding.tvPosterName.setText(item.getPosterName());
        Glide.with(this)
                .load(item.getPosterAvatarUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.ivPosterDetail);

        String currentUid = firebase.getCurrentUserId();
        boolean isOwner   = item.getUserId().equals(currentUid);

        if (isOwner) {
            binding.btnContact.setVisibility(View.GONE);
            binding.btnMarkResolved.setVisibility(item.isResolved() ? View.GONE : View.VISIBLE);
            binding.btnDelete.setVisibility(View.VISIBLE);

            binding.btnMarkResolved.setOnClickListener(v -> markResolved());
            binding.btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            binding.btnContact.setVisibility(View.VISIBLE);
            binding.btnContact.setText(isLost
                    ? getString(R.string.contact_owner)
                    : getString(R.string.contact_finder));
            binding.btnMarkResolved.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);

            binding.btnContact.setOnClickListener(v -> openChat());
        }
    }

    private void markResolved() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Mark as resolved?")
                .setMessage("This will mark the item as reunited with its owner. This cannot be undone.")
                .setPositiveButton("Yes, resolve", (d, w) -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put(Constants.FIELD_IS_RESOLVED, true);

                    firebase.getDb()
                            .collection(Constants.COLLECTION_ITEMS)
                            .document(itemId)
                            .update(update)
                            .addOnSuccessListener(unused -> {
                                Snackbar.make(binding.getRoot(),
                                        "Marked as resolved! 🎉",
                                        Snackbar.LENGTH_SHORT).show();
                                binding.btnMarkResolved.setVisibility(View.GONE);
                                binding.tvDetailStatus.setText(getString(R.string.resolved_badge));
                            })
                            .addOnFailureListener(e ->
                                    Snackbar.make(binding.getRoot(),
                                            "Update failed", Snackbar.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete this post?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deletePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost() {
        firebase.getDb()
                .collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Snackbar.make(binding.getRoot(), "Post deleted", Snackbar.LENGTH_SHORT).show();
                    binding.getRoot().postDelayed(this::finish, 600);
                })
                .addOnFailureListener(e ->
                        Snackbar.make(binding.getRoot(),
                                "Delete failed", Snackbar.LENGTH_SHORT).show());
    }

    private void openChat() {
        String currentUid  = firebase.getCurrentUserId();
        String posterUid   = currentItem.getUserId();
        String chatId      = currentUid.compareTo(posterUid) < 0
                ? currentUid + "_" + posterUid
                : posterUid + "_" + currentUid;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CHAT_ID,   chatId);
        intent.putExtra(Constants.EXTRA_USER_ID,   posterUid);
        intent.putExtra(Constants.EXTRA_USER_NAME, currentItem.getPosterName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
