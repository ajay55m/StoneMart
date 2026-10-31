package com.app.stonemart.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.stonemart.ApiClient;
import com.app.stonemart.ApiConfig;
import com.app.stonemart.LeaveRequestRecord;
import com.app.stonemart.LeaveTypeItem;
import com.app.stonemart.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LeaveRepository - Manages leave categories, entitlement calculations,
 * leave submission API, and local application persistence.
 */
public class LeaveRepository {

    public interface LeaveSubmitCallback {
        void onSuccess(LeaveRequestRecord record);
        void onFallback(LeaveRequestRecord fallbackRecord);
        void onError(String errorMessage);
    }

    private static final String PREF_RECENT_LEAVES = "StoneMart_Recent_Leaves";
    private static final String KEY_LEAVES_JSON = "saved_leaves_list";

    private final Context context;
    private final UserLocalStore userStore;

    public LeaveRepository(Context context) {
        this.context = context.getApplicationContext();
        this.userStore = new UserLocalStore(context);
    }

    public UserLocalStore getUserStore() {
        return userStore;
    }

    public List<LeaveTypeItem> getAvailableLeaveTypes() {
        List<LeaveTypeItem> list = new ArrayList<>();
        list.add(new LeaveTypeItem(3, "CL", "Casual Leave (CL)", "Paid Casual Leave", "PAID", true, true, true, "Paid casual leave for personal absence."));
        list.add(new LeaveTypeItem(2, "SL", "Sick / Medical Leave (SL)", "Medical & Doctor Consultation", "PAID", true, true, true, "Paid sick leave for medical and doctor visits."));
        list.add(new LeaveTypeItem(1, "EL", "Earned / Privilege Leave (EL)", "Monthly Accrued Leave", "PAID", true, true, true, "Accrued paid leave based on monthly tenure."));
        list.add(new LeaveTypeItem(6, "LOP", "Loss of Pay (LOP)", "Emergency Leave • Salary Deducted", "UNPAID", false, true, false, "Unpaid emergency absence or probation leave."));
        list.add(new LeaveTypeItem(4, "PERSONAL", "Personal Leave", "Urgent Personal Absence", "PAID", true, true, false, "Personal urgent category leave."));
        list.add(new LeaveTypeItem(5, "LL", "Special / Long Leave (LL)", "Extended Absence • HR Approval", "UNPAID", false, false, false, "Extended leave policy requiring senior HR approval."));
        return list;
    }

    public void submitLeaveRequest(LeaveTypeItem leaveType, boolean isHalfDay,
                                   String fromDate, String toDate, String halfDaySession,
                                   String reason, LeaveSubmitCallback callback) {
        int staffId = userStore.getStaffId();
        if (staffId <= 0) staffId = 38;

        Map<String, String> params = new HashMap<>();
        params.put("staffid", String.valueOf(staffId));
        params.put("leave_type_id", String.valueOf(leaveType.getId()));
        params.put("leave_code", leaveType.getCode());
        params.put("request_type", isHalfDay ? "HALF_DAY" : "FULL_DAY");
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("reason", reason);
        if (isHalfDay && halfDaySession != null) {
            params.put("half_day_session", halfDaySession);
        }

        final int finalStaffId = staffId;
        final String requestType = isHalfDay ? "HALF_DAY" : "FULL_DAY";

        ApiClient.postRequest(ApiConfig.LEAVE_REQUEST_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                if (json != null && json.optBoolean("status", false)) {
                    JSONObject data = json.optJSONObject("data");
                    int reqId = (data != null && data.has("id")) ? data.optInt("id") : (int) (System.currentTimeMillis() % 10000);
                    String status = (data != null && data.has("status")) ? data.optString("status") : "PENDING_HEAD";
                    double requestedDays = (data != null && data.has("requested_days")) ? data.optDouble("requested_days") : (isHalfDay ? 0.5 : 1.0);
                    int headId = (data != null && data.has("head_employee_id")) ? data.optInt("head_employee_id") : 3;

                    LeaveRequestRecord record = new LeaveRequestRecord(
                            reqId, finalStaffId, 12, leaveType.getId(),
                            leaveType.getCode(), requestType, fromDate, toDate,
                            halfDaySession, requestedDays, reason, headId,
                            status, "MOBILE_APP", "4.0"
                    );

                    saveLeaveRequestLocally(record);
                    if (callback != null) {
                        callback.onSuccess(record);
                    }
                } else {
                    String msg = json != null ? json.optString("message", "Leave application submission failed.") : "Submission failed.";
                    if (callback != null) {
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Offline fallback
                int simId = (int) (System.currentTimeMillis() % 10000);
                LeaveRequestRecord fallback = new LeaveRequestRecord(
                        simId, finalStaffId, 12, leaveType.getId(),
                        leaveType.getCode(), requestType, fromDate, toDate,
                        halfDaySession, (isHalfDay ? 0.5 : 1.0), reason, 3,
                        "PENDING_HEAD", "MOBILE_APP", "4.0"
                );
                saveLeaveRequestLocally(fallback);
                if (callback != null) {
                    callback.onFallback(fallback);
                }
            }
        });
    }

    public void saveLeaveRequestLocally(LeaveRequestRecord record) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_RECENT_LEAVES, Context.MODE_PRIVATE);
        String existingJson = prefs.getString(KEY_LEAVES_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(existingJson);
            JSONArray updated = new JSONArray();
            updated.put(record.toJson());
            for (int i = 0; i < arr.length() && i < 15; i++) {
                updated.put(arr.getJSONObject(i));
            }
            prefs.edit().putString(KEY_LEAVES_JSON, updated.toString()).apply();
        } catch (JSONException ignored) {}
    }

    public List<LeaveRequestRecord> getRecentLeaves() {
        List<LeaveRequestRecord> result = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_RECENT_LEAVES, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LEAVES_JSON, "");

        JSONArray arr = null;
        if (json != null && !json.trim().isEmpty()) {
            try {
                arr = new JSONArray(json);
            } catch (JSONException ignored) {}
        }

        if (arr == null || arr.length() == 0) {
            arr = new JSONArray();
            JSONObject sample = new JSONObject();
            try {
                sample.put("id", 6);
                sample.put("employee_id", 38);
                sample.put("department_id", 12);
                sample.put("leave_type_id", 3);
                sample.put("leave_code", "CL");
                sample.put("request_type", "FULL_DAY");
                sample.put("from_date", "2026-10-14");
                sample.put("to_date", "2026-10-15");
                sample.put("half_day_session", null);
                sample.put("requested_days", 2.0);
                sample.put("reason", "Personal absence");
                sample.put("head_employee_id", 3);
                sample.put("status", "PENDING_HEAD");
                sample.put("source_type", "MOBILE_APP");
                sample.put("available_before", "4.0");
                arr.put(sample);
            } catch (JSONException ignored) {}
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj != null) {
                LeaveRequestRecord record = LeaveRequestRecord.fromJson(obj);
                if (record != null) {
                    result.add(record);
                }
            }
        }
        return result;
    }
}

