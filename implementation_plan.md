# Focused Implementation Plan - Stone Mart Employee Check-In & Check-Out App

## 1. Overview & Immediate Scope

Per user requirement:
> **"First checkin and checkout then manage for that checking checkout now, other may update in future"**

The immediate focus for **Stone Mart** (`d:\Stone-Mart`) is to build the **Employee Check-In / Check-Out** core module and the **Attendance Management / History** module. Future features (Leave requests, branch transfers, etc.) are deferred.

---

## 2. Core Modules in Phase 1

```
+-----------------------------------------------------------------------------------+
|                        STONE MART PHASE 1 ARCHITECTURE                            |
+-----------------------------------------------------------------------------------+
                                          |
                        +-----------------+-----------------+
                        |                                   |
              [SplashActivity]                       [AppPermissions]
                        |                                   |
                        v                                   v
             [UserLocalStore Check]               [GPS Location & Camera]
                        |
        +---------------+---------------+
        | (Not Logged In)               | (Logged In)
        v                               v
[EmployeeLoginActivity]        [DashboardActivity]
(Emp Code + PIN/Pass)                   |
                                        +---> Bottom Navigation
                                                 |
                       +-------------------------+-------------------------+
                       |                                                   |
                       v                                                   v
           [CheckInCheckOutFragment]                           [ManageAttendanceFragment]
           - Digital Clock & Date                              - Today's Detailed Timeline
           - Today's Check-in/out Status                       - Check-in & Out Timestamps
           - Store Geofence Distance (GPS)                     - Total Hours Worked Counter
           - "CHECK IN" / "CHECK OUT" Button                   - Past Attendance Logs Feed
           - Selfie Camera Verification                        - Status Indicators (Present/Late)
```

---

## 3. Screen Specifications & Features

### Screen 1: Employee Login (`EmployeeLoginActivity.java`)
- Modern clean UI with Stone Mart branding.
- Input fields: **Employee ID / Phone** & **Password / PIN**.
- "Remember Me" toggle.
- Validates against `api/employee_login.php` or local mock server.
- Stores employee session: `emp_id`, `emp_name`, `assigned_store_name`, `store_lat`, `store_lng`, `geofence_radius`.

### Screen 2: Check-In / Check-Out Module (`CheckInCheckOutFragment.java`)
- **Real-Time Clock & Date Header**: Dynamic clock display.
- **Current Status Card**:
  - `NOT CHECKED IN` (Gray badge)
  - `CHECKED IN AT 09:15 AM` (Green badge with timer)
  - `CHECKED OUT AT 06:00 PM` (Blue badge with summary)
- **GPS Location & Store Geofence Card**:
  - Fetches employee's live GPS coordinates via `LocationHelper.java`.
  - Calculates distance to assigned store.
  - Status: Green checkmark ("Inside Store Radius - 45m away") or Red warning ("Outside Store Radius - 320m away").
- **Interactive Check-In / Check-Out Action Button**:
  - Gradient "CHECK IN" button when not checked in.
  - Gradient "CHECK OUT" button when checked in.
- **Selfie Camera Verification Flow**:
  - Tapping action button launches camera (`CaptureActivity.java`) to take a mandatory selfie.
  - Captures photo + GPS lat/lng + timestamp.
  - Posts to `submit_self_attendance.php`.
- **Offline Storage Queue**:
  - Saves punch to `OfflineAttendanceQueue.java` if internet is unreachable. Auto-syncs when online.

### Screen 3: Attendance Management & History (`ManageAttendanceFragment.java`)
- **Today's Activity Card**:
  - Summary of Today's Check-In time, Check-Out time, Total Working Hours (e.g. `8 hrs 45 mins`), and Location address.
  - Thumbnail preview of check-in selfie.
- **Attendance History Log (`RecyclerView` with `AttendanceHistoryAdapter`)**:
  - List of past days showing:
    - Date & Day of Week.
    - Status Badge: `PRESENT` (Green), `LATE` (Yellow), `HALF DAY` (Orange), `ABSENT` (Red).
    - Check-in time & Check-out time.
    - Total Hours worked.

---

## 4. File Structure to Create in `d:\Stone-Mart`

```
d:\Stone-Mart\
├── build.gradle
├── settings.gradle
├── gradle.properties
└── app\
    ├── build.gradle
    └── src\main\
        ├── AndroidManifest.xml
        ├── java\com\app\stonemart\
        │   ├── StoneMartApp.java
        │   ├── UserLocalStore.java
        │   ├── AppPermissions.java
        │   ├── LocationHelper.java
        │   ├── ApiConfig.java
        │   ├── AttendanceApi.java
        │   ├── OfflineAttendanceQueue.java
        │   ├── SplashActivity.java
        │   ├── EmployeeLoginActivity.java
        │   ├── DashboardActivity.java
        │   ├── CheckInCheckOutFragment.java
        │   ├── ManageAttendanceFragment.java
        │   ├── AttendanceHistoryAdapter.java
        │   └── AttendanceRecordModel.java
        └── res\
            ├── layout\
            │   ├── activity_splash.xml
            │   ├── activity_employee_login.xml
            │   ├── activity_dashboard.xml
            │   ├── fragment_checkin_checkout.xml
            │   ├── fragment_manage_attendance.xml
            │   └── item_attendance_history.xml
            ├── drawable\
            ├── values\
            │   ├── colors.xml
            │   ├── strings.xml
            │   └── styles.xml
            └── menu\
                └── bottom_nav_menu.xml
```

---

## 5. Execution Steps

1. **Setup Gradle & Android Project Structure** in `d:\Stone-Mart`.
2. **Create Core Utilities**: `UserLocalStore.java`, `LocationHelper.java`, `AppPermissions.java`, `ApiConfig.java`.
3. **Build Authentication Flow**: `EmployeeLoginActivity.java`, `SplashActivity.java`.
4. **Build Check-In / Check-Out Screen**: `CheckInCheckOutFragment.java` with GPS location check & selfie capture.
5. **Build Attendance Management / History Screen**: `ManageAttendanceFragment.java`, `AttendanceHistoryAdapter.java`, `AttendanceRecordModel.java`.
6. **Verify Compilation**: Compile with Gradle and verify debug build.
