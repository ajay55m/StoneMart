package com.app.stonemart.data.repository;

import android.content.Context;

import com.app.stonemart.ApiClient;
import com.app.stonemart.ApiConfig;
import com.app.stonemart.UserLocalStore;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * AttendanceRepository - Single source of truth for attendance records,
 * monthly detail data, calendar metrics, caching, and punch check-in/out.
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

    private final UserLocalStore userStore;

    public AttendanceRepository(Context context) {
        this.userStore = new UserLocalStore(context);
    }

    public UserLocalStore getUserStore() {
        return userStore;
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
     * Submit Check-In or Check-Out Punch
     */
    public void submitPunch(String action, String currentTime, double latitude, double longitude, PunchCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("emp_id", userStore.getEmpId());
        params.put("staffid", String.valueOf(userStore.getStaffId()));
        params.put("action", action);
        params.put("time", currentTime);
        params.put("store_name", userStore.getStoreName());
        params.put("lat", String.valueOf(latitude));
        params.put("lng", String.valueOf(longitude));

        ApiClient.postRequest(ApiConfig.PUNCH_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                String serverMsg = response.optString("message", "Punch recorded successfully");
                if ("CHECK_IN".equals(action)) {
                    userStore.savePunchInState(currentTime, userStore.getStoreName());
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
                    userStore.savePunchInState(currentTime, userStore.getStoreName());
                } else {
                    userStore.savePunchOutState(currentTime);
                }
                if (callback != null) {
                    callback.onFallbackSaved("Saved locally (" + errorMessage + ")", currentTime, action);
                }
            }
        });
    }
}

