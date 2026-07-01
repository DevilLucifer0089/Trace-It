package com.lostandfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.lostandfound.databinding.ActivitySplashBinding;
import com.lostandfound.utils.FirebaseHelper;

/**
 * Entry point of the app.
 * Shows logo + progress indicator for 1.8 seconds,
 * then routes to MainActivity (logged in) or LoginActivity (not logged in).
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivLogo.setAlpha(0f);
        binding.ivLogo.setScaleX(0.7f);
        binding.ivLogo.setScaleY(0.7f);
        binding.ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::routeUser, SPLASH_DELAY_MS);
    }

    private void routeUser() {
        if (FirebaseHelper.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
