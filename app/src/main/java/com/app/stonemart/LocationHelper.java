package com.app.stonemart;

import android.content.Context;
import com.app.stonemart.utils.LocationHelper.LocationCallback;
import com.app.stonemart.utils.LocationHelper.SimpleLocationCallback;

/**
 * LocationHelper - Backward compatibility adapter for com.app.stonemart.utils.LocationHelper.
 */
public class LocationHelper {

    public interface LocationCallback extends com.app.stonemart.utils.LocationHelper.LocationCallback {}
    public interface SimpleLocationCallback extends com.app.stonemart.utils.LocationHelper.SimpleLocationCallback {}

    public static void getCurrentLocation(Context context, SimpleLocationCallback callback) {
        com.app.stonemart.utils.LocationHelper.getCurrentLocation(context, callback::onLocationReceived);
    }

    public static void getCurrentLocation(Context context, double targetLat, double targetLng, float maxDistanceMeters,
                                          LocationCallback callback) {
        com.app.stonemart.utils.LocationHelper.getCurrentLocation(context, targetLat, targetLng, maxDistanceMeters, callback);
    }
}
