package com.lostandfound.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.lostandfound.activities.ItemDetailActivity;
import com.lostandfound.R;
import com.lostandfound.adapters.ItemAdapter;
import com.lostandfound.databinding.FragmentHomeBinding;
import com.lostandfound.models.Item;
import com.lostandfound.utils.Constants;
import com.lostandfound.utils.DummyDataUtils;
import com.lostandfound.utils.FirebaseHelper;

/**
 * Home screen showing all items in a 2-column staggered grid.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ItemAdapter adapter;
    private ListenerRegistration itemsListener;

    private String activeStatusFilter   = "All";
    private String activeCategoryFilter = "";
    private String searchQuery          = "";

    private final List<Item> allItems      = new ArrayList<>();
    private final List<Item> filteredItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setGreeting();
        setupRecyclerView();
        setupStatusChips();
        setupCategoryChips();
        setupSearch();
        setupSwipeRefresh();
        setupProfileClick();

        attachFirestoreListener();
    }

    private void setupProfileClick() {
        binding.ivProfile.setOnClickListener(v -> {
            if (getActivity() instanceof com.lostandfound.activities.MainActivity) {
                ((com.lostandfound.activities.MainActivity) getActivity()).navigateToProfile();
            }
        });
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12)      greeting = getString(R.string.greeting_morning);
        else if (hour < 18) greeting = getString(R.string.greeting_afternoon);
        else                greeting = getString(R.string.greeting_evening);

        binding.tvGreeting.setText(greeting + " 👋");

        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid != null) {
            FirebaseHelper.getInstance().getDb()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && isAdded()) {
                            String name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                binding.tvUserName.setText(name.split(" ")[0]);
                            }
                        }
                    });
        }
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(requireContext(), filteredItems, item -> {
            Intent intent = ItemDetailActivity.createIntent(requireContext(), item.getItemId());
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerViewItems.setLayoutManager(layoutManager);
        binding.recyclerViewItems.setAdapter(adapter);
    }

    private void attachFirestoreListener() {
        if (itemsListener != null) itemsListener.remove();

        Query query = FirebaseHelper.getInstance().getDb()
                .collection(Constants.COLLECTION_ITEMS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(50);

        if (!activeStatusFilter.equals("All")) {
            query = query.whereEqualTo(Constants.FIELD_STATUS, activeStatusFilter);
        }

        itemsListener = query.addSnapshotListener((snapshots, error) -> {
            if (!isAdded()) return;
            binding.swipeRefresh.setRefreshing(false);

            if (error != null || snapshots == null) {
                return;
            }

            allItems.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Item item = doc.toObject(Item.class);
                if (item != null) allItems.add(item);
            }

            if (allItems.isEmpty()) {
                List<Item> dummyItems = DummyDataUtils.getDummyItems(FirebaseHelper.getInstance().getCurrentUserId());
                if (activeStatusFilter.equals("All")) {
                    allItems.addAll(dummyItems);
                } else {
                    for (Item item : dummyItems) {
                        if (item.getStatus().equals(activeStatusFilter)) {
                            allItems.add(item);
                        }
                    }
                }
            }

            applyClientSideFilters();
        });
    }

    private void applyClientSideFilters() {
        filteredItems.clear();

        for (Item item : allItems) {
            if (!activeCategoryFilter.isEmpty()
                    && !item.getCategory().equalsIgnoreCase(activeCategoryFilter)) {
                continue;
            }
            if (!searchQuery.isEmpty()) {
                boolean titleMatch = item.getTitle().toLowerCase()
                        .contains(searchQuery.toLowerCase());
                boolean descMatch  = item.getDescription().toLowerCase()
                        .contains(searchQuery.toLowerCase());
                if (!titleMatch && !descMatch) continue;
            }
            filteredItems.add(item);
        }

        adapter.notifyDataSetChanged();

        boolean isEmpty = filteredItems.isEmpty();
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupStatusChips() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                binding.chipAll.setChecked(true);
                activeStatusFilter = "All";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll)   activeStatusFilter = "All";
                else if (checkedId == R.id.chipLost)  activeStatusFilter = "Lost";
                else if (checkedId == R.id.chipFound) activeStatusFilter = "Found";
            }
            attachFirestoreListener();
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if      (checkedIds.contains(R.id.chipElectronics)) activeCategoryFilter = "Electronics";
            else if (checkedIds.contains(R.id.chipKeys))        activeCategoryFilter = "Keys";
            else if (checkedIds.contains(R.id.chipDocuments))   activeCategoryFilter = "Documents";
            else if (checkedIds.contains(R.id.chipClothing))    activeCategoryFilter = "Clothing";
            else if (checkedIds.contains(R.id.chipPets))        activeCategoryFilter = "Pets";
            else if (checkedIds.contains(R.id.chipOthers))      activeCategoryFilter = "Others";
            else activeCategoryFilter = "";
            applyClientSideFilters();
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                applyClientSideFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                applyClientSideFilters();
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.md_primary);
        binding.swipeRefresh.setOnRefreshListener(this::attachFirestoreListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (itemsListener != null) itemsListener.remove();
        binding = null;
    }
}
