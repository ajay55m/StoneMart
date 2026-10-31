package com.app.stonemart;

import android.content.Context;
import com.app.stonemart.utils.SessionManager;

/**
 * UserLocalStore - Backward compatibility adapter for SessionManager.
 */
public class UserLocalStore {

    private final SessionManager sessionManager;

    public UserLocalStore(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void storeUserSession(String empId, String empName, String storeName, double storeLat, double storeLng,
            float radiusMeters) {
        sessionManager.storeUserSession(empId, empName, storeName, storeLat, storeLng, radiusMeters, 38, "employee", "");
    }

    public void storeUserSession(String empId, String empName, String storeName, double storeLat, double storeLng,
            float radiusMeters, int staffId, String userRole, String email) {
        sessionManager.storeUserSession(empId, empName, storeName, storeLat, storeLng, radiusMeters, staffId, userRole, email);
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public String getEmpId() {
        return sessionManager.getEmpId();
    }

    public String getEmpName() {
        return sessionManager.getEmpName();
    }

    public String getStoreName() {
        return sessionManager.getStoreName();
    }

    public double getStoreLat() {
        return sessionManager.getStoreLat();
    }

    public double getStoreLng() {
        return sessionManager.getStoreLng();
    }

    public float getRadiusMeters() {
        return sessionManager.getRadiusMeters();
    }

    public void savePunchInState(String time, String location) {
        sessionManager.savePunchInState(time, location);
    }

    public void savePunchOutState(String time) {
        sessionManager.savePunchOutState(time);
    }

    public boolean isCheckedIn() {
        return sessionManager.isCheckedIn();
    }

    public String getCheckInTime() {
        return sessionManager.getCheckInTime();
    }

    public String getCheckOutTime() {
        return sessionManager.getCheckOutTime();
    }

    public int getStaffId() {
        return sessionManager.getStaffId();
    }

    public String getUserRole() {
        return sessionManager.getUserRole();
    }

    public String getEmail() {
        return sessionManager.getEmail();
    }

    public void cacheEmployeeDetail(int year, int month, String json) {
        sessionManager.cacheEmployeeDetail(year, month, json);
    }

    public String getCachedEmployeeDetail(int year, int month) {
        return sessionManager.getCachedEmployeeDetail(year, month);
    }

    public void clearSession() {
        sessionManager.clearSession();
    }
}
