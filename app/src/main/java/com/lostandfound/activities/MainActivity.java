package com.lostandfound.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.transition.MaterialFadeThrough;
import com.lostandfound.R;
import com.lostandfound.databinding.ActivityMainBinding;
import com.lostandfound.fragments.HomeFragment;
import com.lostandfound.fragments.MyPostsFragment;
import com.lostandfound.fragments.ProfileFragment;

/**
 * Single-activity host for the three main fragments.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final HomeFragment    homeFragment    = new HomeFragment();
    private final MyPostsFragment myPostsFragment = new MyPostsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            setupFragments();
        }

        setupBottomNavigation();
        setupFab();
    }

    private void setupFragments() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragmentContainer, myPostsFragment, "my_posts").hide(myPostsFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment, true);
                showFab();
                return true;
            } else if (id == R.id.nav_my_posts) {
                switchFragment(myPostsFragment, false);
                hideFab();
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment, false);
                hideFab();
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment target, boolean showFab) {
        if (target == activeFragment) return;

        target.setEnterTransition(new MaterialFadeThrough());
        activeFragment.setExitTransition(new MaterialFadeThrough());

        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }

    private void setupFab() {
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPostActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_anim);
        });
    }

    public void showFab() { binding.fab.show(); }
    public void hideFab() { binding.fab.hide(); }

    public void navigateToProfile() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
    }
}
