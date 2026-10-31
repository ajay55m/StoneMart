# Stone Mart - Manual Setup & Code Writing Guide

This step-by-step guide will walk you through creating, configuring, and writing the code for the **Stone Mart Employee Check-In & Check-Out Android Application** from scratch in Android Studio.

---

## Table of Contents
1. [Step 1: Create New Project in Android Studio](#step-1-create-new-project-in-android-studio)
2. [Step 2: Configure `build.gradle` Dependencies](#step-2-configure-buildgradle-dependencies)
3. [Step 3: Setup `AndroidManifest.xml` & Permissions](#step-3-setup-androidmanifestxml--permissions)
4. [Step 4: Color Palette & Resource Strings](#step-4-color-palette--resource-strings)
5. [Step 5: Session & Storage Helper (`UserLocalStore.java`)](#step-5-session--storage-helper-userlocalstorejava)
6. [Step 6: Location & Geofence Helper (`LocationHelper.java`)](#step-6-location--geofence-helper-locationhelperjava)
7. [Step 7: API Configuration & Models](#step-7-api-configuration--models)
8. [Step 8: Splash Screen & Login Flow](#step-8-splash-screen--login-flow)
9. [Step 9: Main Dashboard & Bottom Navigation](#step-9-main-dashboard--bottom-navigation)
10. [Step 10: Check-In / Check-Out Module (`CheckInCheckOutFragment.java`)](#step-10-check-in--check-out-module-checkincheckoutfragmentjava)
11. [Step 11: Manage Attendance History (`ManageAttendanceFragment.java`)](#step-11-manage-attendance-history-manageattendancefragmentjava)
12. [Step 12: How to Build, Run & Test](#step-12-how-to-build-run--test)

---

## Step 1: Create New Project in Android Studio

1. Open **Android Studio**.
2. Click **New Project** -> Select **Empty Views Activity** -> Click **Next**.
3. Fill in the project settings:
   - **Name**: `Stone-Mart`
   - **Package name**: `com.app.stonemart`
   - **Save location**: `D:\Stone-Mart`
   - **Language**: `Java`
   - **Minimum SDK**: `API 21 (Android 5.0 Lollipop)` or higher (API 24/34 recommended)
   - **Build configuration language**: `Groovy DSL (build.gradle)`
4. Click **Finish** and allow Gradle to complete initial sync.

---

## Step 2: Configure `build.gradle` Dependencies

Open `app/build.gradle` and update dependencies to include AndroidX Material Design, Volley HTTP client, Play Services Location, and Glide:

```groovy
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.app.stonemart'
    compileSdk 34

    defaultConfig {
        applicationId "com.app.stonemart"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // Volley for REST API calls
    implementation 'com.android.volley:volley:1.2.1'

    // Play Services Location (for GPS & Geofencing distance calculation)
    implementation 'com.google.android.gms:play-services-location:21.0.1'

    // Glide for image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
}
```

Click **Sync Now** in Android Studio bar.

---

## Step 3: Setup `AndroidManifest.xml` & Permissions

Open `app/src/main/AndroidManifest.xml` and add hardware permissions and declare activities:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions required for Check-In/Check-Out, Selfie Camera & Location -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.CAMERA" />
    
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.location.gps" android:required="false" />

    <application
        android:name=".StoneMartApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.StoneMart"
        android:usesCleartextTraffic="true">

        <!-- Splash Activity (Launcher) -->
        <activity
            android:name=".SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.StoneMart.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Employee Login Activity -->
        <activity
            android:name=".EmployeeLoginActivity"
            android:exported="false"
            android:theme="@style/Theme.StoneMart.NoActionBar" />

        <!-- Dashboard Activity -->
        <activity
            android:name=".DashboardActivity"
            android:exported="false"
            android:theme="@style/Theme.StoneMart.NoActionBar" />

    </application>

</manifest>
```

---

## Step 4: Color Palette & Resource Strings

Open `app/src/main/res/values/colors.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#0F172A</color>
    <color name="primary_variant">#1E293B</color>
    <color name="accent_emerald">#10B981</color>
    <color name="accent_blue">#2563EB</color>
    <color name="accent_rose">#F43F5E</color>
    <color name="accent_amber">#F59E0B</color>
    <color name="background_light">#F8FAFC</color>
    <color name="card_background">#FFFFFF</color>
    <color name="text_primary">#0F172A</color>
    <color name="text_secondary">#64748B</color>
    <color name="divider">#E2E8F0</color>
</resources>
```

Open `app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">Stone Mart</string>
    <string name="employee_login">Employee Login</string>
    <string name="emp_id_hint">Employee ID / Phone Number</string>
    <string name="password_hint">Password / PIN</string>
    <string name="btn_login">Sign In</string>
    <string name="nav_checkin">Check-In / Out</string>
    <string name="nav_manage">Manage Logs</string>
</resources>
```

Create `app/src/main/res/menu/bottom_nav_menu.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_checkin_checkout"
        android:icon="@android:drawable/ic_menu_today"
        android:title="Check-In/Out" />
    <item
        android:id="@+id/nav_manage_attendance"
        android:icon="@android:drawable/ic_menu_agenda"
        android:title="Manage Logs" />
</menu>
```

---

## Step 5: Session & Storage Helper (`UserLocalStore.java`)

Create file `app/src/main/java/com/app/stonemart/UserLocalStore.java`:

```java
package com.app.stonemart;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {
    private static final String PREF_NAME = "StoneMartPrefs";
    private final SharedPreferences prefs;

    public UserLocalStore(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserSession(String empId, String empName, String storeName, double storeLat, double storeLng, float radiusMeters) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("empId", empId);
        editor.putString("empName", empName);
        editor.putString("storeName", storeName);
        editor.putFloat("storeLat", (float) storeLat);
        editor.putFloat("storeLng", (float) storeLng);
        editor.putFloat("radiusMeters", radiusMeters);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public String getEmpId() { return prefs.getString("empId", "EMP1001"); }
    public String getEmpName() { return prefs.getString("empName", "Employee User"); }
    public String getStoreName() { return prefs.getString("storeName", "Stone Mart Main Branch"); }
    public double getStoreLat() { return prefs.getFloat("storeLat", 26.9124f); }
    public double getStoreLng() { return prefs.getFloat("storeLng", 75.7873f); }
    public float getRadiusMeters() { return prefs.getFloat("radiusMeters", 200.0f); }

    public void savePunchInState(String time, String location) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckedIn", true);
        editor.putString("checkInTime", time);
        editor.putString("checkInLoc", location);
        editor.apply();
    }

    public void savePunchOutState(String time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckedIn", false);
        editor.putString("checkOutTime", time);
        editor.apply();
    }

    public boolean isCheckedIn() { return prefs.getBoolean("isCheckedIn", false); }
    public String getCheckInTime() { return prefs.getString("checkInTime", "--:--"); }
    public String getCheckOutTime() { return prefs.getString("checkOutTime", "--:--"); }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
```

---

## Step 6: Location & Geofence Helper (`LocationHelper.java`)

Create file `app/src/main/java/com/app/stonemart/LocationHelper.java`:

```java
package com.app.stonemart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, float distanceToStoreMeters, boolean isInsideGeofence);
        void onError(String errorMessage);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, double targetLat, double targetLng, float maxDistanceMeters, LocationCallback callback) {
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLat, targetLng, results);
                float distanceMeters = results[0];
                boolean isInside = distanceMeters <= maxDistanceMeters;
                callback.onLocationReceived(location.getLatitude(), location.getLongitude(), distanceMeters, isInside);
            } else {
                callback.onError("Unable to fetch GPS position. Ensure Location is turned on.");
            }
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
```

---

## Step 7: API Configuration & Data Models

Create `ApiConfig.java`:
```java
package com.app.stonemart;

public class ApiConfig {
    public static final String BASE_URL = "https://stonemart.com/api/";
    public static final String LOGIN_URL = BASE_URL + "employee_login.php";
    public static final String PUNCH_URL = BASE_URL + "submit_attendance.php";
    public static final String HISTORY_URL = BASE_URL + "get_attendance_history.php";
}
```

Create `AttendanceRecordModel.java`:
```java
package com.app.stonemart;

public class AttendanceRecordModel {
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private String status; // Present, Late, Half Day
    private String totalHours;
    private String locationName;

    public AttendanceRecordModel(String date, String checkInTime, String checkOutTime, String status, String totalHours, String locationName) {
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.totalHours = totalHours;
        this.locationName = locationName;
    }

    public String getDate() { return date; }
    public String getCheckInTime() { return checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public String getStatus() { return status; }
    public String getTotalHours() { return totalHours; }
    public String getLocationName() { return locationName; }
}
```

---

## Step 8: Splash Screen & Login Flow

Create `EmployeeLoginActivity.java`:
```java
package com.app.stonemart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeLoginActivity extends AppCompatActivity {
    private UserLocalStore userStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        userStore = new UserLocalStore(this);
        EditText etEmpId = findViewById(R.id.etEmpId);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String empId = etEmpId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (empId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter Employee ID and Password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate Successful Employee Auth
            userStore.storeUserSession(empId, "Rajesh Sharma", "Stone Mart - Jaipur Branch", 26.9124, 75.7873, 200.0f);
            Toast.makeText(this, "Welcome, " + empId, Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }
}
```

---

## Step 9: Main Dashboard & Bottom Navigation

Create `DashboardActivity.java`:
```java
package com.app.stonemart;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        // Load Default Fragment
        loadFragment(new CheckInCheckOutFragment());

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_checkin_checkout) {
                selectedFragment = new CheckInCheckOutFragment();
            } else if (item.getItemId() == R.id.nav_manage_attendance) {
                selectedFragment = new ManageAttendanceFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
```

---

## Step 10: Check-In / Check-Out Module (`CheckInCheckOutFragment.java`)

Create `CheckInCheckOutFragment.java`:
```java
package com.app.stonemart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckInCheckOutFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 101;
    private UserLocalStore userStore;
    private TextView tvStatus, tvGeofenceStatus, tvTimeIn, tvTimeOut;
    private Button btnAction;
    private boolean isInsideGeofence = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin_checkout, container, false);
        userStore = new UserLocalStore(requireContext());

        tvStatus = view.findViewById(R.id.tvStatus);
        tvGeofenceStatus = view.findViewById(R.id.tvGeofenceStatus);
        tvTimeIn = view.findViewById(R.id.tvTimeIn);
        tvTimeOut = view.findViewById(R.id.tvTimeOut);
        btnAction = view.findViewById(R.id.btnAction);

        updateUI();
        verifyGeofenceLocation();

        btnAction.setOnClickListener(v -> {
            if (!isInsideGeofence) {
                Toast.makeText(requireContext(), "You must be inside store perimeter to Check-In/Out!", Toast.LENGTH_LONG).show();
                return;
            }
            // Launch Camera for Selfie Verification
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        });

        return view;
    }

    private void verifyGeofenceLocation() {
        LocationHelper.getCurrentLocation(requireContext(), userStore.getStoreLat(), userStore.getStoreLng(), userStore.getRadiusMeters(),
            new LocationHelper.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude, float distanceMeters, boolean inside) {
                    isInsideGeofence = inside;
                    if (inside) {
                        tvGeofenceStatus.setText("✓ Inside Store Perimeter (" + (int) distanceMeters + "m away)");
                        tvGeofenceStatus.setTextColor(0xFF10B981); // Emerald Green
                    } else {
                        tvGeofenceStatus.setText("⚠ Outside Store Radius (" + (int) distanceMeters + "m away)");
                        tvGeofenceStatus.setTextColor(0xFFF43F5E); // Rose Red
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    tvGeofenceStatus.setText("GPS Status: " + errorMessage);
                }
            });
    }

    private void updateUI() {
        boolean checkedIn = userStore.isCheckedIn();
        if (checkedIn) {
            tvStatus.setText("CHECKED IN");
            btnAction.setText("CHECK OUT NOW");
            btnAction.setBackgroundColor(0xFFF43F5E);
        } else {
            tvStatus.setText("NOT CHECKED IN");
            btnAction.setText("CHECK IN NOW");
            btnAction.setBackgroundColor(0xFF10B981);
        }
        tvTimeIn.setText("In Time: " + userStore.getCheckInTime());
        tvTimeOut.setText("Out Time: " + userStore.getCheckOutTime());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            if (!userStore.isCheckedIn()) {
                userStore.savePunchInState(currentTime, userStore.getStoreName());
                Toast.makeText(requireContext(), "Checked In Successfully at " + currentTime, Toast.LENGTH_SHORT).show();
            } else {
                userStore.savePunchOutState(currentTime);
                Toast.makeText(requireContext(), "Checked Out Successfully at " + currentTime, Toast.LENGTH_SHORT).show();
            }
            updateUI();
        }
    }
}
```

---

## Step 11: Manage Attendance History (`ManageAttendanceFragment.java`)

Create `ManageAttendanceFragment.java`:
```java
package com.app.stonemart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageAttendanceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_attendance, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvAttendanceHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<AttendanceRecordModel> list = new ArrayList<>();
        list.add(new AttendanceRecordModel("28 Sep 2026", "09:15 AM", "06:00 PM", "PRESENT", "8 hrs 45 mins", "Jaipur Main Branch"));
        list.add(new AttendanceRecordModel("27 Sep 2026", "09:30 AM", "06:15 PM", "LATE", "8 hrs 45 mins", "Jaipur Main Branch"));
        list.add(new AttendanceRecordModel("26 Sep 2026", "09:05 AM", "06:00 PM", "PRESENT", "8 hrs 55 mins", "Jaipur Main Branch"));
        list.add(new AttendanceRecordModel("25 Sep 2026", "09:10 AM", "02:00 PM", "HALF DAY", "4 hrs 50 mins", "Jaipur Main Branch"));

        AttendanceHistoryAdapter adapter = new AttendanceHistoryAdapter(list);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
```

Create `AttendanceHistoryAdapter.java`:
```java
package com.app.stonemart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {
    private final List<AttendanceRecordModel> list;

    public AttendanceHistoryAdapter(List<AttendanceRecordModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecordModel item = list.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvTime.setText("In: " + item.getCheckInTime() + " | Out: " + item.getCheckOutTime());
        holder.tvStatus.setText(item.getStatus());
        holder.tvHours.setText("Total: " + item.getTotalHours());
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvStatus, tvHours;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHours = itemView.findViewById(R.id.tvHours);
        }
    }
}
```

---

## Step 12: How to Build, Run & Test

1. **Connect Mobile Device / Emulator**: Enable USB Debugging on your Android phone or boot up an Android Virtual Device (AVD).
2. **Build & Install**:
   - In Android Studio, click the green **Run (Play)** button, or execute in command line:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Test Check-In Flow**:
   - Log in using any sample credentials.
   - The app verifies your GPS location distance to assigned store.
   - Tap **CHECK IN NOW** -> Camera opens for selfie capture.
   - Capture picture -> Status updates to **CHECKED IN**.
4. **Test Manage Attendance Feed**:
   - Switch tabs to **Manage Logs** -> Displays today's punch timeline and previous attendance logs.
