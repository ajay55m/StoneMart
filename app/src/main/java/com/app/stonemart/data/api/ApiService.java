package com.app.stonemart.data.api;

import com.app.stonemart.utils.Constants;
import org.json.JSONObject;
import java.util.Map;

/**
 * ApiService - Service layer interface providing direct API communication methods
 * for authentication, geofenced punch operations, monthly attendance, and leave management.
 */
public class ApiService {

    public interface ResponseCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public static void login(String username, String password, ResponseCallback callback) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("id", username);
        params.put("pwd", password);
        ApiClient.postRequest(Constants.LOGIN_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public static void submitPunch(Map<String, String> punchParams, ResponseCallback callback) {
        ApiClient.postRequest(Constants.PUNCH_URL, punchParams, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public static void getMonthlyAttendance(Map<String, String> queryParams, ResponseCallback callback) {
        ApiClient.postRequest(Constants.EMPLOYEE_DETAIL_URL, queryParams, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public static void submitLeaveRequest(Map<String, String> leaveParams, ResponseCallback callback) {
        ApiClient.postRequest(Constants.LEAVE_REQUEST_URL, leaveParams, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
