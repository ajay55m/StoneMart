package com.app.stonemart.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BigMonthlyCalendarView extends View {

    public interface OnDateSelectedListener {
        void onDateSelected(int day, int month, int year, DayStatus status);
    }

    public enum DayStatus {
        PRESENT,   // 🟢 Green
        LEAVE,     // 🔵 Blue
        ABSENT,    // 🔴 Red
        HOLIDAY,   // 🟣 Purple
        MISSING,   // 🟡 Amber
        FUTURE,    // ⚪ Slate
        OFF_DAY    // ⚪ Gray
    }

    private Paint dayHeaderPaint;
    private Paint dateTextPaint;
    private Paint bgCirclePaint;
    private Paint selectedRingPaint;
    private Paint gridLinePaint;

    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private int currentMonth = Calendar.OCTOBER;
    private int currentYear = 2026;

    private OnDateSelectedListener dateSelectedListener;
    private final Map<Integer, DayStatus> monthStatusMap = new HashMap<>();

    public BigMonthlyCalendarView(Context context) {
        super(context);
        init();
    }

    public BigMonthlyCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BigMonthlyCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dayHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayHeaderPaint.setColor(Color.parseColor("#64748B"));
        dayHeaderPaint.setTextSize(spToPx(11f));
        dayHeaderPaint.setFakeBoldText(true);
        dayHeaderPaint.setTextAlign(Paint.Align.CENTER);

        dateTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextPaint.setTextSize(spToPx(14f));
        dateTextPaint.setFakeBoldText(true);
        dateTextPaint.setTextAlign(Paint.Align.CENTER);

        bgCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgCirclePaint.setStyle(Paint.Style.FILL);

        selectedRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedRingPaint.setColor(Color.parseColor("#2563EB"));
        selectedRingPaint.setStyle(Paint.Style.STROKE);
        selectedRingPaint.setStrokeWidth(dpToPx(2.5f));

        gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridLinePaint.setColor(Color.parseColor("#F1F5F9"));
        gridLinePaint.setStrokeWidth(dpToPx(1f));
    }

    private final Set<Integer> permissionDays = new HashSet<>();

    public void setMonthStatusMap(Map<Integer, DayStatus> statusMap) {
        monthStatusMap.clear();
        if (statusMap != null) {
            this.monthStatusMap.putAll(statusMap);
        }
        invalidate();
    }

    public void setPermissionDays(Collection<Integer> days) {
        permissionDays.clear();
        if (days != null) {
            permissionDays.addAll(days);
        }
        invalidate();
    }

    public void setYearMonth(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;
        invalidate();
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        invalidate();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public DayStatus getSelectedDayStatus() {
        return monthStatusMap.getOrDefault(selectedDay, DayStatus.PRESENT);
    }

    private int getStartOffset() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sun, 2 = Mon, ...
        return (dayOfWeek + 5) % 7; // 0 = Mon, 1 = Tue, ..., 6 = Sun
    }

    private int getDaysInMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) dpToPx(330f);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        float padding = dpToPx(8f);
        float gridW = width - padding * 2;

        float colW = gridW / 7f;
        float rowH = dpToPx(48f);

        // 1. Draw Weekday Headers (MON, TUE, WED, THU, FRI, SAT, SUN)
        String[] weekHeaders = new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        float headerY = dpToPx(24f);

        for (int i = 0; i < 7; i++) {
            float cx = padding + i * colW + colW / 2f;
            canvas.drawText(weekHeaders[i], cx, headerY, dayHeaderPaint);
        }

        // Draw Divider under header
        canvas.drawLine(padding, dpToPx(34f), width - padding, dpToPx(34f), gridLinePaint);

        // 2. Dynamic Month Offset Calculation
        int startOffset = getStartOffset();
        int daysInMonth = getDaysInMonth();
        float startY = dpToPx(42f);

        int dayCounter = 1;
        int maxRows = 6;

        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < 7; c++) {
                int cellIndex = r * 7 + c;

                if (cellIndex >= startOffset && dayCounter <= daysInMonth) {
                    float cx = padding + c * colW + colW / 2f;
                    float cy = startY + r * rowH + rowH / 2f;
                    float circleRadius = dpToPx(18f);

                    DayStatus status = monthStatusMap.getOrDefault(dayCounter, DayStatus.FUTURE);

                    // Draw Status Background Pill / Circle
                    int bgColor;
                    int textColor;

                    switch (status) {
                        case PRESENT:
                            bgColor = Color.parseColor("#DCFCE7"); // Light Emerald
                            textColor = Color.parseColor("#15803D"); // Dark Green
                            break;
                        case LEAVE:
                            bgColor = Color.parseColor("#DBEAFE"); // Light Blue
                            textColor = Color.parseColor("#1E40AF"); // Dark Blue
                            break;
                        case ABSENT:
                            bgColor = Color.parseColor("#FEE2E2"); // Light Red
                            textColor = Color.parseColor("#B91C1C"); // Dark Red
                            break;
                        case HOLIDAY:
                            bgColor = Color.parseColor("#F3E8FF"); // Light Purple
                            textColor = Color.parseColor("#7E22CE"); // Dark Purple
                            break;
                        case MISSING:
                            bgColor = Color.parseColor("#FEF3C7"); // Light Amber
                            textColor = Color.parseColor("#B45309"); // Dark Amber
                            break;
                        case OFF_DAY:
                            bgColor = Color.parseColor("#F1F5F9"); // Soft Gray
                            textColor = Color.parseColor("#64748B"); // Slate
                            break;
                        default: // FUTURE
                            bgColor = Color.TRANSPARENT;
                            textColor = Color.parseColor("#94A3B8");
                            break;
                    }

                    if (bgColor != Color.TRANSPARENT) {
                        bgCirclePaint.setColor(bgColor);
                        canvas.drawCircle(cx, cy, circleRadius, bgCirclePaint);
                    }

                    // Distinct Holiday Indicator Dot
                    if (status == DayStatus.HOLIDAY) {
                        Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        dotPaint.setColor(Color.parseColor("#7E22CE"));
                        canvas.drawCircle(cx, cy - circleRadius + dpToPx(3f), dpToPx(2.5f), dotPaint);
                    }

                    // Distinct Permission Indicator Dot (Amber)
                    if (permissionDays.contains(dayCounter)) {
                        Paint permDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        permDotPaint.setColor(Color.parseColor("#D97706"));
                        canvas.drawCircle(cx + circleRadius - dpToPx(2f), cy - circleRadius + dpToPx(3f), dpToPx(3f), permDotPaint);
                    }

                    // Draw Selection Highlight Ring
                    if (dayCounter == selectedDay) {
                        canvas.drawCircle(cx, cy, circleRadius + dpToPx(2f), selectedRingPaint);
                    }

                    // Draw Day Number
                    dateTextPaint.setColor(textColor);
                    canvas.drawText(String.valueOf(dayCounter), cx, cy + dpToPx(4.5f), dateTextPaint);

                    dayCounter++;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            float padding = dpToPx(8f);
            float gridW = getWidth() - padding * 2;
            float colW = gridW / 7f;
            float rowH = dpToPx(48f);
            float startY = dpToPx(42f);

            int col = (int) ((x - padding) / colW);
            int row = (int) ((y - startY) / rowH);

            if (col >= 0 && col < 7 && row >= 0 && row < 6) {
                int cellIndex = row * 7 + col;
                int startOffset = getStartOffset();
                int dayClicked = cellIndex - startOffset + 1;
                int daysInMonth = getDaysInMonth();

                if (dayClicked >= 1 && dayClicked <= daysInMonth) {
                    selectedDay = dayClicked;
                    invalidate();

                    if (dateSelectedListener != null) {
                        dateSelectedListener.onDateSelected(selectedDay, currentMonth, currentYear,
                                monthStatusMap.getOrDefault(selectedDay, DayStatus.FUTURE));
                    }
                    performClick();
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
