package com.meili.mnist.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.meili.mnist.TF;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.meili.mnist.widget.CanvasPaints.bluePaint;
import static com.meili.mnist.widget.CanvasPaints.greenPaintFill;
import static com.meili.mnist.widget.CanvasPaints.redPaint;
import static com.meili.mnist.widget.CanvasPaints.dashPaint;

/**
 * @author zijiao
 * @version 17/8/2
 */
public class CanvasView extends View {
    private final int specW;
    private final int specH;

    public int mode = 0;
    public static final int MODE_LOCK = -1;
    public static final int MODE_PEN = 0;
    public static final int MODE_RECT = 1;
    public static final int MODE_RECT_ADJUST = 2;
    public static final int MODE_LINE = 6;
    public static final int MODE_LINE_RECT = 7;
    public static final int MODE_DRAW_H = 8;
    public static final int MODE_DRAW_H_COPY = 9;
    public static final int MODE_DRAW_H_ADJUST1 = 10;
    public static final int MODE_BAIDU_RECT = 11;
    public static final int MODE_DRAW_D = 12;
    public static final int MODE_DRAW_D_ADJUST = 13;
    public static final int MODE_DRAW_CIRCLE = 14;
    public static final int MODE_VERT_RECT = 15;



    private static int pointSize = 20;

    public Path path;
    public JSONArray pathsArr;
    public JSONArray pointsArr;
    public Rect rect;
    public List<Rect> rects = new ArrayList<>();

    private boolean rectSet = false;
    public boolean[] rSettingChecked = {false, false, false, false};
    private int[] rectSetting = {-1,-1,-1,-1};

    public Point rectStartPoint;
    public List<Rect> adjustPoint;
    public int selectedRectIndex = 0;

    public Point lineStartPoint;
    public Point lineEndPoint;
    public Point lineStartPoint2;
    public Point lineEndPoint2;
    public Point lineStartPoint3;
    public Point lineEndPoint3;
    private boolean isEmptyLineRect = false;

    public Path linePath;
    public List<Path> linePaths=new ArrayList<>();
    public List<Rect> lineRects = new ArrayList<>();
    public List<Rect> tmpLineRects = new ArrayList<>();

    public ArrayList<Path> hPaths = new ArrayList<>();
    public ArrayList<Path> dPaths = new ArrayList<>();
    public ArrayList<LineObject> lineObjects = new ArrayList<>();
    public boolean isDashLine = false;

    public Path  circle;

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        specW = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
        specH = MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 画板宽高写死为屏幕宽度
        int spec = Math.max(specW, specH);
        super.onMeasure(spec, spec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lineRects != null && lineRects.size()>0) {
            for (int i = 0; i < lineRects.size(); i += 2) {
                Rect r1 = lineRects.get(i);
                Rect r2 = lineRects.get(i+1);
                boolean isEmptyR = false;
                if(r1.top>0 || r1.left > 0){
                    canvas.drawRect(r1, redPaint);
                }else{
                    isEmptyR =true;
                }
                if(r2.top>0 || r2.left>0){
                    canvas.drawRect(r2, bluePaint);
                }else{
                    isEmptyR = true;
                }
                if(!isEmptyR && linePaths != null && linePaths.size()>0){
                    canvas.drawPath(linePaths.get(i/2),bluePaint);
                }
            }
        }
        if (tmpLineRects != null && tmpLineRects.size()>0) {
            Rect r1 = tmpLineRects.get(0);
            Rect r2 = tmpLineRects.get(1);
            canvas.drawRect(r1, redPaint);
            canvas.drawRect(r2, bluePaint);
            if(linePath!=null){
                canvas.drawPath(linePath, bluePaint);
            }
        }

        //画图题
        if(hPaths!=null && hPaths.size() >= 2){
            if(isDashLine){
                canvas.drawPath(hPaths.get(0), dashPaint);
            }else{
                canvas.drawPath(hPaths.get(0), redPaint);
            }
            canvas.drawPath(hPaths.get(1), bluePaint);
            if (hPaths.size() > 2) {
                if(isDashLine){
                    canvas.drawPath(hPaths.get(2), dashPaint);
                }else{
                    canvas.drawPath(hPaths.get(2), redPaint);
                }
            }
        }
        if(dPaths!=null && dPaths.size() > 0){
            if(isDashLine){
                canvas.drawPath(dPaths.get(0), dashPaint);
            }else{
                canvas.drawPath(dPaths.get(0), redPaint);
            }
            canvas.drawPath(dPaths.get(1), bluePaint);
        }
       if(lineObjects!=null && lineObjects.size()>0){
           for(LineObject lo :lineObjects){
               if(lo.isDash){
                   canvas.drawPath(lo.path, dashPaint);
               }else{
                   canvas.drawPath(lo.path, redPaint);
               }
//               canvas.drawPath(lo.antPath, bluePaint);
           }
       }
        if(path!=null){
            canvas.drawPath(path, bluePaint);
        }
        if(rect != null){
            canvas.drawRect(rect,redPaint);
        }
        if(rects!=null){
            for(Rect r:rects){
                canvas.drawRect(r, redPaint);
            }
        }
        if(adjustPoint!=null){
            for(Rect r: adjustPoint){
                if(r == null){
                    continue;
                }
                canvas.drawRect(r, greenPaintFill);
            }
        }
        if(circle!=null){
            canvas.drawPath(circle, redPaint);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mode == MODE_LOCK){
            return true;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN && adjustPoint!=null ){
            for(int i=0; i<adjustPoint.size(); i++){
                Rect r = adjustPoint.get(i);
                if(r == null){
                    continue;
                }
                if(r.left<=x && x<=r.right && r.top<=y && y<=r.bottom){
                    if(mode == MODE_DRAW_H) {
                        if (i == 2) {
                            mode = MODE_DRAW_H_COPY;
                            adjustPoint = null;
                            return true;
                        } else if (i == 1) {
                            adjustPoint = null;
                            return true;
                        } else if (i == 0) {
                            adjustPoint = null;
                            mode = MODE_DRAW_H_ADJUST1;
                            lineStartPoint = new Point(x, y);
                            return true;
                        }
                    }else if (mode==MODE_DRAW_D){
                        LineObject lo = lineObjects.remove(lineObjects.size()-1);
                        dPaths = new ArrayList<>();
                        dPaths.add(lo.path);
                        dPaths.add(lo.antPath);
                        if(i == 0){
                            mode = MODE_DRAW_D_ADJUST;
                            lineStartPoint3 = new Point(x, y);
                            adjustPoint = null;
                            invalidate();
                            return true;
                        } else if (i == 1) {
                            adjustPoint = null;
                            invalidate();
                            return true;
                        }
                    }else{
                        if(i < 4){
                            rectStartPoint = new Point(adjustPoint.get(3-i).left+pointSize, adjustPoint.get(3-i).top+pointSize);
                            adjustPoint.set(0, null);
                            adjustPoint.set(1, null);
                            adjustPoint.set(2, null);
                            adjustPoint.set(3, null);
                        }else if(i >=4){
                            rectStartPoint = new Point(adjustPoint.get(11-i).left+pointSize, adjustPoint.get(11-i).top+pointSize);
                            adjustPoint.set(4, null);
                            adjustPoint.set(5, null);
                            adjustPoint.set(6, null);
                            adjustPoint.set(7, null);
                        }
                        if(mode == MODE_LINE){
                            if(i < 4){
                                selectedRectIndex = 0;
                                rect = lineRects.get(lineRects.size()-2);
                            }else{
                                selectedRectIndex = 1;
                                rect = lineRects.get(lineRects.size()-1);
                            }
                            mode = MODE_LINE_RECT;
                        }else{
                            adjustPoint = null;
                            mode = MODE_RECT_ADJUST;
                        }
                    }
                    return true;
                }
            }
        }

        if(mode == MODE_PEN){
            modePenHandler(event);
        }else if(mode == MODE_RECT  || mode == MODE_RECT_ADJUST || mode == MODE_BAIDU_RECT || mode == MODE_LINE_RECT|| mode ==MODE_VERT_RECT){
           modeRectHandler(event);
        }else if(mode == MODE_LINE){
            modeLineHandler(event);
        }else if(mode==MODE_DRAW_H || mode == MODE_DRAW_H_ADJUST1 || mode == MODE_DRAW_H_COPY){
            modeDrawHHandler(event);
        }else if(mode == MODE_DRAW_D || mode == MODE_DRAW_D_ADJUST){
            modeDrawDHandler(event);
        }else if(mode == MODE_DRAW_CIRCLE){
            modeDrawCircleHandle(event);
        }
        return true;
    }

    private void modePenHandler(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        JSONArray p = new JSONArray();
        p.put((int)x);
        p.put((int)y);
        if(pointsArr == null){
            pointsArr = new JSONArray();
        }
        pointsArr.put(p);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(path == null){
                    path = new Path();
                }else{
                    path = new Path(path);
                }
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                if(pathsArr == null){
                    pathsArr = new JSONArray();
                }
                pathsArr.put(pointsArr);
                pointsArr = new JSONArray();
                if(!rectSet){
                    int side = 0;
                    RectF rf = new RectF();
                    path.computeBounds(rf,true);
                    int pathRight = (int)rf.right;
                    int pathLeft = (int)rf.left;
                    int pathBottom = (int)rf.bottom;
                    int pathTop = (int)rf.top;
                    int tmp = Math.max(pathRight - pathLeft, pathBottom - pathTop);
                    if(pathRight - pathLeft < pathBottom - pathTop){
                        side = (tmp-(pathRight-pathLeft))/2;
                        rect = new Rect(pathLeft-side -10, pathTop -10, pathRight+side +10, pathTop+tmp+10);
                    }else{
                        side = 20;
                        rect = new Rect(pathLeft -10, pathTop-side -10, pathLeft+tmp +10, pathBottom+side +10);
                    }

                    firmRectSize();
                    adjustPoint = new ArrayList<>();
                    adjustPoint.add(new Rect(rect.left-pointSize, rect.top-pointSize, rect.left+pointSize, rect.top+pointSize));
                    adjustPoint.add(new Rect(rect.left-pointSize, rect.bottom-pointSize, rect.left+pointSize, rect.bottom+pointSize));
                    adjustPoint.add(new Rect(rect.right-pointSize, rect.top-pointSize, rect.right+pointSize, rect.top+pointSize));
                    adjustPoint.add(new Rect(rect.right-pointSize, rect.bottom-pointSize, rect.right+pointSize, rect.bottom+pointSize));
                }
                break;
        }
        invalidate();
    }

    private void modeRectHandler(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(rect == null){
                    rect  = new Rect(0,0,0,0);
                }
                rect.right+=TF.DRAW_THICKNESS;
                rect.bottom+=TF.DRAW_THICKNESS;
                invalidate(rect);
                rect.left = x;
                rect.top = y;
                rect.right =rect.left;
                rect.bottom = rect.top;
                rectStartPoint = new Point(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                Rect old =new Rect(rect.left,rect.top,rect.right,rect.bottom);
                if(x > rectStartPoint.x){
                    rect.right = x;
                }else{
                    rect.right = rectStartPoint.x;
                    rect.left = x;
                }
                if(y > rectStartPoint.y){
                    rect.bottom = y;
                }else{
                    rect.bottom = rectStartPoint.y;
                    rect.top = y;
                }
                old.union(x,y);
                invalidate(old);
                break;
            case MotionEvent.ACTION_UP:
                if(mode == MODE_RECT || mode == MODE_RECT_ADJUST|| mode ==MODE_BAIDU_RECT || mode ==MODE_VERT_RECT){
                    adjustPoint = new ArrayList<>();
                    adjustPoint.add(new Rect(rect.left-pointSize, rect.top-pointSize, rect.left+pointSize, rect.top+pointSize));
                    adjustPoint.add(new Rect(rect.left-pointSize, rect.bottom-pointSize, rect.left+pointSize, rect.bottom+pointSize));
                    adjustPoint.add(new Rect(rect.right-pointSize, rect.top-pointSize, rect.right+pointSize, rect.top+pointSize));
                    adjustPoint.add(new Rect(rect.right-pointSize, rect.bottom-pointSize, rect.right+pointSize, rect.bottom+pointSize));
                    rectSet = true;
                    if(mode!=MODE_BAIDU_RECT && mode!=MODE_VERT_RECT){
                        mode = MODE_PEN;
                    }
                }else if(mode == MODE_LINE_RECT){
                    if(selectedRectIndex == 0){
                        adjustPoint.set(0,new Rect(rect.left-pointSize, rect.top-pointSize, rect.left+pointSize, rect.top+pointSize));
                        adjustPoint.set(1,new Rect(rect.left-pointSize, rect.bottom-pointSize, rect.left+pointSize, rect.bottom+pointSize));
                        adjustPoint.set(2,new Rect(rect.right-pointSize, rect.top-pointSize, rect.right+pointSize, rect.top+pointSize));
                        adjustPoint.set(3,new Rect(rect.right-pointSize, rect.bottom-pointSize, rect.right+pointSize, rect.bottom+pointSize));
                    }else if(selectedRectIndex == 1){
                        adjustPoint.set(4,new Rect(rect.left-pointSize, rect.top-pointSize, rect.left+pointSize, rect.top+pointSize));
                        adjustPoint.set(5,new Rect(rect.left-pointSize, rect.bottom-pointSize, rect.left+pointSize, rect.bottom+pointSize));
                        adjustPoint.set(6,new Rect(rect.right-pointSize, rect.top-pointSize, rect.right+pointSize, rect.top+pointSize));
                        adjustPoint.set(7,new Rect(rect.right-pointSize, rect.bottom-pointSize, rect.right+pointSize, rect.bottom+pointSize));
                    }
                    Rect r = new Rect(rect);
                    lineRects.set(lineRects.size() - (2 - selectedRectIndex),r);
                        /*
                        if(isEmptyLineRect){
                            lineRects.set(lineRects.size() - selectedRectIndex - 1,r);
                        }
                        */
                    mode = MODE_LINE;
                }
                invalidate();
                break;
        }
    }

    private void modeLineHandler(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        if(event.getAction() == MotionEvent.ACTION_UP){
            lineEndPoint = new Point(x, y);
            linePath = new Path();
            linePath.moveTo(lineStartPoint.x, lineStartPoint.y);
            linePath.lineTo(x, y);
            int rSize1 = 60;
            int rSize2 = 60;
//                if(Math.abs(x-lineStartPoint.x) < rSize1 && Math.abs(y-lineStartPoint.y)<rSize2){
            if( (lineStartPoint.x-rSize1 <= 0 && lineStartPoint.y-rSize2<=0)
                    || (lineEndPoint.x-rSize1<=0 && lineEndPoint.y-rSize2<=0)){
//                    lineEndPoint = lineStartPoint;
                isEmptyLineRect = true;
            }
            Rect r1 = new Rect(lineStartPoint.x - rSize1, lineStartPoint.y - rSize2, lineStartPoint.x + rSize1, lineStartPoint.y + rSize2);
            Rect r2 = new Rect(lineEndPoint.x - rSize1, lineEndPoint.y - rSize2, lineEndPoint.x + rSize1, lineEndPoint.y + rSize2);
            if(lineRects==null){
                lineRects = new ArrayList<>();
            }
            if(linePaths==null){
                linePaths = new ArrayList<>();
            }
            if(lineRects.size() > 0){
                for(int i=0; i<lineRects.size(); i+=2){
                    Rect tmpr =  lineRects.get(i);
                    if(tmpr.right>lineStartPoint.x && tmpr.left<lineStartPoint.x && tmpr.bottom>lineStartPoint.y && tmpr.top <lineStartPoint.y){
                        r1 = new Rect(tmpr);
                        break;
                    }
                }
                for(int i=1; i<lineRects.size(); i+=2){
                    Rect tmpr =  lineRects.get(i);
                    if(tmpr.right>lineEndPoint.x && tmpr.left<lineEndPoint.x && tmpr.bottom>lineEndPoint.y && tmpr.top <lineEndPoint.y){
                        r2 = new Rect(tmpr);
                        break;
                    }
                }
            }
            lineRects.add(r1);
            lineRects.add(r2);
            tmpLineRects = null;
            adjustPoint = new ArrayList<>();
            adjustPoint.add(new Rect(r1.left-pointSize, r1.top-pointSize, r1.left+pointSize, r1.top+pointSize));
            adjustPoint.add(new Rect(r1.left-pointSize, r1.bottom-pointSize, r1.left+pointSize, r1.bottom+pointSize));
            adjustPoint.add(new Rect(r1.right-pointSize, r1.top-pointSize, r1.right+pointSize, r1.top+pointSize));
            adjustPoint.add(new Rect(r1.right-pointSize, r1.bottom-pointSize, r1.right+pointSize, r1.bottom+pointSize));
//                if(!isEmptyLineRect){
            adjustPoint.add(new Rect(r2.left-pointSize, r2.top-pointSize, r2.left+pointSize, r2.top+pointSize));
            adjustPoint.add(new Rect(r2.left-pointSize, r2.bottom-pointSize, r2.left+pointSize, r2.bottom+pointSize));
            adjustPoint.add(new Rect(r2.right-pointSize, r2.top-pointSize, r2.right+pointSize, r2.top+pointSize));
            adjustPoint.add(new Rect(r2.right-pointSize, r2.bottom-pointSize, r2.right+pointSize, r2.bottom+pointSize));
//                }
            linePaths.add(new Path(linePath));
            linePath = null;
            invalidate();
        }else if(event.getAction() == MotionEvent.ACTION_DOWN){
            lineStartPoint = new Point(x,y);
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            rect = null;
            isEmptyLineRect = false;
            linePath = new Path();
            linePath.moveTo(lineStartPoint.x, lineStartPoint.y);
            linePath.lineTo(x, y);
            tmpLineRects = new ArrayList<>();
            int rSize1 = 60;
            int rSize2 = 60;
            Rect r1 = new Rect(lineStartPoint.x - rSize1, lineStartPoint.y - rSize2, lineStartPoint.x + rSize1, lineStartPoint.y + rSize2);
            Rect r2 = new Rect(x - rSize1, y - rSize2, x + rSize1, y + rSize2);
            tmpLineRects.add(r1);
            tmpLineRects.add(r2);
            invalidate();
        }
    }

    private void modeDrawHHandler(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        if(mode==MODE_DRAW_H){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                lineStartPoint = new Point(x,y);
                if(adjustPoint != null){
                    adjustPoint = null;
                    invalidate();
                }
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                Path pH = new Path();
                pH.moveTo(lineStartPoint.x, lineStartPoint.y);
                pH.lineTo(x, y);
                float[]  axis = PathCalculator.getBottomLine(lineStartPoint.x, lineStartPoint.y,x, y,  300);
                Path pB  = new Path();
                pB.moveTo(axis[0],axis[1]);
                pB.lineTo(axis[2],axis[3]);
                if(hPaths == null){
                    hPaths = new ArrayList<>();
                }
                if(hPaths.size() == 0){
                    hPaths.add(pH);
                    hPaths.add(pB);
                }else{
                    hPaths.set(0, pH);
                    hPaths.set(1, pB);
                }
                invalidate();
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                lineEndPoint = new Point(x,y);
                adjustPoint = new ArrayList<>();
                adjustPoint.add(new Rect(lineStartPoint.x-pointSize, lineStartPoint.y-pointSize, lineStartPoint.x+pointSize, lineStartPoint.y+pointSize));
                adjustPoint.add(new Rect(x-pointSize, y-pointSize, x+pointSize, y+pointSize));
                int midx = (lineStartPoint.x+x)/2;
                int midy = (lineStartPoint.y+y)/2;
                adjustPoint.add(new Rect(midx-pointSize*2, midy-pointSize*2, midx+pointSize*2, midy+pointSize*2));
                invalidate();
            }
        }else if(mode == MODE_DRAW_H_ADJUST1){
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                Path pH = new Path();
                pH.moveTo(x, y);
                pH.lineTo(lineEndPoint.x, lineEndPoint.y);
                float[]  axis = PathCalculator.getBottomLine(x, y, lineEndPoint.x, lineEndPoint.y, 300);
                Path pB  = new Path();
                pB.moveTo(axis[0],axis[1]);
                pB.lineTo(axis[2],axis[3]);
                if(hPaths.size() == 0){
                    hPaths.add(pH);
                    hPaths.add(pB);
                }else{
                    hPaths.set(0, pH);
                    hPaths.set(1, pB);
                }
                if(hPaths.size() ==3 ) {
                    hPaths.remove(2);
                }
                lineStartPoint = new Point(x,y);
                invalidate();
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                lineStartPoint = new Point(x,y);
                adjustPoint = new ArrayList<>();
                adjustPoint.add(new Rect(lineStartPoint.x-pointSize, lineStartPoint.y-pointSize, lineStartPoint.x+pointSize, lineStartPoint.y+pointSize));
                adjustPoint.add(new Rect(lineEndPoint.x-pointSize, lineEndPoint.y-pointSize, lineEndPoint.x+pointSize, lineEndPoint.y+pointSize));
                int midx = (lineEndPoint.x+x)/2;
                int midy = (lineEndPoint.y+y)/2;
                adjustPoint.add(new Rect(midx-pointSize*2, midy-pointSize*2, midx+pointSize*2, midy+pointSize*2));
                mode = MODE_DRAW_H;
                invalidate();
            }
        }else if(mode == MODE_DRAW_H_COPY) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float[] axis = PathCalculator.getParellLine(x, y, lineStartPoint.x, lineStartPoint.y, lineEndPoint.x, lineEndPoint.y);
                float x1 = axis[2];
                float y1 = axis[3];
                float x2 = axis[0];
                float y2 = axis[1];
                Path pH2 = new Path();
                pH2.moveTo(x1, y1);
                pH2.lineTo(x2, y2);

                lineStartPoint2 = new Point((int) x1, (int) y1);
                lineEndPoint2 = new Point((int) x2, (int) y2);

                if (hPaths.size() <= 2) {
                    hPaths.add(pH2);
                } else {
                    hPaths.set(2, pH2);
                }
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                adjustPoint = new ArrayList<>();
                adjustPoint.add(new Rect(lineStartPoint.x - pointSize, lineStartPoint.y - pointSize, lineStartPoint.x + pointSize, lineStartPoint.y + pointSize));
                adjustPoint.add(new Rect(lineEndPoint.x - pointSize, lineEndPoint.y - pointSize, lineEndPoint.x + pointSize, lineEndPoint.y + pointSize));
                int midx = (lineStartPoint2.x + lineEndPoint2.x) / 2;
                int midy = (lineStartPoint2.y + lineEndPoint2.y) / 2;
                adjustPoint.add(new Rect(midx - pointSize * 2, midy - pointSize * 2, midx + pointSize * 2, midy + pointSize * 2));
                mode = MODE_DRAW_H;
                invalidate();
            }
        }
    }

    private void modeDrawDHandler(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        if(mode == MODE_DRAW_D){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                lineStartPoint3 = new Point(x,y);
                if(adjustPoint != null){
                    adjustPoint = null;
                    invalidate();
                }
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                Path pD = new Path();
                pD.moveTo(lineStartPoint3.x, lineStartPoint3.y);
                pD.lineTo(x, y);

                float[] kb = PathCalculator.getkb(lineStartPoint3.x, lineStartPoint3.y, x, y);

                Path pD2 = new Path();
                float x2 = 0;
                if(x > lineStartPoint3.x){
                    x2 = lineStartPoint3.x-300;
                }else{
                    x2 = lineStartPoint3.x+300;
                }
                float y2 = kb[0] * x2 + kb[1];
                pD2.moveTo(lineStartPoint3.x, lineStartPoint3.y);
                pD2.lineTo(x2,y2);
//                Log.i("mnist", lineStartPoint3.x+"  "+lineStartPoint3.y+"  "+x+"  "+y+"  "+x2+"  "+y2);

                if(dPaths == null){
                    dPaths = new ArrayList<>();
                }
                if(dPaths.size() == 0){
                    dPaths.add(pD);
                    dPaths.add(pD2);
                }else{
                    dPaths.set(0, pD);
                    dPaths.set(1, pD2);
                }

                invalidate();

            }else if(event.getAction() == MotionEvent.ACTION_UP){
                if(dPaths==null || dPaths.size()==0){
                    return;
                }
                lineEndPoint3 = new Point(x,y);
                adjustPoint = new ArrayList<>();
                adjustPoint.add(new Rect(lineStartPoint3.x-pointSize, lineStartPoint3.y-pointSize, lineStartPoint3.x+pointSize, lineStartPoint3.y+pointSize));
                adjustPoint.add(new Rect(lineEndPoint3.x-pointSize, lineEndPoint3.y-pointSize, lineEndPoint3.x+pointSize, lineEndPoint3.y+pointSize));

                if(lineObjects==null){
                    lineObjects = new ArrayList<>();
                }
                LineObject lo = new LineObject(lineStartPoint3, lineEndPoint3, isDashLine);
                lo.setPaths(dPaths.get(0), dPaths.get(1));
                lineObjects.add(lo);
                dPaths = null;
                invalidate();
            }
        }else if(mode==MODE_DRAW_D_ADJUST){
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                Path pD = new Path();
                pD.moveTo(x,y);
                pD.lineTo(lineEndPoint3.x, lineEndPoint3.y);
                float[] kb = PathCalculator.getkb(x, y,lineEndPoint3.x, lineEndPoint3.y);
                Path pD2 = new Path();
                float x2 = 0;
                if(x < lineEndPoint3.x){
                    x2 = x-300;
                }else{
                    x2 = x+300;
                }
                float y2 = kb[0] * x2 + kb[1];
                pD2.moveTo(lineStartPoint3.x, lineStartPoint3.y);
                pD2.lineTo(x2,y2);
                dPaths.set(0, pD);
                dPaths.set(1, pD2);
                lineStartPoint3 = new Point(x,y);
                invalidate();

            }else if(event.getAction() == MotionEvent.ACTION_UP){
                lineStartPoint3 = new Point(x,y);
                adjustPoint = new ArrayList<>();
                adjustPoint.add(new Rect(lineStartPoint3.x-pointSize, lineStartPoint3.y-pointSize, lineStartPoint3.x+pointSize, lineStartPoint3.y+pointSize));
                adjustPoint.add(new Rect(lineEndPoint3.x-pointSize, lineEndPoint3.y-pointSize, lineEndPoint3.x+pointSize, lineEndPoint3.y+pointSize));
                mode = MODE_DRAW_D;
                LineObject lo = new LineObject(lineStartPoint3, lineEndPoint3, isDashLine);
                lo.setPaths(dPaths.get(0), dPaths.get(1));
                lineObjects.add(lo);
                dPaths = null;

                invalidate();
            }
        }
    }

    private void modeDrawCircleHandle(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        if(mode == MODE_DRAW_CIRCLE){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                circle = new Path();
                circle.moveTo(x,y);
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){

                circle.lineTo(x,y);
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                circle.close();
            }
            invalidate();
        }
    }

    public JSONArray getPoints(){
        if(pathsArr == null){
            pathsArr = new JSONArray();
        }
        Log.i("mnist", "pathsArr="+pathsArr.toString());
        return pathsArr;
    }

    public void clean() {
        path = null;
        rects = null;
        rect = null;
        linePath = null;
        linePaths = null;
        lineRects = null;
        hPaths = null;
        dPaths = null;
        lineObjects = null;
        adjustPoint = null;
        pointsArr = null;
        pathsArr = new JSONArray();
        lineStartPoint = null;
        lineEndPoint = null;
        lineStartPoint2 = null;
        lineEndPoint2 = null;
        rectSet = false;
        circle = null;
        invalidate();
    }

    public void setPointSize(int s){
        pointSize = s;
        if(rect !=null){
            adjustPoint = new ArrayList<>();
            adjustPoint.add(new Rect(rect.left-pointSize, rect.top-pointSize, rect.left+pointSize, rect.top+pointSize));
            adjustPoint.add(new Rect(rect.left-pointSize, rect.bottom-pointSize, rect.left+pointSize, rect.bottom+pointSize));
            adjustPoint.add(new Rect(rect.right-pointSize, rect.top-pointSize, rect.right+pointSize, rect.top+pointSize));
            adjustPoint.add(new Rect(rect.right-pointSize, rect.bottom-pointSize, rect.right+pointSize, rect.bottom+pointSize));
            invalidate();
        }
    }

    public void setRectSize(){
        int[] tmpRect = null;
        if(rects.size() >= 1){
            Rect r = rects.get(rects.size()-1);
            tmpRect = new int[]{r.left, r.top, r.right, r.bottom};
        }else{
            return;
        }
        for(int i=0; i<rSettingChecked.length; i++) {
            if (rSettingChecked[i]) {
                switch (i){
                    case 0:
                    case 1:
                        rectSetting[i] = tmpRect[i];
                        break;
                    case 2:
                        rectSetting[i] = tmpRect[2] - tmpRect[0];
                        break;
                    case 3:
                        rectSetting[i] = tmpRect[3] - tmpRect[1];
                        break;
                }
            }
        }
    }

    private boolean firmRectSize(){
        boolean isRectChanged = false;
        for(int i=0; i<rSettingChecked.length; i++) {
            if (rSettingChecked[i]) {
                if(rectSetting[i] == -1){
                    continue;
                }
                isRectChanged = true;
                switch (i){
                    case 0:
                        rect.left = rectSetting[i];
                        break;
                    case 1:
                        rect.top = rectSetting[i];
                        break;
                    case 2:
                        rect.right = rect.left + rectSetting[2];
                        break;
                    case 3:
                        rect.bottom = rect.top + rectSetting[3];
                        break;
                }
            }
        }
        return isRectChanged;
    }

    public boolean isEmpty() {
        return path==null && rect==null && linePaths==null && dPaths==null && lineObjects==null && hPaths==null && circle==null;
    }

    public Bitmap getCanvasBmp(){
        try{
            setDrawingCacheEnabled(true);
            setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            Bitmap cache = getDrawingCache();
            return cache;
        }catch (Exception err){
            err.printStackTrace();
        }
        return null;
    }





}
