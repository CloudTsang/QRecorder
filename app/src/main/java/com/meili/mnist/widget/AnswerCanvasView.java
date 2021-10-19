package com.meili.mnist.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.graphics.Path;

import com.meili.mnist.QCanvasActivity;
import com.meili.mnist.TF;

import java.util.ArrayList;
import java.util.List;

import static com.meili.mnist.widget.CanvasPaints.bluePaint;
import static com.meili.mnist.widget.CanvasPaints.blackPaint;
import static com.meili.mnist.widget.CanvasPaints.dashPaint;
import static com.meili.mnist.widget.CanvasPaints.redPaint;

public class AnswerCanvasView extends View {
    private final int specW;
    private final int specH;

    //填空题
    public List<Path> paths = new ArrayList<>();
    public List<Rect> rects = new ArrayList<>();

    //连线题
    public List<Path> linePaths = new ArrayList<>();
    public List<Rect> lineRects = new ArrayList<>();
    public List<Rect> grects = new ArrayList<>();


    //画图题
    public List<ArrayList<Path>> hPaths = new ArrayList<>();
    public List<Path> dPaths = new ArrayList<>();
    public List<Rect> degreeRects = new ArrayList<>();
    public List<Float> degrees = new ArrayList<>();
    private List<Paint> paints = new ArrayList<>();
    private List<Paint> dpaints = new ArrayList<>();


    public AnswerCanvasView(Context context) {
        this(context, null);
    }
    public AnswerCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

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

        //填空题
        if(paths!=null){
            for(Path p:paths){
                canvas.drawPath(p, blackPaint);
            }
        }
        if(rects!=null){
            for(Rect r:rects){
                canvas.drawRect(r, redPaint);
            }
        }

        //连线题
        /*if(linePaths!=null){
            for(Path p:linePaths){
                canvas.drawPath(p, blackPaint);
            }
        }*/
        if(lineRects!=null){
            for(int i=0; i<lineRects.size()-1; i+=2){
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
        if(grects!=null){
            for(Rect r:grects){
                    canvas.drawRect(r, blackPaint);
                }
        }

        //画图题
        if(hPaths!=null && hPaths.size()>0){
            for(int i=0;i<hPaths.size();i++){
                ArrayList<Path> tmpPaths = hPaths.get(i);
                Paint p = redPaint;
                if(paints!=null &&i<paints.size()){
                    p = paints.get(i);
                }
                canvas.drawPath(tmpPaths.get(0), p);
                if(tmpPaths.size()>1){
                    canvas.drawPath(tmpPaths.get(1), p);
                    canvas.drawPath(tmpPaths.get(2), blackPaint);
                }
            }
        }
        if(dPaths!=null && dPaths.size()>0){
            for(int i=0;i<dPaths.size();i++){
                Path p = dPaths.get(i);
                canvas.drawPath(p, dpaints.get(i));
            }
        }
        if(degreeRects!=null && degreeRects.size()>0){
            for(int i=0; i<degreeRects.size(); i++){
                Rect rect = degreeRects.get(i);
                float d = degrees.get(i);
                Path p = new Path();
                p.moveTo(rect.left, rect.top);
                p.lineTo(rect.right, rect.top);
                p.lineTo(rect.right,rect.bottom);
                canvas.save();
                canvas.rotate(d, rect.left,rect.bottom);
                canvas.drawPath(p, redPaint);
//                canvas.drawRect(rect, redPaint);
                canvas.restore();
            }
        }
    }

    public void drawAnswer(AnswerRecord rec){
        rects = new ArrayList<>();
        paths = new ArrayList<>();
        for(AnswerObject obj : rec.objs){
            if(obj.rect!=null){
                rects.add(obj.rect);
            }
            if(obj.path!=null){
                paths.add(obj.path);
            }
        }
//        invalidate();
    }

    public void drawAnswerLine(AnswerRecord rec){
        linePaths = new ArrayList<>();
        lineRects = new ArrayList<>();
        grects = new ArrayList<>();
        for(ArrayList<Rect> rs:rec.ansRects){
            for(Rect r:rs){
                lineRects.add(r);
            }
        }
        for(ArrayList<Path> ps:rec.ansPaths){
            for(Path p:ps){
                linePaths.add(p);
            }
        }
        for(Rect grs:rec.groupRects){
            grects.add(grs);
        }
//        invalidate();
    }

    public void drawAnswerDrawH(AnswerRecord rec){
        hPaths = new ArrayList<>();
        dPaths = new ArrayList<>();
        paints = new ArrayList<>();
        degrees=  new ArrayList<>();
        degreeRects = new ArrayList<>();
//        degreePaths = new ArrayList<>();
        for(int i=0; i<rec.ansPoints.size(); i++){
            ArrayList<Point> pts = rec.ansPoints.get(i);
            ArrayList<Path> tmpPaths = new ArrayList<>();
            Point pt1 = pts.get(0);
            Point pt2 = pts.get(1);
            Path p1 = new Path();
            p1.moveTo(pt1.x, pt1.y);
            p1.lineTo(pt2.x, pt2.y);
            tmpPaths.add(p1);

            float k = PathCalculator.getkb(pt1.x, pt1.y, pt2.x, pt2.y)[0];
            float angle =  (float)(Math.atan(k) * 180/3.1415);
//            Log.i("mnist", "angle="+angle + " k="+k);
            if(angle<0 && pt2.y > pt1.y){//第三象限
                angle = 90+angle;
            }else if(angle>0 && pt2.y<pt1.y){//第二象限
                angle = angle+90;
            }else if(angle<0 && pt2.y < pt1.y){//第一象限
                angle=angle-90;
            }else if(angle>0 && pt2.y > pt1.y){//第四象限
                angle=-(90-angle);
            }
            degrees.add(angle);
            Rect r = new Rect(pt2.x, pt2.y-40, pt2.x+40, pt2.y);
            degreeRects.add(r);

            if(pts.size()>2){
                Point pt3 = pts.get(2);
                Point pt4 = pts.get(3);
                Path p2 = new Path();
                p2.moveTo(pt3.x, pt3.y);
                p2.lineTo(pt4.x, pt4.y);

                degrees.add((float)angle);
                Rect r2 = new Rect(pt4.x, pt4.y-40, pt4.x+40, pt4.y);
                degreeRects.add(r2);
                Path p3 = new Path();
                p3.moveTo(pt2.x, pt2.y);
                p3.lineTo(pt4.x, pt4.y);
                tmpPaths.add(p2);
                tmpPaths.add(p3);
            }

            hPaths.add(tmpPaths);
            boolean isDash = rec.dash.get(i);
            if(isDash){
                paints.add(CanvasPaints.dashPaint);
            }else{
                paints.add(CanvasPaints.redPaint);
            }
        }


        dpaints = new ArrayList<>();
        dPaths = new ArrayList<>();
        for(int i=0; i<rec.lineObjects.size();i++){
            List<LineObject> los = rec.lineObjects.get(i);
            if(los==null || los.size()==0){
                continue;
            }
            for(LineObject lo:los){
                Path p = new Path();
                p.moveTo(lo.startPoint.x, lo.startPoint.y);
                p.lineTo(lo.endPoint.x, lo.endPoint.y);
                dPaths.add(p);
                if(lo.isDash){
                    dpaints.add(dashPaint);
                }else{
                    dpaints.add(redPaint);
                }
            }
        }
//        invalidate();
    }

    public void drawAnswerSolveEqual(AnswerRecord rec){
        rects = new ArrayList<>();
        for(AnswerObject obj : rec.objs){
            rects.add(new Rect(obj.rect));
        }
        invalidate();
    }

    public void drawAnswer(AnswerRecord rec, float scaleX, float scaleY, int offsetx, int offsety){
        rects = new ArrayList<>();
        paths = new ArrayList<>();
        Matrix mat = new Matrix();
        mat.setScale(scaleX, scaleY);
        for(AnswerObject obj : rec.objs){
            if(obj.rect!=null){
                Rect r= new Rect(
                        (int)(obj.rect.left*scaleX)+offsetx,
                        (int)(obj.rect.top*scaleY)+offsety,
                        (int)(obj.rect.right*scaleX)+offsetx,
                        (int)(obj.rect.bottom*scaleY)+offsety
                        );
                rects.add(r);
            }
            if(obj.path!=null){
                Path p = new Path(obj.path);
                p.transform(mat);
                p.offset(offsetx, offsety);
                paths.add(p);
            }else if(obj.pobj!=null && obj.pobj.paths!=null){
                for(Path p0:obj.pobj.paths){
                    Path p = new Path(p0);
                    p.transform(mat);
                    p.offset(offsetx, offsety);
                    paths.add(p);
                }
            }
        }
//        invalidate();
    }

    public void drawAnswerLine(AnswerRecord rec, float scaleX, float scaleY, int offsetx, int offsety){
        grects = new ArrayList<>();
        lineRects = new ArrayList<>();
        linePaths = new ArrayList<>();
        for(int i=0; i<rec.ansRects.size(); i++){
            ArrayList<Rect> rs = rec.ansRects.get(i);
            int minx = 100000;
            int miny = 100000;
            int maxx = -1;
            int maxy = -1;
            for(int j=0;j<rs.size()-1;j+=2){
                Rect r1 = new Rect(rs.get(j));
                Rect r2 = new Rect(rs.get(j+1));

                r1.left = (int)((float)r1.left*scaleX) + offsetx;
                r1.top = (int)((float)r1.top*scaleY) + offsety;
                r1.right = (int)((float)(r1.right)*scaleX) + offsetx;
                r1.bottom = (int)((float)(r1.bottom)*scaleY) + offsety;
                r2.left = (int)((float)r2.left*scaleX) + offsetx;
                r2.top = (int)((float)r2.top*scaleY) + offsety;
                r2.right = (int)((float)(r2.right)*scaleX) + offsetx;
                r2.bottom = (int)((float)(r2.bottom)*scaleY) + offsety;
                lineRects.add(r1);
                lineRects.add(r2);

                Path p = new Path();
                p.moveTo(r1.left + (r1.right-r1.left)/2, r1.top + (r1.bottom-r1.top)/2);
                p.lineTo(r2.left + (r2.right-r2.left)/2, r2.top + (r2.bottom-r2.top)/2);
                linePaths.add(p);

                minx = Math.min(r1.left, minx);
                miny = Math.min(r1.top, miny);
                maxx = Math.max(r1.right, maxx);
                maxy = Math.max(r1.bottom, maxy);
                minx = Math.min(r2.left, minx);
                miny = Math.min(r2.top, miny);
                maxx = Math.max(r2.right, maxx);
                maxy = Math.max(r2.bottom, maxy);
            }

            minx -= 20;
            miny -= 20;
            maxx += 20;
            maxy += 20;
            Rect r = new Rect(minx,miny,maxx,maxy);
            grects.add(r);
        }
//        invalidate();
    }

    public void drawAnswerDrawH(AnswerRecord rec, float scaleX, float scaleY, int offsetx, int offsety){
//        Log.i("mnist", scaleX+"  "+scaleY+"  "+offsetx+"  "+offsety);
        hPaths = new ArrayList<>();
        dPaths = new ArrayList<>();
        paints = new ArrayList<>();
        degrees=  new ArrayList<>();
        degreeRects = new ArrayList<>();
//        degreePaths = new ArrayList<>();
        for(int i=0; i<rec.ansPoints.size(); i++){
            ArrayList<Point> pts = rec.ansPoints.get(i);
            ArrayList<Path> tmpPaths = new ArrayList<>();

            Point pt1 = resizePoint(pts.get(0), scaleX, scaleY, offsetx, offsety);
            Point pt2 = resizePoint(pts.get(1), scaleX, scaleY, offsetx, offsety);

            pt2 = PathCalculator.warpPointVert(pts.get(0),pts.get(1), pt1,pt2,scaleX,scaleY);

            Path p1 = new Path();
            p1.moveTo(pt1.x, pt1.y);
            p1.lineTo(pt2.x, pt2.y);
            tmpPaths.add(p1);

            float k = PathCalculator.getkb(pt1.x, pt1.y, pt2.x, pt2.y)[0];
            float angle =  (float)(Math.atan(k) * 180/3.1415);
//            Log.i("mnist", "angle="+angle + " k="+k);
            if(angle<0 && pt2.y > pt1.y){//第三象限
                angle = 90+angle;
            }else if(angle>0 && pt2.y<pt1.y){//第二象限
                angle = angle+90;
            }else if(angle<0 && pt2.y < pt1.y){//第一象限
                angle=angle-90;
            }else if(angle>0 && pt2.y > pt1.y){//第四象限
                angle=-(90-angle);
            }
            degrees.add(angle);
            Rect r = new Rect(pt2.x, pt2.y-40, pt2.x+40, pt2.y);
            degreeRects.add(r);

            if(pts.size()>2){
                Point pt3 = resizePoint(pts.get(2),scaleX,scaleY,offsetx,offsety);
                Point pt4 = resizePoint(pts.get(3),scaleX,scaleY,offsetx,offsety);

                pt3 = PathCalculator.warpPointVert(pts.get(3),pts.get(2), pt4,pt3,scaleX,scaleY);
//                pt4 = PathCalculator.warpPointVert(pts.get(2),pts.get(3), pt3,pt4,scaleX,scaleY);

                Path p2 = new Path();
                p2.moveTo(pt3.x, pt3.y);
                p2.lineTo(pt4.x, pt4.y);

                degrees.add((float)angle);
                Rect r2 = new Rect(pt4.x, pt4.y-40, pt4.x+40, pt4.y);
                degreeRects.add(r2);
                Path p3 = new Path();
                p3.moveTo(pt2.x, pt2.y);
                p3.lineTo(pt4.x, pt4.y);
                tmpPaths.add(p2);
                tmpPaths.add(p3);
            }

            hPaths.add(tmpPaths);
            boolean isDash = rec.dash.get(i);
            if(isDash){
                paints.add(CanvasPaints.dashPaint);
            }else{
                paints.add(CanvasPaints.redPaint);
            }
        }

        dpaints = new ArrayList<>();
        dPaths = new ArrayList<>();
        for(int i=0; i<rec.lineObjects.size();i++){
            List<LineObject> los = rec.lineObjects.get(i);
            if(los==null || los.size()==0){
                continue;
            }
            for(LineObject lo:los){
                Path p = new Path();
                Point sp = resizePoint(lo.startPoint,scaleX,scaleY,offsetx,offsety);
                Point ep = resizePoint(lo.endPoint,scaleX,scaleY,offsetx,offsety);
                p.moveTo(sp.x, sp.y);
                p.lineTo(ep.x, ep.y);
                dPaths.add(p);
                if(lo.isDash){
                    dpaints.add(dashPaint);
                }else{
                    dpaints.add(redPaint);
                }
            }
        }

    }

    public void clean(){
        rects = null;
        paths = null;
        linePaths = null;
        lineRects = null;
        grects = null;
        hPaths = null;
        dPaths = null;
        paints = null;
        dpaints = null;
        degrees = null;
        degreeRects = null;
//        degreePaths = null;
        invalidate();
    }

    private Point resizePoint(Point p, float scaleX, float scaleY, int offsetx, int offsety){
        int x = (int)(p.x*scaleX+offsetx);
        int y = (int)(p.y*scaleY+offsety);
        return new Point(x,y);
    }
}
