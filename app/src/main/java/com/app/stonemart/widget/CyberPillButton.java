package com.app.stonemart.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

public class CyberPillButton extends View {

    private String text = "LOG IN ➔";
    private float textSizePx;
    private int textColorNormal = 0xFFFFFFFF;

    private boolean isPressedState = false;
    private float scaleFactor = 1.0f;
    private ValueAnimator scaleAnimator;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bodyRect = new RectF();

    public CyberPillButton(Context context) {
        super(context);
        init(context, null);
    }

    public CyberPillButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CyberPillButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setClickable(true);
        setFocusable(true);

        float density = getResources().getDisplayMetrics().density;
        textSizePx = 15f * density;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
                    android.R.attr.text,
                    android.R.attr.textSize,
                    android.R.attr.textColor
            });
            String t = a.getString(0);
            if (t != null) text = t;
            textSizePx = a.getDimension(1, textSizePx);
            textColorNormal = a.getColor(2, textColorNormal);
            a.recycle();
        }

        // Setup Text Paint
        textPaint.setTextSize(textSizePx);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setLetterSpacing(0.06f);
        textPaint.setColor(textColorNormal);

        // Setup Drop Shadow
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#401D4ED8"));
    }

    public void setText(String newText) {
        this.text = newText;
        invalidate();
    }

    public String getText() {
        return text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        int desiredHeight = (int) (54 * density);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        float marginVertical = 2f * density;
        float marginHorizontal = 2f * density;
        bodyRect.set(marginHorizontal, marginVertical, w - marginHorizontal, h - marginVertical);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        float cx = width / 2f;
        float cy = height / 2f;

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, cx, cy);

        // 1. Draw Executive Blue Gradient Pill Background
        float cornerRadius = bodyRect.height() / 3f;

        LinearGradient gradient = new LinearGradient(
                bodyRect.left, bodyRect.top,
                bodyRect.right, bodyRect.bottom,
                new int[]{Color.parseColor("#1D4ED8"), Color.parseColor("#4F46E5")},
                null,
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);

        // Subtle Drop Shadow
        shadowPaint.setShadowLayer(12f, 0, 6f, Color.parseColor("#401D4ED8"));
        canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, shadowPaint);
        shadowPaint.clearShadowLayer();

        canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, bgPaint);

        // Pressed Dark Overlay Tint
        if (isPressedState) {
            Paint pressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pressPaint.setColor(Color.parseColor("#20000000"));
            canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, pressPaint);
        }

        // 2. Draw Centered Crisp White Text
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = bodyRect.centerY() - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text, bodyRect.centerX(), textY, textPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressedState = true;
                animateScale(0.96f);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (isPressedState) {
                    isPressedState = false;
                    animateScale(1.0f);
                    invalidate();
                    performClick();
                }
                isPressedState = false;
                invalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                isPressedState = false;
                animateScale(1.0f);
                invalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void animateScale(float targetScale) {
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
        }
        scaleAnimator = ValueAnimator.ofFloat(scaleFactor, targetScale);
        scaleAnimator.setDuration(160);
        scaleAnimator.setInterpolator(new OvershootInterpolator(1.2f));
        scaleAnimator.addUpdateListener(animation -> {
            scaleFactor = (float) animation.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        scaleAnimator.start();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
