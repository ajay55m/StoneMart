package com.app.stonemart.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * LocationHelper - GPS RTK Geofence precision tracking utility.
 * Calculates Haversine distance between device GPS coordinate and target yard geofence.
 */
public class LocationHelper {

    public interface SimpleLocationCallback {
        void onLocationReceived(double latitude, double longitude);
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, float distanceToStoreMeters,
                                boolean isInsideGeofence);

        void onError(String errorMessage);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, SimpleLocationCallback callback) {
        if (context == null || callback == null) return;
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && (location.getLatitude() != 0.0 || location.getLongitude() != 0.0)) {
                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
            } else {
                // Active satellite/network location fix request
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(freshLoc -> {
                            if (freshLoc != null) {
                                callback.onLocationReceived(freshLoc.getLatitude(), freshLoc.getLongitude());
                            } else {
                                callback.onLocationReceived(0.0, 0.0);
                            }
                        })
                        .addOnFailureListener(e -> callback.onLocationReceived(0.0, 0.0));
            }
        }).addOnFailureListener(e -> {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(freshLoc -> {
                        if (freshLoc != null) {
                            callback.onLocationReceived(freshLoc.getLatitude(), freshLoc.getLongitude());
                        } else {
                            callback.onLocationReceived(0.0, 0.0);
                        }
                    })
                    .addOnFailureListener(err -> callback.onLocationReceived(0.0, 0.0));
        });
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, double targetLat, double targetLng, float maxDistanceMeters,
                                          LocationCallback callback) {
        if (context == null || callback == null) return;
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLat, targetLng, results);
                float distanceMeters = results[0];
                boolean isInside = distanceMeters <= maxDistanceMeters;
                callback.onLocationReceived(location.getLatitude(), location.getLongitude(), distanceMeters, isInside);
            } else {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(freshLoc -> {
                            if (freshLoc != null) {
                                float[] results = new float[1];
                                Location.distanceBetween(freshLoc.getLatitude(), freshLoc.getLongitude(), targetLat, targetLng, results);
                                float distanceMeters = results[0];
                                boolean isInside = distanceMeters <= maxDistanceMeters;
                                callback.onLocationReceived(freshLoc.getLatitude(), freshLoc.getLongitude(), distanceMeters, isInside);
                            } else {
                                callback.onError("Unable to fetch GPS position. Ensure Location is turned on.");
                            }
                        })
                        .addOnFailureListener(err -> callback.onError("Unable to fetch GPS position. Ensure Location is turned on."));
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
