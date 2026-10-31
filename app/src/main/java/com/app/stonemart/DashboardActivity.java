package com.app.stonemart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.app.stonemart.widget.CurvedBubbleNavigationView;
import com.app.stonemart.widget.PremiumAlertDialog;
import com.google.android.material.card.MaterialCardView;

/**
 * DashboardActivity - Main application shell featuring real-time network connectivity
 * animation alerts (matching 4S Contracting design) and smooth tab navigation.
 */
public class DashboardActivity extends AppCompatActivity {
    private UserLocalStore userStore;

    // Real-time Network Monitoring & Animated Alert
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver connectivityReceiver;

    private MaterialCardView networkAlertCard;
    private ImageView ivNetworkAlertIcon;
    private TextView tvNetworkAlertMessage;
    private TextView tvNetworkAlertClose;

    private final Handler networkAlertHandler = new Handler(Looper.getMainLooper());
    private Runnable hideNetworkAlertRunnable;
    private boolean wasOffline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Apply Notification Bar / System Status Bar Padding
        View root = findViewById(R.id.rootDashboard);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        userStore = new UserLocalStore(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        setupNetworkAlertView();

        CurvedBubbleNavigationView navView = findViewById(R.id.bottom_navigation);

        // Load Default Fragment (Home -> Attendance History Logs)
        loadFragment(new ManageAttendanceFragment());

        navView.setOnItemSelectedListener(index -> {
            Fragment selectedFragment = null;
            switch (index) {
                case 0: // Home -> Attendance History
                    selectedFragment = new ManageAttendanceFragment();
                    break;
                case 1: // Check In/Out -> In and Out Punch Screen
                    selectedFragment = new CheckInCheckOutFragment();
                    break;
                case 2: // Leave Form -> Leave Application Portal
                    selectedFragment = new LeaveFormFragment();
                    break;
                case 3: // Logout -> Trigger Log Out Dialog
                    showLogoutDialog();
                    break;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
        });

        checkInitialNetworkState();
        registerNetworkCallback();
    }

    private void setupNetworkAlertView() {
        networkAlertCard = findViewById(R.id.networkAlertCard);
        ivNetworkAlertIcon = findViewById(R.id.ivNetworkAlertIcon);
        tvNetworkAlertMessage = findViewById(R.id.tvNetworkAlertMessage);
        tvNetworkAlertClose = findViewById(R.id.tvNetworkAlertClose);

        if (tvNetworkAlertClose != null) {
            tvNetworkAlertClose.setOnClickListener(v -> {
                if (isNetworkAvailable()) {
                    wasOffline = false;
                    animateNetworkAlertOut();
                } else {
                    retriggerNetworkAlertIn();
                }
            });
        }
    }

    private void checkInitialNetworkState() {
        boolean isOnline = isNetworkAvailable();
        if (!isOnline) {
            wasOffline = true;
            showNetworkAlert(false);
        } else {
            wasOffline = false;
        }
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    runOnUiThread(() -> {
                        if (wasOffline) {
                            wasOffline = false;
                            showNetworkAlert(true);
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    runOnUiThread(() -> {
                        wasOffline = true;
                        showNetworkAlert(false);
                    });
                }
            };
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        } else {
            connectivityReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean online = isNetworkAvailable();
                    if (!online) {
                        wasOffline = true;
                        showNetworkAlert(false);
                    } else if (wasOffline) {
                        wasOffline = false;
                        showNetworkAlert(true);
                    }
                }
            };
            registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void showNetworkAlert(boolean isOnline) {
        if (networkAlertCard == null) return;

        if (hideNetworkAlertRunnable != null) {
            networkAlertHandler.removeCallbacks(hideNetworkAlertRunnable);
        }

        if (isOnline) {
            // ONLINE STATE: "We are back..." (Dark Emerald Green #0B381D)
            networkAlertCard.setCardBackgroundColor(Color.parseColor("#0B381D"));
            if (ivNetworkAlertIcon != null) {
                ivNetworkAlertIcon.setImageResource(R.drawable.ic_wifi_on);
            }
            if (tvNetworkAlertMessage != null) {
                tvNetworkAlertMessage.setText("We are back online");
            }

            animateNetworkAlertIn();

            hideNetworkAlertRunnable = this::animateNetworkAlertOut;
            networkAlertHandler.postDelayed(hideNetworkAlertRunnable, 3000);
        } else {
            // OFFLINE STATE: "You're offline right now" (Dark Crimson Red #450A0A)
            networkAlertCard.setCardBackgroundColor(Color.parseColor("#450A0A"));
            if (ivNetworkAlertIcon != null) {
                ivNetworkAlertIcon.setImageResource(R.drawable.ic_wifi_off);
            }
            if (tvNetworkAlertMessage != null) {
                tvNetworkAlertMessage.setText("You're offline right now");
            }

            animateNetworkAlertIn();
        }
    }

    private void animateNetworkAlertIn() {
        if (networkAlertCard == null) return;
        if (networkAlertCard.getVisibility() != View.VISIBLE) {
            networkAlertCard.setVisibility(View.VISIBLE);
            networkAlertCard.setAlpha(0f);

            float screenWidth = getResources().getDisplayMetrics().widthPixels;
            networkAlertCard.setTranslationX(-screenWidth);

            networkAlertCard.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(340)
                    .setInterpolator(new OvershootInterpolator(1.3f))
                    .start();
        }
    }

    private void animateNetworkAlertOut() {
        if (networkAlertCard == null) return;
        if (networkAlertCard.getVisibility() == View.VISIBLE) {
            float screenWidth = getResources().getDisplayMetrics().widthPixels;

            networkAlertCard.animate()
                    .alpha(0f)
                    .translationX(screenWidth)
                    .setDuration(280)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        if (networkAlertCard != null) {
                            networkAlertCard.setVisibility(View.GONE);
                            networkAlertCard.setTranslationX(0f);
                        }
                    })
                    .start();
        }
    }

    private void retriggerNetworkAlertIn() {
        if (networkAlertCard == null) return;
        final float screenWidth = getResources().getDisplayMetrics().widthPixels;
        networkAlertCard.animate()
                .translationX(-screenWidth)
                .setDuration(150)
                .withEndAction(() -> {
                    if (networkAlertCard != null) {
                        networkAlertCard.setTranslationX(-screenWidth);
                        networkAlertCard.animate()
                                .translationX(0f)
                                .alpha(1f)
                                .setDuration(320)
                                .setInterpolator(new OvershootInterpolator(1.3f))
                                .start();
                    }
                })
                .start();
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNet = connectivityManager.getActiveNetwork();
                if (activeNet == null) return false;
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNet);
                return capabilities != null &&
                        (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                         capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
            } else {
                android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void showLogoutDialog() {
        PremiumAlertDialog.showConfirmation(this,
                "Log Out",
                "Are you sure you want to log out of Stone Mart? You will need your credentials to sign in again.",
                "Log Out",
                "Cancel",
                this::performLogout);
    }

    private void performLogout() {
        userStore.clearSession();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, EmployeeLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connectivityManager != null && networkCallback != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                }
            } catch (Exception ignored) {}
        }
        if (connectivityReceiver != null) {
            try {
                unregisterReceiver(connectivityReceiver);
            } catch (Exception ignored) {}
            connectivityReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkInitialNetworkState();
    }

    @Override
    protected void onDestroy() {
        if (hideNetworkAlertRunnable != null) {
            networkAlertHandler.removeCallbacks(hideNetworkAlertRunnable);
        }
        super.onDestroy();
    }
}