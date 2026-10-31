package com.app.stonemart.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AttendanceChartView extends View {

    private Paint linePaint;
    private Paint fillPaint;
    private Paint gridPaint;
    private Paint labelPaint;
    private Paint dotOuterPaint;
    private Paint dotInnerPaint;
    private Paint tooltipBgPaint;
    private Paint tooltipTextPaint;

    private float[] dataPoints = new float[]{6.5f, 8.0f, 9.5f, 8.2f, 7.8f, 4.0f, 0.0f};
    private String[] labels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private int highlightIndex = 2; // Wed peak
    private float maxDataValue = 10f;

    public AttendanceChartView(Context context) {
        super(context);
        init();
    }

    public AttendanceChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AttendanceChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#2563EB"));
        linePaint.setStrokeWidth(dpToPx(3f));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setPathEffect(new CornerPathEffect(dpToPx(12f)));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(dpToPx(1f));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{dpToPx(4f), dpToPx(4f)}, 0));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#64748B"));
        labelPaint.setTextSize(spToPx(11f));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        dotOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotOuterPaint.setColor(Color.parseColor("#2563EB"));
        dotOuterPaint.setStyle(Paint.Style.FILL);

        dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotInnerPaint.setColor(Color.WHITE);
        dotInnerPaint.setStyle(Paint.Style.FILL);

        tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipBgPaint.setColor(Color.parseColor("#0F172A"));
        tooltipBgPaint.setStyle(Paint.Style.FILL);

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(spToPx(10f));
        tooltipTextPaint.setFakeBoldText(true);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setChartData(float[] newPoints, String[] newLabels, int highlightIdx) {
        if (newPoints != null && newPoints.length > 0) {
            this.dataPoints = newPoints;
            this.labels = newLabels != null ? newLabels : new String[newPoints.length];
            this.highlightIndex = Math.min(Math.max(0, highlightIdx), newPoints.length - 1);

            // Re-calculate max data value
            float max = 1f;
            for (float val : newPoints) {
                if (val > max) max = val;
            }
            this.maxDataValue = max * 1.25f;

            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0 || dataPoints == null || dataPoints.length == 0) return;

        float paddingLeft = dpToPx(16f);
        float paddingRight = dpToPx(16f);
        float paddingTop = dpToPx(44f); // Leave room for top tooltip
        float paddingBottom = dpToPx(28f); // Leave room for X-axis labels

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        int numPoints = dataPoints.length;
        float stepX = numPoints > 1 ? chartWidth / (numPoints - 1) : chartWidth;

        // Draw horizontal grid lines (3 lines)
        for (int i = 0; i < 3; i++) {
            float gridY = paddingTop + (chartHeight / 2f) * i;
            canvas.drawLine(paddingLeft, gridY, width - paddingRight, gridY, gridPaint);
        }

        // Calculate point coordinates
        float[] px = new float[numPoints];
        float[] py = new float[numPoints];

        for (int i = 0; i < numPoints; i++) {
            px[i] = paddingLeft + i * stepX;
            float normVal = Math.min(1.0f, Math.max(0.0f, dataPoints[i] / maxDataValue));
            py[i] = paddingTop + chartHeight * (1.0f - normVal);
        }

        // Build curve path
        Path linePath = new Path();
        Path fillPath = new Path();

        linePath.moveTo(px[0], py[0]);
        fillPath.moveTo(px[0], height - paddingBottom);
        fillPath.lineTo(px[0], py[0]);

        for (int i = 0; i < numPoints - 1; i++) {
            float cx1 = px[i] + stepX / 2f;
            float cy1 = py[i];
            float cx2 = px[i] + stepX / 2f;
            float cy2 = py[i + 1];

            linePath.cubicTo(cx1, cy1, cx2, cy2, px[i + 1], py[i + 1]);
            fillPath.cubicTo(cx1, cy1, cx2, cy2, px[i + 1], py[i + 1]);
        }

        fillPath.lineTo(px[numPoints - 1], height - paddingBottom);
        fillPath.close();

        // Fill shader
        LinearGradient fillShader = new LinearGradient(
                0, paddingTop,
                0, height - paddingBottom,
                new int[]{Color.parseColor("#4D2563EB"), Color.parseColor("#052563EB")},
                null,
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(fillShader);

        // Draw fill & line
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw X-axis labels & points
        for (int i = 0; i < numPoints; i++) {
            if (labels != null && i < labels.length && labels[i] != null) {
                canvas.drawText(labels[i], px[i], height - dpToPx(6f), labelPaint);
            }
        }

        // Draw Highlight Point & Floating Tooltip Card
        if (highlightIndex >= 0 && highlightIndex < numPoints) {
            float hx = px[highlightIndex];
            float hy = py[highlightIndex];

            // Pulse outer circle
            canvas.drawCircle(hx, hy, dpToPx(7f), dotOuterPaint);
            canvas.drawCircle(hx, hy, dpToPx(3.5f), dotInnerPaint);

            // Floating Tooltip Card
            String tooltipText = String.format("%.1f hrs", dataPoints[highlightIndex]);
            float textWidth = tooltipTextPaint.measureText(tooltipText);
            float tooltipWidth = textWidth + dpToPx(16f);
            float tooltipHeight = dpToPx(24f);

            float tooltipLeft = Math.max(paddingLeft, Math.min(width - paddingRight - tooltipWidth, hx - tooltipWidth / 2f));
            float tooltipTop = hy - tooltipHeight - dpToPx(8f);

            RectF tooltipRect = new RectF(tooltipLeft, tooltipTop, tooltipLeft + tooltipWidth, tooltipTop + tooltipHeight);
            canvas.drawRoundRect(tooltipRect, dpToPx(8f), dpToPx(8f), tooltipBgPaint);

            canvas.drawText(tooltipText, tooltipRect.centerX(), tooltipRect.centerY() + dpToPx(3.5f), tooltipTextPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
