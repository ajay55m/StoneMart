package com.app.stonemart;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {
    private static final String PREF_NAME = "StoneMartPrefs";
    private final SharedPreferences prefs;

    public UserLocalStore(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserSession(String empId, String empName, String storeName, double storeLat, double storeLng,
            float radiusMeters) {
        storeUserSession(empId, empName, storeName, storeLat, storeLng, radiusMeters, 0, "employee", "");
    }

    public void storeUserSession(String empId, String empName, String storeName, double storeLat, double storeLng,
            float radiusMeters, int staffId, String userRole, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("empId", empId);
        editor.putString("empName", empName);
        editor.putString("storeName", storeName);
        editor.putFloat("storeLat", (float) storeLat);
        editor.putFloat("storeLng", (float) storeLng);
        editor.putFloat("radiusMeters", radiusMeters);
        editor.putInt("staffId", staffId);
        editor.putString("userRole", userRole);
        editor.putString("email", email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public String getEmpId() {
        return prefs.getString("empId", "EMP1001");
    }

    public String getEmpName() {
        return prefs.getString("empName", "Employee User");
    }

    public String getStoreName() {
        return prefs.getString("storeName", "Stone Mart Main Branch");
    }

    public double getStoreLat() {
        return prefs.getFloat("storeLat", 26.9124f);
    }

    public double getStoreLng() {
        return prefs.getFloat("storeLng", 75.7873f);
    }

    public float getRadiusMeters() {
        return prefs.getFloat("radiusMeters", 200.0f);
    }

    public void savePunchInState(String time, String location) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckedIn", true);
        editor.putString("checkInTime", time);
        editor.putString("checkInLoc", location);
        editor.apply();
    }

    public void savePunchOutState(String time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckedIn", false);
        editor.putString("checkOutTime", time);
        editor.apply();
    }

    public boolean isCheckedIn() {
        return prefs.getBoolean("isCheckedIn", false);
    }

    public String getCheckInTime() {
        return prefs.getString("checkInTime", "--:--");
    }

    public String getCheckOutTime() {
        return prefs.getString("checkOutTime", "--:--");
    }

    public int getStaffId() {
        return prefs.getInt("staffId", 2);
    }

    public String getUserRole() {
        return prefs.getString("userRole", "employee");
    }

    public String getEmail() {
        return prefs.getString("email", "");
    }

    public void cacheEmployeeDetail(int year, int month, String jsonString) {
        prefs.edit().putString("cache_emp_detail_" + year + "_" + month, jsonString).apply();
    }

    public String getCachedEmployeeDetail(int year, int month) {
        return prefs.getString("cache_emp_detail_" + year + "_" + month, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
