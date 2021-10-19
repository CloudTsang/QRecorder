package com.meili.mnist.widget;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.TypedValue;

import com.meili.mnist.TF;

public class CanvasPaints {
    public static  Paint bluePaint;
    public static  Paint redPaint;
    public static  Paint greenPaint;
    public static  Paint blackPaint;
    public static  Paint dashPaint;
    public static  Paint greenPaintFill;

    public static void initPaints(Activity act){
        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, act.getResources().getDisplayMetrics()));
        bluePaint.setStyle(Paint.Style.STROKE);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, act.getResources().getDisplayMetrics()));
        redPaint.setStyle(Paint.Style.STROKE);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, act.getResources().getDisplayMetrics()));
        greenPaint.setStyle(Paint.Style.STROKE);

        greenPaintFill = new Paint(greenPaint);
        greenPaintFill.setStyle(Paint.Style.FILL);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, act.getResources().getDisplayMetrics()));
        blackPaint.setStyle(Paint.Style.STROKE);

        dashPaint = new Paint(redPaint);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{10,10}, 0));
    }
}
