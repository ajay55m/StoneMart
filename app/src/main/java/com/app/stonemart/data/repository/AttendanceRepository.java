package com.app.stonemart.data.repository;

import android.content.Context;

import com.app.stonemart.ApiClient;
import com.app.stonemart.ApiConfig;
import com.app.stonemart.UserLocalStore;
import com.app.stonemart.data.model.LocationMaster;
import com.app.stonemart.utils.Constants;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * AttendanceRepository - Single source of truth for attendance records,
 * monthly detail data, calendar metrics, caching, location master fetching, and punch check-in/out.
 */
public class AttendanceRepository {

    public interface AttendanceDataCallback {
        void onDataLoaded(JSONObject jsonResponse, boolean isFromCache);
        void onError(String errorMessage);
    }

    public interface PunchCallback {
        void onSuccess(String message, String timestamp, String action);
        void onFallbackSaved(String message, String timestamp, String action);
    }

    public interface LocationCallback {
        void onLoaded(LocationMaster location);
        void onError(String error);
    }

    private final UserLocalStore userStore;

    public AttendanceRepository(Context context) {
        this.userStore = new UserLocalStore(context);
    }

    public UserLocalStore getUserStore() {
        return userStore;
    }

    /**
     * Fetch Office Location Master from server (get_location.php)
     */
    public void fetchLocation(int locationId, LocationCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("location_id", String.valueOf(locationId));

        ApiClient.postRequest(Constants.GET_LOCATION_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response != null && ("success".equalsIgnoreCase(response.optString("status")) || response.optBoolean("success", false))) {
                        JSONObject d = response.optJSONObject("data");
                        if (d != null) {
                            LocationMaster loc = new LocationMaster();
                            loc.id = d.optInt("id", locationId);
                            loc.locationName = d.optString("location_name", "Office Location");
                            loc.latitude = d.optDouble("latitude", 0.0);
                            loc.longitude = d.optDouble("longitude", 0.0);
                            loc.maxLat = d.optDouble("max_lat", 0.0);
                            loc.maxLong = d.optDouble("max_long", 0.0);
                            if (callback != null) {
                                callback.onLoaded(loc);
                            }
                            return;
                        }
                    }
                    String msg = response != null ? response.optString("message", "Unable to fetch location details") : "Empty response";
                    if (callback != null) {
                        callback.onError(msg);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to parse location data");
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Stale-While-Revalidate: Returns cached data immediately if available,
     * then executes network fetch and returns fresh data.
     */
    public void getMonthlyAttendance(int year, int month, AttendanceDataCallback callback) {
        // 1. Instant Cache Check (<2ms)
        String cached = userStore.getCachedEmployeeDetail(year, month);
        if (cached != null && !cached.trim().isEmpty()) {
            try {
                JSONObject cachedObj = new JSONObject(cached);
                if (callback != null) {
                    callback.onDataLoaded(cachedObj, true);
                }
            } catch (Exception ignored) {}
        }

        // 2. Network Sync
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String fromStr = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        String toStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, maxDays);

        Map<String, String> params = new HashMap<>();
        int staffId = userStore.getStaffId();
        params.put("staffid", String.valueOf(staffId > 0 ? staffId : 38));
        params.put("from", fromStr);
        params.put("to", toStr);

        ApiClient.postRequest(ApiConfig.EMPLOYEE_DETAIL_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                userStore.cacheEmployeeDetail(year, month, response.toString());
                if (callback != null) {
                    callback.onDataLoaded(response, false);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * Submit Check-In or Check-Out Punch with dynamic location_id and location name
     */
    public void submitPunch(String action, String currentTime, double latitude, double longitude,
                            int locationId, String locationName, PunchCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("emp_id", userStore.getEmpId());
        params.put("staffid", String.valueOf(userStore.getStaffId()));
        params.put("action", action);
        params.put("time", currentTime);
        params.put("store_name", locationName != null && !locationName.isEmpty() ? locationName : userStore.getStoreName());
        params.put("location_id", String.valueOf(locationId));
        params.put("lat", String.valueOf(latitude));
        params.put("lng", String.valueOf(longitude));

        ApiClient.postRequest(ApiConfig.PUNCH_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                String serverMsg = response.optString("message", "Punch recorded successfully");
                if ("CHECK_IN".equals(action)) {
                    userStore.savePunchInState(currentTime, locationName != null ? locationName : userStore.getStoreName());
                } else {
                    userStore.savePunchOutState(currentTime);
                }
                if (callback != null) {
                    callback.onSuccess(serverMsg, currentTime, action);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Offline fallback save
                if ("CHECK_IN".equals(action)) {
                    userStore.savePunchInState(currentTime, locationName != null ? locationName : userStore.getStoreName());
                } else {
                    userStore.savePunchOutState(currentTime);
                }
                if (callback != null) {
                    callback.onFallbackSaved("Saved locally (" + errorMessage + ")", currentTime, action);
                }
            }
        });
    }

    /**
     * Overloaded submitPunch defaulting to assigned store details
     */
    public void submitPunch(String action, String currentTime, double latitude, double longitude, PunchCallback callback) {
        submitPunch(action, currentTime, latitude, longitude, Constants.TEST_LOCATION_ID, userStore.getStoreName(), callback);
    }
}
