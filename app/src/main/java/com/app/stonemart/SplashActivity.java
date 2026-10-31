package com.app.stonemart;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * SplashActivity - Ultra-smooth animated startup splash screen.
 * Features 900ms spring scale-up & fade animation matching 4S Contracting design.
 */
public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // System status bar styling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#F8FAFC"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setContentView(R.layout.activity_splash);

        // Apply Window Insets for status bar padding
        View root = findViewById(R.id.rootSplash);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        View logoCard = findViewById(R.id.logo_card);
        View subtitle = findViewById(R.id.logo_subtitle);

        if (logoCard != null) {
            logoCard.setAlpha(0f);
            logoCard.setScaleX(0.85f);
            logoCard.setScaleY(0.85f);
            logoCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(900)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (subtitle != null) {
            subtitle.setAlpha(0f);
            subtitle.animate()
                    .alpha(1f)
                    .setDuration(1200)
                    .start();
        }

        // Branding delay, then transition to main app
        handler.postDelayed(this::proceedToNextScreen, 1500);
    }

    private void proceedToNextScreen() {
        if (navigated || isFinishing()) {
            return;
        }
        navigated = true;
        handler.removeCallbacksAndMessages(null);

        UserLocalStore userStore = new UserLocalStore(SplashActivity.this);
        Intent intent;
        if (userStore.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, DashboardActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, EmployeeLoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
