package com.meili.mnist.mnist;

import android.graphics.Point;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class UserLog extends JSONObject {
    public final static int EMPTY = -1;
    public final static int START = 0;
    public final static int DRAW = 1;
    public final static int BACK = 2;
    public final static int CONFIRM = 3;
    public final static int CLEAN = 4;
    public final static int DRAG = 5;
    public final static int SELECT = 6;

    public int sec = 0;
    public int op = EMPTY;
    public int group = -1;
    public List<List<Integer>> p = null;
    public UserLog(int second, int operate, List<List<Integer>> path){
        sec = second;
        op = operate;
        p = path;
    }

    public List<List<Integer>> getP(){
        return p;
    }

    public JSONObject getJsonObj(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("sec", sec);
            obj.put("op", op);
            obj.put("group", group);
            if(p!=null && p.size()>0){
                obj.put("path", new JSONArray(p));
            }

        }catch (Exception err){
            Log.i("mnist", err.getStackTrace().toString());
        }
        return obj;
    }
}


