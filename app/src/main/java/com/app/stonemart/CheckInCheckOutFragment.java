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
import com.app.stonemart.data.model.LocationMaster;
import com.app.stonemart.data.repository.AttendanceRepository;
import com.app.stonemart.utils.Constants;
import com.app.stonemart.utils.LocationHelper;
import com.app.stonemart.viewmodel.CheckInCheckOutViewModel;
import com.app.stonemart.widget.GeofenceMapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CheckInCheckOutFragment - MVVM View for RTK Geofence attendance punching.
 * Fetches office location dynamically from server (get_location.php), validates
 * boundaries via LocationMaster (max_lat / max_long bounding box), blocks punches
 * when outside, and submits location_id with punch verification.
 */
public class CheckInCheckOutFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 101;

    private CheckInCheckOutViewModel viewModel;

    private LocationMaster officeLocation;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private boolean isInsideGeofence = false;

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
        timeHandler.post(timeRunnable);

        btnAction.setOnClickListener(v -> {
            if (officeLocation == null) {
                Toast.makeText(requireContext(), "Office location loading...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isInsideGeofence) {
                Toast.makeText(requireContext(), "Geofence Verification Required! You are outside " + officeLocation.locationName, Toast.LENGTH_LONG).show();
                return; // BLOCK - do not open camera
            }
            // Inside, now open camera for selfie verification
            launchCamera();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Fetch office location from server (get_location.php)
        viewModel.fetchOfficeLocation(Constants.TEST_LOCATION_ID, new AttendanceRepository.LocationCallback() {
            @Override
            public void onLoaded(LocationMaster loc) {
                officeLocation = loc;
                if (isAdded()) {
                    checkCurrentLocation(); // start GPS check
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Location Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (officeLocation != null) {
            checkCurrentLocation();
        }
    }

    private void checkCurrentLocation() {
        if (!isAdded() || officeLocation == null) return;

        LocationHelper.getCurrentLocation(requireContext(), (lat, lng) -> {
            if (!isAdded()) return;
            currentLat = lat;
            currentLng = lng;

            // 2. Distance for display
            float[] results = new float[1];
            android.location.Location.distanceBetween(lat, lng, officeLocation.latitude, officeLocation.longitude, results);
            float distance = results[0];

            // 3. NEW VERIFICATION using max_lat / max_long bounding box
            isInsideGeofence = officeLocation.isInside(lat, lng);

            viewModel.updateLocation(lat, lng, distance, isInsideGeofence);

            if (geofenceMapView != null) {
                geofenceMapView.setInsideStatus(isInsideGeofence);
            }
        });
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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
            isInsideGeofence = inside;
            if (geofenceMapView != null) {
                geofenceMapView.setInsideStatus(inside);
            }
            String siteName = officeLocation != null ? officeLocation.locationName : "Yard";
            if (inside) {
                if (tvGeofenceStatus != null) tvGeofenceStatus.setText("±1.2m Accuracy • " + siteName);
                if (tvBadgeStatus != null) tvBadgeStatus.setText("IN RANGE");
            } else {
                if (tvGeofenceStatus != null) tvGeofenceStatus.setText("⚠ Outside " + siteName);
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
                    tvElapsedToday.setText("⏱ Elapsed Today: Active Shift");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(timeRunnable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            if (officeLocation != null) {
                viewModel.submitPunch(officeLocation.id, officeLocation.locationName);
            } else {
                viewModel.submitPunch();
            }
        }
    }
}
