package com.app.stonemart.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.stonemart.R;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormatSymbols;
import java.util.Locale;

/**
 * PremiumAlertDialog - Enterprise-grade, modern styled dialogs matching the
 * Stone Mart brand design system (24dp rounded corners, smooth entrance physics,
 * colored semantic badges, and elegant typography).
 */
public class PremiumAlertDialog {

    public enum DialogType {
        ERROR,
        WARNING,
        SUCCESS,
        INFO,
        CONFIRMATION
    }

    public interface OnMonthSelectedListener {
        void onMonthSelected(int monthIndex);
    }

    /**
     * Standard Quick Alert (Error, Warning, Success, Info)
     */
    public static Dialog show(Context context, DialogType type, String title, String message,
                              @Nullable String buttonText, @Nullable Runnable onDismiss) {
        if (context == null) return null;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_alert);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.65f);
        }

        View card = dialog.findViewById(R.id.cardDialogRoot);
        FrameLayout frameIcon = dialog.findViewById(R.id.frameDialogIcon);
        ImageView ivIcon = dialog.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        TextView tvBadge = dialog.findViewById(R.id.tvDialogBadge);
        MaterialButton btnPrimary = dialog.findViewById(R.id.btnDialogPrimary);

        if (tvTitle != null) tvTitle.setText(title != null ? title : "Notice");
        if (tvMessage != null) tvMessage.setText(message != null ? message : "");
        if (btnPrimary != null) {
            btnPrimary.setText(buttonText != null ? buttonText : "OK");
            btnPrimary.setOnClickListener(v -> {
                dialog.dismiss();
                if (onDismiss != null) onDismiss.run();
            });
        }

        // Apply Semantic Theming
        applySemanticTheme(type, frameIcon, ivIcon, tvBadge, btnPrimary);

        animateEntry(card);
        dialog.show();
        return dialog;
    }

    public static Dialog showError(Context context, String title, String message) {
        return show(context, DialogType.ERROR, title, message, "OK", null);
    }

    public static Dialog showError(Context context, String title, String message, Runnable onDismiss) {
        return show(context, DialogType.ERROR, title, message, "OK", onDismiss);
    }

    public static Dialog showSuccess(Context context, String title, String message) {
        return show(context, DialogType.SUCCESS, title, message, "Continue", null);
    }

    public static Dialog showWarning(Context context, String title, String message) {
        return show(context, DialogType.WARNING, title, message, "Understood", null);
    }

    public static Dialog showInfo(Context context, String title, String message) {
        return show(context, DialogType.INFO, title, message, "Close", null);
    }

    /**
     * Confirmation Dialog with Positive & Negative Actions (e.g., Logout)
     */
    public static Dialog showConfirmation(Context context, String title, String message,
                                          String positiveText, String negativeText,
                                          @Nullable Runnable onPositive) {
        if (context == null) return null;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_alert);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.65f);
        }

        View card = dialog.findViewById(R.id.cardDialogRoot);
        FrameLayout frameIcon = dialog.findViewById(R.id.frameDialogIcon);
        ImageView ivIcon = dialog.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        TextView tvBadge = dialog.findViewById(R.id.tvDialogBadge);
        MaterialButton btnPrimary = dialog.findViewById(R.id.btnDialogPrimary);
        MaterialButton btnSecondary = dialog.findViewById(R.id.btnDialogSecondary);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);

        // Confirmation theme (Warm red / caution)
        applySemanticTheme(DialogType.CONFIRMATION, frameIcon, ivIcon, tvBadge, btnPrimary);

        if (btnSecondary != null) {
            btnSecondary.setVisibility(View.VISIBLE);
            btnSecondary.setText(negativeText != null ? negativeText : "Cancel");
            btnSecondary.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnPrimary != null) {
            btnPrimary.setText(positiveText != null ? positiveText : "Confirm");
            btnPrimary.setOnClickListener(v -> {
                dialog.dismiss();
                if (onPositive != null) onPositive.run();
            });
        }

        animateEntry(card);
        dialog.show();
        return dialog;
    }

    /**
     * Dedicated Rich Notifications & Alerts Popup (Static Default)
     */
    public static Dialog showNotifications(Context context) {
        return showNotifications(context, null, null, null, 0);
    }

    /**
     * Dedicated Rich Notifications & Alerts Popup (Dynamic Live API Data)
     */
    public static Dialog showNotifications(Context context,
                                           @Nullable java.util.Collection<org.json.JSONObject> permissions,
                                           @Nullable java.util.Collection<org.json.JSONObject> holidays,
                                           @Nullable String monthName,
                                           int year) {
        if (context == null) return null;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_notifications);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.65f);
        }

        TextView tvSubtitle = dialog.findViewById(R.id.tvNotificationsSubtitle);
        if (tvSubtitle != null && monthName != null && year > 0) {
            tvSubtitle.setText("Live HRMS Feed • " + monthName + " " + year);
        }

        android.widget.LinearLayout container = dialog.findViewById(R.id.layoutNotificationsContainer);
        if (container != null && (permissions != null || holidays != null)) {
            container.removeAllViews();
            float density = context.getResources().getDisplayMetrics().density;
            int pad14 = (int) (14 * density);
            int pad8 = (int) (8 * density);
            int pad4 = (int) (4 * density);
            int pad6 = (int) (6 * density);
            int margin12 = (int) (12 * density);

            int totalCount = 0;

            // 1. Dynamic Permissions from API
            if (permissions != null) {
                for (org.json.JSONObject perm : permissions) {
                    if (perm == null) continue;
                    totalCount++;

                    String date = perm.optString("date", "");
                    String fromTime = perm.optString("from_time", "");
                    String toTime = perm.optString("to_time", "");
                    int minutes = perm.optInt("permission_minutes", 0);
                    String reason = perm.optString("reason", "Late arrival permission");
                    String status = perm.optString("approval_status", "APPROVED");

                    android.widget.LinearLayout card = new android.widget.LinearLayout(context);
                    card.setOrientation(android.widget.LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_pill_code_amber);
                    card.setPadding(pad14, pad14, pad14, pad14);
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, margin12);
                    card.setLayoutParams(lp);

                    // Top Row: Title & Status Badge
                    android.widget.RelativeLayout topRow = new android.widget.RelativeLayout(context);
                    android.widget.RelativeLayout.LayoutParams topLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    topRow.setLayoutParams(topLp);

                    TextView tvTitle = new TextView(context);
                    tvTitle.setText("Late-Arrival Permission");
                    tvTitle.setTextColor(Color.parseColor("#B45309"));
                    tvTitle.setTextSize(14);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    android.widget.RelativeLayout.LayoutParams titleLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    titleLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                    titleLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(titleLp);
                    topRow.addView(tvTitle);

                    TextView tvBadge = new TextView(context);
                    tvBadge.setText(status.toUpperCase());
                    tvBadge.setTextColor(Color.parseColor("#D97706"));
                    tvBadge.setTextSize(10);
                    tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_unselected);
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                    tvBadge.setPadding(pad8, pad4, pad8, pad4);
                    android.widget.RelativeLayout.LayoutParams badgeLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    badgeLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
                    badgeLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                    tvBadge.setLayoutParams(badgeLp);
                    topRow.addView(tvBadge);

                    card.addView(topRow);

                    // Details text
                    TextView tvDesc = new TextView(context);
                    String timeSlot = (!fromTime.isEmpty() && !toTime.isEmpty()) ? " (" + fromTime + " – " + toTime + ")" : "";
                    tvDesc.setText("• " + date + ": " + minutes + " mins permission window" + timeSlot + ".\n• Reason: " + reason);
                    tvDesc.setTextColor(Color.parseColor("#78350F"));
                    tvDesc.setTextSize(12);
                    tvDesc.setLineSpacing(pad4, 1.0f);
                    android.widget.LinearLayout.LayoutParams descLp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    descLp.setMargins(0, pad6, 0, 0);
                    tvDesc.setLayoutParams(descLp);
                    card.addView(tvDesc);

                    container.addView(card);
                }
            }

            // 2. Dynamic Holidays from API
            if (holidays != null) {
                for (org.json.JSONObject hol : holidays) {
                    if (hol == null) continue;
                    totalCount++;

                    String date = hol.optString("date", "");
                    String name = hol.optString("name", "Holiday");
                    String type = hol.optString("type", "Official");
                    String remarks = hol.optString("remarks", "");

                    android.widget.LinearLayout card = new android.widget.LinearLayout(context);
                    card.setOrientation(android.widget.LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_pill_code_purple);
                    card.setPadding(pad14, pad14, pad14, pad14);
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, margin12);
                    card.setLayoutParams(lp);

                    // Top Row
                    android.widget.RelativeLayout topRow = new android.widget.RelativeLayout(context);
                    android.widget.RelativeLayout.LayoutParams topLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    topRow.setLayoutParams(topLp);

                    TextView tvTitle = new TextView(context);
                    tvTitle.setText(name);
                    tvTitle.setTextColor(Color.parseColor("#6B21A8"));
                    tvTitle.setTextSize(14);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    android.widget.RelativeLayout.LayoutParams titleLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    titleLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
                    titleLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(titleLp);
                    topRow.addView(tvTitle);

                    TextView tvBadge = new TextView(context);
                    tvBadge.setText((type.toUpperCase()) + " HOLIDAY");
                    tvBadge.setTextColor(Color.parseColor("#7E22CE"));
                    tvBadge.setTextSize(10);
                    tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_unselected);
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E8FF")));
                    tvBadge.setPadding(pad8, pad4, pad8, pad4);
                    android.widget.RelativeLayout.LayoutParams badgeLp = new android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
                    badgeLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
                    badgeLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
                    tvBadge.setLayoutParams(badgeLp);
                    topRow.addView(tvBadge);

                    card.addView(topRow);

                    // Details text
                    TextView tvDesc = new TextView(context);
                    String remarkText = (remarks != null && !remarks.isEmpty()) ? " (" + remarks + ")" : "";
                    tvDesc.setText("• Date: " + date + " • Official calendar rest day recorded for shift schedule" + remarkText + ".");
                    tvDesc.setTextColor(Color.parseColor("#581C87"));
                    tvDesc.setTextSize(12);
                    tvDesc.setLineSpacing(pad4, 1.0f);
                    android.widget.LinearLayout.LayoutParams descLp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                    descLp.setMargins(0, pad6, 0, 0);
                    tvDesc.setLayoutParams(descLp);
                    card.addView(tvDesc);

                    container.addView(card);
                }
            }

            // Only display items returned from backend. If none found for this month, show clean empty state.
            if (totalCount == 0) {
                android.widget.LinearLayout emptyCard = new android.widget.LinearLayout(context);
                emptyCard.setOrientation(android.widget.LinearLayout.VERTICAL);
                emptyCard.setGravity(Gravity.CENTER);
                emptyCard.setBackgroundResource(R.drawable.bg_pill_unselected);
                emptyCard.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8FAFC")));
                emptyCard.setPadding(pad14, (int) (26 * density), pad14, (int) (26 * density));
                android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                emptyCard.setLayoutParams(lp);

                TextView tvEmptyTitle = new TextView(context);
                tvEmptyTitle.setText("No Notifications");
                tvEmptyTitle.setTextColor(Color.parseColor("#0F172A"));
                tvEmptyTitle.setTextSize(15);
                tvEmptyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvEmptyTitle.setGravity(Gravity.CENTER);
                emptyCard.addView(tvEmptyTitle);

                TextView tvEmptySub = new TextView(context);
                tvEmptySub.setText("No permissions, holidays, or official alerts recorded in backend for this period.");
                tvEmptySub.setTextColor(Color.parseColor("#64748B"));
                tvEmptySub.setTextSize(13);
                tvEmptySub.setGravity(Gravity.CENTER);
                android.widget.LinearLayout.LayoutParams subLp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                subLp.setMargins(0, pad6, 0, 0);
                tvEmptySub.setLayoutParams(subLp);
                emptyCard.addView(tvEmptySub);

                container.addView(emptyCard);
            }
        }

        MaterialButton btnDismiss = dialog.findViewById(R.id.btnDismissNotifications);
        if (btnDismiss != null) {
            btnDismiss.setOnClickListener(v -> dialog.dismiss());
        }

        animateEntry(dialog.findViewById(android.R.id.content));
        dialog.show();
        return dialog;
    }

    /**
     * Dedicated Month Filter Picker
     */
    public static Dialog showMonthPicker(Context context, int year, int selectedMonth,
                                         @NonNull OnMonthSelectedListener listener) {
        if (context == null) return null;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_month_picker);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.65f);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvMonthPickerTitle);
        if (tvTitle != null) {
            tvTitle.setText("Select Month (" + year + ")");
        }

        GridLayout grid = dialog.findViewById(R.id.gridMonths);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelMonthPicker);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (grid != null) {
            grid.removeAllViews();
            String[] shortMonths = new DateFormatSymbols(Locale.US).getShortMonths(); // Jan, Feb, ...

            int density = (int) context.getResources().getDisplayMetrics().density;
            int margin = 4 * density;

            for (int i = 0; i < 12; i++) {
                final int monthIndex = i;
                MaterialButton btn = new MaterialButton(context);
                btn.setText(shortMonths[i]);
                btn.setTextSize(13);
                btn.setInsetTop(0);
                btn.setInsetBottom(0);
                btn.setCornerRadius(12 * density);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 44 * density;
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                lp.setMargins(margin, margin, margin, margin);
                btn.setLayoutParams(lp);

                boolean isSelected = (i == selectedMonth);
                if (isSelected) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1D4ED8")));
                    btn.setTextColor(Color.WHITE);
                    btn.setElevation(2 * density);
                } else {
                    btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8FAFC")));
                    btn.setTextColor(Color.parseColor("#334155"));
                    btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
                    btn.setStrokeWidth(1 * density);
                    btn.setElevation(0);
                }

                btn.setOnClickListener(v -> {
                    dialog.dismiss();
                    listener.onMonthSelected(monthIndex);
                });

                grid.addView(btn);
            }
        }

        animateEntry(dialog.findViewById(android.R.id.content));
        dialog.show();
        return dialog;
    }

    private static void applySemanticTheme(DialogType type, FrameLayout frameIcon, ImageView ivIcon,
                                           TextView tvBadge, MaterialButton btnPrimary) {
        if (type == null) type = DialogType.INFO;

        switch (type) {
            case ERROR:
                if (frameIcon != null) frameIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_alert_triangle);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
                }
                if (tvBadge != null) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("ERROR");
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                    tvBadge.setTextColor(Color.parseColor("#DC2626"));
                }
                if (btnPrimary != null) {
                    btnPrimary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
                    btnPrimary.setRippleColor(ColorStateList.valueOf(Color.parseColor("#F87171")));
                }
                break;

            case WARNING:
                if (frameIcon != null) frameIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_alert_circle);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
                }
                if (tvBadge != null) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("ATTENTION");
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                    tvBadge.setTextColor(Color.parseColor("#D97706"));
                }
                if (btnPrimary != null) {
                    btnPrimary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
                    btnPrimary.setRippleColor(ColorStateList.valueOf(Color.parseColor("#FBBF24")));
                }
                break;

            case SUCCESS:
                if (frameIcon != null) frameIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_check_circle);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));
                }
                if (tvBadge != null) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("SUCCESS");
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                    tvBadge.setTextColor(Color.parseColor("#16A34A"));
                }
                if (btnPrimary != null) {
                    btnPrimary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#16A34A")));
                    btnPrimary.setRippleColor(ColorStateList.valueOf(Color.parseColor("#4ADE80")));
                }
                break;

            case CONFIRMATION:
                if (frameIcon != null) frameIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_logout_dialog);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
                }
                if (tvBadge != null) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("SESSION ACTION");
                    tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                    tvBadge.setTextColor(Color.parseColor("#DC2626"));
                }
                if (btnPrimary != null) {
                    btnPrimary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
                    btnPrimary.setRippleColor(ColorStateList.valueOf(Color.parseColor("#F87171")));
                }
                break;

            case INFO:
            default:
                if (frameIcon != null) frameIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_info);
                    ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#1D4ED8")));
                }
                if (tvBadge != null) {
                    tvBadge.setVisibility(View.GONE);
                }
                if (btnPrimary != null) {
                    btnPrimary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1D4ED8")));
                    btnPrimary.setRippleColor(ColorStateList.valueOf(Color.parseColor("#60A5FA")));
                }
                break;
        }
    }

    private static void animateEntry(View view) {
        if (view == null) return;
        view.setScaleX(0.88f);
        view.setScaleY(0.88f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(260)
                .setInterpolator(new OvershootInterpolator(1.15f))
                .start();
    }
}

