package com.app.stonemart.data.model;

import org.json.JSONObject;

/**
 * Leave - Typed model representing an employee leave request.
 */
public class Leave {

    private final int id;
    private final int staffId;
    private final int departmentId;
    private final int leaveTypeId;
    private final String leaveCode;
    private final String requestType; // FULL_DAY, HALF_DAY
    private final String fromDate;
    private final String toDate;
    private final String halfDaySession; // AM, PM
    private final double requestedDays;
    private final String reason;
    private final int headEmployeeId;
    private final String status; // PENDING_HEAD, APPROVED, REJECTED
    private final String sourceType;

    public Leave(int id, int staffId, int departmentId, int leaveTypeId, String leaveCode,
                 String requestType, String fromDate, String toDate, String halfDaySession,
                 double requestedDays, String reason, int headEmployeeId, String status, String sourceType) {
        this.id = id;
        this.staffId = staffId;
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
    }

    public static Leave fromJson(JSONObject json) {
        if (json == null) return null;
        return new Leave(
                json.optInt("id", 0),
                json.optInt("employee_id", json.optInt("staffid", 38)),
                json.optInt("department_id", 12),
                json.optInt("leave_type_id", 1),
                json.optString("leave_code", "CL"),
                json.optString("request_type", "FULL_DAY"),
                json.optString("from_date", ""),
                json.optString("to_date", ""),
                json.optString("half_day_session", null),
                json.optDouble("requested_days", 1.0),
                json.optString("reason", ""),
                json.optInt("head_employee_id", 3),
                json.optString("status", "PENDING_HEAD"),
                json.optString("source_type", "MOBILE_APP")
        );
    }

    public int getId() { return id; }
    public int getStaffId() { return staffId; }
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
}
