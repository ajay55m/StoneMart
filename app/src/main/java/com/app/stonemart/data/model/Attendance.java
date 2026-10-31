package com.app.stonemart.data.model;

import org.json.JSONObject;

/**
 * Attendance - Typed model representing a daily attendance record.
 */
public class Attendance {

    private final String date;
    private final String code; // P (Present), A (Absent), L (Leave)
    private final String inTime;
    private final String outTime;
    private final String netHours;
    private final String locationName;
    private final boolean verified;

    public Attendance(String date, String code, String inTime, String outTime,
                      String netHours, String locationName, boolean verified) {
        this.date = date;
        this.code = code;
        this.inTime = inTime;
        this.outTime = outTime;
        this.netHours = netHours;
        this.locationName = locationName;
        this.verified = verified;
    }

    public static Attendance fromJson(JSONObject obj) {
        if (obj == null) return null;
        String date = obj.optString("date", "");
        String code = obj.optString("code", "P");
        String inTime = obj.optString("in_time", "--:--");
        String outTime = obj.optString("out_time", "--:--");
        String netHours = obj.optString("net_hours", "Shift Logged");
        String loc = obj.optString("location", "Yard #4 Verified");
        boolean verified = obj.optBoolean("verified", true);
        return new Attendance(date, code, inTime, outTime, netHours, loc, verified);
    }

    public String getDate() { return date; }
    public String getCode() { return code; }
    public String getInTime() { return inTime; }
    public String getOutTime() { return outTime; }
    public String getNetHours() { return netHours; }
    public String getLocationName() { return locationName; }
    public boolean isVerified() { return verified; }
    public boolean isPresent() { return "P".equalsIgnoreCase(code); }
    public boolean isAbsent() { return "A".equalsIgnoreCase(code); }
}
