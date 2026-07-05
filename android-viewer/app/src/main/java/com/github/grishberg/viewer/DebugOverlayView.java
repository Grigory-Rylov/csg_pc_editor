package com.github.grishberg.viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.github.grishberg.cad3d.ui.DebugPointUi;
import com.github.grishberg.cad3d.ui.DebugPointsRenderer;

import java.util.ArrayList;
import java.util.List;

public class DebugOverlayView extends View implements DebugPointsRenderer {
    private final Paint pointPaint;
    private final Paint textPaint;
    private final List<DebugPointUi> debugPoints = new ArrayList<>();
    private final float circleRadius;

    public DebugOverlayView(Context context) {
        this(context, null);
    }

    public DebugOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Конвертируем 10dp в пиксели
        circleRadius = 10 * getResources().getDisplayMetrics().density;

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public void renderDebugPoints(List<DebugPointUi> points) {
        debugPoints.clear();
        if (points != null) {
            debugPoints.addAll(points);
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (DebugPointUi point : debugPoints) {
            // Преобразуем 3D координаты в 2D экранные координаты

            // Устанавливаем цвет точки
            pointPaint.setColor(point.color.toArgb());

            // Рисуем окружность
            canvas.drawCircle(point.x, point.y, circleRadius, pointPaint);

            // Рисуем текст рядом
            canvas.drawText(point.title, point.x + circleRadius + 5,
                point.y, textPaint);
        }
    }
}