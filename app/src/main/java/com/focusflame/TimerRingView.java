package com.focusflame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Doira progress ring — Taymer progressini ko'rsatadi
 */
public class TimerRingView extends View {

    private Paint bgPaint, progressPaint, centerPaint;
    private float progress = 0f; // 0.0 → 1.0
    private RectF oval;

    public TimerRingView(Context context) {
        super(context);
        init();
    }

    public TimerRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Orqa fon halqa
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(18f);
        bgPaint.setColor(0xFF171A21);

        // Progress halqa (binafsha)
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(18f);
        progressPaint.setColor(0xFF6C5CFF);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Markaz doirasi
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(0xFF0F1117);

        oval = new RectF();
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate(); // Qayta chizish
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        float radius = Math.min(cx, cy) - 20f;

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Orqa fon halqa
        canvas.drawArc(oval, 0, 360, false, bgPaint);

        // Progress halqa (yuqoridan boshlanadi)
        float sweepAngle = 360f * progress;
        canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);

        // Markaz doirasi (ichki qoralik)
        float innerRadius = radius - 20f;
        canvas.drawCircle(cx, cy, innerRadius, centerPaint);
    }
}
