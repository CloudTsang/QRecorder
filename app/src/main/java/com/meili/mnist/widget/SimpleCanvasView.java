package com.meili.mnist.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import android.graphics.Path;

import com.meili.mnist.TF;

import java.util.ArrayList;

public class SimpleCanvasView extends View {
    private ArrayList<Path> paths = new ArrayList<>();
    public ArrayList<Rect> rects;
    public Path path;
    private Paint paint;
    private String pointStr;
    private ArrayList<String> pointStrs = new ArrayList<>();
    public boolean isLock = false;
    public SimpleCanvasView(Context context) {
        this(context, null);
    }
    public SimpleCanvasView(Context context, AttributeSet attrs){
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, getResources().getDisplayMetrics()));
        paint.setStyle(Paint.Style.STROKE);
        pointStr = "[";
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int specW = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
        int specH = MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY);
        super.onMeasure(specW, specH);
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(path != null){
            canvas.drawPath(path, paint);
        }
        if(rects!=null){
            for(Rect r:rects){
                canvas.drawRect(r, CanvasPaints.redPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(isLock){
            return true;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                if(path == null){
                    path = new Path();
                }
                if(pointStr.contains("]]]")){
                    pointStr.replace("]]]", "]],");
                }
                pointStr += "[[" + (int)x + "," + (int)y + "]";
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                pointStr += ",[" + (int)x + "," + (int)y + "]";
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new Path(path));
                pointStr += "],";
                pointStrs.add(pointStr);
                break;
        }
        invalidate();
        return true;
    }

    public String getPointsString(){
        if(pointStr.length() == 1){
            return "[]";
        }
        String retPoints = pointStr.substring(0, pointStr.length() - 1);
        retPoints = retPoints + "]";
        return retPoints;
    }

    public void clean(){
        pointStr = "[";
        paths = new ArrayList<>();
        pointStrs = new ArrayList<>();
        path = null;
        rects = null;
        invalidate();
    }

    public void stepBack(){
        if(paths.size() > 1){
            paths.remove(paths.size()-1);
            path = new Path(paths.get(paths.size()-1));
            pointStrs.remove(pointStrs.size()-1);
            pointStr = new String(pointStrs.get(pointStrs.size()-1));
        }else{
            paths = new ArrayList<>();
            pointStrs = new ArrayList<>();
            path = null;
            pointStr = "[";
        }
        invalidate();
    }

    public boolean isEmpty(){
        return paths.size() == 0;
    }

}
