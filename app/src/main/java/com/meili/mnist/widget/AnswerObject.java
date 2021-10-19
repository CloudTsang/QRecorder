package com.meili.mnist.widget;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.graphics.Path;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnswerObject {
    public Path path;
    public Rect rect;

    public String result;
    public JSONArray points;
    public ArrayList<String> solves;
    public int solveAnswer;

    public VertEqualObject vobj;

    public ArrayList<View> viewGroups = new ArrayList<>();
    public PathObject pobj;
    public String mnistMeth;
    public int qtype;

    public AnswerObject(PathObject po, String res){
        int w = po.maxX - po.minX;
        int h = po.maxY - po.minY;
        if(w > h){
            int offset = (int)((w-h)/2);
            rect = new Rect(po.minX, po.maxX,po.minY - offset ,po.maxY + offset);

        }else{
            int offset = (int)((h-w)/2);
            rect = new Rect(po.minX - offset,po.maxX + offset,po.minY, po.maxY);
        }
        result = res;

        points = new JSONArray();
        for(int i=0;i<po.points.size();i++){
            points.put(po.points.get(i));
        }
        pobj = po;
    }

    public AnswerObject(Rect r, Path p, String res){
        rect = r;
        path = p;
        result = res;
    }

    public AnswerObject(int l, int t, int r, int b, ArrayList<String> anss, int fanss){
        rect = new Rect(l,t,r,b);
        solves = anss;
        solveAnswer = fanss;
    }

    public AnswerObject(int l, int t, int r, int b, VertEqualObject vo){
        rect = new Rect(l,t,r,b);
        vobj = vo;
    }
}
