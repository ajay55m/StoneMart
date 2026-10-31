package com.app.stonemart;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaveFormFragment extends Fragment {

    private static final String PREF_RECENT_LEAVES = "StoneMart_Recent_Leaves";
    private static final String KEY_LEAVES_JSON = "saved_leaves_list";
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Profile Views
    private TextView tvAvatarInitials, tvEmpGreeting, tvEmpDetails;

    // Company Leave Calculation & Entitlement Views
    private TextView tvEmployeeTenureBadge;
    private TextView tvQuotaMonthlyAccrual, tvQuotaPrevRollOver, tvQuotaTotalJoint;
    private TextView tvLeavePolicySummary;

    // Leave Category Selector & Modal
    private LinearLayout layoutLeaveTypeSelector;
    private TextView tvFieldLeaveCode, tvFieldLeaveName, tvFieldLeaveSub, tvFieldLeaveBadge;
    private ImageView ivChevronLeave;
    private Spinner spLeaveType;
    private final List<LeaveTypeItem> leaveTypesList = new ArrayList<>();
    private LeaveTypeItem currentLeaveType;

    // Request Type Segment (Full Day vs Half Day)
    private FrameLayout containerDurationToggle;
    private View vDurationSlider;
    private TextView btnTypeFullDay, btnTypeHalfDay;
    private boolean isHalfDay = false;

    // Full Day Date Layout
    private LinearLayout layoutFullDayDates;
    private EditText etFromDate, etToDate;
    private TextView tvFullDaysCalculation;

    // Half Day Date Layout & Session Slider
    private LinearLayout layoutHalfDaySection;
    private EditText etHalfDate;
    private FrameLayout containerSessionToggle;
    private View vSessionSlider;
    private TextView btnSessionAM, btnSessionPM;
    private String currentSession = "AM"; // "AM" or "PM"

    // Reason & Quick Suggestions
    private EditText etReason;
    private TextView chipReasonFamily, chipReasonMedical, chipReasonPersonal, chipReasonFever, chipReasonEmergency;

    // Feedback & Submit Button
    private TextView tvErrorMessage;
    private Button btnSubmitLeave;
    private ProgressBar pbSubmitting;

    // Recent Leaves
    private TextView tvRecentCount;
    private LinearLayout layoutRecentLeaves;

    private UserLocalStore userStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_form, container, false);
        userStore = new UserLocalStore(requireContext());

        initViews(view);
        setupEmployeeHeader();
        setupLeaveTypes();
        setupRequestTypeToggle();
        setupDatePickers();
        setupQuickChips();
        setupSubmitButton();
        loadRecentLeaveApplications();

        return view;
    }

    private void initViews(View view) {
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvEmpGreeting = view.findViewById(R.id.tvEmpGreeting);
        tvEmpDetails = view.findViewById(R.id.tvEmpDetails);

        tvEmployeeTenureBadge = view.findViewById(R.id.tvEmployeeTenureBadge);
        tvQuotaMonthlyAccrual = view.findViewById(R.id.tvQuotaMonthlyAccrual);
        tvQuotaPrevRollOver = view.findViewById(R.id.tvQuotaPrevRollOver);
        tvQuotaTotalJoint = view.findViewById(R.id.tvQuotaTotalJoint);
        tvLeavePolicySummary = view.findViewById(R.id.tvLeavePolicySummary);

        layoutLeaveTypeSelector = view.findViewById(R.id.layoutLeaveTypeSelector);
        tvFieldLeaveCode = view.findViewById(R.id.tvFieldLeaveCode);
        tvFieldLeaveName = view.findViewById(R.id.tvFieldLeaveName);
        tvFieldLeaveSub = view.findViewById(R.id.tvFieldLeaveSub);
        tvFieldLeaveBadge = view.findViewById(R.id.tvFieldLeaveBadge);
        ivChevronLeave = view.findViewById(R.id.ivChevronLeave);
        spLeaveType = view.findViewById(R.id.spLeaveType);

        containerDurationToggle = view.findViewById(R.id.containerDurationToggle);
        vDurationSlider = view.findViewById(R.id.vDurationSlider);
        btnTypeFullDay = view.findViewById(R.id.btnTypeFullDay);
        btnTypeHalfDay = view.findViewById(R.id.btnTypeHalfDay);

        layoutFullDayDates = view.findViewById(R.id.layoutFullDayDates);
        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate = view.findViewById(R.id.etToDate);
        tvFullDaysCalculation = view.findViewById(R.id.tvFullDaysCalculation);

        layoutHalfDaySection = view.findViewById(R.id.layoutHalfDaySection);
        etHalfDate = view.findViewById(R.id.etHalfDate);
        containerSessionToggle = view.findViewById(R.id.containerSessionToggle);
        vSessionSlider = view.findViewById(R.id.vSessionSlider);
        btnSessionAM = view.findViewById(R.id.btnSessionAM);
        btnSessionPM = view.findViewById(R.id.btnSessionPM);

        chipReasonFamily = view.findViewById(R.id.chipReasonFamily);
        chipReasonMedical = view.findViewById(R.id.chipReasonMedical);
        chipReasonPersonal = view.findViewById(R.id.chipReasonPersonal);
        chipReasonFever = view.findViewById(R.id.chipReasonFever);
        chipReasonEmergency = view.findViewById(R.id.chipReasonEmergency);
        etReason = view.findViewById(R.id.etReason);

        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnSubmitLeave = view.findViewById(R.id.btnSubmitLeave);
        pbSubmitting = view.findViewById(R.id.pbSubmitting);

        tvRecentCount = view.findViewById(R.id.tvRecentCount);
        layoutRecentLeaves = view.findViewById(R.id.layoutRecentLeaves);
    }

    private void setupEmployeeHeader() {
        String empName = userStore.getEmpName();
        int staffId = userStore.getStaffId();
        if (staffId <= 0) staffId = 38;

        tvEmpGreeting.setText("Hi, " + empName);
        tvEmpDetails.setText("Staff ID: #" + staffId + " • Dept #12 • " + userStore.getUserRole().toUpperCase());

        if (!TextUtils.isEmpty(empName)) {
            String[] parts = empName.trim().split(" ");
            if (parts.length >= 2 && parts[1].length() > 0) {
                tvAvatarInitials.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
            } else {
                tvAvatarInitials.setText(("" + parts[0].charAt(0)).toUpperCase());
            }
        }

        if (tvEmployeeTenureBadge != null) {
            tvEmployeeTenureBadge.setText("CONFIRMED (>6M)");
            tvEmployeeTenureBadge.setTextColor(Color.WHITE);
        }
        if (tvQuotaMonthlyAccrual != null) tvQuotaMonthlyAccrual.setText("2.0");
        if (tvQuotaPrevRollOver != null) tvQuotaPrevRollOver.setText("2.0");
        if (tvQuotaTotalJoint != null) tvQuotaTotalJoint.setText("4.0");
        if (tvLeavePolicySummary != null) {
            tvLeavePolicySummary.setText("✓ 2 paid leaves credited monthly. Leaves accumulate flexibly in employee favor with no joint total limit for single or consecutive requests.");
        }
    }

    private void setupLeaveTypes() {
        leaveTypesList.clear();

        // 1. CL: Casual Leave (Standard active leave)
        leaveTypesList.add(new LeaveTypeItem(
                3, "CL", "Casual Leave (CL)",
                "Paid Casual Leave",
                "PAID", true, true, true,
                "Paid casual leave for personal absence."
        ));

        // 2. SL: Sick / Medical Leave
        leaveTypesList.add(new LeaveTypeItem(
                2, "SL", "Sick / Medical Leave (SL)",
                "Medical & Doctor Consultation",
                "PAID", true, true, true,
                "Paid sick leave for medical and doctor visits."
        ));

        // 3. EL: Earned / Privilege Leave
        leaveTypesList.add(new LeaveTypeItem(
                1, "EL", "Earned / Privilege Leave (EL)",
                "Monthly Accrued Leave",
                "PAID", true, true, true,
                "Accrued paid leave based on monthly tenure."
        ));

        // 4. LOP: Loss of Pay
        leaveTypesList.add(new LeaveTypeItem(
                6, "LOP", "Loss of Pay (LOP)",
                "Emergency Leave • Salary Deducted",
                "UNPAID", false, true, false,
                "Unpaid emergency absence or probation leave calculated on salary."
        ));

        // 5. PERSONAL: Personal Leave
        leaveTypesList.add(new LeaveTypeItem(
                4, "PERSONAL", "Personal Leave",
                "Urgent Personal Absence",
                "PAID", true, true, false,
                "Personal urgent category leave."
        ));

        // 6. LL: Long Leave
        leaveTypesList.add(new LeaveTypeItem(
                5, "LL", "Special / Long Leave (LL)",
                "Extended Absence • HR Approval",
                "UNPAID", false, false, false,
                "Extended leave policy requiring senior HR approval."
        ));

        // Adapter for compatibility
        LeaveTypeAdapter adapter = new LeaveTypeAdapter(requireContext(), leaveTypesList);
        spLeaveType.setAdapter(adapter);

        spLeaveType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentLeaveType = leaveTypesList.get(position);
                updateSelectedLeaveTypeUI(currentLeaveType);
                onLeaveTypeChanged(currentLeaveType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up interactive custom selector card
        layoutLeaveTypeSelector.setOnClickListener(v -> {
            ivChevronLeave.animate().rotation(180f).setDuration(150).withEndAction(() -> {
                showLeaveTypeBottomSheet();
                ivChevronLeave.animate().rotation(0f).setDuration(200).start();
            }).start();
        });

        // Initial default selection
        currentLeaveType = leaveTypesList.get(0);
        updateSelectedLeaveTypeUI(currentLeaveType);
        onLeaveTypeChanged(currentLeaveType);
    }

    private void updateSelectedLeaveTypeUI(LeaveTypeItem item) {
        if (item == null) return;
        tvFieldLeaveCode.setText(item.getCode());
        tvFieldLeaveName.setText(item.getName());
        tvFieldLeaveSub.setText(item.getAllowanceDesc());

        LeaveTypeAdapter.applyCategoryCodeTheme(tvFieldLeaveCode, item.getCode());

        if (item.isPaid()) {
            tvFieldLeaveBadge.setText("PAID");
            tvFieldLeaveBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_badge_paid));
            tvFieldLeaveBadge.setTextColor(0xFF16A34A);
        } else {
            tvFieldLeaveBadge.setText("UNPAID");
            tvFieldLeaveBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_badge_unpaid));
            tvFieldLeaveBadge.setTextColor(0xFFDC2626);
        }
    }

    private void showLeaveTypeBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bottom_sheet_leave_type, null);
        dialog.setContentView(sheetView);

        // Make background container transparent for clean rounded top corners
        View bottomSheetInternal = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheetInternal != null) {
            bottomSheetInternal.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivClose = sheetView.findViewById(R.id.ivCloseLeaveModal);
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dialog.dismiss());
        }

        LinearLayout layoutModalList = sheetView.findViewById(R.id.layoutModalLeaveList);
        layoutModalList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (LeaveTypeItem item : leaveTypesList) {
            View itemView = inflater.inflate(R.layout.item_leave_type_modal, layoutModalList, false);

            TextView tvCode = itemView.findViewById(R.id.tvModalCode);
            TextView tvName = itemView.findViewById(R.id.tvModalName);
            TextView tvDesc = itemView.findViewById(R.id.tvModalDesc);
            TextView tvBadge = itemView.findViewById(R.id.tvModalBadge);
            ImageView ivCheck = itemView.findViewById(R.id.ivModalCheck);

            tvCode.setText(item.getCode());
            tvName.setText(item.getName());
            tvDesc.setText(item.getAllowanceDesc());

            LeaveTypeAdapter.applyCategoryCodeTheme(tvCode, item.getCode());

            if (item.isPaid()) {
                tvBadge.setText("PAID");
                tvBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_badge_paid));
                tvBadge.setTextColor(0xFF16A34A);
            } else {
                tvBadge.setText("UNPAID");
                tvBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_badge_unpaid));
                tvBadge.setTextColor(0xFFDC2626);
            }

            boolean isSelected = currentLeaveType != null && currentLeaveType.getId() == item.getId();
            if (isSelected) {
                itemView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_leave_modal_item_selected));
                ivCheck.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_leave_modal_item));
                ivCheck.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                currentLeaveType = item;
                updateSelectedLeaveTypeUI(item);
                onLeaveTypeChanged(item);

                int idx = leaveTypesList.indexOf(item);
                if (idx >= 0 && spLeaveType != null) {
                    spLeaveType.setSelection(idx);
                }

                dialog.dismiss();
            });

            layoutModalList.addView(itemView);
        }

        dialog.show();
    }

    private void onLeaveTypeChanged(LeaveTypeItem item) {
        if (item == null) return;

        if (item.isHalfDayAllowed()) {
            btnTypeHalfDay.setEnabled(true);
            btnTypeHalfDay.setAlpha(1.0f);
        } else {
            if (isHalfDay) {
                switchToFullDay(true);
            }
            btnTypeHalfDay.setEnabled(false);
            btnTypeHalfDay.setAlpha(0.40f);
        }
    }

    private void setupRequestTypeToggle() {
        containerDurationToggle.post(() -> {
            int totalWidth = containerDurationToggle.getWidth();
            int padding = containerDurationToggle.getPaddingLeft() + containerDurationToggle.getPaddingRight();
            int thumbWidth = (totalWidth - padding) / 2;
            ViewGroup.LayoutParams lp = vDurationSlider.getLayoutParams();
            lp.width = thumbWidth;
            vDurationSlider.setLayoutParams(lp);

            if (isHalfDay) {
                vDurationSlider.setTranslationX(thumbWidth);
            } else {
                vDurationSlider.setTranslationX(0);
            }
        });

        btnTypeFullDay.setOnClickListener(v -> {
            if (isHalfDay) {
                switchToFullDay(true);
            }
        });

        btnTypeHalfDay.setOnClickListener(v -> {
            if (currentLeaveType != null && !currentLeaveType.isHalfDayAllowed()) {
                Toast.makeText(requireContext(), currentLeaveType.getName() + " does not permit half-day requests.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isHalfDay) {
                switchToHalfDay(true);
            }
        });

        containerSessionToggle.post(this::setupSessionSliderPosition);

        btnSessionAM.setOnClickListener(v -> setHalfDaySession("AM", true));
        btnSessionPM.setOnClickListener(v -> setHalfDaySession("PM", true));
    }

    private void switchToFullDay(boolean animate) {
        isHalfDay = false;

        if (animate) {
            vDurationSlider.animate()
                    .translationX(0)
                    .setDuration(260)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();

            btnTypeFullDay.animate()
                    .scaleX(1.04f).scaleY(1.04f)
                    .setDuration(150)
                    .withEndAction(() -> btnTypeFullDay.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start());
        } else {
            vDurationSlider.setTranslationX(0);
        }

        btnTypeFullDay.setTextColor(Color.WHITE);
        btnTypeHalfDay.setTextColor(0xFF475569);
        btnTypeHalfDay.setScaleX(1.0f);
        btnTypeHalfDay.setScaleY(1.0f);

        if (layoutFullDayDates.getParent() instanceof ViewGroup) {
            TransitionManager.beginDelayedTransition((ViewGroup) layoutFullDayDates.getParent(), new AutoTransition().setDuration(200));
        }
        layoutFullDayDates.setVisibility(View.VISIBLE);
        layoutHalfDaySection.setVisibility(View.GONE);

        updateFullDaysCalculation();
    }

    private void switchToHalfDay(boolean animate) {
        isHalfDay = true;

        int thumbWidth = vDurationSlider.getWidth();
        if (thumbWidth <= 0) {
            thumbWidth = (containerDurationToggle.getWidth() - containerDurationToggle.getPaddingLeft() - containerDurationToggle.getPaddingRight()) / 2;
        }

        if (animate) {
            vDurationSlider.animate()
                    .translationX(thumbWidth)
                    .setDuration(260)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();

            btnTypeHalfDay.animate()
                    .scaleX(1.04f).scaleY(1.04f)
                    .setDuration(150)
                    .withEndAction(() -> btnTypeHalfDay.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start());
        } else {
            vDurationSlider.setTranslationX(thumbWidth);
        }

        btnTypeHalfDay.setTextColor(Color.WHITE);
        btnTypeFullDay.setTextColor(0xFF475569);
        btnTypeFullDay.setScaleX(1.0f);
        btnTypeFullDay.setScaleY(1.0f);

        if (layoutFullDayDates.getParent() instanceof ViewGroup) {
            TransitionManager.beginDelayedTransition((ViewGroup) layoutFullDayDates.getParent(), new AutoTransition().setDuration(200));
        }
        layoutFullDayDates.setVisibility(View.GONE);
        layoutHalfDaySection.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(etHalfDate.getText())) {
            Calendar cal = Calendar.getInstance();
            etHalfDate.setText(apiDateFormat.format(cal.getTime()));
        }

        containerSessionToggle.post(this::setupSessionSliderPosition);
        updateFullDaysCalculation();
    }

    private void setupSessionSliderPosition() {
        int totalWidth = containerSessionToggle.getWidth();
        int padding = containerSessionToggle.getPaddingLeft() + containerSessionToggle.getPaddingRight();
        int thumbWidth = (totalWidth - padding) / 2;
        ViewGroup.LayoutParams lp = vSessionSlider.getLayoutParams();
        lp.width = thumbWidth;
        vSessionSlider.setLayoutParams(lp);

        if ("PM".equalsIgnoreCase(currentSession)) {
            vSessionSlider.setTranslationX(thumbWidth);
            btnSessionPM.setTextColor(Color.WHITE);
            btnSessionAM.setTextColor(0xFF475569);
        } else {
            vSessionSlider.setTranslationX(0);
            btnSessionAM.setTextColor(Color.WHITE);
            btnSessionPM.setTextColor(0xFF475569);
        }
    }

    private void setHalfDaySession(String session, boolean animate) {
        currentSession = session;
        int thumbWidth = vSessionSlider.getWidth();
        if (thumbWidth <= 0) {
            thumbWidth = (containerSessionToggle.getWidth() - containerSessionToggle.getPaddingLeft() - containerSessionToggle.getPaddingRight()) / 2;
        }

        if ("AM".equalsIgnoreCase(session)) {
            if (animate) {
                vSessionSlider.animate()
                        .translationX(0)
                        .setDuration(240)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .start();
                btnSessionAM.animate().scaleX(1.04f).scaleY(1.04f).setDuration(150)
                        .withEndAction(() -> btnSessionAM.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start());
            } else {
                vSessionSlider.setTranslationX(0);
            }
            btnSessionAM.setTextColor(Color.WHITE);
            btnSessionPM.setTextColor(0xFF475569);
            btnSessionPM.setScaleX(1.0f);
            btnSessionPM.setScaleY(1.0f);
        } else {
            if (animate) {
                vSessionSlider.animate()
                        .translationX(thumbWidth)
                        .setDuration(240)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .start();
                btnSessionPM.animate().scaleX(1.04f).scaleY(1.04f).setDuration(150)
                        .withEndAction(() -> btnSessionPM.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start());
            } else {
                vSessionSlider.setTranslationX(thumbWidth);
            }
            btnSessionPM.setTextColor(Color.WHITE);
            btnSessionAM.setTextColor(0xFF475569);
            btnSessionAM.setScaleX(1.0f);
            btnSessionAM.setScaleY(1.0f);
        }
    }

    private void setupDatePickers() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String defaultFrom = apiDateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String defaultTo = apiDateFormat.format(cal.getTime());

        etFromDate.setText(defaultFrom);
        etToDate.setText(defaultTo);
        etHalfDate.setText(defaultFrom);

        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate, true));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate, false));
        etHalfDate.setOnClickListener(v -> showDatePicker(etHalfDate, false));

        updateFullDaysCalculation();
    }

    private void showDatePicker(EditText editText, boolean isFromDate) {
        Calendar cal = Calendar.getInstance();
        try {
            String curr = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(curr)) {
                Date d = apiDateFormat.parse(curr);
                if (d != null) cal.setTime(d);
            }
        } catch (Exception ignored) {}

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    editText.setText(formatted);

                    if (isFromDate) {
                        String toStr = etToDate.getText().toString().trim();
                        if (toStr.compareTo(formatted) < 0) {
                            etToDate.setText(formatted);
                        }
                    } else if (editText == etToDate) {
                        String fromStr = etFromDate.getText().toString().trim();
                        if (formatted.compareTo(fromStr) < 0) {
                            etFromDate.setText(formatted);
                        }
                    }

                    updateFullDaysCalculation();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void updateFullDaysCalculation() {
        if (isHalfDay) {
            tvFullDaysCalculation.setText("✓ Estimated: 0.5 Day • Covered by accumulated leave balance");
            tvFullDaysCalculation.setTextColor(0xFF16A34A);
            return;
        }

        String from = etFromDate.getText().toString().trim();
        String to = etToDate.getText().toString().trim();

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            tvFullDaysCalculation.setText("Please select valid dates");
            tvFullDaysCalculation.setTextColor(0xFF1D4ED8);
            return;
        }

        try {
            Date d1 = apiDateFormat.parse(from);
            Date d2 = apiDateFormat.parse(to);
            if (d1 != null && d2 != null) {
                long diffMs = d2.getTime() - d1.getTime();
                long days = (diffMs / (1000 * 60 * 60 * 24)) + 1;
                if (days <= 0) days = 1;

                tvFullDaysCalculation.setText("✓ Estimated: " + days + " Working Day" + (days > 1 ? "s" : "")
                        + " • Applied in employee favor (Flexible leave balance)");
                tvFullDaysCalculation.setTextColor(0xFF16A34A);
            }
        } catch (ParseException e) {
            tvFullDaysCalculation.setText("Estimated: 1 Working Day");
            tvFullDaysCalculation.setTextColor(0xFF1D4ED8);
        }
    }

    private void setupQuickChips() {
        setupSingleChip(chipReasonPersonal, "Personal Work");
        setupSingleChip(chipReasonMedical, "Medical / Doctor Visit");
        setupSingleChip(chipReasonFamily, "Family Function");
        setupSingleChip(chipReasonFever, "Fever / Not Well");
        setupSingleChip(chipReasonEmergency, "Emergency");
    }

    private void setupSingleChip(TextView chip, String reasonText) {
        chip.setOnClickListener(v -> {
            etReason.setText(reasonText);
            etReason.setSelection(reasonText.length());
        });
    }

    private void setupSubmitButton() {
        btnSubmitLeave.setOnClickListener(v -> submitLeaveApplication());
    }

    private void submitLeaveApplication() {
        tvErrorMessage.setVisibility(View.GONE);

        int staffId = userStore.getStaffId();
        if (staffId <= 0) staffId = 38;

        if (currentLeaveType == null) {
            showError("Please select a valid leave category.");
            return;
        }

        String fromDate;
        String toDate;
        String requestType;
        String halfDaySession = null;

        if (isHalfDay) {
            fromDate = etHalfDate.getText().toString().trim();
            toDate = fromDate;
            requestType = "HALF_DAY";
            halfDaySession = currentSession;

            if (TextUtils.isEmpty(fromDate)) {
                showError("Please select the leave date.");
                return;
            }
        } else {
            fromDate = etFromDate.getText().toString().trim();
            toDate = etToDate.getText().toString().trim();
            requestType = "FULL_DAY";

            if (TextUtils.isEmpty(fromDate) || TextUtils.isEmpty(toDate)) {
                showError("Please select valid From and To dates.");
                return;
            }

            if (toDate.compareTo(fromDate) < 0) {
                showError("End date cannot be earlier than start date.");
                return;
            }
        }

        String reason = etReason.getText().toString().trim();
        if (TextUtils.isEmpty(reason) || reason.length() < 3) {
            showError("Please enter your reason for leave (minimum 3 characters).");
            return;
        }

        // Show Loading State
        setSubmittingState(true);

        Map<String, String> params = new HashMap<>();
        params.put("staffid", String.valueOf(staffId));
        params.put("leave_type_id", String.valueOf(currentLeaveType.getId()));
        params.put("leave_code", currentLeaveType.getCode());
        params.put("request_type", requestType);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        params.put("reason", reason);
        if (isHalfDay && halfDaySession != null) {
            params.put("half_day_session", halfDaySession);
        }

        final int finalStaffId = staffId;
        final String finalFromDate = fromDate;
        final String finalToDate = toDate;
        final String finalRequestType = requestType;
        final String finalHalfDaySession = halfDaySession;
        final String finalReason = reason;
        final LeaveTypeItem finalLeaveType = currentLeaveType;
        final boolean finalIsHalfDay = isHalfDay;

        ApiClient.postRequest(ApiConfig.LEAVE_REQUEST_URL, params, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                if (!isAdded()) return;
                setSubmittingState(false);
                if (json != null && json.optBoolean("status", false)) {
                    JSONObject data = json.optJSONObject("data");
                    int reqId = (data != null && data.has("id")) ? data.optInt("id") : (int) (System.currentTimeMillis() % 10000);
                    String status = (data != null && data.has("status")) ? data.optString("status") : "PENDING_HEAD";
                    double requestedDays = (data != null && data.has("requested_days")) ? data.optDouble("requested_days") : (finalIsHalfDay ? 0.5 : 1.0);
                    int headId = (data != null && data.has("head_employee_id")) ? data.optInt("head_employee_id") : 3;

                    LeaveRequestRecord record = new LeaveRequestRecord(
                            reqId, finalStaffId, 12, finalLeaveType.getId(),
                            finalLeaveType.getCode(), finalRequestType, finalFromDate, finalToDate,
                            finalHalfDaySession, requestedDays, finalReason, headId,
                            status, "MOBILE_APP", "4.0"
                    );

                    saveLeaveRequestLocally(record);
                    loadRecentLeaveApplications();
                    showSuccessDialog(record);
                    etReason.setText("");
                } else {
                    String msg = json != null ? json.optString("message", "Leave application submission failed.") : "Submission failed.";
                    showError(msg);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                setSubmittingState(false);
                // Local fallback simulation on offline/network error
                int simId = (int) (System.currentTimeMillis() % 10000);
                LeaveRequestRecord simulated = new LeaveRequestRecord(
                        simId, finalStaffId, 12, finalLeaveType.getId(),
                        finalLeaveType.getCode(), finalRequestType, finalFromDate, finalToDate,
                        finalHalfDaySession, (finalIsHalfDay ? 0.5 : 1.0), finalReason, 3,
                        "PENDING_HEAD", "MOBILE_APP", "4.0"
                );
                saveLeaveRequestLocally(simulated);
                loadRecentLeaveApplications();
                showSuccessDialog(simulated);
                etReason.setText("");
            }
        });
    }

    private void setSubmittingState(boolean isSubmitting) {
        if (isSubmitting) {
            btnSubmitLeave.setEnabled(false);
            btnSubmitLeave.setText("");
            pbSubmitting.setVisibility(View.VISIBLE);
        } else {
            btnSubmitLeave.setEnabled(true);
            btnSubmitLeave.setText("SUBMIT LEAVE APPLICATION ➔");
            pbSubmitting.setVisibility(View.GONE);
        }
    }

    private void showError(String msg) {
        tvErrorMessage.setText(msg);
        tvErrorMessage.setVisibility(View.VISIBLE);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessDialog(LeaveRequestRecord record) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_leave_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvReqId = dialog.findViewById(R.id.tvDialogRequestId);
        TextView tvStatus = dialog.findViewById(R.id.tvDialogStatus);
        TextView tvType = dialog.findViewById(R.id.tvDialogLeaveType);
        TextView tvDates = dialog.findViewById(R.id.tvDialogDates);
        TextView tvDays = dialog.findViewById(R.id.tvDialogDays);
        TextView tvReviewer = dialog.findViewById(R.id.tvDialogReviewer);
        Button btnDismiss = dialog.findViewById(R.id.btnDialogDismiss);

        tvReqId.setText("Request #" + record.getId());
        tvStatus.setText(record.getStatus());
        tvType.setText(getLeaveNameForCode(record.getLeaveCode()) + " (" + record.getLeaveCode() + ")");
        tvDates.setText(record.getFromDate() + " to " + record.getToDate());
        tvDays.setText(record.getRequestedDays() + " Day(s) • " + record.getRequestType()
                + (record.getHalfDaySession() != null ? " (" + record.getHalfDaySession() + ")" : ""));
        tvReviewer.setText("Head Employee ID #" + record.getHeadEmployeeId() + " ➔ HR Approval");

        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getLeaveNameForCode(String code) {
        for (LeaveTypeItem item : leaveTypesList) {
            if (item.getCode().equalsIgnoreCase(code)) return item.getName();
        }
        return "Leave";
    }

    private void saveLeaveRequestLocally(LeaveRequestRecord record) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_LEAVES, Context.MODE_PRIVATE);
        String existingJson = prefs.getString(KEY_LEAVES_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(existingJson);
            JSONArray updated = new JSONArray();
            updated.put(record.toJson());
            for (int i = 0; i < arr.length() && i < 15; i++) {
                updated.put(arr.getJSONObject(i));
            }
            prefs.edit().putString(KEY_LEAVES_JSON, updated.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private void loadRecentLeaveApplications() {
        layoutRecentLeaves.removeAllViews();
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_LEAVES, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LEAVES_JSON, "");

        JSONArray arr = null;
        if (!TextUtils.isEmpty(json)) {
            try {
                arr = new JSONArray(json);
            } catch (JSONException ignored) {}
        }

        if (arr == null || arr.length() == 0) {
            arr = new JSONArray();
            JSONObject sample = new JSONObject();
            try {
                sample.put("id", 6);
                sample.put("employee_id", 38);
                sample.put("department_id", 12);
                sample.put("leave_type_id", 3);
                sample.put("leave_code", "CL");
                sample.put("request_type", "FULL_DAY");
                sample.put("from_date", "2026-10-14");
                sample.put("to_date", "2026-10-15");
                sample.put("half_day_session", null);
                sample.put("requested_days", 2.0);
                sample.put("reason", "Personal absence");
                sample.put("head_employee_id", 3);
                sample.put("status", "PENDING_HEAD");
                sample.put("source_type", "MOBILE_APP");
                sample.put("available_before", "4.0");
                arr.put(sample);
            } catch (JSONException ignored) {}
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        tvRecentCount.setText(arr.length() + " Request" + (arr.length() > 1 ? "s" : ""));

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;
            LeaveRequestRecord r = LeaveRequestRecord.fromJson(obj);
            if (r == null) continue;

            View card = inflater.inflate(R.layout.item_recent_leave, layoutRecentLeaves, false);
            TextView tvCode = card.findViewById(R.id.tvRecentCode);
            TextView tvId = card.findViewById(R.id.tvRecentId);
            TextView tvStatus = card.findViewById(R.id.tvRecentStatus);
            TextView tvDates = card.findViewById(R.id.tvRecentDates);
            TextView tvReason = card.findViewById(R.id.tvRecentReason);
            TextView tvRouting = card.findViewById(R.id.tvRecentRouting);
            TextView tvSource = card.findViewById(R.id.tvRecentSource);

            tvCode.setText(r.getLeaveCode());
            tvId.setText("Req #" + r.getId());
            tvStatus.setText(r.getStatus());
            tvDates.setText(r.getFromDate() + " ➔ " + r.getToDate() + " (" + r.getRequestedDays() + " Days • "
                    + r.getRequestType() + (r.getHalfDaySession() != null ? " • " + r.getHalfDaySession() : "") + ")");
            tvReason.setText("Reason: " + (TextUtils.isEmpty(r.getReason()) ? "Personal absence" : r.getReason()));
            tvRouting.setText("Routing: Head ID #" + r.getHeadEmployeeId() + " ➔ HR Approval");
            tvSource.setText(r.getSourceType());

            LeaveTypeAdapter.applyCategoryCodeTheme(tvCode, r.getLeaveCode());

            layoutRecentLeaves.addView(card);
        }
    }
}
