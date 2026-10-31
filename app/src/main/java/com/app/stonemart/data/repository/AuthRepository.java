package com.app.stonemart.data.repository;

import android.content.Context;

import com.app.stonemart.ApiClient;
import com.app.stonemart.ApiConfig;
import com.app.stonemart.UserLocalStore;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthRepository - Handles authentication, session state and employee credentials.
 */
public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(String formattedName, String userRole);
        void onError(String errorMessage);
    }

    private final UserLocalStore userStore;

    public AuthRepository(Context context) {
        this.userStore = new UserLocalStore(context);
    }

    public boolean isLoggedIn() {
        return userStore.isLoggedIn();
    }

    public UserLocalStore getUserStore() {
        return userStore;
    }

    public void login(String username, String password, AuthCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("id", username);
        params.put("pwd", password);

        ApiClient.postRequest(ApiConfig.LOGIN_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    boolean success = response.optBoolean("success", false);
                    if (success) {
                        JSONObject userObj = response.optJSONObject("user");
                        int userId = userObj != null ? userObj.optInt("id", 0) : 0;
                        String returnedUser = userObj != null ? userObj.optString("username", username) : username;
                        int staffId = userObj != null ? userObj.optInt("staffid", 0) : 0;
                        String userRole = userObj != null ? userObj.optString("userrole", "employee") : "employee";
                        String email = userObj != null ? userObj.optString("email", "") : "";

                        String formattedName = returnedUser.length() > 0
                                ? returnedUser.substring(0, 1).toUpperCase() + returnedUser.substring(1)
                                : username;
                        String storeName = "Plot 12 • Heavy Sawn Slab Yard (Sector 4B)";

                        userStore.storeUserSession(String.valueOf(userId > 0 ? userId : username), formattedName,
                                storeName, 26.9124, 75.7873, 200.0f, staffId, userRole, email);

                        if (callback != null) {
                            callback.onSuccess(formattedName, userRole);
                        }
                    } else {
                        String msg = response.optString("message", "Invalid Username or Password.");
                        if (callback != null) {
                            callback.onError(msg);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError("Unable to parse server response.");
                    }
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

    public void logout() {
        userStore.clearSession();
    }
}

