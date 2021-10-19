package com.meili.mnist.mnist;

import android.app.Activity;
import android.util.Log;

import com.meili.mnist.widget.LineObject;
import com.meili.mnist.widget.PathObject;
import com.meili.mnist.widget.VertEqualObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.spec.OAEPParameterSpec;

public class VertEqualDetect {

    public static Hashtable EqPartSplit(ArrayList<PathObject> pos, float lineThresRate){
        Collections.sort(pos, new Comparator< PathObject >() {
            @Override
            public int compare(PathObject obj1, PathObject obj2) {
                if ( obj1.minY+(obj1.maxY-obj1.minY)/2 > obj2.minY+(obj2.maxY-obj2.minY)/2) {
//                if ( obj1.minY > obj2.minY) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        PathObject tmpAllPo = new PathObject();
        for(PathObject p :pos){
            tmpAllPo.addPaths(p);
        }
        float lineThres = (tmpAllPo.maxX-tmpAllPo.minX)*lineThresRate;

        ArrayList<ArrayList<PathObject>> partPos = new ArrayList<>();
        ArrayList<PathObject> tmpPartPos = new ArrayList<>();
        ArrayList<PathObject> linePos = new ArrayList<>();
        int lastMid = 0;
        int lastY = 0;
        for(int i=0;i<pos.size(); i++){
            PathObject p = pos.get(i);
            if((p.minY + (p.maxY-p.minY)/2 > lastY && lastMid<p.minY && tmpPartPos.size()>0) || p.maxX-p.minX>lineThres){
                partPos.add(tmpPartPos);
                tmpPartPos = new ArrayList<>();
            }

            lastY = Math.max(p.maxY, lastY);
            lastMid = Math.max(p.minY+(p.maxY-p.minY)/2, lastMid);
            if(p.maxX-p.minX > lineThres){
                partPos.add(null);
                linePos.add(p);
            }else{
                tmpPartPos.add(p);
            }
        }
        if(tmpPartPos.size()>0){
            partPos.add(tmpPartPos);
        }

        Hashtable table = new Hashtable();
        table.put("partPos", partPos);
        table.put("linePos", linePos);
        table.put("tmpAllPo", tmpAllPo);
        return table;
    }

    public static Hashtable mnistEq(ArrayList<ArrayList<PathObject>> partPos, ArrayList<PathObject> linePos, int type, Activity act, YdMnistClassifierLite tflite){
        ArrayList<String> strs = new ArrayList<>();
        for(int i=0;i<partPos.size();i++){
            ArrayList<PathObject> ppos = partPos.get(i);
            if(ppos == null){
                strs.add("_");
                continue;
            }
            if(ppos.size()==0){
                continue;
            }
            Collections.sort(ppos, new Comparator< PathObject >() {
                @Override
                public int compare(PathObject obj1, PathObject obj2) {
                    if ( obj1.minX > obj2.minX) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            if(type == VertEqualObject.DIVIDE && i==2){
                PathObject lo = linePos.get(0);
                int dlineIndex = -1;
                for(int j=0;j<ppos.size();j++){
                    PathObject po = ppos.get(j);
                    if(po.minX > lo.minX){
                        dlineIndex = j-1;
                        break;
                    }
                }
                ArrayList<PathObject> ppos1 = new ArrayList<>();
                for(int di=0 ; di<dlineIndex ; di++){
                    ppos1.add(ppos.get(di));
                }
                ArrayList<PathObject> ppos2 = new ArrayList<>();
                for(int di = dlineIndex+1; di<ppos.size() ; di++){
                    ppos2.add(ppos.get(di));
                }

                Hashtable t1 = GlobMethods.mnistNum(act,tflite, ppos1);
                Hashtable t2 = GlobMethods.mnistNum(act,tflite, ppos2);
                String s1 = (String)t1.get("res");
                String s2 = (String)t2.get("res");
                ppos1 = (ArrayList<PathObject>)t1.get("list");
                ppos2 = (ArrayList<PathObject>)t2.get("list");
                strs.add(s1+"/"+s2);
                ppos1.add(ppos.get(dlineIndex));
                ppos1.addAll(ppos2);
                partPos.set(i, ppos1);
            }else{
                Hashtable t = GlobMethods.mnistNum(act,tflite, ppos);
                String s = (String)t.get("res");
                ppos = (ArrayList<PathObject>)t.get("list");
                partPos.set(i, ppos);
                strs.add(s);
            }

        }
        Hashtable ret = new Hashtable();
        ret.put("strs", strs);
        ret.put("partPos", partPos);
        return ret;
    }

    public static ArrayList<String> formatPrint(ArrayList<String> strs, ArrayList<ArrayList<PathObject>> partPos, int type){
        int maxLen = 0;
        int maxIndex = 0;
        ArrayList<PathObject> maxPpos = new ArrayList<>();
        for(int i=0;i<partPos.size(); i++){
            ArrayList<PathObject> ppos = partPos.get(i);
            if(ppos == null){
                continue;
            }
            if(ppos.size()>maxLen){
                maxLen = ppos.size();
                maxIndex = i;
                maxPpos =  partPos.get(i);
            }
        }

        ArrayList<String> formatStrs = new ArrayList<>();
        int dividesignIndex = 0;
        for(int i=0;i<partPos.size();i++){
            if(i == 3){
                Log.i("mnist", "here");
            }
            ArrayList<PathObject> ppos = partPos.get(i);
            if(i==maxIndex){
                formatStrs.add(strs.get(maxIndex));
                continue;
            }
            String tmpS = "";
            if(strs.get(i).equals("_")){
                if(type == VertEqualObject.DIVIDE){
                    if(i==1){
                        for(int i2=0; i2<strs.get(2).length(); i2++){
                            String c = String.valueOf(strs.get(2).charAt(i2));
                            if(c.equals("/")){
                                dividesignIndex = i2;
                                tmpS = "";
                                for(int j=0;j<i2+1;j++){
                                    tmpS += " ";
                                }
                                for(int j=0;j<strs.get(2).length() - i2-1;j++){
                                    tmpS += "_";
                                }
                                break;
                            }
                        }
                    }else{
                        tmpS = "";
                        for(int j=0;j<dividesignIndex+1;j++){
                            tmpS += " ";
                        }
                        for(int j=0;j<strs.get(2).length() - dividesignIndex-1;j++){
                            tmpS += "_";
                        }
                    }
                }else{
                    for(int j=0;j<maxLen;j++){
                        tmpS += "_";
                    }
                }
                formatStrs.add(tmpS);
                continue;
            }

            for(int j=0;j<maxLen;j++){
                tmpS += " ";
            }
            int k=0;
            for(int j=0;j<ppos.size(); j++){
                PathObject po = ppos.get(j);
                boolean aligned = false;

                for(int k0=0; k0<maxPpos.size()-k;k0++){
                    PathObject mpo = maxPpos.get(k0+k);
                    float offset = (Math.min(po.maxY-po.minY, mpo.maxY-mpo.minY))/4;
                    float mid = po.minX + (po.maxX-po.minX + 20)/2;
                    float mid2 = mpo.minX + (mpo.maxX-mpo.minX + 20)/2;
                    if( (mpo.maxX+offset > mid && mid > mpo.minX-offset)
                        ||(po.maxX+offset > mid2 && mid2> po.minX-offset)){
                        k += k0;
                        tmpS = tmpS.substring(0, k) + String.valueOf(strs.get(i).charAt(j)) + tmpS.substring(k+1);
                         k += 1;
                        aligned = true;
                        break;
                    }
                }
                if(!aligned){
                    tmpS = tmpS.substring(0, k) + String.valueOf(strs.get(i).charAt(j)) + tmpS.substring(k+1);
                    k+=1;
                }
            }
            formatStrs.add(tmpS);
        }

        for(int e=0; e<formatStrs.size(); e++){
            String fs = formatStrs.get(e);
            fs = fs.replace("D", "0");
            fs = fs.replace(">", "×");
            fs = fs.replace("<", "×");
            fs = fs.replace( "×", "x");
            Log.i("mnist", fs);
            formatStrs.set(e, fs);
        }
        return formatStrs;
    }

    public static VertEqualObject vertEqualMnist(String paths, Activity act, YdMnistClassifierLite tflite){
        try {
            JSONArray pos = new JSONArray(paths);
            ArrayList<PathObject> pObjs = new ArrayList<>();
            for(int i=0 ; i<pos.length() ; i++){
                JSONArray points = pos.getJSONArray(i);
                if(points.length()<3){
                    continue;
                }
                PathObject tmpObj = PathObject.getPathFromAxis(points);
                if(tmpObj.maxX==tmpObj.minX && tmpObj.maxY == tmpObj.minY){
                    tmpObj.isBlank = true;
                }
                pObjs.add(tmpObj);
            }
            Collections.sort(pObjs, new Comparator< PathObject >() {
                @Override
                public int compare(PathObject obj1, PathObject obj2) {
                    if ( obj1.minX > obj2.minX) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            Hashtable t1 = EqPartSplit(pObjs, (float) 0.5);
            ArrayList<ArrayList<PathObject>> partPos = (ArrayList<ArrayList<PathObject>>)t1.get("partPos");
            ArrayList<PathObject> linePos = (ArrayList<PathObject>)t1.get("linePos");

            for(int i=0; i<partPos.size(); i++){
                ArrayList<PathObject> ppos = partPos.get(i);
                if(ppos == null){
                    continue;
                }
                if(ppos.size()==1){
                    Hashtable t = GlobMethods.mnistNum(act,tflite, ppos);
                    String s = (String)t.get("res");
                    if(s.equals("-")){
                        partPos.set(i, null);
                    }
                }
            }
            int eqType = -1;
            if(partPos.get(1) == null){
                eqType = VertEqualObject.DIVIDE;
            }else{
                eqType = VertEqualObject.ADD_MULT_SUB;
            }
            Hashtable t2 = mnistEq(partPos, linePos, eqType, act, tflite);
            ArrayList<String> strs = (ArrayList<String>) t2.get("strs");
            partPos = (ArrayList<ArrayList<PathObject>>)t2.get("partPos");
            strs = formatPrint(strs, partPos, eqType);

            return new VertEqualObject(eqType, strs);


        }catch (Exception err){
            err.printStackTrace();
            return null;
        }

    }

}
