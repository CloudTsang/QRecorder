package com.meili.mnist.widget;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Path;
import android.util.JsonReader;
import android.util.Log;

import com.meili.mnist.QCanvasActivity;
import com.meili.mnist.TF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnswerRecord {
    public int qtype;
    public int qid;
    public String url="";
    public ArrayList<AnswerObject> objs;
    public int wid;
    public int hei;
    public int offsetX = 0;
    public int offsetY = 0;
    public String mainQString = "";
    public String qString = "";
    public Boolean isPhto = false;

    public int canvasWid;
    public int canvasHei;
    public JSONArray rans;

    public ArrayList<Integer> ops;

    //连线题使用
    public ArrayList<ArrayList<Rect>> ansRects;
    public ArrayList<ArrayList<Path>> ansPaths;
    public ArrayList<Rect> groupRects;

    //垂线题使用
    public ArrayList<ArrayList<Point>> ansPoints;
    public ArrayList<ArrayList<LineObject>> lineObjects;
    public ArrayList<Boolean> dash;

    public static boolean isLTRB = false;

    public AnswerRecord(int qt){
        qtype = qt;
        objs = new ArrayList<>();
        rans = new JSONArray();
        ops = new ArrayList<>();

        ansRects = new ArrayList<>();
        ansPaths = new ArrayList<>();
        groupRects = new ArrayList<>();

        ansPoints = new ArrayList<>();
        lineObjects = new ArrayList<>();
        dash = new ArrayList<>();
    }
    public AnswerRecord(){
        objs = new ArrayList<>();
        rans = new JSONArray();
        ops = new ArrayList<>();

        ansRects = new ArrayList<>();
        ansPaths = new ArrayList<>();
        groupRects = new ArrayList<>();

        ansPoints = new ArrayList<>();
        lineObjects = new ArrayList<>();
        dash = new ArrayList<>();
    }

    public JSONObject getJsonObj(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("vn", TF.VERSION);
            obj.put("id", qid);
            obj.put("ty", "" + qtype);

            JSONArray st = new JSONArray();
            st.put(canvasWid);
            st.put(canvasHei);
            obj.put("st", st);

            JSONObject cn = new JSONObject();
            JSONArray sizeArr = new JSONArray();
            sizeArr.put(offsetX);
            sizeArr.put(offsetY);
            sizeArr.put(wid);
            sizeArr.put(hei);
            cn.put("rl", url);
            cn.put("rc", sizeArr);
            cn.put("sm", mainQString);
            cn.put("tx", qString);
            cn.put("vd", new JSONArray());
            cn.put("pg", new JSONArray());
            obj.put("cn", cn);
            if(isPhto){
                obj.put("isPhoto", "T");
            }else{
                obj.put("isPhoto", "F");
            }

            JSONArray ansDataArr = new JSONArray();
            ansDataArr = getJsonObjWords(ansDataArr);
            ansDataArr = getJsonObjLine(ansDataArr);
            ansDataArr = getJsonObjDwarH(ansDataArr);

            obj.put("ans", ansDataArr);
            return obj;
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

    private JSONArray getJsonObjWords(JSONArray ansDataArr){
        try{
            for(int i=0; i<objs.size(); i++){
                AnswerObject a = objs.get(i);
                JSONArray area = new JSONArray();
                area.put(a.rect.left);
                area.put(a.rect.top);
                area.put(a.rect.right-a.rect.left);
                area.put(a.rect.bottom-a.rect.top);

                JSONObject ansDataObj = new JSONObject();
                ansDataObj.put("idx", 1);
                if(a.solves!=null && a.solves.size()>0){
                    //解答题&递等式题
                    ansDataObj.put("idx", 1);
                    ansDataObj.put("as", a.solves.get(a.solveAnswer));
                    JSONArray solveAns = new JSONArray();
                    for(String s:a.solves){
                        solveAns.put(s);
                    }
                    ansDataObj.put("pgas", solveAns);
                }else if(a.vobj!=null){
                    ansDataObj.put("as", a.vobj.answer);
                    JSONObject eqObj = new JSONObject();
                    eqObj.put("ty", a.vobj.qtype);
                    JSONArray solveAns = new JSONArray();

//                    ArrayList<String> ansEqs = new ArrayList<>();
//                    if(a.vobj.qtype == VertEqualObject.ADD || a.vobj.qtype == VertEqualObject.SUB){
//                        ansEqs = VertEqCreator.eqToTxtAddSub(a.vobj.eqs);
//                    }else if(a.vobj.qtype == VertEqualObject.MULT){
//                        ansEqs = VertEqCreator.eqToTxtMult(a.vobj.eqs);
//                    }else if(a.vobj.qtype == VertEqualObject.DIVIDE){
//                        ansEqs = VertEqCreator.eqToTxtDivide(a.vobj.eqs);
//                    }
//                  ansEqs = VertEqCreator.printEq(ansEqs);
                    ArrayList<String> ansEqs = VertEqCreator.printEq(a.vobj.eqs);
                    for(String s:ansEqs){
                        s = s.replace("x", "×");
//                        s = s.replace("_", "口");
//                        s = s.replace(" ", "口");
                        solveAns.put(s);
                    }
                    eqObj.put("q1", solveAns);
                    if(a.vobj.eqsExam!=null){
                        solveAns = new JSONArray();
                        ArrayList<String> ansEqs2 = VertEqCreator.eqToTxtExam(a.vobj.eqsExam);
                        ansEqs2 = VertEqCreator.printEq(ansEqs2);
                        for(String s:ansEqs2){
                            s = s.replace("x", "×");
                            solveAns.put(s);
                        }
                        eqObj.put("q2", solveAns);
                    }
                    ansDataObj.put("pgas", eqObj);
                }else if(a.result!=null && a.result.length()>0){
                    //普通填空题
                    ansDataObj.put("as", a.result);
                    ansDataObj.put("pgas", "");
                    if(a.points==null){
                        ansDataObj.put("ps", new JSONArray());
                    }else{
                        ansDataObj.put("ps", a.points);
                    }
                    ansDataObj.put("mnist", a.mnistMeth);
                }
                ansDataObj.put("optype", a.qtype);
                ansDataObj.put("area", area);
                ansDataArr.put(ansDataObj);
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return ansDataArr;
    }

    private JSONArray getJsonObjLine(JSONArray ansDataArr){
        try {
            if(ansRects==null){
                return ansDataArr;
            }
            for(int j=0;j<ansRects.size();j++) {
                ArrayList<Rect> rects = ansRects.get(j);
                JSONObject ansDataObj = new JSONObject();
                ansDataObj.put("idx", 1);
                ansDataObj.put("as", "");
                ansDataObj.put("pgas", "");
                JSONArray areaArr = new JSONArray();
                JSONArray pointArr = new JSONArray();
                for (int i = 0; i < rects.size() - 1; i += 2) {
                    Rect r1 = rects.get(i);
                    Rect r2 = rects.get(i + 1);
                    boolean isEmpty = false;
                    if (r1.top == r2.top && r1.left == r2.left) {
                        isEmpty = true;
                    }
                    JSONArray tmp = new JSONArray();
                    tmp.put(rect2arr(r1));
                    if (!isEmpty) {
                        tmp.put(rect2arr(r2));
                    }
                    areaArr.put(tmp);

                    JSONArray tmp2 = new JSONArray();
                    tmp2.put(getRectMidPoint(r1));
                    if (!isEmpty) {
                        tmp2.put(getRectMidPoint(r2));
                    }
                    pointArr.put(tmp2);
                }
                ansDataObj.put("optype", QCanvasActivity.QTYPE_LINE);
                ansDataObj.put("area", areaArr);
                ansDataObj.put("ps", pointArr);
                ansDataArr.put(ansDataObj);
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return ansDataArr;
    }

    private JSONArray getJsonObjDwarH(JSONArray ansDataArr){
        try{
            if(ansPoints==null){
                return null;
            }
            for(int i=0; i<ansPoints.size(); i++){
                ArrayList<Point> pts = ansPoints.get(i);
                boolean isDash = false;
                if(dash!=null && i<dash.size()){
                    isDash = dash.get(i);
                }
                JSONObject ansDataObj = new JSONObject();
                ansDataObj.put("idx", 1);
                JSONArray ptsj = new JSONArray();
                if(pts.size()==2){
                    ansDataObj.put("as", "single");
                    Point pt1 = pts.get(0);
                    Point pt2 = pts.get(1);
                    JSONArray pt1j = new JSONArray();
                    pt1j.put(pt1.x);
                    pt1j.put(pt1.y);
                    JSONArray pt2j = new JSONArray();
                    pt2j.put(pt2.x);
                    pt2j.put(pt2.y);

                    ptsj.put(pt1j);
                    ptsj.put(pt2j);
                    if(isDash){
                        ptsj.put(2);
                    }else{
                        ptsj.put(1);
                    }

                }else if(pts.size()==4){
                    Point pt1 = pts.get(0);
                    Point pt2 = pts.get(1);
                    Point pt3 = pts.get(2);
                    Point pt4 = pts.get(3);
                    /*if(pt1.y == pt2.y){
                        ansDataObj.put("as", "multC");
                    }else */if(pt1.x == pt2.x){
                        ansDataObj.put("as", "multU");
                        ptsj.put(pt1.x);
                        ptsj.put(pt1.y);
                        ptsj.put(pt4.x);
                        ptsj.put(pt4.y);
                    }else{
                        ansDataObj.put("as", "mult");
                        float[] kb1 = PathCalculator.getkb(pt1.x,pt1.y,pt3.x, pt3.y);
                        float[] kb2 = PathCalculator.getkb(pt2.x,pt2.y,pt4.x, pt4.y);
                        float[] kb3 = PathCalculator.getkb(pt1.x,pt1.y,pt2.x, pt2.y);
                        JSONArray kbs = new JSONArray();
                        kbs.put(kb3[0]);
                        kbs.put(kb1[1]);
                        kbs.put(kb2[1]);
                        ptsj.put(kbs);

                        JSONArray ps = new JSONArray();
                        ps.put(pt1.x);
                        ps.put(pt1.y);
                        ps.put(pt2.x);
                        ps.put(pt2.y);
                        ps.put(pt3.x);
                        ps.put(pt3.y);
                        ps.put(pt4.x);
                        ps.put(pt4.y);
                        ptsj.put(ps);
                    }
                    if(isDash){
                        ptsj.put(2);
                    }else{
                        ptsj.put(1);
                    }
                }

                ArrayList<LineObject> lobjs = lineObjects.get(i);
                if(lobjs!=null && lobjs.size()>0){
                    JSONArray dlines = new JSONArray();
                    for(LineObject lo:lobjs){
                        JSONArray dline = new JSONArray();
                        float[] kb = PathCalculator.getkb(lo.startPoint.x, lo.startPoint.y, lo.endPoint.x, lo.endPoint.y);
                        dline.put(kb[0]);
                        dline.put(kb[1]);
                        if(isDash){
                            dline.put(2);
                        }else{
                            dline.put(1);
                        }
                        JSONArray dlinePts = new JSONArray();
                        dlinePts.put(lo.startPoint.x);
                        dlinePts.put(lo.startPoint.y);
                        dlinePts.put(lo.endPoint.x);
                        dlinePts.put(lo.endPoint.y);
                        dline.put(dlinePts);
                        dlines.put(dline);
                    }
                    ptsj.put(dlines);
                }
                ansDataObj.put("pgas", ptsj);
                ansDataObj.put("optype", QCanvasActivity.QTYPE_DRAW_H);
                ansDataArr.put(ansDataObj);
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return ansDataArr;
    }


    private static JSONArray rect2arr(Rect r){
        JSONArray arr = new JSONArray();
        arr.put(r.left);
        arr.put(r.top);
        arr.put(r.right);
        arr.put(r.bottom);
        return arr;
    }

    private static Rect arr2rect(JSONArray obj){
        try {
            Rect r = new Rect(
                        obj.getInt(0),
                    obj.getInt(1),
                    obj.getInt(2),
                    obj.getInt(3)
            )    ;
            return r;
        }catch (Exception err){
            err.printStackTrace();
        }
        return null;
    }

    private static Point arr2point(JSONArray obj){
        try{
            Point p = new Point(obj.getInt(0), obj.getInt(1));
            return p;
        }catch (Exception err){
            err.printStackTrace();
        }
        return null;
    }

    private static LineObject arr2lineobject(JSONArray obj){
        try {
            JSONArray axis = obj.getJSONArray(3);
            Point p1 = new Point(axis.getInt(0), axis.getInt(1));
            Point p2 = new Point(axis.getInt(2), axis.getInt(3));
            int dash = obj.getInt(2);
            LineObject ret = new LineObject(p1,p2,dash!=1);
            return ret;
        }catch (Exception err){
            err.printStackTrace();
        }
        return null;
    }

    private static JSONArray getRectMidPoint(Rect r){
        JSONArray arr = new JSONArray();
        arr.put(r.left + (int)(r.right-r.left)/2);
        arr.put(r.top + (int)(r.bottom-r.top)/2);
        return arr;
    }

    public static AnswerRecord getRecord(JSONObject obj){
        try {
            AnswerRecord rec = new AnswerRecord();
            rec.qid = obj.getInt("id");
            rec.qtype = Integer.parseInt(obj.getString("ty"));

            JSONObject cn = obj.getJSONObject("cn");
            if(obj.has("isPhoto")){
                String tmp = obj.getString("isPhoto");
                if(tmp.equals("T")){
                    rec.isPhto = true;
                }
            }
            rec.url = cn.getString("rl");
            JSONArray size = cn.getJSONArray("rc");
            rec.offsetX = size.getInt(0);
            rec.offsetY = size.getInt(1);
            rec.wid = size.getInt(2);
            rec.hei = size.getInt(3);

            JSONArray arr = obj.getJSONArray("ans");
            ArrayList<AnswerObject> ansObjs = new ArrayList<>();
            for(int i=0; i<arr.length(); i++) {
                JSONObject jobj = arr.getJSONObject(i);
                int tmpQtype = rec.qtype;
                if(jobj.has("optype")){
                    tmpQtype = jobj.getInt("optype");
                }
                if(tmpQtype == QCanvasActivity.QTYPE_PIC){
                    JSONArray area = jobj.getJSONArray("area");
                    JSONArray paths;
                    if(jobj.has("ps")){
                        paths = jobj.getJSONArray("ps");
                    }else{
                        paths = new JSONArray();
                    }
                    String ans = jobj.getString("as");
                    rec.rans.put(ans);
                    PathObject wordObj = new PathObject();
                    for(int widx=0; widx<paths.length(); widx++){
                        JSONArray word = paths.getJSONArray(widx);
                        for(int pidx=0; pidx< word.length(); pidx++){
                            PathObject tmpPathObj = PathObject.getPathFromAxis(word.getJSONArray(pidx));
                            wordObj.addPaths(tmpPathObj);
                        }
                    }
                    AnswerObject aobj = new AnswerObject(wordObj, ans);
                    if(isLTRB){
                        Rect r = new Rect(area.getInt(0),area.getInt(1), area.getInt(2),area.getInt(3));
                        aobj.rect = r;
                    }else{
                        Rect r = new Rect(area.getInt(0),area.getInt(1), area.getInt(0)+area.getInt(2),area.getInt(1)+area.getInt(3));
                        aobj.rect = r;
                    }
                    aobj.qtype = tmpQtype;
                    ansObjs.add(aobj);
                }else if(tmpQtype ==QCanvasActivity.QTYPE_LINE){
                    JSONArray area = jobj.getJSONArray("area");
                    ArrayList<Rect> rects = new ArrayList<>();
                    for(int j=0; j<area.length(); j++){
                        JSONArray tmpRects = area.getJSONArray(j);
                        Rect r1 = arr2rect(tmpRects.getJSONArray(0));
                        Rect r2 = null;
                        if(tmpRects.length() == 1){
                            r2 = new Rect(r1);
                        }else{
                            r2 = arr2rect(tmpRects.getJSONArray(1));
                        }
                        rects.add(r1);
                        rects.add(r2);
                    }
                    rec.ansRects.add(rects);
                }else if(tmpQtype ==QCanvasActivity.QTYPE_DRAW_H){
                    String as = jobj.getString("as");
                    JSONArray pgas = jobj.getJSONArray("pgas");
                    if(pgas.length()==0){
                        continue;
                    }
                    if(as.equals("mult")){
                        JSONArray pts = pgas.getJSONArray(1);
                        ArrayList<Point> ansPts = new ArrayList<>();
                        for(int index=0; index<pts.length()-1;index+=2){
                            int x = pts.getInt(index);
                            int y = pts.getInt(index+1);
                            ansPts.add(new Point(x,y));
                        }
                        rec.ansPoints.add(ansPts);
                        rec.dash.add( pgas.getInt(2)!=1 );
                    }else if(as.equals("single")){
                        ArrayList<Point> ansPts = new ArrayList<>();
                        ansPts.add(arr2point(pgas.getJSONArray(0)));
                        ansPts.add(arr2point(pgas.getJSONArray(1)));
                        rec.ansPoints.add(ansPts);
                        if(pgas.length()>2){
                            rec.dash.add( pgas.getInt(2)!=1 );
                        }else{
                            rec.dash.add( false );
                        }

                        if(pgas.length()>3){
                            JSONArray loj = pgas.getJSONArray(3);
                            ArrayList<LineObject> los = new ArrayList<>();
                            for(int index=0; index<loj.length(); index++){
                                LineObject lo = arr2lineobject(loj.getJSONArray(index));
                                los.add(lo);
                            }
                            rec.lineObjects.add(los);
                        }
                    }
                }else if(tmpQtype==QCanvasActivity.QTYPE_EQUALS || tmpQtype==QCanvasActivity.QTYPE_SOLVE){
                    JSONArray area = jobj.getJSONArray("area");
                    String ans = jobj.getString("as");
                    rec.rans.put(ans);
                    ArrayList<String> strs = new ArrayList<>();
                    JSONArray sarr = jobj.getJSONArray("pgas");
                    int solveIndex = sarr.length()-1;
                    for(int j=0;j<sarr.length();j++){
                        String solves = sarr.getString(j);
                        if(solves.equals(ans)){
                            solveIndex = j;
                        }
                        strs.add(solves);
                    }
                    AnswerObject aobj = new AnswerObject(area.getInt(0), area.getInt(1), area.getInt(0)+area.getInt(2), area.getInt(1)+area.getInt(3), strs,solveIndex);
                    ansObjs.add(aobj);
                }
                else if(tmpQtype==QCanvasActivity.QTYPE_VERT_EQUAL){
                    JSONArray area = jobj.getJSONArray("area");
                    String ans = jobj.getString("as");
                    rec.rans.put(ans);
                    int ty = jobj.getJSONObject("pgas").getInt("ty");
                    JSONArray q1 = jobj.getJSONObject("pgas").getJSONArray("q1");
                    String veq = "";
                    if(ty==VertEqualObject.ADD || ty==VertEqualObject.SUB || ty == VertEqualObject.MULT){
                        veq = q1.getString(0) + q1.getString(1);
                        veq = veq.replace(" ","");

                    }
                    QCanvasActivity.eq.createEquation(veq, 0,0,1280,720, Configuration.ORIENTATION_LANDSCAPE);
                    VertEqualObject vobj = new VertEqualObject(QCanvasActivity.eq.eq);
                    AnswerObject aobj = new AnswerObject(area.getInt(0), area.getInt(1), area.getInt(0)+area.getInt(2), area.getInt(1)+area.getInt(3), vobj);
                    aobj.qtype= QCanvasActivity.QTYPE_VERT_EQUAL;
                    ansObjs.add(aobj);


                }
            }
            rec.objs = ansObjs;
            return rec;
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

}
