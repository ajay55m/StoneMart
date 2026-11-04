package com.app.stonemart.data.model;

import android.location.Location;

/**
 * LocationMaster - Represents a geo-located work site/yard/office boundary
 * with bounding box coordinates and radius tolerances for RTK attendance punching.
 */
public class LocationMaster {
    public int id;
    public String locationName;
    public double latitude;
    public double longitude;
    public double maxLat;
    public double maxLong;
    public float radiusMeters = 350.0f;

    public LocationMaster() {
    }

    public LocationMaster(int id, String locationName, double latitude, double longitude, double maxLat, double maxLong) {
        this(id, locationName, latitude, longitude, maxLat, maxLong, 350.0f);
    }

    public LocationMaster(int id, String locationName, double latitude, double longitude, double maxLat, double maxLong, float radiusMeters) {
        this.id = id;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxLat = maxLat;
        this.maxLong = maxLong;
        this.radiusMeters = radiusMeters > 0 ? radiusMeters : 350.0f;
    }

    public double getMinLat() {
        return (2 * latitude) - maxLat;
    }

    public double getMinLong() {
        return (2 * longitude) - maxLong;
    }

    /**
     * Verifies if employee GPS position falls within the defined geofence boundary.
     * Evaluates both bounding-box boundaries and radius distance tolerance.
     */
    public boolean isInside(double empLat, double empLng) {
        if (empLat == 0.0 || empLng == 0.0 || latitude == 0.0 || longitude == 0.0) {
            return false;
        }

        // 1. Distance check against radius tolerance (e.g. 350-500m)
        float[] results = new float[1];
        Location.distanceBetween(empLat, empLng, latitude, longitude, results);
        float distance = results[0];
        if (distance <= radiusMeters) {
            return true;
        }

        // 2. Bounding box verification (max_lat / max_long)
        if (maxLat > latitude && maxLong > longitude) {
            double minLat = getMinLat();
            double minLong = getMinLong();
            if (empLat >= minLat && empLat <= maxLat && empLng >= minLong && empLng <= maxLong) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return locationName != null ? locationName : "Location #" + id;
    }
}
