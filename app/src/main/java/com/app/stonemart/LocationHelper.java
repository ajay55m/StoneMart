package com.app.stonemart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, float distanceToStoreMeters,
                boolean isInsideGeofence);

        void onError(String errorMessage);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, double targetLat, double targetLng, float maxDistanceMeters,
            LocationCallback callback) {
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLat, targetLng,
                        results);
                float distanceMeters = results[0];
                boolean isInside = distanceMeters <= maxDistanceMeters;
                callback.onLocationReceived(location.getLatitude(), location.getLongitude(), distanceMeters, isInside);
            } else {
                callback.onError("Unable to fetch GPS position. Ensure Location is turned on.");
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}