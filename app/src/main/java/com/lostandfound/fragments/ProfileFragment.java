package com.lostandfound.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.lostandfound.R;
import com.lostandfound.activities.LoginActivity;
import com.lostandfound.activities.ItemDetailActivity;
import com.lostandfound.adapters.ItemAdapter;
import com.lostandfound.utils.DummyDataUtils;
import com.lostandfound.databinding.FragmentProfileBinding;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile screen. Shows user info, post counts and Active/Resolved tabs.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseHelper         firebase;
    private ItemAdapter            adapter;
    private final List<Item>       userPosts  = new ArrayList<>();

    private boolean showingResolved = false;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) uploadAvatar(uri);
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding  = FragmentProfileBinding.inflate(inflater, container, false);
        firebase = FirebaseHelper.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();
        setupRecyclerView();
        setupTabs();
        setupMenu();

        binding.fabEditAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));
    }

    private void loadUserProfile() {
        String uid = firebase.getCurrentUserId();
        if (uid == null) return;

        firebase.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener((doc, e) -> {
                    if (!isAdded() || doc == null || !doc.exists()) return;

                    binding.tvProfileName.setText(doc.getString("name"));
                    binding.tvProfileEmail.setText(doc.getString("email"));

                    String avatarUrl = doc.getString("profilePictureUrl");
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .circleCrop()
                            .into(binding.ivProfilePic);
                });

        loadUserPosts(false);
        loadUserPosts(true);
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(requireContext(), userPosts, item -> {
            Intent intent = ItemDetailActivity.createIntent(requireContext(), item.getItemId());
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        binding.recyclerUserPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUserPosts.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(
                new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                        showingResolved = tab.getPosition() == 1;
                        loadUserPosts(showingResolved);
                    }
                    @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab t) {}
                    @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab t) {}
                });
    }

    private void loadUserPosts(boolean resolved) {
        String uid = firebase.getCurrentUserId();
        if (uid == null) return;

        firebase.getDb()
                .collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .whereEqualTo(Constants.FIELD_IS_RESOLVED, resolved)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    userPosts.clear();
                    int count = 0;
                    for (var doc : snapshots.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) { userPosts.add(item); count++; }
                    }

                    if (userPosts.isEmpty()) {
                        for (Item dummy : DummyDataUtils.getDummyItems(uid)) {
                            if (uid.equals(dummy.getUserId()) && dummy.isResolved() == resolved) {
                                userPosts.add(dummy);
                                count++;
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (resolved) binding.tvResolvedCount.setText(String.valueOf(count));
                    else          binding.tvActiveCount.setText(String.valueOf(count));
                });
    }

    private void uploadAvatar(Uri imageUri) {
        String uid = firebase.getCurrentUserId();

        firebase.getAvatarRef(uid)
                .putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return firebase.getAvatarRef(uid).getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    firebase.getDb()
                            .collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .update("profilePictureUrl", downloadUri.toString())
                            .addOnSuccessListener(unused ->
                                    Snackbar.make(requireView(),
                                            "Profile picture updated!",
                                            Snackbar.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(),
                                "Failed to update picture",
                                Snackbar.LENGTH_SHORT).show());
    }

    private void setupMenu() {
        binding.toolbarProfile.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Log out?")
                        .setMessage("You'll need to log back in to post or message.")
                        .setPositiveButton("Log out", (d, w) -> {
                            firebase.getAuth().signOut();
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
