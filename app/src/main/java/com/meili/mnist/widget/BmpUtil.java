package com.meili.mnist.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.meili.mnist.TF;
import com.meili.mnist.mnist.MnistData;
import com.meili.mnist.mnist.YdMnistClassifierLite;

import org.json.JSONArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BmpUtil {
    public static List<PathObject> getPathObject(JSONArray pos, boolean sort, List<Integer> groups)throws Exception{
        List<PathObject> pObjs = new ArrayList<>();
        List<PathObject> tmpPObjs = new ArrayList<>();
        for(int i=0 ; i<pos.length() ; i++){
            JSONArray points = pos.getJSONArray(i);
            PathObject tmpObj = PathObject.getPathFromAxis(points);
            if(tmpObj.maxX==tmpObj.minX && tmpObj.maxY == tmpObj.minY){
                continue;
            }
            tmpPObjs.add(tmpObj);
        }

        if(sort){
            Collections.sort(tmpPObjs, new Comparator< PathObject >() {
                @Override
                public int compare(PathObject obj1, PathObject obj2) {
                    if ( obj1.minX > obj2.minX) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        }

        int curGroup = 0;
        int groupIndex = 0;
        if(groups != null && groups.size()>groupIndex){
            curGroup = groups.get(groupIndex);
        }
        int nextGroup = 0;
        for(int i=0 ; i<tmpPObjs.size(); i++){
            PathObject tmpObj = tmpPObjs.get(i);
            boolean isAdded = false;
            if(groups != null && groupIndex < groups.size() - 1){
                groupIndex ++;
                nextGroup = groups.get(groupIndex);
            }
            if(nextGroup == curGroup){
                for(int j=pObjs.size()-1 ; j>=0 ; j--){
                    PathObject tmpObj2 = pObjs.get(j);
                    if(tmpObj2.checkConnect(tmpObj)){
                        tmpObj2.addPaths(tmpObj);
                        pObjs.set(j, tmpObj2);
                        isAdded = true;
                        break;
                    }
                }
            }

            if(!isAdded){
                pObjs.add(tmpObj);
            }
        }
//        if(sort){
//            Collections.reverse(pObjs);
//        }
        return pObjs;
    }

    public static List<PathObject> getPathObject(JSONArray pos, boolean sort, YdMnistClassifierLite ydclassifierlite, Activity act)throws Exception{
        List<PathObject> pObjs = new ArrayList<>();
        List<PathObject> tmpPObjs = new ArrayList<>();
        for(int i=0 ; i<pos.length() ; i++){
            JSONArray points = pos.getJSONArray(i);
            PathObject tmpObj = PathObject.getPathFromAxis(points);
            if(tmpObj.maxX==tmpObj.minX && tmpObj.maxY == tmpObj.minY){
                tmpObj.isBlank = true;
            }
            tmpPObjs.add(tmpObj);
        }

        if(sort){
            Collections.sort(tmpPObjs, new Comparator< PathObject >() {
                @Override
                public int compare(PathObject obj1, PathObject obj2) {
                    if ( obj1.minX > obj2.minX) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        }
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (TF.DRAW_THICKNESS*0.5), act.getResources().getDisplayMetrics()));
        pt.setStyle(Paint.Style.STROKE);

        for(int i=0 ; i<tmpPObjs.size(); i++){
            PathObject tmpObj = tmpPObjs.get(i);
            boolean isAdded = false;
            for(int j=pObjs.size()-1 ; j>=0 ; j--){
                PathObject tmpObj2 = pObjs.get(j);
                Log.i("mnist", "tmpObj2.checkConnect(tmpObj) = "+tmpObj2.checkConnect(tmpObj));
                if(tmpObj2.checkConnect(tmpObj)){
                    if(tmpObj2.check5(tmpObj)){
                       /* if(i<tmpPObjs.size()-1){
                            PathObject tmpObj3 = tmpPObjs.get(i+1);
                            if(tmpObj.checkCross(tmpObj3)){
                                break;
                            }
                        }*/
                        PathObject tmpObj3 = new PathObject();
                        tmpObj3.addPaths(tmpObj);
                        tmpObj3.addPaths(tmpObj2);
                        Bitmap tmpBmp = tmpObj3.drawPathToSize(pt, TF.MNIST_SIZE);
                        ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                        MnistData mnistResult = ydclassifierlite.inference(imgData);
                        int idx0 = mnistResult.topIndex();
                        String str0 = TF.labels.substring(idx0,idx0+1);
//                        Log.i("mnist", "check 5 : " + str0);
                        if(str0.equals("5")){
                            tmpObj3.checked5=true;
                            pObjs.set(j, tmpObj3);
                            tmpObj = tmpObj3;
                            isAdded = true;
                            break;
                        }
                    }
                    tmpObj2.addPaths(tmpObj);
                    pObjs.set(j, tmpObj2);
                    tmpObj = tmpObj2;
                    isAdded = true;
                    break;
                }
                if(tmpObj2.checkChn8(tmpObj) && (j==i+1 || j==i-1) ){
                    Bitmap tmpBmp = tmpObj2.drawPathToSize(pt, TF.MNIST_SIZE);
                    ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                    MnistData mnistResult = ydclassifierlite.inference(imgData);
                    int idx0 = mnistResult.topIndex();
                    String str0 = TF.labels.substring(idx0,idx0+1);
                    if(str0.equals("???") || str0.equals("1")){
                        PathObject tmpObj3 = new PathObject();
                        tmpObj3.addPaths(tmpObj);
                        tmpObj3.addPaths(tmpObj2);
                        tmpBmp = tmpObj3.drawPathToSize(pt, TF.MNIST_SIZE);
                        imgData = ydclassifierlite.ImgData(tmpBmp);
                        mnistResult = ydclassifierlite.inference(imgData);
                        idx0 = mnistResult.topIndex();
                        str0 = TF.labels.substring(idx0,idx0+1);
                        if(str0.equals("???")){
                            tmpObj3.checked8 = true;
                            pObjs.set(j, tmpObj3);
                            tmpObj = tmpObj3;
                            isAdded = true;
                            break;
                        }
                    }
                }
            }
            PathObject tmpObjGe = null;
            int geIndex = pObjs.size()-1;
            for(int k=pObjs.size()-1; k >= 0; k--){
                if(tmpObjGe == null){
                    tmpObjGe = new PathObject();
                    tmpObjGe.addPaths(tmpObj);
                }
                PathObject tmpObjGe1 = pObjs.get(k);
                if(tmpObjGe1 == tmpObj){
                    continue;
                }
                tmpObjGe.addPaths(tmpObjGe1);
                if(tmpObjGe.paths.size() ==3){
                    geIndex = k;
                    break;
                }else if(tmpObjGe.paths.size() > 3){
                    tmpObjGe = null;
                }
            }
            if(tmpObjGe!=null && tmpObjGe.paths.size()==3){
                Bitmap tmpBmp = tmpObjGe.drawPathToSize(pt, TF.MNIST_SIZE);
                ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                MnistData mnistResult = ydclassifierlite.inference(imgData);
                int idx0 = mnistResult.topIndex();
                String str0 = TF.labels.substring(idx0,idx0+1);
                if(str0.equals("???")){
                    tmpObjGe.checkedGe = true;
                    isAdded = true;
                    tmpObj = tmpObjGe;
                    pObjs = pObjs.subList(0,geIndex);
                    pObjs.add(tmpObjGe);
                }
            }

//            Bitmap tmpBmp = tmpObj.drawPathToSize(pt, TF.MNIST_SIZE);
//            ydclassifierlite.ImgData(tmpBmp);
            if(!isAdded){
                pObjs.add(tmpObj);
            }
        }

        return pObjs;
    }


    public static ArrayList<PathObject> getFractionPathObject(ArrayList<PathObject> tmpPObjs, YdMnistClassifierLite ydclassifierlite, Activity act)throws Exception{
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (TF.DRAW_THICKNESS*0.5), act.getResources().getDisplayMetrics()));
        pt.setStyle(Paint.Style.STROKE);
        ArrayList<PathObject> pObjs = new ArrayList<>();
        for(int i=0 ; i<tmpPObjs.size(); i++){
            PathObject tmpObj = tmpPObjs.get(i);
            boolean isAdded = false;
            for(int j=pObjs.size()-1 ; j>=0 ; j--){
                PathObject tmpObj2 = pObjs.get(j);
                if(tmpObj2.checkConnect(tmpObj)){
                    if(tmpObj2.check5(tmpObj)){
                        if(i<tmpPObjs.size()-1){
                            PathObject tmpObj3 = tmpPObjs.get(i+1);
                            if(tmpObj.checkCross(tmpObj3)){
                                break;
                            }
                        }
                        PathObject tmpObj3 = new PathObject();
                        tmpObj3.addPaths(tmpObj);
                        tmpObj3.addPaths(tmpObj2);
                        Bitmap tmpBmp = tmpObj3.drawPathToSize(pt, TF.MNIST_SIZE);
                        ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                        MnistData mnistResult = ydclassifierlite.inference(imgData);
                        int idx0 = mnistResult.topIndex();
                        String str0 = TF.labels.substring(idx0,idx0+1);
                        if(str0.equals("5")){
                            tmpObj3.checked5=true;
                            pObjs.set(j, tmpObj3);
                            tmpObj = tmpObj3;
                            isAdded = true;
                            break;
                        }
                    }
                    tmpObj2.addPaths(tmpObj);
                    pObjs.set(j, tmpObj2);
                    tmpObj = tmpObj2;
                    isAdded = true;
                    break;
                }
            }
            if(!isAdded){
                pObjs.add(tmpObj);
            }
        }
        return pObjs;
    }

    public static Bitmap DrawWholeByAxis(JSONArray pos, int thickness, DisplayMetrics metrics) throws Exception{
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS, metrics));
        pt.setStyle(Paint.Style.STROKE);

        JSONArray points = pos.getJSONArray(0);
        PathObject pObj = PathObject.getPathFromAxis(points);

        for(int i=1 ; i<pos.length() ; i++){
            points = pos.getJSONArray(i);
            PathObject tmpObj = PathObject.getPathFromAxis(points);
            pObj.addPaths(tmpObj);
        }

        Bitmap result = pObj.drawPathToSize(pt, TF.DRAW_FULL_SIZE);
        return result;
    }

    public static Bitmap ResizeBitmap(Bitmap bm, int newWidth, int newHeight) {
        // ?????????????????????
        int width = bm.getWidth();
        int height = bm.getHeight();
        // ??????????????????
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // ?????????????????????matrix??????
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // ??????????????????
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * bitmap??????base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        Log.i("mnist:", result);
        return result;
    }

    public static Bitmap Base64ToBitmap(String b64){
        byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static byte[] bitmapToBytes(Bitmap bitmap){
        byte[] bitmapBytes = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                bitmapBytes = baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        Log.i("mnist:", result);
        return bitmapBytes;
    }
}
