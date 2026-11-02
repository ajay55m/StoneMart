package com.app.stonemart.utils;

/**
 * Constants - Centralized application constants, API endpoints, preference keys,
 * and geofence configuration values across the StoneMart platform.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Network Base URL & Endpoints
    public static final String BASE_URL = "https://stonemart.org/SMCS_APP/fcm_app/";
    public static final String LOGIN_URL = BASE_URL + "subadminlogin.php";
    public static final String PUNCH_URL = BASE_URL + "submit_attendance.php";
    public static final String GET_LOCATION_URL = BASE_URL + "get_location.php";
    public static final String HISTORY_URL = BASE_URL + "get_attendance_history.php";
    public static final String EMPLOYEE_DETAIL_URL = BASE_URL + "api-employee-detail.php";
    public static final String LEAVE_REQUEST_URL = BASE_URL + "api-leave-request.php";

    // For testing, later replace with employee's assigned location
    public static final int TEST_LOCATION_ID = 12;

    // SharedPreferences Namespaces
    public static final String PREF_NAME = "StoneMartPrefs";
    public static final String PREF_RECENT_LEAVES = "StoneMart_Recent_Leaves";

    // Session Preference Keys
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_EMP_ID = "empId";
    public static final String KEY_EMP_NAME = "empName";
    public static final String KEY_STORE_NAME = "storeName";
    public static final String KEY_STORE_LAT = "storeLat";
    public static final String KEY_STORE_LNG = "storeLng";
    public static final String KEY_RADIUS_METERS = "radiusMeters";
    public static final String KEY_STAFF_ID = "staffId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IS_CHECKED_IN = "isCheckedIn";
    public static final String KEY_CHECK_IN_TIME = "checkInTime";
    public static final String KEY_CHECK_OUT_TIME = "checkOutTime";
    public static final String KEY_CHECK_IN_LOC = "checkInLoc";
    public static final String KEY_SAVED_LEAVES_LIST = "saved_leaves_list";

    // Default Yard & Geofence Coordinates (Jaipur Facility Sector 4B)
    public static final double DEFAULT_LAT = 26.9124;
    public static final double DEFAULT_LNG = 75.7873;
    public static final float DEFAULT_RADIUS_METERS = 200.0f;
    public static final String DEFAULT_STORE_NAME = "Plot 12 • Heavy Sawn Slab Yard (Sector 4B)";
}
