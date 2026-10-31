package com.app.stonemart.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class GeofenceMapView extends View {

    private Paint mapBgPaint;
    private Paint roadPaint;
    private Paint blockPaint;
    private Paint geofenceZoneFillPaint;
    private Paint geofenceZoneStrokePaint;
    private Paint radarPulsePaint;
    private Paint userPinPaint;
    private Paint userPinInnerPaint;
    private Paint userPinPulsePaint;
    private Paint poiPaint;
    private Paint poiTextPaint;
    private Paint glassCardBgPaint;
    private Paint glassCardStrokePaint;
    private Paint glassCardTextPaint;
    private Paint subLabelPaint;

    private float radarRadiusFactor = 0f;
    private ValueAnimator radarAnimator;
    private boolean isInside = true;

    private Path roadPath1 = new Path();
    private Path roadPath2 = new Path();
    private Path blockPath1 = new Path();
    private Path blockPath2 = new Path();

    public GeofenceMapView(Context context) {
        super(context);
        init();
    }

    public GeofenceMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeofenceMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mapBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mapBgPaint.setColor(Color.parseColor("#0F172A")); // Dark Slate GPS Satellite Map Theme

        roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roadPaint.setColor(Color.parseColor("#1E293B"));
        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setStrokeWidth(dpToPx(14f));
        roadPaint.setStrokeCap(Paint.Cap.ROUND);

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockPaint.setColor(Color.parseColor("#161E2E"));
        blockPaint.setStyle(Paint.Style.FILL);

        geofenceZoneFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        geofenceZoneFillPaint.setColor(Color.parseColor("#1A2563EB"));
        geofenceZoneFillPaint.setStyle(Paint.Style.FILL);

        geofenceZoneStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        geofenceZoneStrokePaint.setColor(Color.parseColor("#2563EB"));
        geofenceZoneStrokePaint.setStrokeWidth(dpToPx(2.5f));
        geofenceZoneStrokePaint.setStyle(Paint.Style.STROKE);
        geofenceZoneStrokePaint.setPathEffect(new DashPathEffect(new float[]{dpToPx(8f), dpToPx(5f)}, 0));

        radarPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radarPulsePaint.setColor(Color.parseColor("#332563EB"));
        radarPulsePaint.setStyle(Paint.Style.STROKE);
        radarPulsePaint.setStrokeWidth(dpToPx(2f));

        userPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPinPaint.setColor(Color.parseColor("#2563EB"));
        userPinPaint.setStyle(Paint.Style.FILL);

        userPinInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPinInnerPaint.setColor(Color.WHITE);
        userPinInnerPaint.setStyle(Paint.Style.FILL);

        userPinPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPinPulsePaint.setColor(Color.parseColor("#602563EB"));
        userPinPulsePaint.setStyle(Paint.Style.FILL);

        poiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        poiPaint.setColor(Color.parseColor("#10B981"));
        poiPaint.setStyle(Paint.Style.FILL);

        poiTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        poiTextPaint.setColor(Color.parseColor("#94A3B8"));
        poiTextPaint.setTextSize(spToPx(9.5f));
        poiTextPaint.setFakeBoldText(true);

        glassCardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassCardBgPaint.setColor(Color.parseColor("#CC0F172A"));
        glassCardBgPaint.setStyle(Paint.Style.FILL);

        glassCardStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassCardStrokePaint.setColor(Color.parseColor("#33384662"));
        glassCardStrokePaint.setStyle(Paint.Style.STROKE);
        glassCardStrokePaint.setStrokeWidth(dpToPx(1f));

        glassCardTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassCardTextPaint.setColor(Color.WHITE);
        glassCardTextPaint.setTextSize(spToPx(10.5f));
        glassCardTextPaint.setFakeBoldText(true);

        subLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subLabelPaint.setColor(Color.parseColor("#64748B"));
        subLabelPaint.setTextSize(spToPx(10f));

        startRadarAnimation();
    }

    private void startRadarAnimation() {
        radarAnimator = ValueAnimator.ofFloat(0f, 1f);
        radarAnimator.setDuration(2400);
        radarAnimator.setRepeatCount(ValueAnimator.INFINITE);
        radarAnimator.setInterpolator(new LinearInterpolator());
        radarAnimator.addUpdateListener(animation -> {
            radarRadiusFactor = (float) animation.getAnimatedValue();
            invalidate();
        });
        radarAnimator.start();
    }

    public void setInsideStatus(boolean inside) {
        this.isInside = inside;
        if (inside) {
            geofenceZoneStrokePaint.setColor(Color.parseColor("#2563EB"));
            userPinPaint.setColor(Color.parseColor("#2563EB"));
            userPinPulsePaint.setColor(Color.parseColor("#602563EB"));
        } else {
            geofenceZoneStrokePaint.setColor(Color.parseColor("#EF4444"));
            userPinPaint.setColor(Color.parseColor("#EF4444"));
            userPinPulsePaint.setColor(Color.parseColor("#60EF4444"));
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (radarAnimator != null) {
            radarAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        // 1. Satellite Base Dark Map Surface
        canvas.drawRect(0, 0, width, height, mapBgPaint);

        // 2. Draw Realistic Vector Site Blocks & Buildings
        blockPath1.reset();
        blockPath1.moveTo(dpToPx(10f), dpToPx(10f));
        blockPath1.lineTo(width * 0.35f, dpToPx(10f));
        blockPath1.lineTo(width * 0.3f, height * 0.4f);
        blockPath1.lineTo(dpToPx(10f), height * 0.35f);
        blockPath1.close();
        canvas.drawPath(blockPath1, blockPaint);

        blockPath2.reset();
        blockPath2.moveTo(width * 0.65f, height * 0.55f);
        blockPath2.lineTo(width - dpToPx(10f), height * 0.5f);
        blockPath2.lineTo(width - dpToPx(10f), height - dpToPx(10f));
        blockPath2.lineTo(width * 0.6f, height - dpToPx(10f));
        blockPath2.close();
        canvas.drawPath(blockPath2, blockPaint);

        // 3. Draw Vector Site Roads
        roadPath1.reset();
        roadPath1.moveTo(-50, height * 0.65f);
        roadPath1.quadTo(width * 0.4f, height * 0.7f, width + 50, height * 0.25f);
        canvas.drawPath(roadPath1, roadPaint);

        roadPath2.reset();
        roadPath2.moveTo(width * 0.45f, -50);
        roadPath2.lineTo(width * 0.48f, height + 50);
        canvas.drawPath(roadPath2, roadPaint);

        // 4. Center Geofence Radius Zone
        float cx = width / 2f;
        float cy = height / 2f - dpToPx(4f);
        float radius = Math.min(width, height) * 0.36f;

        // Radar Ripple Wave
        float rippleRadius = radius + radarRadiusFactor * dpToPx(34f);
        radarPulsePaint.setAlpha((int) ((1f - radarRadiusFactor) * 200));
        canvas.drawCircle(cx, cy, rippleRadius, radarPulsePaint);

        // Geofence Circle Boundary & Area Fill
        canvas.drawCircle(cx, cy, radius, geofenceZoneFillPaint);
        canvas.drawCircle(cx, cy, radius, geofenceZoneStrokePaint);

        // 5. Site POI Points
        float poi1X = cx - radius * 0.55f;
        float poi1Y = cy - radius * 0.45f;
        canvas.drawCircle(poi1X, poi1Y, dpToPx(4f), poiPaint);
        canvas.drawText("Gate A • Entry", poi1X + dpToPx(7f), poi1Y + dpToPx(3f), poiTextPaint);

        float poi2X = cx + radius * 0.5f;
        float poi2Y = cy + radius * 0.5f;
        canvas.drawCircle(poi2X, poi2Y, dpToPx(4f), poiPaint);
        canvas.drawText("Quarry Yard #4", poi2X + dpToPx(7f), poi2Y + dpToPx(3f), poiTextPaint);

        // 6. User Live GPS Location Marker Pin with Pulse
        float pinX = isInside ? cx + dpToPx(8f) : width * 0.18f;
        float pinY = isInside ? cy - dpToPx(6f) : height * 0.82f;

        // Pulse ring under user pin
        canvas.drawCircle(pinX, pinY, dpToPx(14f), userPinPulsePaint);
        // Outer pin circle
        canvas.drawCircle(pinX, pinY, dpToPx(9f), userPinPaint);
        // Inner white dot
        canvas.drawCircle(pinX, pinY, dpToPx(3.5f), userPinInnerPaint);

        // 7. Top Floating Glass Status Card
        String cardText = isInside ? "📍 Yard #4 Quarry Geofence • Locked" : "⚠️ Outside Geofence Zone";
        float cardTextW = glassCardTextPaint.measureText(cardText);
        float cardW = cardTextW + dpToPx(20f);
        float cardH = dpToPx(28f);

        RectF cardRect = new RectF(dpToPx(10f), dpToPx(10f), dpToPx(10f) + cardW, dpToPx(10f) + cardH);
        canvas.drawRoundRect(cardRect, dpToPx(14f), dpToPx(14f), glassCardBgPaint);
        canvas.drawRoundRect(cardRect, dpToPx(14f), dpToPx(14f), glassCardStrokePaint);
        canvas.drawText(cardText, cardRect.left + dpToPx(10f), cardRect.centerY() + dpToPx(4f), glassCardTextPaint);

        // Top Right RTK Badge
        String rtkText = "⚡ RTK 1.2m";
        float rtkW = glassCardTextPaint.measureText(rtkText) + dpToPx(16f);
        RectF rtkRect = new RectF(width - dpToPx(10f) - rtkW, dpToPx(10f), width - dpToPx(10f), dpToPx(10f) + cardH);
        canvas.drawRoundRect(rtkRect, dpToPx(14f), dpToPx(14f), glassCardBgPaint);
        canvas.drawRoundRect(rtkRect, dpToPx(14f), dpToPx(14f), glassCardStrokePaint);
        canvas.drawText(rtkText, rtkRect.left + dpToPx(8f), rtkRect.centerY() + dpToPx(4f), glassCardTextPaint);

        // Bottom Coordinates Footer
        subLabelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Lat: 26.9124, Lng: 75.7873 • 5G Beacon GT-884", width - dpToPx(12f), height - dpToPx(8f), subLabelPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
