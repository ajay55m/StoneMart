package com.app.stonemart;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaveRequestRecord {
    private final int id;
    private final int employeeId;
    private final int departmentId;
    private final int leaveTypeId;
    private final String leaveCode;
    private final String requestType;
    private final String fromDate;
    private final String toDate;
    private final String halfDaySession;
    private final double requestedDays;
    private final String reason;
    private final int headEmployeeId;
    private final String status;
    private final String sourceType;
    private final String availableBefore;

    public LeaveRequestRecord(int id, int employeeId, int departmentId, int leaveTypeId,
                              String leaveCode, String requestType, String fromDate,
                              String toDate, String halfDaySession, double requestedDays,
                              String reason, int headEmployeeId, String status,
                              String sourceType, String availableBefore) {
        this.id = id;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.leaveTypeId = leaveTypeId;
        this.leaveCode = leaveCode;
        this.requestType = requestType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.halfDaySession = halfDaySession;
        this.requestedDays = requestedDays;
        this.reason = reason;
        this.headEmployeeId = headEmployeeId;
        this.status = status;
        this.sourceType = sourceType;
        this.availableBefore = availableBefore;
    }

    public static LeaveRequestRecord fromJson(JSONObject json) {
        if (json == null) return null;
        int id = json.optInt("id", 0);
        int employeeId = json.optInt("employee_id", 0);
        int departmentId = json.optInt("department_id", 0);
        int leaveTypeId = json.optInt("leave_type_id", 0);
        String leaveCode = json.optString("leave_code", "CL");
        String requestType = json.optString("request_type", "FULL_DAY");
        String fromDate = json.optString("from_date", "");
        String toDate = json.optString("to_date", "");
        String halfDaySession = json.isNull("half_day_session") ? null : json.optString("half_day_session", null);
        double requestedDays = json.optDouble("requested_days", 1.0);
        String reason = json.optString("reason", "");
        int headEmployeeId = json.optInt("head_employee_id", 0);
        String status = json.optString("status", "PENDING_HEAD");
        String sourceType = json.optString("source_type", "MOBILE_APP");
        String availableBefore = json.isNull("available_before") ? "N/A" : String.valueOf(json.opt("available_before"));

        return new LeaveRequestRecord(id, employeeId, departmentId, leaveTypeId, leaveCode,
                requestType, fromDate, toDate, halfDaySession, requestedDays,
                reason, headEmployeeId, status, sourceType, availableBefore);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("employee_id", employeeId);
            json.put("department_id", departmentId);
            json.put("leave_type_id", leaveTypeId);
            json.put("leave_code", leaveCode);
            json.put("request_type", requestType);
            json.put("from_date", fromDate);
            json.put("to_date", toDate);
            json.put("half_day_session", halfDaySession);
            json.put("requested_days", requestedDays);
            json.put("reason", reason);
            json.put("head_employee_id", headEmployeeId);
            json.put("status", status);
            json.put("source_type", sourceType);
            json.put("available_before", availableBefore);
        } catch (JSONException ignored) {}
        return json;
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public int getDepartmentId() { return departmentId; }
    public int getLeaveTypeId() { return leaveTypeId; }
    public String getLeaveCode() { return leaveCode; }
    public String getRequestType() { return requestType; }
    public String getFromDate() { return fromDate; }
    public String getToDate() { return toDate; }
    public String getHalfDaySession() { return halfDaySession; }
    public double getRequestedDays() { return requestedDays; }
    public String getReason() { return reason; }
    public int getHeadEmployeeId() { return headEmployeeId; }
    public String getStatus() { return status; }
    public String getSourceType() { return sourceType; }
    public String getAvailableBefore() { return availableBefore; }
}
