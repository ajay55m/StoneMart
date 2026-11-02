package com.app.stonemart.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Clean, thread-safe session manager encapsulating SharedPreferences
 * for user login states, employee profile data, geofence parameters, and local attendance cache.
 */
public class SessionManager {

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserSession(String empId, String empName, String storeName,
                                 double storeLat, double storeLng, float radiusMeters,
                                 int staffId, String userRole, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_EMP_ID, empId);
        editor.putString(Constants.KEY_EMP_NAME, empName);
        editor.putString(Constants.KEY_STORE_NAME, storeName);
        editor.putFloat(Constants.KEY_STORE_LAT, (float) storeLat);
        editor.putFloat(Constants.KEY_STORE_LNG, (float) storeLng);
        editor.putFloat(Constants.KEY_RADIUS_METERS, radiusMeters);
        editor.putInt(Constants.KEY_STAFF_ID, staffId);
        editor.putString(Constants.KEY_USER_ROLE, userRole);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getEmpId() {
        return prefs.getString(Constants.KEY_EMP_ID, "EMP1001");
    }

    public String getEmpName() {
        return prefs.getString(Constants.KEY_EMP_NAME, "Employee User");
    }

    public String getStoreName() {
        return prefs.getString(Constants.KEY_STORE_NAME, Constants.DEFAULT_STORE_NAME);
    }

    public double getStoreLat() {
        return prefs.getFloat(Constants.KEY_STORE_LAT, (float) Constants.DEFAULT_LAT);
    }

    public double getStoreLng() {
        return prefs.getFloat(Constants.KEY_STORE_LNG, (float) Constants.DEFAULT_LNG);
    }

    public float getRadiusMeters() {
        return prefs.getFloat(Constants.KEY_RADIUS_METERS, Constants.DEFAULT_RADIUS_METERS);
    }

    public int getStaffId() {
        return prefs.getInt(Constants.KEY_STAFF_ID, 38);
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "employee");
    }

    public String getEmail() {
        return prefs.getString(Constants.KEY_EMAIL, "");
    }

    public void savePunchInState(String time, String location) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.KEY_IS_CHECKED_IN, true);
        editor.putString(Constants.KEY_CHECK_IN_TIME, time);
        editor.putString(Constants.KEY_CHECK_IN_LOC, location);
        editor.apply();
    }

    public void savePunchOutState(String time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.KEY_IS_CHECKED_IN, false);
        editor.putString(Constants.KEY_CHECK_OUT_TIME, time);
        editor.apply();
    }

    public boolean isCheckedIn() {
        return prefs.getBoolean(Constants.KEY_IS_CHECKED_IN, false);
    }

    public String getCheckInTime() {
        return prefs.getString(Constants.KEY_CHECK_IN_TIME, "--:--");
    }

    public String getCheckOutTime() {
        return prefs.getString(Constants.KEY_CHECK_OUT_TIME, "--:--");
    }

    public void cacheEmployeeDetail(int year, int month, String json) {
        prefs.edit().putString("cached_detail_" + year + "_" + month, json).apply();
    }

    public String getCachedEmployeeDetail(int year, int month) {
        return prefs.getString("cached_detail_" + year + "_" + month, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

