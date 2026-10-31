package com.app.stonemart;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.app.stonemart.core.Resource;
import com.app.stonemart.viewmodel.LeaveFormViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * LeaveFormFragment - MVVM View for leave applications.
 * Observes LeaveFormViewModel for categories, half-day toggling, validation, and submission states.
 */
public class LeaveFormFragment extends Fragment {

    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private LeaveFormViewModel viewModel;

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
    private LeaveTypeAdapter spinnerAdapter;

    // Request Type Segment (Full Day vs Half Day)
    private FrameLayout containerDurationToggle;
    private View vDurationSlider;
    private TextView btnTypeFullDay, btnTypeHalfDay;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_form, container, false);
        viewModel = new ViewModelProvider(this).get(LeaveFormViewModel.class);

        initViews(view);
        setupEmployeeHeader();
        setupLeaveTypes();
        setupRequestTypeToggle();
        setupDatePickers();
        setupQuickChips();
        setupSubmitButton();
        setupObservers();

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

        etReason = view.findViewById(R.id.etReason);
        chipReasonFamily = view.findViewById(R.id.chipReasonFamily);
        chipReasonMedical = view.findViewById(R.id.chipReasonMedical);
        chipReasonPersonal = view.findViewById(R.id.chipReasonPersonal);
        chipReasonFever = view.findViewById(R.id.chipReasonFever);
        chipReasonEmergency = view.findViewById(R.id.chipReasonEmergency);

        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnSubmitLeave = view.findViewById(R.id.btnSubmitLeave);
        pbSubmitting = view.findViewById(R.id.pbSubmitting);

        tvRecentCount = view.findViewById(R.id.tvRecentCount);
        layoutRecentLeaves = view.findViewById(R.id.layoutRecentLeaves);
    }

    private void setupObservers() {
        viewModel.leaveTypes.observe(getViewLifecycleOwner(), types -> {
            if (types == null) return;
            leaveTypesList.clear();
            leaveTypesList.addAll(types);
            if (spinnerAdapter != null) {
                spinnerAdapter.notifyDataSetChanged();
            }
        });

        viewModel.selectedLeaveType.observe(getViewLifecycleOwner(), item -> {
            if (item == null) return;
            updateSelectedLeaveTypeUI(item);
            onLeaveTypeChanged(item);
        });

        viewModel.isHalfDay.observe(getViewLifecycleOwner(), halfDay -> {
            if (halfDay == null) return;
            if (halfDay) {
                applyHalfDayUI(true);
            } else {
                applyFullDayUI(true);
            }
        });

        viewModel.halfDaySession.observe(getViewLifecycleOwner(), session -> {
            if (session == null) return;
            applySessionUI(session, true);
        });

        viewModel.submitResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    setSubmittingState(true);
                    break;
                case SUCCESS:
                    setSubmittingState(false);
                    if (resource.getData() != null) {
                        showSuccessDialog(resource.getData());
                    }
                    etReason.setText("");
                    break;
                case ERROR:
                    setSubmittingState(false);
                    showError(resource.getMessage() != null ? resource.getMessage() : "Failed to submit leave application.");
                    break;
            }
        });

        viewModel.recentLeaves.observe(getViewLifecycleOwner(), leaves -> {
            if (leaves != null) {
                renderRecentLeaveCards(leaves);
            }
        });
    }

    private void setupEmployeeHeader() {
        UserLocalStore userStore = viewModel.getUserStore();
        String empName = userStore.getEmpName();
        if (empName == null || empName.trim().isEmpty()) {
            empName = "Arthi T";
        }

        if (tvEmpGreeting != null) {
            tvEmpGreeting.setText("Hi, " + empName);
        }

        if (tvEmpDetails != null) {
            int staffId = userStore.getStaffId();
            String storeName = userStore.getStoreName();
            tvEmpDetails.setText("Employee ID: #" + (staffId > 0 ? staffId : 38) + " • " + (storeName != null ? storeName : "Stone Mart Yard"));
        }

        if (tvAvatarInitials != null) {
            String[] parts = empName.split(" ");
            if (parts.length >= 2 && parts[1].length() > 0) {
                tvAvatarInitials.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
            } else {
                tvAvatarInitials.setText(("" + parts[0].charAt(0)).toUpperCase());
            }
        }

        if (tvEmployeeTenureBadge != null) {
            tvEmployeeTenureBadge.setText("Permanent Staff (2+ Years)");
        }
        if (tvQuotaMonthlyAccrual != null) {
            tvQuotaMonthlyAccrual.setText("2.0");
        }
        if (tvQuotaPrevRollOver != null) {
            tvQuotaPrevRollOver.setText("2.0");
        }
        if (tvQuotaTotalJoint != null) {
            tvQuotaTotalJoint.setText("4.0");
        }
        if (tvLeavePolicySummary != null) {
            tvLeavePolicySummary.setText("✓ 2 paid leaves credited monthly. Leaves accumulate flexibly in employee favor with no joint total limit for single or consecutive requests.");
        }
    }

    private void setupLeaveTypes() {
        spinnerAdapter = new LeaveTypeAdapter(requireContext(), leaveTypesList);
        spLeaveType.setAdapter(spinnerAdapter);

        spLeaveType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < leaveTypesList.size()) {
                    viewModel.selectLeaveType(leaveTypesList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        layoutLeaveTypeSelector.setOnClickListener(v -> {
            ivChevronLeave.animate().rotation(180f).setDuration(150).withEndAction(() -> {
                showLeaveTypeBottomSheet();
                ivChevronLeave.animate().rotation(0f).setDuration(200).start();
            }).start();
        });
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

        LeaveTypeItem currentSelected = viewModel.selectedLeaveType.getValue();

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

            boolean isSelected = currentSelected != null && currentSelected.getId() == item.getId();
            if (isSelected) {
                itemView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_leave_modal_item_selected));
                ivCheck.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_leave_modal_item));
                ivCheck.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                viewModel.selectLeaveType(item);
                for (int i = 0; i < leaveTypesList.size(); i++) {
                    if (leaveTypesList.get(i).getId() == item.getId()) {
                        spLeaveType.setSelection(i);
                        break;
                    }
                }
                dialog.dismiss();
            });

            layoutModalList.addView(itemView);
        }

        dialog.show();
    }

    private void onLeaveTypeChanged(LeaveTypeItem item) {
        if (item == null) return;
        if (!item.isHalfDayAllowed()) {
            btnTypeHalfDay.setAlpha(0.35f);
            btnTypeHalfDay.setClickable(false);
            if (Boolean.TRUE.equals(viewModel.isHalfDay.getValue())) {
                viewModel.setHalfDay(false);
            }
        } else {
            btnTypeHalfDay.setAlpha(1.0f);
            btnTypeHalfDay.setClickable(true);
        }
        updateFullDaysCalculation();
    }

    private void setupRequestTypeToggle() {
        containerDurationToggle.post(() -> {
            int totalWidth = containerDurationToggle.getWidth();
            int padding = containerDurationToggle.getPaddingLeft() + containerDurationToggle.getPaddingRight();
            int thumbWidth = (totalWidth - padding) / 2;
            ViewGroup.LayoutParams lp = vDurationSlider.getLayoutParams();
            lp.width = thumbWidth;
            vDurationSlider.setLayoutParams(lp);

            if (Boolean.TRUE.equals(viewModel.isHalfDay.getValue())) {
                vDurationSlider.setTranslationX(thumbWidth);
                btnTypeHalfDay.setTextColor(Color.WHITE);
                btnTypeFullDay.setTextColor(0xFF475569);
            } else {
                vDurationSlider.setTranslationX(0);
                btnTypeFullDay.setTextColor(Color.WHITE);
                btnTypeHalfDay.setTextColor(0xFF475569);
            }
        });

        btnTypeFullDay.setOnClickListener(v -> viewModel.setHalfDay(false));
        btnTypeHalfDay.setOnClickListener(v -> {
            LeaveTypeItem cur = viewModel.selectedLeaveType.getValue();
            if (cur != null && !cur.isHalfDayAllowed()) {
                Toast.makeText(requireContext(), cur.getName() + " does not permit Half Day requests.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setHalfDay(true);
        });

        btnSessionAM.setOnClickListener(v -> viewModel.setHalfDaySession("AM"));
        btnSessionPM.setOnClickListener(v -> viewModel.setHalfDaySession("PM"));
    }

    private void applyFullDayUI(boolean animate) {
        int thumbWidth = vDurationSlider.getWidth();
        if (thumbWidth <= 0) {
            thumbWidth = (containerDurationToggle.getWidth() - containerDurationToggle.getPaddingLeft() - containerDurationToggle.getPaddingRight()) / 2;
        }

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

    private void applyHalfDayUI(boolean animate) {
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

        String session = viewModel.halfDaySession.getValue();
        if ("PM".equalsIgnoreCase(session)) {
            vSessionSlider.setTranslationX(thumbWidth);
            btnSessionPM.setTextColor(Color.WHITE);
            btnSessionAM.setTextColor(0xFF475569);
        } else {
            vSessionSlider.setTranslationX(0);
            btnSessionAM.setTextColor(Color.WHITE);
            btnSessionPM.setTextColor(0xFF475569);
        }
    }

    private void applySessionUI(String session, boolean animate) {
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
        Boolean halfDay = viewModel != null ? viewModel.isHalfDay.getValue() : false;
        if (Boolean.TRUE.equals(halfDay)) {
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

        boolean halfDay = Boolean.TRUE.equals(viewModel.isHalfDay.getValue());
        String fromDate = halfDay ? etHalfDate.getText().toString().trim() : etFromDate.getText().toString().trim();
        String toDate = halfDay ? fromDate : etToDate.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        viewModel.submitLeave(fromDate, toDate, reason);
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

    private void renderRecentLeaveCards(List<LeaveRequestRecord> leaves) {
        layoutRecentLeaves.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        tvRecentCount.setText(leaves.size() + " Request" + (leaves.size() > 1 ? "s" : ""));

        for (LeaveRequestRecord r : leaves) {
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
