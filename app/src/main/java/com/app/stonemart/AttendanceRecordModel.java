package com.app.stonemart;

public class AttendanceRecordModel {
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private String status;
    private String netDuration;
    private String locationName;
    private String verificationTag;
    private boolean isPayrollSynced;

    public AttendanceRecordModel(String date, String checkInTime, String checkOutTime, String status,
            String netDuration, String locationName, String verificationTag, boolean isPayrollSynced) {
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.netDuration = netDuration;
        this.locationName = locationName;
        this.verificationTag = verificationTag;
        this.isPayrollSynced = isPayrollSynced;
    }

    public String getDate() {
        return date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public String getNetDuration() {
        return netDuration;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getVerificationTag() {
        return verificationTag;
    }

    public boolean isPayrollSynced() {
        return isPayrollSynced;
    }
}