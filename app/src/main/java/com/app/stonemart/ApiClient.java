package com.app.stonemart;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Pre-seeded cookie to bypass Apache/cPanel anti-bot verification challenge
    private static volatile String activeCookie = "humans_21909=1";
    private static final Pattern COOKIE_CHALLENGE_PATTERN = Pattern
            .compile("document\\.cookie\\s*=\\s*[\"']([^\"']+)[\"']");

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 StoneMart/1.0";

    public interface ApiCallback {
        void onSuccess(JSONObject response);

        void onError(String errorMessage);
    }

    public static void postRequest(String urlString, Map<String, String> postData, ApiCallback callback) {
        executor.execute(() -> executeRequestInternal(urlString, "POST", postData, 0, callback));
    }

    public static void getRequest(String urlString, ApiCallback callback) {
        executor.execute(() -> executeRequestInternal(urlString, "GET", null, 0, callback));
    }

    private static void executeRequestInternal(String urlString, String method,
            Map<String, String> postData, int retryCount,
            ApiCallback callback) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setDoInput(true);

            // Essential Headers for Keep-Alive, GZIP Compression, and Cloudflare/Apache WAF
            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Connection", "Keep-Alive");

            if (activeCookie != null && !activeCookie.trim().isEmpty()) {
                conn.setRequestProperty("Cookie", activeCookie);
            }

            if ("POST".equalsIgnoreCase(method)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                StringBuilder postBody = new StringBuilder();
                if (postData != null) {
                    for (Map.Entry<String, String> entry : postData.entrySet()) {
                        if (postBody.length() != 0)
                            postBody.append('&');
                        postBody.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        postBody.append('=');
                        postBody.append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", "UTF-8"));
                    }
                }

                byte[] postDataBytes = postBody.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                    os.flush();
                }
            }

            // Capture any standard Set-Cookie response headers from server
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            if (headerFields != null && headerFields.containsKey("Set-Cookie")) {
                List<String> setCookies = headerFields.get("Set-Cookie");
                if (setCookies != null) {
                    for (String cookieItem : setCookies) {
                        if (cookieItem != null && !cookieItem.isEmpty()) {
                            String cookiePart = cookieItem.split(";")[0].trim();
                            appendCookie(cookiePart);
                        }
                    }
                }
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 400)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                is = conn.getInputStream();
            }

            // Decompress GZIP if compressed by server
            String contentEncoding = conn.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.toLowerCase().contains("gzip") && is != null) {
                is = new GZIPInputStream(is);
            }

            StringBuilder responseSb = new StringBuilder();
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseSb.append(line);
                    }
                }
            }

            String rawResponse = responseSb.toString().trim();
            Log.d(TAG, "Response [" + responseCode + "] from " + urlString + ": " + rawResponse);

            // 1. Detect & Solve Anti-Bot Javascript Cookie Challenge
            // e.g. <script>document.cookie = "humans_21909=1";
            // document.location.reload(true)</script>
            if (rawResponse.contains("document.cookie")) {
                Matcher matcher = COOKIE_CHALLENGE_PATTERN.matcher(rawResponse);
                if (matcher.find()) {
                    String challengeCookie = matcher.group(1);
                    Log.w(TAG, "Anti-Bot WAF Challenge detected! Solved cookie: " + challengeCookie);
                    appendCookie(challengeCookie);

                    if (retryCount < 2) {
                        Log.d(TAG, "Auto-retrying request with verified cookie (Attempt " + (retryCount + 1) + ")");
                        executeRequestInternal(urlString, method, postData, retryCount + 1, callback);
                        return;
                    }
                }
            }

            // 2. Parse JSON Response
            if (rawResponse.startsWith("{") && rawResponse.endsWith("}")) {
                JSONObject jsonResponse = new JSONObject(rawResponse);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(jsonResponse);
                    }
                });
                return;
            }

            // 3. Fallback for non-JSON or HTML error responses
            if (responseCode == 401) {
                dispatchError(callback, "Invalid Username or Password. Please verify your credentials.");
            } else if (responseCode == 403 || responseCode == 409) {
                dispatchError(callback, "Security verification failed. Please try again.");
            } else if (rawResponse.toLowerCase().contains("<html") || rawResponse.toLowerCase().contains("<!doctype")) {
                dispatchError(callback,
                        "Server returned an unexpected response (Code " + responseCode + "). Please try again.");
            } else if (!rawResponse.isEmpty()) {
                dispatchError(callback, rawResponse);
            } else {
                dispatchError(callback, "Empty response from server (Code " + responseCode + ").");
            }

        } catch (Exception e) {
            Log.e(TAG, "API Request Exception: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ignored) {
                }
            }
            String errMsg = e.getMessage() != null ? e.getMessage() : "Network connection failed";
            if (errMsg.contains("cannot be converted to JSONObject") || errMsg.contains("document.cookie")) {
                errMsg = "Server security verification in progress. Please try again.";
            }
            final String finalErr = errMsg;
            dispatchError(callback, finalErr);
        }
    }

    private static synchronized void appendCookie(String newCookie) {
        if (newCookie == null || newCookie.trim().isEmpty())
            return;
        newCookie = newCookie.trim();
        if (activeCookie == null || activeCookie.trim().isEmpty()) {
            activeCookie = newCookie;
        } else if (!activeCookie.contains(newCookie.split("=")[0])) {
            activeCookie = activeCookie + "; " + newCookie;
        } else {
            // Replace existing cookie value
            String prefix = newCookie.split("=")[0] + "=";
            String[] parts = activeCookie.split(";\\s*");
            StringBuilder updated = new StringBuilder();
            for (String p : parts) {
                if (p.startsWith(prefix)) {
                    if (updated.length() > 0)
                        updated.append("; ");
                    updated.append(newCookie);
                } else if (!p.isEmpty()) {
                    if (updated.length() > 0)
                        updated.append("; ");
                    updated.append(p);
                }
            }
            activeCookie = updated.toString();
        }
        Log.d(TAG, "Current Active Cookies: " + activeCookie);
    }

    private static void dispatchError(ApiCallback callback, String errorMessage) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onError(errorMessage);
            }
        });
    }
}
