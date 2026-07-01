package com.lostandfound.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;
import com.lostandfound.R;
import com.lostandfound.adapters.ItemAdapter;
import com.lostandfound.utils.DummyDataUtils;
import com.lostandfound.activities.ItemDetailActivity;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists the current user's posts (both active and resolved).
 * Placeholder fragment referenced by MainActivity's bottom navigation.
 */
public class MyPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter  adapter;
    private final List<Item> posts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ItemAdapter(requireContext(), posts, item -> {
            Intent intent = ItemDetailActivity.createIntent(requireContext(), item.getItemId());
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        loadPosts();
    }

    private void loadPosts() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseHelper.getInstance().getDb()
                .collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    posts.clear();
                    for (var doc : snapshots.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) posts.add(item);
                    }

                    if (posts.isEmpty()) {
                        for (Item dummy : DummyDataUtils.getDummyItems(uid)) {
                            if (uid.equals(dummy.getUserId())) posts.add(dummy);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
