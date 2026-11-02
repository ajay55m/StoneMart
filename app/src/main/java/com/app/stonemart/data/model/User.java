package com.app.stonemart.data.model;

import org.json.JSONObject;

/**
 * User - Typed model representing an authenticated employee profile.
 */
public class User {

    private final int id;
    private final String username;
    private final int staffId;
    private final String userRole;
    private final String email;
    private final String formattedName;
    private final String storeName;
    private final double storeLat;
    private final double storeLng;
    private final float radiusMeters;

    public User(int id, String username, int staffId, String userRole, String email,
                String formattedName, String storeName, double storeLat, double storeLng, float radiusMeters) {
        this.id = id;
        this.username = username;
        this.staffId = staffId;
        this.userRole = userRole;
        this.email = email;
        this.formattedName = formattedName;
        this.storeName = storeName;
        this.storeLat = storeLat;
        this.storeLng = storeLng;
        this.radiusMeters = radiusMeters;
    }

    public static User fromJson(JSONObject obj, String defaultUsername) {
        if (obj == null) return null;
        int id = obj.optInt("id", 0);
        String username = obj.optString("username", defaultUsername);
        int staffId = obj.optInt("staffid", 0);
        String userRole = obj.optString("userrole", "employee");
        String email = obj.optString("email", "");
        String formattedName = username.length() > 0
                ? username.substring(0, 1).toUpperCase() + username.substring(1)
                : defaultUsername;
        String storeName = "Plot 12 • Heavy Sawn Slab Yard (Sector 4B)";
        return new User(id, username, staffId, userRole, email, formattedName, storeName, 26.9124, 75.7873, 200.0f);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public int getStaffId() { return staffId; }
    public String getUserRole() { return userRole; }
    public String getEmail() { return email; }
    public String getFormattedName() { return formattedName; }
    public String getStoreName() { return storeName; }
    public double getStoreLat() { return storeLat; }
    public double getStoreLng() { return storeLng; }
    public float getRadiusMeters() { return radiusMeters; }
}

