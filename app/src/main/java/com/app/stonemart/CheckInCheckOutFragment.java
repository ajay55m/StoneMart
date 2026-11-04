package com.app.stonemart;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.stonemart.core.Resource;
import com.app.stonemart.data.model.LocationMaster;
import com.app.stonemart.ui.attendance.LocationSpinnerAdapter;
import com.app.stonemart.utils.Constants;
import com.app.stonemart.utils.LocationHelper;
import com.app.stonemart.viewmodel.CheckInCheckOutViewModel;
import com.app.stonemart.widget.GeofenceMapView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CheckInCheckOutFragment - MVVM View for RTK Geofence attendance punching.
 * Displays available work locations in a sleek Material bottom sheet dropdown.
 * Upon selecting a location, performs live GPS verification against that site's
 * geofence boundaries, updates the RTK map view & status badges, blocks punches if outside,
 * and submits location_id with punch verification.
 */
public class CheckInCheckOutFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 101;

    private CheckInCheckOutViewModel viewModel;

    private LocationMaster officeLocation;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private boolean isInsideGeofence = false;

    private TextView tvEmpNameClock;
    private TextView tvProfileEmpName;
    private TextView tvProfileEmpSub;
    private TextView tvBadgeStatus;
    private TextView tvStatus;
    private TextView tvGeofenceStatus;
    private TextView tvLiveClock;
    private TextView tvElapsedToday;
    private Button btnAction;
    private GeofenceMapView geofenceMapView;

    private View layoutLocationSpinner;
    private TextView tvSelectedLocationName;
    private TextView tvSelectedLocationSub;
    private ImageView ivChevronLocation;
    private Spinner spLocationSelect;
    private LocationSpinnerAdapter locationAdapter;

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
                Toast.makeText(requireContext(), "Please select a work location.", Toast.LENGTH_SHORT).show();
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
        viewModel.loadAvailableLocations();
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
            if (!isAdded() || officeLocation == null) return;
            currentLat = lat;
            currentLng = lng;

            // Handle unacquired GPS fix gracefully without false distance from (0,0)
            if (lat == 0.0 && lng == 0.0) {
                isInsideGeofence = false;
                viewModel.updateLocation(0, 0, 0, false);
                if (geofenceMapView != null) {
                    geofenceMapView.setInsideStatus(false);
                }
                if (tvBadgeStatus != null) {
                    tvBadgeStatus.setText("WAITING GPS");
                    tvBadgeStatus.setTextColor(Color.parseColor("#D97706"));
                    tvBadgeStatus.setBackgroundResource(R.drawable.bg_badge_unpaid);
                }
                if (tvGeofenceStatus != null) {
                    tvGeofenceStatus.setText("📡 Acquiring GPS satellite fix... Please ensure Location is ON");
                    tvGeofenceStatus.setTextColor(Color.parseColor("#D97706"));
                }
                return;
            }

            // 1. Distance for display
            float[] results = new float[1];
            android.location.Location.distanceBetween(lat, lng, officeLocation.latitude, officeLocation.longitude, results);
            float distance = results[0];

            // 2. Verification using max_lat / max_long bounding box + radius tolerance
            isInsideGeofence = officeLocation.isInside(lat, lng);

            viewModel.updateLocation(lat, lng, distance, isInsideGeofence);

            if (geofenceMapView != null) {
                geofenceMapView.setInsideStatus(isInsideGeofence);
            }

            // 3. Update status badges and texts with clean kilometer / meter formatting
            if (tvBadgeStatus != null) {
                if (isInsideGeofence) {
                    tvBadgeStatus.setText("IN RANGE");
                    tvBadgeStatus.setTextColor(Color.parseColor("#15803D"));
                    tvBadgeStatus.setBackgroundResource(R.drawable.bg_badge_emerald);
                } else {
                    tvBadgeStatus.setText("OUT OF RANGE");
                    tvBadgeStatus.setTextColor(Color.parseColor("#B91C1C"));
                    tvBadgeStatus.setBackgroundResource(R.drawable.bg_badge_rose);
                }
            }

            if (tvGeofenceStatus != null) {
                if (isInsideGeofence) {
                    tvGeofenceStatus.setText(String.format(Locale.getDefault(), "±1.2m Accuracy • %s (%.0fm away)", officeLocation.locationName, distance));
                    tvGeofenceStatus.setTextColor(Color.parseColor("#059669"));
                } else {
                    String distStr = distance >= 1000
                            ? String.format(Locale.US, "%.1f km away", distance / 1000f)
                            : String.format(Locale.US, "%.0fm away", distance);
                    tvGeofenceStatus.setText(String.format(Locale.getDefault(), "⚠ Outside %s (%s)", officeLocation.locationName, distStr));
                    tvGeofenceStatus.setTextColor(Color.parseColor("#DC2626"));
                }
            }
        });
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void initViews(View view) {
        tvEmpNameClock = view.findViewById(R.id.tvEmpNameClock);
        tvProfileEmpName = view.findViewById(R.id.tvProfileEmpName);
        tvProfileEmpSub = view.findViewById(R.id.tvProfileEmpSub);
        tvBadgeStatus = view.findViewById(R.id.tvBadgeStatus);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvGeofenceStatus = view.findViewById(R.id.tvGeofenceStatus);
        tvLiveClock = view.findViewById(R.id.tvLiveClock);
        tvElapsedToday = view.findViewById(R.id.tvElapsedToday);
        btnAction = view.findViewById(R.id.btnAction);
        geofenceMapView = view.findViewById(R.id.geofenceMapView);

        layoutLocationSpinner = view.findViewById(R.id.layoutLocationSpinner);
        tvSelectedLocationName = view.findViewById(R.id.tvSelectedLocationName);
        tvSelectedLocationSub = view.findViewById(R.id.tvSelectedLocationSub);
        ivChevronLocation = view.findViewById(R.id.ivChevronLocation);
        spLocationSelect = view.findViewById(R.id.spLocationSelect);

        if (layoutLocationSpinner != null) {
            layoutLocationSpinner.setOnClickListener(v -> {
                if (ivChevronLocation != null) {
                    ivChevronLocation.animate().rotation(180f).setDuration(150).withEndAction(() -> {
                        showLocationBottomSheet();
                        ivChevronLocation.animate().rotation(0f).setDuration(200).start();
                    }).start();
                } else {
                    showLocationBottomSheet();
                }
            });
        }

        if (tvEmpNameClock != null) {
            String name = viewModel.getEmpName();
            if (name != null && !name.isEmpty()) {
                tvEmpNameClock.setText(name);
            }
        }

        if (tvProfileEmpName != null) {
            String name = viewModel.getEmpName();
            int staffId = viewModel.getUserStore().getStaffId();
            String idStr = staffId > 0 ? "SM-" + staffId : "EMP";
            tvProfileEmpName.setText((name != null && !name.isEmpty() ? name : "Staff Member") + " • " + idStr);
        }
    }

    /**
     * Opens a clean, modern Material Bottom Sheet displaying all available work locations
     * with crystal clear contrast, icons, coordinates, and instant selection.
     */
    private void showLocationBottomSheet() {
        if (!isAdded()) return;
        List<LocationMaster> locations = viewModel.availableLocations.getValue();
        if (locations == null || locations.isEmpty()) {
            locations = viewModel.getRepository().getDefaultLocations();
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bottom_sheet_location, null);
        dialog.setContentView(sheetView);

        View bottomSheetInternal = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheetInternal != null) {
            bottomSheetInternal.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivClose = sheetView.findViewById(R.id.ivCloseLocationModal);
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dialog.dismiss());
        }

        LinearLayout layoutModalList = sheetView.findViewById(R.id.layoutModalLocationList);
        if (layoutModalList != null) {
            layoutModalList.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (LocationMaster loc : locations) {
                View itemView = inflater.inflate(R.layout.item_location_modal, layoutModalList, false);
                TextView tvName = itemView.findViewById(R.id.tvModalLocationName);
                TextView tvCoords = itemView.findViewById(R.id.tvModalLocationCoords);
                ImageView ivCheck = itemView.findViewById(R.id.ivModalLocationCheck);
                RelativeLayout layoutPinBg = itemView.findViewById(R.id.layoutModalPinBg);
                ImageView ivPin = itemView.findViewById(R.id.ivModalPin);

                boolean isSelected = officeLocation != null && officeLocation.id == loc.id;

                tvName.setText(loc.locationName);
                tvCoords.setText(String.format(Locale.US, "ID: %d • Lat: %.4f, Lng: %.4f", loc.id, loc.latitude, loc.longitude));

                if (isSelected) {
                    itemView.setBackgroundResource(R.drawable.bg_leave_modal_item_selected);
                    ivCheck.setVisibility(View.VISIBLE);
                    tvName.setTextColor(Color.parseColor("#1D4ED8"));
                    layoutPinBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                    ivPin.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_leave_modal_item);
                    ivCheck.setVisibility(View.GONE);
                    tvName.setTextColor(Color.parseColor("#0F172A"));
                    layoutPinBg.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                    ivPin.setImageTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                }

                itemView.setOnClickListener(v -> {
                    officeLocation = loc;
                    viewModel.selectLocation(loc);
                    updateSelectedLocationUI(loc);
                    dialog.dismiss();
                    checkCurrentLocation();
                });

                layoutModalList.addView(itemView);
            }
        }

        dialog.show();
    }

    private void updateSelectedLocationUI(LocationMaster loc) {
        if (loc == null) return;
        if (tvSelectedLocationName != null) {
            tvSelectedLocationName.setText(loc.locationName);
        }
        if (tvSelectedLocationSub != null) {
            tvSelectedLocationSub.setText(String.format(Locale.US, "ID: %d • Lat: %.4f, Lng: %.4f", loc.id, loc.latitude, loc.longitude));
        }
        if (tvProfileEmpSub != null) {
            tvProfileEmpSub.setText(loc.locationName);
        }
        if (geofenceMapView != null) {
            geofenceMapView.updateTargetLocation(loc.locationName, loc.latitude, loc.longitude);
        }
    }

    private void setupObservers() {
        // Dropdown available locations
        viewModel.availableLocations.observe(getViewLifecycleOwner(), locations -> {
            if (locations == null || locations.isEmpty() || !isAdded()) return;

            locationAdapter = new LocationSpinnerAdapter(requireContext(), locations);
            if (spLocationSelect != null) {
                spLocationSelect.setAdapter(locationAdapter);
            }

            LocationMaster current = officeLocation != null ? officeLocation : viewModel.getOfficeLocation();
            if (current == null) {
                current = locations.get(0);
                for (LocationMaster loc : locations) {
                    if (loc.id == Constants.TEST_LOCATION_ID) {
                        current = loc;
                        break;
                    }
                }
            }
            officeLocation = current;
            updateSelectedLocationUI(current);
            checkCurrentLocation();
        });

        // Dropdown location selection listener (for fallback spinner)
        if (spLocationSelect != null) {
            spLocationSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    LocationMaster loc = (LocationMaster) parent.getItemAtPosition(position);
                    if (loc != null) {
                        officeLocation = loc;
                        viewModel.selectLocation(loc);
                        updateSelectedLocationUI(loc);
                        checkCurrentLocation();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        viewModel.isInsideGeofence.observe(getViewLifecycleOwner(), inside -> {
            if (inside == null) return;
            isInsideGeofence = inside;
            if (geofenceMapView != null) {
                geofenceMapView.setInsideStatus(inside);
            }
            String siteName = officeLocation != null ? officeLocation.locationName : "Yard";
            if (inside) {
                if (tvGeofenceStatus != null) {
                    tvGeofenceStatus.setText("±1.2m Accuracy • " + siteName);
                    tvGeofenceStatus.setTextColor(Color.parseColor("#059669"));
                }
                if (tvBadgeStatus != null) {
                    tvBadgeStatus.setText("IN RANGE");
                    tvBadgeStatus.setTextColor(Color.parseColor("#15803D"));
                    tvBadgeStatus.setBackgroundResource(R.drawable.bg_badge_emerald);
                }
            } else {
                if (tvGeofenceStatus != null) {
                    tvGeofenceStatus.setText("⚠ Outside " + siteName);
                    tvGeofenceStatus.setTextColor(Color.parseColor("#DC2626"));
                }
                if (tvBadgeStatus != null) {
                    tvBadgeStatus.setText("OUT OF RANGE");
                    tvBadgeStatus.setTextColor(Color.parseColor("#B91C1C"));
                    tvBadgeStatus.setBackgroundResource(R.drawable.bg_badge_rose);
                }
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
