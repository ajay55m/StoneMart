package com.app.stonemart.data.model;

public class LocationMaster {
    public int id;
    public String locationName;
    public double latitude, longitude, maxLat, maxLong;

    public double getMinLat() { return (2 * latitude) - maxLat; }
    public double getMinLong() { return (2 * longitude) - maxLong; }

    public boolean isInside(double empLat, double empLng) {
        return empLat >= getMinLat() && empLat <= maxLat
            && empLng >= getMinLong() && empLng <= maxLong;
    }
}
