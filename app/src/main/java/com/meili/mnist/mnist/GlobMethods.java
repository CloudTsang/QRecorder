package com.meili.mnist.mnist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathMeasure;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import com.meili.mnist.TF;
import com.meili.mnist.widget.AnswerObject;
import com.meili.mnist.widget.AnswerRecord;
import com.meili.mnist.widget.BmpUtil;
import com.meili.mnist.widget.PathObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobMethods {
    public static String mnist(Activity act, YdMnistClassifierLite ydclassifierlite, String pos){
        try{
            JSONArray arr = new JSONArray(pos);
            Hashtable table = mnist(act, ydclassifierlite, arr);
            return (String)table.get("res");
        }catch (Exception err){
            err.printStackTrace();
            return "";
        }

    }

    public static String mnistStr(Activity act, YdMnistClassifierLite ydclassifierlite, JSONArray pos){
        try{
            Hashtable table = mnist(act, ydclassifierlite, pos);
            return (String)table.get("res");
        }catch (Exception err){
            err.printStackTrace();
            return "";
        }
    }

    public static Hashtable mnist(Activity act, YdMnistClassifierLite ydclassifierlite, JSONArray pos){
        try{
            Paint pt = new Paint();
            pt.setColor(Color.BLACK);
            pt.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (TF.DRAW_THICKNESS*0.5), act.getResources().getDisplayMetrics()));
            pt.setStyle(Paint.Style.STROKE);
;
            List<PathObject> pObjs = BmpUtil.getPathObject(pos, true, ydclassifierlite, act);
            List<PathObject> retPObjs = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            PathObject mP = null;
            for(int j=0; j<pObjs.size(); j++){
                PathObject tmpP = pObjs.get(j);
                if(tmpP.isBlank){
                    continue;
                }
                String str0 = "";
                if(tmpP.checked8 && tmpP.paths.size()==2){
                    str0 = "八";
                }
                if(tmpP.checked5){
                    str0 = "5";
                }
                if(tmpP.checkedGe && tmpP.paths.size()==3){
                    str0 = "个";
                }
                //小数点判断
                if(str0.length() == 0 && tmpP.paths.size() == 1 && j>=1){
                    PathObject  lastObj = pObjs.get(j-1);
                    int lastH = lastObj.maxY - lastObj.minY;
                    float size1 = Math.max(lastObj.maxX-lastObj.minX, lastObj.maxY-lastObj.minY);
                    float size2 = tmpP.maxX - tmpP.minX;
                    if(size2 < size1*0.3
                            && tmpP.minY > lastObj.minY + lastH/2){
                        str0 = ".";
                    }
                }

                ArrayList<ArrayList<PathObject>> fras = tmpP.checkFraction();
                if(fras!=null){
                    ArrayList<PathObject> up = BmpUtil.getFractionPathObject(fras.get(0), ydclassifierlite, act);
                    for(PathObject fobj:up){
                        Bitmap tmpBmp = fobj.drawPathToSize(pt, TF.MNIST_SIZE);
                        ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                        MnistData mnistResult = ydclassifierlite.inference(imgData);
                        int idx0 = mnistResult.topIndex();
                        str0 += TF.labels.substring(idx0,idx0+1);
                        Log.i("mnist", "str0 = " + str0);
                    }
                    str0 += ",";

                    ArrayList<PathObject> down = BmpUtil.getFractionPathObject(fras.get(1), ydclassifierlite, act);
                    for(PathObject fobj:down){
                        Bitmap tmpBmp = fobj.drawPathToSize(pt, TF.MNIST_SIZE);
                        ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                        MnistData mnistResult = ydclassifierlite.inference(imgData);
                        int idx0 = mnistResult.topIndex();
                        str0 += TF.labels.substring(idx0,idx0+1);
                        Log.i("mnist", "str0 = " + str0);
                    }

                    str0 = "[fra]" + str0 + "[/fra]";
                }

                if(str0.length() == 0){
                    Bitmap tmpBmp = tmpP.drawPathToSize(pt, TF.MNIST_SIZE);
                    ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                    MnistData mnistResult = ydclassifierlite.inference(imgData);
                    int idx0 = mnistResult.topIndex();
                    str0 = TF.labels.substring(idx0,idx0+1);
                    Log.i("mnist", "str0 = " + str0);
                    if(str0.equals("÷") && tmpP.paths.size() >= 4){
                        str0 = "六";
                    }else if(str0.equals("<") && tmpP.paths.size() == 2){
                        str0 = "七";
                    }else if(str0.equals("m")){
                        mP = tmpP;
                        continue;
                    }else if(mP!=null){
                        boolean isM23 = false;
                        if(str0.equals("2")){
                            str0 = "㎡";
                            isM23 = true;
                        }else if(str0.equals("3")){
                            str0 = "m³";
                            isM23 = true;
                        }else{
                            builder.append("m");
                            retPObjs.add(mP);
                        }
                        if(isM23){
                            PathObject nP = new PathObject();
                            nP.addPaths(mP);
                            nP.addPaths(tmpP);
                            tmpP = nP;
                        }
                    }
                }
                retPObjs.add(tmpP);
                builder.append(str0);
            }

            Log.i("mnist:", builder.toString());
            String retS = builder.toString();
            Pattern p = Pattern.compile("[0-9]+\\[fra\\]");
            Matcher m = p.matcher(retS);
            while (m.find()){
                String s0 = m.group();
                s0 = s0.replace("[fra]",",");
                s0 = "[fra]"+s0;
                retS = retS.replace(m.group(), s0);
            }
            Hashtable table = new Hashtable();
            table.put("list", retPObjs);
            table.put("res", retS);
//            return retS;
            return table;
        }catch(Exception err){
            err.printStackTrace();
            return null;
        }

    }


    public static Hashtable mnistNum(Activity act, YdMnistClassifierLite ydclassifierlite, ArrayList<PathObject> tmpPObjs){
        try {
            Paint pt = new Paint();
            pt.setColor(Color.BLACK);
            pt.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) (TF.DRAW_THICKNESS * 0.5), act.getResources().getDisplayMetrics()));
            pt.setStyle(Paint.Style.STROKE);

            ArrayList<PathObject> pObjs = new ArrayList<>();

            for(int i=0 ; i<tmpPObjs.size(); i++) {
                PathObject tmpObj = tmpPObjs.get(i);
                boolean isAdded = false;
                for(int j=pObjs.size()-1 ; j>=0 ; j--) {
                    PathObject tmpObj2 = pObjs.get(j);
                    if(tmpObj2.checkConnect(tmpObj)) {
                        if(tmpObj2.check5(tmpObj)){
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


            StringBuilder builder = new StringBuilder();
            for(int j=0; j<pObjs.size(); j++) {
                PathObject tmpP = pObjs.get(j);
                String str0 = "";
                if(tmpP.checked5){
                    str0 = "5";
                }

                if(str0.length() == 0 && tmpP.paths.size() == 1 && j>=1){
                    PathObject  lastObj = pObjs.get(j-1);
                    int lastH = lastObj.maxY - lastObj.minY;
                    float size1 = Math.max(lastObj.maxX-lastObj.minX, lastObj.maxY-lastObj.minY);
                    float size2 = tmpP.maxX - tmpP.minX;
                    if(size2 < size1*0.3
                            && tmpP.minY > lastObj.minY + lastH/2){
                        str0 = ".";
                    }
                }

                if(str0.length() == 0){
                    Bitmap tmpBmp = tmpP.drawPathToSize(pt, TF.MNIST_SIZE);
                    ByteBuffer imgData = ydclassifierlite.ImgData(tmpBmp);
                    MnistData mnistResult = ydclassifierlite.inference(imgData);
                    int idx0 = mnistResult.topIndex();
                    str0 = TF.labels.substring(idx0,idx0+1);
                }
                builder.append(str0);
            }
            Hashtable table = new Hashtable();
            table.put("list", pObjs);
            table.put("res", builder.toString());
            return table;
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

}
