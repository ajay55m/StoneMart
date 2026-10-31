package com.app.stonemart;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.stonemart.widget.BigMonthlyCalendarView;
import com.app.stonemart.widget.PremiumAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManageAttendanceFragment extends Fragment {

    private UserLocalStore userStore;

    private TextView tvAvatarInitials;
    private TextView tvEmpName;
    private TextView tvSubTitle;
    private ImageButton btnSearch;
    private RelativeLayout btnNotificationContainer;

    private TextView tvCountPresent;
    private TextView tvCountLeave;
    private TextView tvCountHoliday;
    private TextView tvCountMissing;

    private TextView tvCalendarMonthTitle;
    private View btnFilterMonth;
    private TextView tvFilterMonthText;
    private BigMonthlyCalendarView bigCalendarView;

    private int currentYear = 2026;
    private int currentMonth = Calendar.OCTOBER;

    private TextView tvSelectedDateTitle;
    private TextView tvSelectedStatusBadge;
    private TextView tvSelectedPunchIn;
    private TextView tvSelectedPunchOut;
    private TextView tvSelectedNetHours;
    private TextView tvSelectedLocation;
    private TextView tvSelectedTag;

    private View cardPermissionDetail;
    private TextView tvPermissionHeader;
    private TextView tvPermissionStatusBadge;
    private TextView tvPermissionTime;
    private TextView tvPermissionDuration;
    private TextView tvPermissionReason;

    private final Map<Integer, JSONObject> attendanceMap = new HashMap<>();
    private final Map<Integer, JSONObject> holidayMap = new HashMap<>();
    private final Map<Integer, JSONObject> permissionMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_attendance, container, false);
        userStore = new UserLocalStore(requireContext());

        initViews(view);
        setupHeaderData();

        Calendar todayCal = Calendar.getInstance();
        currentYear = 2026;
        currentMonth = Calendar.OCTOBER;

        if (bigCalendarView != null) {
            bigCalendarView.setYearMonth(currentYear, currentMonth);
            bigCalendarView.setSelectedDay(todayCal.get(Calendar.DAY_OF_MONTH));
        }

        updateMonthTitle();
        setupSummaryPills();
        setupCalendarInteractivity();

        updateSelectedDayDetail(todayCal.get(Calendar.DAY_OF_MONTH), BigMonthlyCalendarView.DayStatus.FUTURE);

        return view;
    }

    private void initViews(View view) {
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvEmpName = view.findViewById(R.id.tvEmpName);
        tvSubTitle = view.findViewById(R.id.tvSubTitle);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnNotificationContainer = view.findViewById(R.id.btnNotificationContainer);

        tvCountPresent = view.findViewById(R.id.tvCountPresent);
        tvCountLeave = view.findViewById(R.id.tvCountLeave);
        tvCountHoliday = view.findViewById(R.id.tvCountHoliday);
        tvCountMissing = view.findViewById(R.id.tvCountMissing);

        tvCalendarMonthTitle = view.findViewById(R.id.tvCalendarMonthTitle);
        btnFilterMonth = view.findViewById(R.id.btnFilterMonth);
        tvFilterMonthText = view.findViewById(R.id.tvFilterMonthText);
        bigCalendarView = view.findViewById(R.id.bigCalendarView);

        tvSelectedDateTitle = view.findViewById(R.id.tvSelectedDateTitle);
        tvSelectedStatusBadge = view.findViewById(R.id.tvSelectedStatusBadge);
        tvSelectedPunchIn = view.findViewById(R.id.tvSelectedPunchIn);
        tvSelectedPunchOut = view.findViewById(R.id.tvSelectedPunchOut);
        tvSelectedNetHours = view.findViewById(R.id.tvSelectedNetHours);
        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        tvSelectedTag = view.findViewById(R.id.tvSelectedTag);

        cardPermissionDetail = view.findViewById(R.id.cardPermissionDetail);
        tvPermissionHeader = view.findViewById(R.id.tvPermissionHeader);
        tvPermissionStatusBadge = view.findViewById(R.id.tvPermissionStatusBadge);
        tvPermissionTime = view.findViewById(R.id.tvPermissionTime);
        tvPermissionDuration = view.findViewById(R.id.tvPermissionDuration);
        tvPermissionReason = view.findViewById(R.id.tvPermissionReason);

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> showMonthFilterDialog());
        }
        if (btnNotificationContainer != null) {
            btnNotificationContainer.setOnClickListener(v -> showNotifications());
        }
        if (btnFilterMonth != null) {
            btnFilterMonth.setOnClickListener(v -> showMonthFilterDialog());
        }
    }

    private void updateMonthTitle() {
        String monthName = new DateFormatSymbols(Locale.US).getMonths()[currentMonth];
        if (tvCalendarMonthTitle != null) {
            tvCalendarMonthTitle.setText(monthName + " " + currentYear + " Calendar");
        }
        if (tvFilterMonthText != null) {
            tvFilterMonthText.setText(monthName.substring(0, 3) + " " + currentYear);
        }
    }

    private void setupHeaderData() {
        String empName = userStore.getEmpName();
        String empId = userStore.getEmpId();
        String storeName = userStore.getStoreName();

        if (empName == null || empName.trim().isEmpty()) {
            empName = "Arthi T";
        }

        if (tvEmpName != null) {
            tvEmpName.setText("Hi, " + empName);
        }

        if (tvSubTitle != null) {
            tvSubTitle.setText("Employee ID: #" + (empId != null ? empId : "E36") + " • " + (storeName != null ? storeName : "Yard #4"));
        }

        if (tvAvatarInitials != null) {
            String[] parts = empName.split(" ");
            if (parts.length >= 2 && parts[1].length() > 0) {
                tvAvatarInitials.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
            } else {
                tvAvatarInitials.setText("AT");
            }
        }
    }

    private void setupSummaryPills() {
        fetchEmployeeDetailFromApi(currentYear, currentMonth);
    }

    private void fetchEmployeeDetailFromApi(int year, int month) {
        // 1. INSTANT LOCAL CACHE LOAD (<2ms response for zero loading lag)
        String cachedJson = userStore.getCachedEmployeeDetail(year, month);
        if (cachedJson != null && !cachedJson.trim().isEmpty()) {
            try {
                JSONObject cachedObj = new JSONObject(cachedJson);
                parseAndApplyEmployeeDetail(cachedObj);
            } catch (Exception ignored) {}
        }

        // 2. BACKGROUND SERVER SYNC (Seamless background refresh)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String fromStr = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        String toStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, maxDays);

        Map<String, String> params = new HashMap<>();
        int staffId = userStore.getStaffId();
        params.put("staffid", String.valueOf(staffId > 0 ? staffId : 38));
        params.put("from", fromStr);
        params.put("to", toStr);

        ApiClient.postRequest(ApiConfig.EMPLOYEE_DETAIL_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (!isAdded()) return;
                userStore.cacheEmployeeDetail(year, month, response.toString());
                parseAndApplyEmployeeDetail(response);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Log.e("ManageAttendance", "API Error: " + errorMessage);
            }
        });
    }

    private void parseAndApplyEmployeeDetail(JSONObject response) {
        if (response == null) return;
        try {
            boolean success = response.optBoolean("success", false);
            if (!success) return;

            // 1. Employee Profile Header
            JSONObject empObj = response.optJSONObject("employee");
            if (empObj != null) {
                String firstName = empObj.optString("first_name", "");
                String lastName = empObj.optString("last_name", "");
                String empNum = empObj.optString("employee_number", "E36");
                String role = empObj.optString("userrole", "employee");

                String fullName = (firstName + " " + lastName).trim();
                if (fullName.isEmpty()) fullName = userStore.getEmpName();

                if (tvEmpName != null) {
                    tvEmpName.setText("Hi, " + fullName);
                }
                if (tvSubTitle != null) {
                    tvSubTitle.setText("Emp No: #" + empNum + " • Role: " + role.toUpperCase());
                }
                if (tvAvatarInitials != null && fullName.length() > 0) {
                    String[] parts = fullName.split(" ");
                    if (parts.length >= 2 && parts[1].length() > 0) {
                        tvAvatarInitials.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
                    } else {
                        tvAvatarInitials.setText(("" + parts[0].charAt(0)).toUpperCase());
                    }
                }
            }

            // 2. Reset Data Maps
            attendanceMap.clear();
            holidayMap.clear();
            permissionMap.clear();

            // 3. Parse Attendance Array (Code P: Present, Code A: Absent)
            JSONArray attArray = response.optJSONArray("attendance");
            int presentCountFromList = 0;
            int absentCountFromList = 0;

            if (attArray != null) {
                for (int i = 0; i < attArray.length(); i++) {
                    JSONObject attObj = attArray.optJSONObject(i);
                    if (attObj != null) {
                        int day = parseDayFromDate(attObj.optString("date"));
                        if (day > 0) {
                            attendanceMap.put(day, attObj);
                            String code = attObj.optString("code", "P");
                            if ("A".equalsIgnoreCase(code)) {
                                absentCountFromList++;
                            } else {
                                presentCountFromList++;
                            }
                        }
                    }
                }
            }

            // 4. Parse Holidays Array (Public & Company Holidays)
            JSONArray holArray = response.optJSONArray("holidays");
            if (holArray != null) {
                for (int i = 0; i < holArray.length(); i++) {
                    JSONObject holObj = holArray.optJSONObject(i);
                    if (holObj != null) {
                        int day = parseDayFromDate(holObj.optString("date"));
                        if (day > 0) holidayMap.put(day, holObj);
                    }
                }
            }

            // 5. Parse Permissions Array
            JSONArray permArray = response.optJSONArray("permissions");
            if (permArray != null) {
                for (int i = 0; i < permArray.length(); i++) {
                    JSONObject permObj = permArray.optJSONObject(i);
                    if (permObj != null) {
                        int day = parseDayFromDate(permObj.optString("date"));
                        if (day > 0) permissionMap.put(day, permObj);
                    }
                }
            }

            // 6. Update Monthly Metric Summary Pills
            JSONObject summary = response.optJSONObject("summary");
            int presentCount = summary != null ? summary.optInt("P", presentCountFromList) : presentCountFromList;
            int absentCount = summary != null ? summary.optInt("A", absentCountFromList) : absentCountFromList;
            int holidayCount = holidayMap.size();

            if (tvCountPresent != null) tvCountPresent.setText(String.valueOf(presentCount));
            if (tvCountMissing != null) tvCountMissing.setText(String.valueOf(absentCount));
            if (tvCountLeave != null) tvCountLeave.setText("0");
            if (tvCountHoliday != null) tvCountHoliday.setText(String.valueOf(holidayCount));

            // 7. Update BigMonthlyCalendarView Grid (P = Green, A = Red, Holiday = Purple, Permission = Amber Dot)
            updateCalendarStatusesFromApi();

            // 8. Refresh detail sheet for current selected day
            int selected = bigCalendarView != null ? bigCalendarView.getSelectedDay() : Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            BigMonthlyCalendarView.DayStatus status = bigCalendarView != null ?
                    bigCalendarView.getSelectedDayStatus() : BigMonthlyCalendarView.DayStatus.FUTURE;
            updateSelectedDayDetail(selected, status);
        } catch (Exception e) {
            Log.e("ManageAttendance", "Parse error", e);
        }
    }

    private void updateCalendarStatusesFromApi() {
        if (bigCalendarView == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Map<Integer, BigMonthlyCalendarView.DayStatus> statusMap = new HashMap<>();

        for (int day = 1; day <= maxDays; day++) {
            if (attendanceMap.containsKey(day)) {
                JSONObject att = attendanceMap.get(day);
                String code = att != null ? att.optString("code", "P") : "P";
                if ("A".equalsIgnoreCase(code)) {
                    // Code A = Absent (Red Circle)
                    statusMap.put(day, BigMonthlyCalendarView.DayStatus.ABSENT);
                } else if ("L".equalsIgnoreCase(code)) {
                    statusMap.put(day, BigMonthlyCalendarView.DayStatus.LEAVE);
                } else {
                    // Code P = Present (Green Circle)
                    statusMap.put(day, BigMonthlyCalendarView.DayStatus.PRESENT);
                }
            } else if (holidayMap.containsKey(day)) {
                // Official Holiday (Purple Circle)
                statusMap.put(day, BigMonthlyCalendarView.DayStatus.HOLIDAY);
            } else {
                // Check if day is Sunday
                cal.set(Calendar.DAY_OF_MONTH, day);
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    statusMap.put(day, BigMonthlyCalendarView.DayStatus.OFF_DAY);
                } else {
                    statusMap.put(day, BigMonthlyCalendarView.DayStatus.FUTURE);
                }
            }
        }

        bigCalendarView.setMonthStatusMap(statusMap);
        bigCalendarView.setPermissionDays(permissionMap.keySet());
    }

    private void setupCalendarInteractivity() {
        if (bigCalendarView != null) {
            bigCalendarView.setOnDateSelectedListener((day, month, year, status) -> {
                updateSelectedDayDetail(day, status);
            });
        }
    }

    private void updateSelectedDayDetail(int day, BigMonthlyCalendarView.DayStatus status) {
        String monthName = new DateFormatSymbols(Locale.US).getMonths()[currentMonth];
        if (tvSelectedDateTitle != null) {
            tvSelectedDateTitle.setText(monthName + " " + day + ", " + currentYear);
        }

        if (tvSelectedStatusBadge == null) return;

        JSONObject attObj = attendanceMap.get(day);
        JSONObject holObj = holidayMap.get(day);
        JSONObject permObj = permissionMap.get(day);

        // 1. Update Selected Attendance Card
        if (attObj != null) {
            String code = attObj.optString("code", "P");
            String inTime = formatTime12Hr(attObj.optString("in_time"));
            String outTime = formatTime12Hr(attObj.optString("out_time"));

            if ("A".equalsIgnoreCase(code)) {
                tvSelectedStatusBadge.setText("● ABSENT / MISSING LOG");
                tvSelectedStatusBadge.setTextColor(Color.parseColor("#B91C1C"));
                tvSelectedStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                if (tvSelectedPunchIn != null) tvSelectedPunchIn.setText("--:--");
                if (tvSelectedPunchOut != null) tvSelectedPunchOut.setText("--:--");
                if (tvSelectedNetHours != null) tvSelectedNetHours.setText("0h 00m");
                if (tvSelectedLocation != null) tvSelectedLocation.setText("No punch log recorded");
                if (tvSelectedTag != null) {
                    tvSelectedTag.setText("⚠ Action Req.");
                    tvSelectedTag.setTextColor(Color.parseColor("#B91C1C"));
                }
            } else {
                tvSelectedStatusBadge.setText("● ATTENDED / PRESENT");
                tvSelectedStatusBadge.setTextColor(Color.parseColor("#15803D"));
                tvSelectedStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                if (tvSelectedPunchIn != null) tvSelectedPunchIn.setText(inTime);
                if (tvSelectedPunchOut != null) tvSelectedPunchOut.setText(outTime);
                if (tvSelectedNetHours != null) tvSelectedNetHours.setText("Shift Logged");
                if (tvSelectedLocation != null) {
                    if (holObj != null) {
                        tvSelectedLocation.setText("Verified • Holiday: " + holObj.optString("name"));
                    } else {
                        tvSelectedLocation.setText("Geofence: Yard #4 Verified");
                    }
                }
                if (tvSelectedTag != null) {
                    tvSelectedTag.setText("✓ Verified");
                    tvSelectedTag.setTextColor(Color.parseColor("#059669"));
                }
            }
        } else if (holObj != null) {
            String holName = holObj.optString("name", "Company Holiday");
            String holType = holObj.optString("type", "Company");
            tvSelectedStatusBadge.setText("● HOLIDAY: " + holName.toUpperCase());
            tvSelectedStatusBadge.setTextColor(Color.parseColor("#7E22CE"));
            tvSelectedStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E8FF")));
            if (tvSelectedPunchIn != null) tvSelectedPunchIn.setText("--:--");
            if (tvSelectedPunchOut != null) tvSelectedPunchOut.setText("--:--");
            if (tvSelectedNetHours != null) tvSelectedNetHours.setText("Holiday");
            if (tvSelectedLocation != null) tvSelectedLocation.setText(holName + " (" + holType + " Holiday)");
            if (tvSelectedTag != null) {
                tvSelectedTag.setText("Official Holiday");
                tvSelectedTag.setTextColor(Color.parseColor("#7E22CE"));
            }
        } else {
            renderDefaultDayDetail(day, status);
        }

        // 2. Update Permission Detail Card below Attendance Card
        updatePermissionCard(permObj);
    }

    private void updatePermissionCard(JSONObject permObj) {
        if (cardPermissionDetail == null) return;

        if (permObj != null) {
            cardPermissionDetail.setVisibility(View.VISIBLE);

            String fromTime = formatTime12Hr(permObj.optString("from_time"));
            String toTime = formatTime12Hr(permObj.optString("to_time"));
            int minutes = permObj.optInt("permission_minutes", 0);
            String reason = permObj.optString("reason", "Late Arrival Permission");
            String approvalStatus = permObj.optString("approval_status", "APPROVED");

            if (tvPermissionHeader != null) {
                tvPermissionHeader.setText("Late-Arrival Permission");
            }
            if (tvPermissionStatusBadge != null) {
                tvPermissionStatusBadge.setText("● " + approvalStatus.toUpperCase());
                if ("APPROVED".equalsIgnoreCase(approvalStatus)) {
                    tvPermissionStatusBadge.setTextColor(Color.parseColor("#D97706"));
                    tvPermissionStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                } else {
                    tvPermissionStatusBadge.setTextColor(Color.parseColor("#64748B"));
                    tvPermissionStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                }
            }
            if (tvPermissionTime != null) {
                tvPermissionTime.setText(fromTime + " - " + toTime);
            }
            if (tvPermissionDuration != null) {
                tvPermissionDuration.setText(minutes + " Mins");
            }
            if (tvPermissionReason != null) {
                tvPermissionReason.setText("Reason: " + reason);
            }
        } else {
            cardPermissionDetail.setVisibility(View.GONE);
        }
    }

    private void renderDefaultDayDetail(int day, BigMonthlyCalendarView.DayStatus status) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        cal.set(Calendar.DAY_OF_MONTH, day);

        if (status == BigMonthlyCalendarView.DayStatus.OFF_DAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            tvSelectedStatusBadge.setText("● SUNDAY OFF");
            tvSelectedStatusBadge.setTextColor(Color.parseColor("#64748B"));
            tvSelectedStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            if (tvSelectedPunchIn != null) tvSelectedPunchIn.setText("--:--");
            if (tvSelectedPunchOut != null) tvSelectedPunchOut.setText("--:--");
            if (tvSelectedNetHours != null) tvSelectedNetHours.setText("Off Day");
            if (tvSelectedLocation != null) tvSelectedLocation.setText("Weekly Sunday Rest");
            if (tvSelectedTag != null) {
                tvSelectedTag.setText("Day Off");
                tvSelectedTag.setTextColor(Color.parseColor("#64748B"));
            }
        } else {
            tvSelectedStatusBadge.setText("● NO PUNCH LOG");
            tvSelectedStatusBadge.setTextColor(Color.parseColor("#64748B"));
            tvSelectedStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            if (tvSelectedPunchIn != null) tvSelectedPunchIn.setText("--:--");
            if (tvSelectedPunchOut != null) tvSelectedPunchOut.setText("--:--");
            if (tvSelectedNetHours != null) tvSelectedNetHours.setText("--");
            if (tvSelectedLocation != null) tvSelectedLocation.setText("No attendance log recorded for this date");
            if (tvSelectedTag != null) {
                tvSelectedTag.setText("No Data");
                tvSelectedTag.setTextColor(Color.parseColor("#94A3B8"));
            }
        }
    }

    private void showMonthFilterDialog() {
        PremiumAlertDialog.showMonthPicker(requireContext(), currentYear, currentMonth, which -> {
            currentMonth = which; // 0..11

            if (bigCalendarView != null) {
                bigCalendarView.setYearMonth(currentYear, currentMonth);
                bigCalendarView.setSelectedDay(1);
            }

            updateMonthTitle();
            fetchEmployeeDetailFromApi(currentYear, currentMonth);
        });
    }

    private int parseDayFromDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private String formatTime12Hr(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || timeStr.equalsIgnoreCase("null")) {
            return "--:--";
        }
        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                String ampm = hour >= 12 ? "PM" : "AM";
                int hour12 = hour % 12;
                if (hour12 == 0) hour12 = 12;
                return String.format(Locale.US, "%02d:%02d %s", hour12, minute, ampm);
            }
        } catch (Exception ignored) {}
        return timeStr;
    }

    private void showNotifications() {
        String monthName = new DateFormatSymbols(Locale.US).getMonths()[currentMonth];
        PremiumAlertDialog.showNotifications(requireContext(), permissionMap.values(), holidayMap.values(), monthName, currentYear);
    }
}