package com.app.stonemart;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.stonemart.core.Resource;
import com.app.stonemart.viewmodel.CheckInCheckOutViewModel;
import com.app.stonemart.widget.GeofenceMapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CheckInCheckOutFragment - MVVM View for RTK Geofence attendance punching.
 * Observes CheckInCheckOutViewModel for geofence state, check-in status, and punch operations.
 */
public class CheckInCheckOutFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 101;

    private CheckInCheckOutViewModel viewModel;

    private TextView tvEmpNameClock;
    private TextView tvBadgeStatus;
    private TextView tvStatus;
    private TextView tvGeofenceStatus;
    private TextView tvLiveClock;
    private TextView tvElapsedToday;
    private Button btnAction;
    private GeofenceMapView geofenceMapView;

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (tvLiveClock != null) {
                String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());
                tvLiveClock.setText(currentTime);
            }
            timeHandler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin_checkout, container, false);
        viewModel = new ViewModelProvider(this).get(CheckInCheckOutViewModel.class);

        initViews(view);
        setupObservers();
        verifyGeofenceLocation();
        timeHandler.post(timeRunnable);

        btnAction.setOnClickListener(v -> {
            Boolean inside = viewModel.isInsideGeofence.getValue();
            if (inside != null && !inside) {
                Toast.makeText(requireContext(), "Geofence Verification Required! You are within ±1.2m Accuracy.",
                        Toast.LENGTH_SHORT).show();
            }
            // Launch Camera for Mandatory Selfie Verification
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        });

        return view;
    }

    private void initViews(View view) {
        tvEmpNameClock = view.findViewById(R.id.tvEmpNameClock);
        tvBadgeStatus = view.findViewById(R.id.tvBadgeStatus);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvGeofenceStatus = view.findViewById(R.id.tvGeofenceStatus);
        tvLiveClock = view.findViewById(R.id.tvLiveClock);
        tvElapsedToday = view.findViewById(R.id.tvElapsedToday);
        btnAction = view.findViewById(R.id.btnAction);
        geofenceMapView = view.findViewById(R.id.geofenceMapView);

        if (tvEmpNameClock != null) {
            String name = viewModel.getEmpName();
            if (name != null && !name.isEmpty()) {
                tvEmpNameClock.setText(name);
            }
        }
    }

    private void setupObservers() {
        viewModel.isInsideGeofence.observe(getViewLifecycleOwner(), inside -> {
            if (inside == null) return;
            if (geofenceMapView != null) {
                geofenceMapView.setInsideStatus(inside);
            }
            if (inside) {
                if (tvGeofenceStatus != null) tvGeofenceStatus.setText("±1.2m Accuracy");
                if (tvBadgeStatus != null) tvBadgeStatus.setText("IN RANGE");
            } else {
                if (tvGeofenceStatus != null) tvGeofenceStatus.setText("⚠ Outside Yard");
                if (tvBadgeStatus != null) tvBadgeStatus.setText("OUT OF RANGE");
            }
        });

        viewModel.isCheckedIn.observe(getViewLifecycleOwner(), checkedIn -> {
            if (checkedIn == null) return;
            if (checkedIn) {
                if (tvStatus != null) {
                    tvStatus.setText("✓ PUNCH IN COMPLETED • " + viewModel.getCheckInTime());
                }
                if (btnAction != null) {
                    btnAction.setText("➔ PUNCH OUT / END SHIFT 🔒");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB"))); // Azure Blue
                }
                if (tvElapsedToday != null) {
                    tvElapsedToday.setText("⏱ Elapsed Today: 3h 44m");
                }
            } else {
                if (tvStatus != null) {
                    tvStatus.setText("● NOT CHECKED IN • Ready for Shift");
                }
                if (btnAction != null) {
                    btnAction.setText("➔ PUNCH IN / START SHIFT 🔒");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981"))); // Emerald Green
                }
                if (tvElapsedToday != null) {
                    tvElapsedToday.setText("⏱ Elapsed Today: 0h 00m");
                }
            }
        });

        viewModel.punchResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.getStatus() == Resource.Status.LOADING) {
                Toast.makeText(requireContext(), "Syncing punch with cPanel...", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), resource.getData(), Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyGeofenceLocation() {
        UserLocalStore userStore = viewModel.getUserStore();
        LocationHelper.getCurrentLocation(requireContext(), userStore.getStoreLat(), userStore.getStoreLng(),
                userStore.getRadiusMeters(),
                new LocationHelper.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, float distanceMeters,
                                                    boolean inside) {
                        viewModel.updateLocation(latitude, longitude, distanceMeters, inside);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (tvGeofenceStatus != null) tvGeofenceStatus.setText("GPS RTK: Active");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(timeRunnable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            viewModel.submitPunch();
        }
    }
}
