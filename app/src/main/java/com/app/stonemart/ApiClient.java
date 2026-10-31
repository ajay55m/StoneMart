package com.app.stonemart;

import com.app.stonemart.data.api.ApiClient.ApiCallback;
import java.util.Map;

/**
 * ApiClient - Backward compatibility adapter for com.app.stonemart.data.api.ApiClient.
 */
public class ApiClient {

    public interface ApiCallback extends com.app.stonemart.data.api.ApiClient.ApiCallback {}

    public static void postRequest(String urlString, Map<String, String> postData, ApiCallback callback) {
        com.app.stonemart.data.api.ApiClient.postRequest(urlString, postData, callback);
    }

    public static void getRequest(String urlString, ApiCallback callback) {
        com.app.stonemart.data.api.ApiClient.getRequest(urlString, callback);
    }
}
