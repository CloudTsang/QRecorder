package com.meili.mnist.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;

import android.os.StrictMode;
import android.util.Log;

import com.meili.mnist.ocr.BaiduOcr;

import org.json.JSONArray;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathObject {
    public List<Path> paths;
    public JSONArray startPoint;
    public JSONArray endPoint;
    public List<JSONArray> points;
    public int maxX = -1;
    public int maxY = -1;
    public int minX = -1;
    public int minY = -1;
    public boolean lineMatch = false;
    public boolean isBlank = false;
    public boolean checked8 = false;
    public boolean checked5 = false;
    public boolean checkedGe = false;
    private ArrayList<RectF> bounds;

    public PathObject(){
            paths = new ArrayList<>();
            points = new ArrayList<>();
    }

    public boolean addPath(Path p){
        RectF rf = new RectF();
        p.computeBounds(rf,true);
        minX = (minX==-1)?(int)rf.left:Math.min((int)rf.left,minX);
        minY = (minY==-1)?(int)rf.top:Math.min((int)rf.top,minY);
        maxX = (maxX==-1)?(int)rf.right:Math.max((int)rf.right,maxX);
        maxY = (maxY==-1)?(int)rf.bottom:Math.max((int)rf.bottom,maxY);
        paths.add(p);
        return true;
    }

    public boolean addPaths(PathObject obj){
       /* if(paths.size()>=2){
            return false;
        }*/
        paths.addAll(obj.paths);
//        Log.i("mnist", minX+ " " + maxX + " "+minY+" "+maxY);
        minX = (minX==-1)?obj.minX:Math.min(obj.minX,minX);
        minY = (minY==-1)?obj.minY:Math.min(obj.minY,minY);
        maxX = (maxX==-1)?obj.maxX:Math.max(obj.maxX,maxX);
        maxY = (maxY==-1)?obj.maxY:Math.max(obj.maxY,maxY);
//        Log.i("mnist", minX+ " " + maxX + " "+minY+" "+maxY);
        points.addAll(obj.points);
        return true;
    }

    public boolean checkConnect(PathObject obj){
        int minx = obj.minX;
        int maxx = obj.maxX;
        int miny = obj.minY;
        int maxy = obj.maxY;

       boolean iscross = checkCross(obj);
       if(iscross){
//           Log.i("mnist", "iscross");
           return true;
       }

        boolean judgeTotIn = (maxx<maxX && minx>minX) || (maxx>maxX && minx < minX);
        if(judgeTotIn){
//            Log.i("mnist", "judgetotin");
            return true;
        }
        boolean judgeIn = false;
        if(minx < maxX && minX < maxx) {
            float th =(float)(maxX-minx)/(float)(maxx-minX);
            if( Math.abs(th) > 0.75){
                judgeIn = true;
            }
        }
        if(judgeIn){
//            Log.i("mnist", "judgein");
            return true;
        }
        boolean isdot = (maxY - minY) < (maxy-miny)/3 && minY > miny+(maxy-miny)/2;
        if(isdot){
            Log.i("mnist", "isdot");
            return false;
        }
        if(minX < minx && minx < maxX - (maxX-minX)/2){
//            Log.i("mnist", "checkconnect1");
            return true;
        }
        if(check5(obj)){
            Log.i("mnist", "check5");
            return true;
        }
        return false;
    }

    public boolean check5(PathObject obj) {
        if(paths.size()+obj.paths.size()>2){
            return false;
        }
        int minx = obj.minX;
        int maxx = obj.maxX;
        int miny = obj.minY;
        int maxy = obj.maxY;
        boolean judge5 = (miny < minY + (maxY - minY) / 4) && ((maxy - miny) < (maxY - minY) / 4);
        if (minx > maxX) {
            judge5 = judge5 && (minx - maxX) < (maxx - minx);
        } else {
            boolean judgeUpleft = minx < maxX && maxy < maxY;
            judge5 = judge5 && judgeUpleft;
        }
        if (judge5) {
            return true;
        }
        return false;
    }

    public boolean checkChn8(PathObject obj){
        if(paths.size() > 1 || obj.paths.size() > 1){
            return false;
        }
        try{
            PathObject leftObj = null;
            PathObject rightObj = null;
            if(minX < obj.minX){
                leftObj = this;
                rightObj = obj;
            }else{
                leftObj = obj;
                rightObj = this;
            }
            if(leftObj.startPoint.getInt(0) < leftObj.endPoint.getInt(0)
            || leftObj.startPoint.getInt(1) > leftObj.endPoint.getInt(1)){
                return false;
            }
            if(rightObj.startPoint.getInt(0) > rightObj.endPoint.getInt(0)
            || rightObj.startPoint.getInt(1) > rightObj.endPoint.getInt(1)){
                return false;
            }
            if (rightObj.startPoint.getInt(0) - leftObj.startPoint.getInt(0) < rightObj.endPoint.getInt(0) - leftObj.endPoint.getInt(0)) {
                return true;
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return false;
    }

    public boolean checkCross(PathObject obj){
        boolean check1 = obj.maxX > minX && obj.minX < maxX;
        if(!check1){
            return false;
        }
        float crossPart = (maxX - obj.minX)*3;
//        Log.i("mnist", "corssPart = "+crossPart+"   maxX-minX = "+(maxX-minX));
        boolean check2 = crossPart > obj.maxX-obj.minX || crossPart > maxX-minX;
        return  check2;
//        return true;
    }

    public ArrayList<ArrayList<PathObject>> checkFraction(){
        if(paths.size() < 3){
            return null;
        }
        bounds = new ArrayList<>();

        float curFractionLineLenght = 0;
        RectF fractionLineRect = null;
        int fractionLineIndex = -1;
        for(int i=0; i<paths.size(); i++){
            RectF rf = new RectF();
            Path p = paths.get(i);
            p.computeBounds(rf,true);
            if(rf.bottom-rf.top >= (maxY-minY)*0.7){
                bounds.add(rf);
                continue;
            }
            if(rf.top > minY + (maxY-minY)*0.3 && rf.bottom < minY + (maxY-minY)*0.7 //y轴位于中间部分
                    && rf.right-rf.left >= (maxX-minX)*0.8
                    && rf.right-rf.left > curFractionLineLenght
                    ){
                curFractionLineLenght = rf.right-rf.left;
                fractionLineIndex = i;
                fractionLineRect = rf;
            }
            bounds.add(rf);
        }
        if(fractionLineRect == null){
            Log.i("mnist", "fraction line not found");
            return null;
        }
        Log.i("mnist", "fractionLineIndex = "+fractionLineIndex);
        ArrayList<PathObject> up = new ArrayList<>();
        ArrayList<PathObject> down = new ArrayList<>();
        boolean upword = false;
        boolean downword = false;
        for(int i=0; i<bounds.size(); i++){
            if(i == fractionLineIndex){
                continue;
            }
            RectF rf = bounds.get(i);
            if(rf.centerY() < fractionLineRect.top){
                Path p = paths.get(i);
                PathObject obj = new PathObject();
                obj.addPath(p);
                up.add(obj);
                if(!upword && rf.bottom-rf.top>=(maxY-minY)*0.2){
                    upword = true;
                }
            }else if(rf.centerY() > fractionLineRect.bottom){
                PathObject obj = new PathObject();
                obj.addPath(paths.get(i));
                down.add(obj);
                if(!downword && rf.bottom-rf.top>=(maxY-minY)*0.2){
                    downword = true;
                }
            }
        }
        if(!upword || !downword){
            Log.i("mnist", "upword="+upword+"  downword="+downword);
            return null;
        }
        ArrayList<ArrayList<PathObject>> ret = new ArrayList<>();
        ret.add(up);
        ret.add(down);
        return ret;
    }

    public Bitmap drawPath(Paint paint){
        int size = Math.max((maxX-minX), (maxY-minY));
        size = (int)paint.getStrokeWidth()*2 + size;

        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.parseColor("#FFFFFF"));
        Canvas c = new Canvas(b);

        int offsetX = (size - (maxX - minX))/2;
        int offsetY = (size - (maxY - minY))/2;

        for(int i=0; i<paths.size(); i++){
            Path p = paths.get(i);
            p.offset(-minX + offsetX, -minY + offsetY);
            c.drawPath(p, paint);
        }
        return b;
    }

    public Bitmap drawPath2(Paint paint){
        int spacex= 100;
        int bmpW = maxX - minX + spacex;
        if(bmpW > 1980){
            bmpW = 1980;
            spacex = 0;
        }
        int spacey = 100;
        int bmpH = maxY - minY + spacey;
        if(bmpH > 1080){
            bmpH = 1080;
            spacey = 0;
        }
        Bitmap b = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
//        Bitmap b = Bitmap.createBitmap(1980, 1080, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.parseColor("#FFFFFF"));
        Canvas c = new Canvas(b);

        for(int i=0; i<paths.size(); i++){
            Path p = new Path(paths.get(i));
            p.offset(-minX + spacex/2, -minY + spacey/2);
            c.drawPath(p, paint);
        }
        return b;
    }

    public Bitmap drawPath3(Paint paint){
        float scale = (float) 0.5;
        int spacex = 100;
        int spacey = 100;

        Path supPath = new Path(BaiduOcr.getSupPath());
        RectF rf = new RectF();
        supPath.computeBounds(rf,true);
        float scale0 = ((maxY-minY)*scale)/(rf.bottom-rf.top);

        int bmpW  = (int)((rf.right-rf.left)* scale0+spacex/2 + 50 +  (maxX-minX)*scale+spacex/2);
        int bmpH = maxY-minY+spacey;

        float offsetX = (bmpW - (maxX-minX + spacex)*scale);
        float offsetY = ((maxY-minY + spacey)*scale)/2;
        float startX = minX*scale;
        float startY = minY*scale;

        Matrix matrix = new Matrix();
        matrix.setScale(scale,scale);

//        Bitmap b = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Bitmap b = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.parseColor("#FFFFFF"));
        Canvas c = new Canvas(b);
        for(int i=0; i<paths.size(); i++){
            Path p = new Path(paths.get(i));
            p.transform(matrix);
            p.offset(-startX+offsetX, -startY+offsetY);
            c.drawPath(p, paint);
        }
        Matrix matrix0 = new Matrix();
        matrix0.setScale(scale0, scale0);
        supPath.offset(-rf.left+spacex/2, -rf.top+spacey/2+ ((rf.bottom-rf.top + spacey)*scale)/2);
        supPath.transform(matrix0);
        c.drawPath(supPath, paint);
        return b;
    }


    public Bitmap drawPathToSize(Paint paint, int psize){
        Bitmap b = Bitmap.createBitmap(psize, psize, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.parseColor("#FFFFFF"));
        Canvas c = new Canvas(b);

        float scaleWidth = (float) (psize * 0.9) / (maxX - minX);
        float scaleHeight = (float) (psize * 0.9) / (maxY - minY);
        float scale = Math.min(scaleHeight, scaleWidth);
        Matrix matrix = new Matrix();
        matrix.setScale(scale,scale);

        float offsetX = (psize - (maxX-minX)*scale)/2;
        float offsetY = (psize - (maxY-minY)*scale)/2;
        float startX = minX*scale;
        float startY = minY*scale;
//        Log.i("mnist", "paths.size()=" + paths.size());
        for(int i=0; i<paths.size(); i++){
            Path p = new Path(paths.get(i));
            p.transform(matrix);
            p.offset(-startX+offsetX, -startY+offsetY);
            c.drawPath(p, paint);
        }
        return b;
    }



    public static PathObject getPathFromAxis(JSONArray points) throws Exception{
        PathObject obj = new PathObject();
        obj.startPoint = points.getJSONArray(0);
        obj.endPoint = points.getJSONArray(points.length()-1);
        obj.points.add(points);

        int minX = -1;
        int minY = -1;
        int maxX = -1;
        int maxY = -1;
        Path p = new Path();
        boolean startMatch = false;
        boolean endMatch = false;

        for(int j=0; j<points.length() ; j++) {
            JSONArray point = points.getJSONArray(j);
            int x = point.getInt(0);
            int y = point.getInt(1);
            if (j == 0) {
                p.moveTo(x, y);
            } else {
                p.lineTo(x, y);
            }

            minX = (minX == -1) ? x : Math.min(x, minX);
            maxX = (maxX == -1) ? x : Math.max(x, maxX);
            minY = (minY == -1) ? y : Math.min(y, minY);
            maxY = (maxY == -1) ? y : Math.max(y, maxY);
            if(minY == obj.startPoint.getInt(1) && x==obj.startPoint.getInt(0)){
                startMatch = true;
            }else if(minY != obj.startPoint.getInt(1)){
                startMatch = false;
            }
            if(maxY == obj.endPoint.getInt(1) && x==obj.endPoint.getInt(0)){
                endMatch = true;
            }else if(maxY != obj.endPoint.getInt(1)){
                endMatch = false;
            }
        }
        obj.lineMatch = startMatch && endMatch;
        obj.paths.add(p);
        obj.minX = minX;
        obj.minY = minY;
        obj.maxX = maxX;
        obj.maxY = maxY;
        return obj;
    }


    public static PathObject getPathFromAxis2(JSONArray paths) throws Exception{
        PathObject obj = new PathObject();
        int minX = -1;
        int minY = -1;
        int maxX = -1;
        int maxY = -1;
        Path p = new Path();

        for(int i=0; i<paths.length(); i++){
            JSONArray points = paths.getJSONArray(i);
            obj.points.add(points);
            for(int j=0; j<points.length() ; j++) {
                JSONArray point = points.getJSONArray(j);
                int x = point.getInt(0);
                int y = point.getInt(1);
                if (j == 0) {
                    p.moveTo(x, y);
                } else {
                    p.lineTo(x, y);
                }

                minX = (minX == -1) ? x : Math.min(x, minX);
                maxX = (maxX == -1) ? x : Math.max(x, maxX);
                minY = (minY == -1) ? y : Math.min(y, minY);
                maxY = (maxY == -1) ? y : Math.max(y, maxY);
            }
            obj.paths.add(p);
            obj.minX = minX;
            obj.minY = minY;
            obj.maxX = maxX;
            obj.maxY = maxY;
        }
        return obj;
    }

}
