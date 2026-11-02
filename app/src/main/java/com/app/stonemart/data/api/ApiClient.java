package com.app.stonemart.data.api;

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

/**
 * ApiClient - Core network engine featuring thread pooling, GZIP decompression,
 * and automatic anti-bot / cPanel challenge cookie handling.
 */
public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

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

    private static void executeRequestInternal(String urlString, String method, Map<String, String> postData,
            int redirectCount, ApiCallback callback) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);

            conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            conn.setRequestProperty("Accept", "application/json, text/javascript, text/html, */*; q=0.01");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("Origin", "https://stonemart.org");
            conn.setRequestProperty("Referer", "https://stonemart.org/SMCS_APP/fcm_app/login.php");

            if (activeCookie != null && !activeCookie.isEmpty()) {
                conn.setRequestProperty("Cookie", activeCookie);
            }

            if ("POST".equalsIgnoreCase(method)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                if (postData != null && !postData.isEmpty()) {
                    StringBuilder data = new StringBuilder();
                    for (Map.Entry<String, String> entry : postData.entrySet()) {
                        if (data.length() > 0)
                            data.append("&");
                        data.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", "UTF-8"));
                    }
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(data.toString().getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                }
            }

            int responseCode = conn.getResponseCode();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            if (headerFields != null && headerFields.containsKey("Set-Cookie")) {
                List<String> cookies = headerFields.get("Set-Cookie");
                if (cookies != null) {
                    for (String cookie : cookies) {
                        String cookieVal = cookie.split(";")[0];
                        appendCookie(cookieVal);
                    }
                }
            }

            InputStream is = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                is = conn.getInputStream();
            }

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

            if (rawResponse.contains("<script>") && rawResponse.contains("document.cookie")) {
                Matcher m = COOKIE_CHALLENGE_PATTERN.matcher(rawResponse);
                if (m.find()) {
                    String challengeCookie = m.group(1);
                    appendCookie(challengeCookie);
                    if (redirectCount < 2) {
                        executeRequestInternal(urlString, method, postData, redirectCount + 1, callback);
                        return;
                    }
                }
            }

            if (rawResponse.startsWith("<") && rawResponse.endsWith(">")) {
                dispatchError(callback, "Server returned HTML webpage instead of JSON.");
                return;
            }

            JSONObject jsonObject = new JSONObject(rawResponse);
            dispatchSuccess(callback, jsonObject);

        } catch (Exception e) {
            String errMsg = e.getMessage() != null ? e.getMessage() : "Network connection failed";
            if (errMsg.contains("cannot be converted to JSONObject") || errMsg.contains("document.cookie")) {
                errMsg = "Server security verification in progress. Please try again.";
            }
            dispatchError(callback, errMsg);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
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
    }

    private static void dispatchSuccess(ApiCallback callback, JSONObject jsonObject) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onSuccess(jsonObject);
            }
        });
    }

    private static void dispatchError(ApiCallback callback, String errorMessage) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onError(errorMessage);
            }
        });
    }
}

