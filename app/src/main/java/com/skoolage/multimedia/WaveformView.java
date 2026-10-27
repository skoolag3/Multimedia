package com.skoolage.multimedia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

public class WaveformView extends View {
    private final LinkedList<Integer> amplitudes = new LinkedList<>();
    private final Paint paint = new Paint();
    private static final int MAX_POINTS = 100;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(4f);
    }

    public void addAmplitude(int amplitude) {
        amplitudes.add(amplitude);
        if (amplitudes.size() > MAX_POINTS) {
            amplitudes.removeFirst();
        }
        invalidate();
    }

    public void reset() {
        amplitudes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2f;
        float spacing = width / MAX_POINTS;

        int i = 0;
        for (int amp : amplitudes) {
            float normalized = Math.min(amp / 32767f, 1f);
            float lineHeight = normalized * (height / 2f);
            float x = i * spacing;
            canvas.drawLine(x, centerY - lineHeight, x, centerY + lineHeight, paint);
            i++;
        }
    }
}
