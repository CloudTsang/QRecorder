package com.meili.mnist.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meili.mnist.TF;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VertEqCreator {
    public String qStr;
    private int top;
    private int left;
    private int stageH;
    private int stageW;
    private int orient;
    private Context cnt;
    private int size;
    private int tsize;

    public ArrayList<TextView> textViews;
    public ArrayList<Rect> rects;
    public ArrayList<TextView> textViews2;
    public ArrayList<Rect> rects2;
    public ArrayList<String> eq;
    //验算式
    public ArrayList<String> eq2;

    public Path path;
    public Path path2;
    public int rightBorder = 0;
    public int leftBorder = 0;
    public int bottomBorder = 0;
    //是否有验算式
    public boolean hasExam = false;

    public VertEqCreator(Context context){
            cnt = context;
    }

    public boolean createEquation(String s, int l, int t, int w, int h, int o){
        qStr = s;
        left = l;
        top = t;
        stageH = h;
        stageW = w;
        orient = o;
        Hashtable ret = generate(s);
//        eq = (ArrayList<String>) ret.get("raw");
//        ArrayList<String> sorted = (ArrayList<String>) ret.get("sort");
        eq = (ArrayList<String>) ret.get("sort");
        if(eq.size() < 3){
            return false;
        }
        Log.i("mnist", "eq = "+eq.toString());
        if(eq.get(1).equals("÷")){
            printEq(eqToTxtDivide(eq));
            try{
                eq2 = generateExam(eq);
                if(eq2!=null){
                    printEq(eqToTxtExam(eq2));
                    hasExam = createEqE(w, h);
                }
            }catch (Exception err){
                err.printStackTrace();
            }
            return createEqD( w, h);
//            return
        }else{
            hasExam = false;
            if(eq.get(1).equals("+") || eq.get(1).equals("-")){
//                alignDot(eq)
//                printEq(eqToTxtAddSub(eq));
//                printEq(sorted);
                printEq(eq);
            }else{
//                printEq(eqToTxtMult(eq));
//                printEq(sorted);
                printEq(eq);
            }
            return createEqASM2(w, h);
        }
    }

    public boolean createEquation(ArrayList<String> eq0, int l, int t, int w, int h, int o){
        left = l;
        top = t;
        stageH = h;
        stageW = w;
        orient = o;
        eq = eq0;
        Log.i("mnist", "eq = "+eq.toString());
        if(eq.get(1).equals("÷")){
            return createEqD( w, h);
        }else{
            return createEqASM2(w, h);
        }
    }

    //加减乘法竖式样式生成
    private boolean createEqASM( int w, int h){
        int maxWords = (Math.max(eq.get(0).length(), eq.get(eq.size()-1).length()))+1;
        if(orient == Configuration.ORIENTATION_PORTRAIT){
            size = (int)Math.floor(w*0.8 / maxWords);
            leftBorder = (h - maxWords*size)/2;
            rightBorder=  h - leftBorder;
        }else if(orient == Configuration.ORIENTATION_LANDSCAPE){
            size = (int)Math.floor(h*0.8 / (eq.size()-1));
            leftBorder = (w - maxWords*size)/2;
            rightBorder=  w - leftBorder;
        }
        tsize = (int)((float)size / cnt.getResources().getDisplayMetrics().scaledDensity);
        textViews = new ArrayList<>();
        rects = new ArrayList<>();
        Paint blackPaint = new Paint();
        path = new Path();

        int curY = 0;
        int curX = rightBorder;

        int numLen = 0;
        numLen = eq.get(0).length();
        for(int i=numLen-1; i>=0; i--){
            String c = String.valueOf(eq.get(0).charAt(i));
            if(c.equals("_") || c.equals("口")) {
                curX -=size;
                continue;
            }
            TextView t = null;
            if(c.equals(".")){
                t = createText(c, tsize, curX - (int)(size/4), curY);
            }else{
                curX -= size; //*(numLen - i);
                t = createText(c, tsize, curX, curY);
            }
            textViews.add(t);
        }
        curY += size;
        textViews.add(createText(eq.get(1), tsize, leftBorder, curY));
        numLen = eq.get(2).length();
        curX = rightBorder;
        for(int i=numLen-1; i>=0; i--){
            String c = String.valueOf(eq.get(2).charAt(i));
            if(c.equals("_") || c.equals("口")) {
                curX -= size;
                continue;
            }
            TextView t = null;
//            if(c.equals(".") && false){
            if(c.equals(".")){
                t = createText(c, tsize, curX - (int)(size/4), curY);
            }else{
                curX -= size; //*(numLen - i);
                t = createText(c, tsize, curX, curY);
            }
            textViews.add(t);
        }
        curY += size+5;
        int midNum = 0;
        for(int i=3; i<eq.size(); i++){
            String c = eq.get(i);
            if(c.equals("!")){
                midNum ++;
                continue;
            }
            curX = rightBorder;
            if(c.equals("_")){
                curY += size/4+5;
                blackPaint.setColor(Color.BLACK);
                blackPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, cnt.getResources().getDisplayMetrics()));
                blackPaint.setStyle(Paint.Style.STROKE);
                path.moveTo(leftBorder, curY);
                path.lineTo(rightBorder, curY);
                curY += 5;
            }else{
                numLen = eq.get(i).length();
                boolean hasDot = eq.get(eq.size()-1).contains(".");
                for(int j=0; j<eq.get(i).length(); j++){
                    String c1 = String.valueOf(eq.get(i).charAt(j));
                    if(c1.equals("_") || c1.equals(" ") || c1.equals("口")) {
                        continue;
                    }
                    TextView t = null;
                    Rect r = null;
                    if(i == eq.size()-1){
                        if(c1.equals(".")){
                            t = createText(c1, tsize, rightBorder - size*(numLen - j-1)-size/4, curY);
                            hasDot = false;
                        }else{
                            if(hasDot){
                                t = createText(c1, tsize, rightBorder - size*(numLen - j - 1), curY);
                                r = createRect(rightBorder - size*(numLen - j - 1), curY, size, size);
                            }else{
                                t = createText(c1, tsize, rightBorder - size*(numLen - j), curY);
                                r = createRect(rightBorder - size*(numLen - j), curY, size, size);
                            }
                        }
                    }else{
                        t = createText(c1, tsize, rightBorder - size*(numLen - j + midNum), curY);
                        r = createRect(rightBorder - size*(numLen - j + midNum), curY, size, size);
                    }
                    textViews.add(t);
                    if(r!=null){
                        rects.add(r);
                    }
                }
                midNum ++;
                curY += size + 5;
            }
        }
        bottomBorder = curY;
        return true;
    }

    /**/
    private boolean createEqASM2( int w, int h){
        int maxWords = (Math.max(eq.get(0).length(), eq.get(eq.size()-1).length()))+1;
        if(orient == Configuration.ORIENTATION_PORTRAIT){
            size = (int)Math.floor(w*0.8 / maxWords);
            leftBorder = (h - maxWords*size)/2;
            rightBorder=  h - leftBorder;
        }else if(orient == Configuration.ORIENTATION_LANDSCAPE){
            size = (int)Math.floor(h*0.8 / (eq.size()-1));
            leftBorder = (w - maxWords*size)/2;
            if(leftBorder<0){
                size = size/2;
                leftBorder = (w - maxWords*size)/2;
            }
            rightBorder=  w - leftBorder;
        }
        tsize = (int)((float)size / cnt.getResources().getDisplayMetrics().scaledDensity);
        textViews = new ArrayList<>();
        rects = new ArrayList<>();
        Paint blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, cnt.getResources().getDisplayMetrics()));
        blackPaint.setStyle(Paint.Style.STROKE);

        path = new Path();
        int curY = 0;
//        Pattern regex = Pattern.compile("[+\\-×÷]{1}");
        for(int i=0; i<eq.size(); i++){
            String c = eq.get(i);
            if(c.equals("!")){
                continue;
            }
            if(c.contains("_")){
                curY += size/4+5;
                path.moveTo(leftBorder, curY);
                path.lineTo(rightBorder, curY);
                curY += 5;
            }else{
                int numLen = eq.get(i).length();
//                boolean hasDot = eq.get(eq.size()-1).contains(".");
                int curX = rightBorder;
                for(int j=numLen-1; j>=0; j--){
                    String c1 = String.valueOf(eq.get(i).charAt(j));
                    TextView t = null;
                    if(c1.equals("+") || c1.equals("-") || c1.equals("×")){
                        t = createText(c1, tsize, leftBorder, curY);
                        textViews.add(t);
                        continue;
                    }
                    if(c1.equals("_") || c1.equals(" ") || c1.equals("口")) {
                        curX -= size;
                        continue;
                    }

                    if(c1.equals(".")){
                        t = createText(c1, tsize, curX - (int)(size/4), curY);
                    }else{
                        curX -= size;
                        t = createText(c1, tsize, curX, curY);
                    }
                    textViews.add(t);
                }
                curY += size+5;
            }
        }
        return true;
    }
    /**/

    //除法竖式样式生成
    private boolean createEqD( int w, int h){
        String divisor = eq.get(0);
        String dividend = eq.get(2);
        String quetient = eq.get(eq.size()-1);
        int line1 = quetient.length() + dividend.length() + 1;
        int line2 = divisor.length()+dividend.length() + 1;

        int lenDivisor = divisor.length();
        int lenQuotient = quetient.length();
        if(divisor.contains(".")){
            lenDivisor -= 1;
            line2 --;
        }
        if(dividend.contains(".")){
            line1 --;
            line2 --;
        }
        if(quetient.contains(".")){
            lenQuotient --;
            line1 --;
        }
        //第一位有效数字的位置
        int startPos = 0;
        for(int p=0;p<divisor.length();p++){
            String pstr = String.valueOf(divisor.charAt(p));
            if(pstr.equals("0") ){
                startPos ++;
            }else{
                break;
            }
        }
        int startPos2 = 0;
        for(int p=0;p<quetient.length();p++){
            String pstr = String.valueOf(quetient.charAt(p));
            if(pstr.equals("0")){
                startPos2 ++;
            }else if(pstr.equals(".")){

            }else{
                break;
            }
        }

        int maxWords = Math.max(line1, line2);
        int validNum = 0;
        for(int i=0; i<eq.size(); i++){
            if(eq.get(i).equals("!") || eq.get(i).equals("_") || eq.get(i).equals("口")){
                continue;
            }
            validNum ++;
        }
        validNum ++;
        if(orient == Configuration.ORIENTATION_PORTRAIT){
            size = (int)Math.floor(w * 0.8 / maxWords);
            leftBorder = (h - maxWords*size)/2;
            rightBorder=  h - leftBorder;
        }else if(orient == Configuration.ORIENTATION_LANDSCAPE){
            size = (int)Math.floor(h * 0.8/ validNum);
            leftBorder = (w - maxWords*size)/2;
            rightBorder=  w - leftBorder;
        }
        tsize = (int)((float)size / cnt.getResources().getDisplayMetrics().scaledDensity);

        textViews = new ArrayList<>();
        rects = new ArrayList<>();

        int curY = 10;
        int curX = rightBorder;

        String curNum = quetient;
        for(int i=curNum.length()-1; i>=0; i--){
            String c = String.valueOf(curNum.charAt(i));
            TextView t;
            if(c.equals(".")){
                t = createText(c, tsize, curX - (int)(size/4), curY);
            }else{
                curX -= size; //*(numLen - i);
                t = createText(c, tsize, curX, curY);
            }

            Rect r = createRect(curX, curY, size, size);
            textViews.add(t);
            rects.add(r);
        }
        curY += size + size/4;
        curX = rightBorder;

        Paint blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, cnt.getResources().getDisplayMetrics()));
        blackPaint.setStyle(Paint.Style.STROKE);
        path = new Path();
        path.moveTo(rightBorder, curY);
        path.lineTo(rightBorder-lenDivisor*size, curY);
        path.quadTo(rightBorder-lenDivisor*size-size/4 , curY+size , rightBorder-(lenDivisor+1)*size, curY + size/4*5);

        curY += size/4;
        curNum = divisor;
        for(int i=curNum.length()-1; i>=0; i--){
            String c = String.valueOf(curNum.charAt(i));
            TextView t;
            if(c.equals(".")){
                t = createText(c, tsize, curX - (int)(size/4), curY);
            }else{
                curX -= size; //*(numLen - i);
                t = createText(c, tsize, curX, curY);
            }
            textViews.add(t);
        }
        curX -= size;
        curNum = dividend;
        for(int i=curNum.length()-1; i>=0; i--){
            String c = String.valueOf(curNum.charAt(i));
            TextView t;
            if(c.equals(".")){
                t = createText(c, tsize, curX - (int)(size/4), curY);
            }else{
                curX -= size; //*(numLen - i);
                t = createText(c, tsize, curX, curY);
            }
            textViews.add(t);
        }

        curY += size;

        int curQuo = 0;
        for(int i=3; i<eq.size()-1; i++){
            curNum = eq.get(i);
            curX = rightBorder - ((lenQuotient-startPos2) - curQuo - 1) * size;

            if(curNum.equals("_") ||eq.get(i).equals("口")){
                curY += size/4;
                path.moveTo(rightBorder, curY);
                path.lineTo(rightBorder-lenDivisor*size, curY);
                curY += size/4;
                if(i!=eq.size()-3 || (i==eq.size()-3 && !eq.get(eq.size()-2).equals("0") && curQuo<lenQuotient-1)){
                    curQuo+=1;
                }

                continue;
            }
            if(curNum.equals("!")){
                curQuo += 1;
                continue;
            }
            for(int j=curNum.length()-1; j>=0; j--){
                String c = String.valueOf(curNum.charAt(j));
//                curX -= size;
//                TextView t = createText(c, tsize, curX, curY);
                TextView t;
                if(c.equals(".")){
                    t = createText(c, tsize, curX - (int)(size/4), curY);
                }else{
                    curX -= size; //*(numLen - i);
                    t = createText(c, tsize, curX, curY);
                }
                Rect r = createRect(curX, curY, size, size);
                textViews.add(t);
                rects.add(r);
            }
            curY += size;
        }
        return true;
    }

    private boolean createEqE(int w, int h){
        int maxWords = eq2.get(eq2.size()-1).length()+1;
        if(orient == Configuration.ORIENTATION_PORTRAIT){
            size = (int)Math.floor(w*0.8 / maxWords);
            leftBorder = (h - maxWords*size)/2;
            rightBorder=  h - leftBorder;
        }else if(orient == Configuration.ORIENTATION_LANDSCAPE){
            size = (int)Math.floor(h*0.8 / (eq.size()-1));
            leftBorder = (w - maxWords*size)/2;
            rightBorder=  w - leftBorder;
        }
        tsize = (int)((float)size / cnt.getResources().getDisplayMetrics().scaledDensity);

        textViews2 = new ArrayList<>();
        rects2 = new ArrayList<>();

        Paint blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TF.DRAW_THICKNESS*3, cnt.getResources().getDisplayMetrics()));
        blackPaint.setStyle(Paint.Style.STROKE);

        path2 = new Path();
        int curY = 0;
        int phrase = 0;
        int space = 0;
        for(int i=0; i<eq2.size(); i++){
            String s = eq2.get(i);
            if(s.equals("_")){
                curY += size/4+5;
                path2.moveTo(leftBorder, curY);
                path2.lineTo(rightBorder, curY);
                curY += 5;
                if(phrase == 0){
                    if(eq2.get(2).length() == 1){
                        phrase = 2;
                    }else{
                        phrase = 1;
                    }
                }else if(phrase==1){
                    phrase = 2;
                }
                continue;
            }
            if(s.equals("×")){
                textViews2.add(createText(s, tsize, leftBorder, curY));
                continue;
            }
            if(s.equals("+")){
                textViews2.add(createText(s, tsize, leftBorder, curY));
                continue;
            }
            if(s.equals("!")){
                space++;
                continue;
            }
            for(int j=0; j<s.length(); j++){
                String c = String.valueOf(s.charAt(j));
                TextView t = null;
                Rect r = null;
                if(phrase != 1){
                    t = createText(c, tsize, rightBorder - size*(s.length() - j), curY);
                    r = createRect(rightBorder - size*(s.length() - j), curY, size, size);
                }else{
                    t = createText(c, tsize, rightBorder - size*(s.length() - j + space), curY);
                    r = createRect(rightBorder - size*(s.length() - j + space), curY, size, size);
                }
                textViews2.add(t);
                rects2.add(r);
            }
            if(phrase==1){
                space ++;
            }
            curY+=size;
        }
        return true;

    }

    //竖式数据整理
//    public ArrayList<String> generate(String s){
    public Hashtable generate(String s){
        ArrayList<String> ret = new ArrayList<>();
        Hashtable resTable = new Hashtable();
        ArrayList<String> ret2 = new ArrayList<>();
        ArrayList<String> ret3 = new ArrayList<>();

        s = s.replace("=","");
        s = s.replace("*","×");
        s = s.replace("/","÷");
        Pattern regex = Pattern.compile("[+\\-×÷]{1}");
        String[] arr = regex.split(s);
        if(arr.length < 2){
            return null;
        }
        int curIndex = 0;
        for(int i=0; i<arr.length; i++){
            //数字
            ret.add(arr[i]);
            curIndex += arr[i].length();
            if(i!=arr.length-1){
                //符号
                ret.add(String.valueOf(s.charAt(curIndex)));
                curIndex += 1;
            }
        }
        ret2.add(ret.get(0));
        for(int i=1;i<ret.size(); i+=2){
            ArrayList<String> tmp = new ArrayList<>();
            ArrayList<String> tmp2;
            tmp.add(ret2.get(ret2.size()-1)); //上一段得数
            tmp.add(ret.get(i));
            tmp.add(ret.get(i+1));
            String op = tmp.get(1); //运算符
            String[] tmpArr = {tmp.get(0), tmp.get(2)};
            if(op.equals("÷")){
                tmp = generateDivide2(tmpArr, tmp);
                tmp2 = eqToTxtDivide(tmp);
            }else if(op.equals("×")){
                tmp = generateMult(tmpArr, tmp);
                tmp2 = eqToTxtMult(tmp);
            }else{
                tmp = generateAddSub(tmpArr, tmp, arr);
//                tmp = alignDot(tmp);
                tmp2 = eqToTxtAddSub(tmp);
            }
            if(ret3.size()==0){
                ret3.addAll(tmp2);
            }else{
                ret3.remove(ret3.size()-1);
                ret3.addAll(tmp2);
//                ret3.addAll(tmp2.subList(1, tmp2.size()));
            }
//            ret2.remove(ret2.size()-1);
            ret2.addAll(tmp);
        }
        resTable.put("raw", ret2);
        resTable.put("sort", ret3);
//        return ret2;
        return resTable;
    }

    private ArrayList<String> generateMult(String[] arr, ArrayList<String> ret){
        String a = arr[0];//第一个数
        BigDecimal a2 = new BigDecimal(a);
        BigDecimal ten = new BigDecimal(10);
        while (new BigDecimal(a2.intValue()).compareTo(a2) != 0){
            a2 = a2.multiply(ten);
        }
        String a3 = String.valueOf(a2.intValue());
        String op = ret.get(1); //运算符
        ret.add("_");
        if(arr[1].length() > 1){
            for(int i=arr[1].length()-1; i>=0; i--){
                if (arr[1].charAt(i) == '.') {
                    continue;
                }
                String b = String.valueOf(arr[1].charAt(i));
                if (b.equals("0")){
                    ret.add("!");
                    continue;
                }
                String c = calc(a3, op, b);
                ret.add(c);
            }
        }else{
            String b = arr[1];
            String c = calc(a, op, b);
            ret.add(c);
        }
        if(ret.get(ret.size()-1).equals("!") && ret.size()==6){
            ret.remove(ret.size()-1);
            String c = calc(a, op, arr[1]);
            ret.set(ret.size()-1, c);
        }
        else if(arr[1].length() > 1){
            ret.add("_");
            String b = arr[1];
            String c = calc(a, op, b);
            ret.add(c);
        }
        int dotNum = 0;
        for(int i=0; i<ret.get(0).length(); i++){
            if(String.valueOf(ret.get(0).charAt(i)).equals(".")){
                dotNum += ret.get(0).length()-i;
            }
        }
        for(int i=0; i<ret.get(2).length(); i++){
            if(String.valueOf(ret.get(2).charAt(i)).equals(".")){
                dotNum += ret.get(2).length()-i;
            }
        }

        String tmpAns = ret.get(ret.size()-1);
        if(tmpAns.length()<dotNum && !tmpAns.contains(".")){
            tmpAns+=".";
            dotNum++;
        }
        while (tmpAns.length()<dotNum){
            tmpAns += "0";
        }
        ret.set(ret.size()-1, tmpAns);
        //多位数乘法是否只有一个有效乘积
        int hasMoreNum = 0;
        for(int i=4; i<ret.size()-2;i++){
            String s = ret.get(i);
            if(!s.equals("!")){
                hasMoreNum++;
                if(hasMoreNum >= 2){
                    break;
                }
            }
        }

        if(hasMoreNum == 1){
            ArrayList<String> ret2 = new ArrayList<>();
            ret2.addAll(ret.subList(0,4));
            ret2.add(ret.get(ret.size()-1));
            ret= ret2;
        }
        return ret;
    }

    private ArrayList<String> generateAddSub(String[] arr, ArrayList<String> ret, String[] totArr){
        String a = arr[0];//第一个数
        String op = ret.get(1); //运算符

        int maxCommaNum = 0;
        for(int i=0; i<totArr.length; i++){
            if(totArr[i].contains(".")){
                maxCommaNum = Math.max(totArr[i].length() - totArr[i].indexOf(".")-1, maxCommaNum);
            }
        }
        ret.add("_");
        String b =String.valueOf(arr[1]);
        String c = calc(a, op, b);
        ret.add(c);

        for(int i=0; i<ret.size(); i++){
            if(Pattern.matches("[0-9.]+", ret.get(i))){
                int commaNum = 0;
                if(ret.get(i).contains(".")){
                    commaNum = ret.get(i).length() - ret.get(i).indexOf(".")-1;
                }
                StringBuilder builder = new StringBuilder(ret.get(i));
                for(int idx=0; idx<maxCommaNum-commaNum; idx++){
                    builder.append("口");
                }
                ret.set(i, builder.toString());
            }
        }
        return ret;
    }

    private ArrayList<String> generateDivide(String[] arr, ArrayList<String> ret){
        String a = arr[0];//第一个数
        String op = ret.get(1); //运算符
        //除法返回数组结构：[被除数，÷，除数，-，被除数，除数，-，……，余数，最后得数]
        StringBuilder skipRemainder = new StringBuilder();//余数
        StringBuilder divisorStr = new StringBuilder();//被除数（上一位除法的余数+现一位
        StringBuilder finalQuotient = new StringBuilder();//最后得数
        BigDecimal dividend = new BigDecimal(arr[1]);

        if(dividend.floatValue() == 0){
            return ret;
        }
        boolean started = false;
        int tmpSkip = 0;
        for(int i=0; i<a.length(); i++){
            if(divisorStr.length() == 0 && a.charAt(i) == '0' && started){
                //上一位余数为0且现一位为0时
                skipRemainder.append("0");
                finalQuotient.append("0");
                if(i == a.length()-1){
                    if(skipRemainder.length() > 0){
                        ret.add((new BigDecimal(skipRemainder.toString())).stripTrailingZeros().toPlainString());
                    }
                    ret.add(finalQuotient.toString());
                    continue;
                }
                tmpSkip ++;
//                ret.add("!"); //！= 跳到下一位
                continue;
            }
            if(tmpSkip>0){
                for(int sk=0;sk<tmpSkip;sk++){
                    ret.add("!");
                }
            }
            tmpSkip = 0;

            divisorStr.append(a.charAt(i));
            if(String.valueOf(a.charAt(i)).equals(".")){
                continue;
            }
            BigDecimal divisor = new BigDecimal(divisorStr.toString());
            if(divisor.floatValue() < dividend.floatValue() && started){
                //被除数小于除数不能除的场合continue
                skipRemainder.append(a.charAt(i));
                finalQuotient.append("0");
                if(i == a.length()-1){
                    if(skipRemainder.length() > 0){
                        ret.add((new BigDecimal(skipRemainder.toString())).stripTrailingZeros().toPlainString());
                    }
                    ret.add(finalQuotient.toString());
                    continue;
                }
                ret.add("!");
                continue;
            }
            else if(divisor.floatValue() >= dividend.floatValue()){
                if(started){
                    ret.add(divisorStr.toString());
                }
                String tmpstr = calc(divisorStr.toString(), op, arr[1]);
                String[] tmpresult = tmpstr.split("&&");
                String quotient = tmpresult[0];//商
                String remainder = tmpresult[1];//余数
                if(i == a.length()-1){
                    ret.add(calc(quotient, "×", arr[1]));
                    ret.add("_");
                    ret.add(remainder);
                    finalQuotient.append(quotient);
                    ret.add(finalQuotient.toString());
                    break;
                }
                ret.add(calc(quotient, "×", arr[1]));
                ret.add("_");
                if(remainder.equals("0")){
                    divisorStr = new StringBuilder();
//                    ret.add("!");
                }else{
                    divisorStr = new StringBuilder(remainder);
                }

                if(!started){
                    started = true;
                }
                finalQuotient.append(quotient);
            }
        }
        return ret;
    }

    private ArrayList<String> generateDivide2(String[] arr, ArrayList<String> ret){
        boolean isInifinite = true;
        String divisorStr = arr[0];
        String dividendStr = arr[1];

        BigDecimal divisor = new BigDecimal(divisorStr);
        BigDecimal dividend = new BigDecimal(dividendStr);
        BigDecimal ten = new BigDecimal(10);

        if (new BigDecimal(divisor.intValue()).compareTo(divisor) == 0 && new BigDecimal(dividend.intValue()).compareTo(dividend) == 0){
            if(divisor.compareTo(dividend) > 0){
                //整数被除数>除数，整数除法取余数
                return generateDivide(arr, ret);
            }
        }
        //整|小数被除数<除数，小数除法, 去除小数位得到整数除法的过程后替换被除数和除数
        BigDecimal finalQuetient = divisor.divide(dividend,5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
        if(dividend.multiply(finalQuetient).compareTo(divisor)!=0){
            isInifinite = false;
        }

        BigDecimal tmpFinalQuetient = new BigDecimal(finalQuetient.toPlainString());
        while(new BigDecimal(tmpFinalQuetient.intValue()).compareTo(tmpFinalQuetient) != 0 ){
            tmpFinalQuetient = tmpFinalQuetient.multiply(ten);
        }
        tmpFinalQuetient = new BigDecimal(tmpFinalQuetient.intValue());

        BigDecimal tmpDividend = new BigDecimal(dividend.toPlainString());
        while(new BigDecimal(tmpDividend.intValue()).compareTo(tmpDividend) != 0 ){
            tmpDividend = tmpDividend.multiply(ten);
        }
        tmpDividend = new BigDecimal(tmpDividend.intValue());
        BigDecimal tmpDivisor = tmpDividend.multiply(tmpFinalQuetient);
        String a = tmpDivisor.toPlainString();
        String b = tmpDividend.toPlainString();
        String[] tmpArr = {a, b};
        ArrayList<String> tmpRet = new ArrayList<>();
        tmpRet.add(a);
        tmpRet.add("÷");
        tmpRet.add(b);
        try{
            tmpRet = generateDivide(tmpArr, tmpRet);
            Log.i("mnist", "tmpRet = " +tmpRet.toString());
        }catch (Exception err){
            err.printStackTrace();
        }
        int validNum = 0;
        for(int i=0; i<divisorStr.length(); i++){
            String c = String.valueOf(divisorStr.charAt(i));
            if(c.equals("0") || c.equals(".")){
                validNum++;
            }else{
                break;
            }
        }
        int spaceToAdd = a.length() - (divisorStr.length()-validNum);
        if(!isInifinite){
            spaceToAdd = 5;
        }
        for(int i=0; i<spaceToAdd; i++){
            divisorStr += " ";
        }
        tmpRet.set(0, divisorStr);
        tmpRet.set(2, dividendStr);
        tmpRet.set(tmpRet.size()-1, finalQuetient.toPlainString());
        Log.i("mnist", tmpRet.toString());

        return tmpRet;
    }

    private ArrayList<String> generateExam(ArrayList<String> eq0){
        String dividend = eq0.get(2);
        String quotient = eq0.get(eq0.size()-1);
        String remainder = eq0.get(eq0.size()-2);
        if(dividend.contains(".") || quotient.contains(".") || eq0.get(0).contains("0")){
            return null;
        }
        if(remainder.equals("0") || remainder.contains(".")){
            return null;
        }
        String[] arr1 = {quotient,dividend};
        ArrayList<String> ret1 = new ArrayList<>();
        ret1.add(quotient);
        ret1.add("×");
        ret1.add(dividend);
        ret1 = generateMult(arr1, ret1);

        String divisor = ret1.get(ret1.size()-1);
        String[] arr2 = {divisor, remainder};
        ArrayList<String> ret2 = new ArrayList<>();
        ret2.add(divisor);
        ret2.add("+");
        ret2.add(remainder);
        ret2 = generateAddSub(arr2, ret2, arr2);

        ret2.remove(0);
        ret1.addAll(ret2);
        Log.i("mnist",ret1.toString());
        return ret1;
    }

    public static ArrayList<String> eqToTxtMult(ArrayList<String> eq0){
        ArrayList<String> ret = new ArrayList<>();
        int maxLen = 0;
        for(String s:eq0){
            maxLen = Math.max(s.length(), maxLen);
            if(s.equals("_") || s.equals("!")){
                continue;
            }
        }
        String line = printLine(maxLen);
        ret.add(eq0.get(0));
        ret.add(eq0.get(1)+" "+eq0.get(2));

        ret.add(line);
        String space = "";
        for(int i=4; i<eq0.size(); i++){
            String s = eq0.get(i);
            if(s.equals("_")){
                ret.add(line);
                continue;
            }
            if(s.equals("!")){
                space += "口";
                continue;
            }
            if(i == eq0.size()-1){
                ret.add(s);
            }else{
                ret.add(s+space);
            }
            space += "口";
        }
        return ret;
    }

    public static ArrayList<String> eqToTxtAddSub(ArrayList<String> eq0){
        ArrayList<String> ret = new ArrayList<>();
        ret.add(eq0.get(0).replace(" ","口"));

        ret.add(eq0.get(1)+" "+eq0.get(2).replace(" ","口"));
        int maxLen = 0;
        for(String s:eq0){
            maxLen = Math.max(s.length(), maxLen);
        }
        String line = printLine(maxLen);
        ret.add(line);
        ret.add(eq0.get(4).replace(" ","口"));
        return  ret;
    }

    public static ArrayList<String> eqToTxtDivide(ArrayList<String> eq0){
        String divisor = eq0.get(0);
        String dividend = eq0.get(2);
        String quetient = eq0.get(eq0.size()-1);
        int startPos2 = 0;
        int lenQuotient = quetient.length();
        if(quetient.contains(".")){
            lenQuotient--;
        }
        for(int p=0;p<quetient.length();p++){
            String pstr = String.valueOf(quetient.charAt(p));
            if(pstr.equals("0")){
                startPos2 ++;
            }else if(pstr.equals(".")){
            }else{
                break;
            }
        }
        ArrayList<String> ret = new ArrayList<>();
        ret.add(quetient);

        String line = printLine(divisor.length());
        ret.add(line);
        ret.add(dividend + "/" + divisor.replace(" ","口"));

        int curQuo = 0;
        for(int i=3;i<eq0.size()-1; i++){
            String s = eq0.get(i);
            if(s.equals("_")){
                ret.add(line);
                if(i!=eq0.size()-3 || (i == eq0.size()-3 && !eq0.get(eq0.size()-2).equals("0")  && curQuo<lenQuotient-1) ){
                    curQuo++;
                }
                continue;
            }
            if(s.equals("!")){
                curQuo ++;
                continue;
            }
            int spacetoadd = (lenQuotient-startPos2) - curQuo - 1;
            for(int j=0;j<spacetoadd;j++){
                s += "口";
            }
            ret.add(s);
        }
        return ret;
    }

    public static ArrayList<String> eqToTxtExam(ArrayList<String> eq0){
        ArrayList<String> ret = new ArrayList<>();
        int maxLen = 0;
        for(String s:eq0){
            maxLen = Math.max(s.length(), maxLen);
        }

        String line = printLine(maxLen);
        ret.add(eq0.get(0));
        ret.add(eq0.get(1)+" "+eq0.get(2));
        ret.add(line);
        String space = "";
        for(int i=4;i<eq0.size()-6;i++){
            if(eq0.get(i).equals("!")){
                space+="口";
                continue;
            }

            ret.add(eq0.get(i) + space);
            space+="口";
        }
        if(eq0.get(2).length()>1){
            ret.add(line);
        }
        ret.add(eq0.get(eq0.size()-5));
        ret.add(eq0.get(eq0.size()-4) + " "+ eq0.get(eq0.size()-3));
        ret.add(line);
        ret.add(eq0.get(eq0.size()-1));
        return ret;
    }

    private static String printLine(int len){
        String l = "";
        for(int i=0;i<len;i++){
            l+="_";
        }
        return l;
    }

    public static ArrayList<String> printEq(ArrayList<String> eq0){
        Log.i("mnist", eq0.toString());
        int maxLen = 0;
        for(String s:eq0){
            maxLen = Math.max(maxLen, s.length());
        }
        ArrayList<String> ret = new ArrayList<>();
        for(String s:eq0){
            while (s.length()<maxLen){
                s = "  "+s;
            }
            ret.add(s);
            Log.i("mnist", s);
        }
        return ret;
    }

    private ArrayList<String> alignDot(ArrayList<String> tmpEq){
        int maxDotLen = 0;
        for(int i=0; i<tmpEq.size(); i++){
            String s = tmpEq.get(i);
            if(!Pattern.matches("[0-9.]+", s)){
                continue;
            }
            String[] arr = s.split("\\.");
            if(arr.length<2){
                continue;
            }
            maxDotLen = Math.max(maxDotLen, arr[1].length());
        }
        if(maxDotLen==0){
            return tmpEq;
        }
        for(int i=0; i<tmpEq.size(); i++) {
            String s = tmpEq.get(i);
            if (!Pattern.matches("[0-9.]+", s)) {
                continue;
            }
            String[] arr = s.split("\\.");
            String s2 = arr[0];
            int start = 0;
            if(arr.length<2){
//                s2 += " ";
            }else{
                s2 += "."+arr[1];
                start = arr[1].length();
            }
            for(int j=start; j<maxDotLen;j++){
                s2 += " ";
            }
            tmpEq.set(i, s2);
        }
        return tmpEq;
    }

    private ArrayList<String> alignZero(ArrayList<String> tmpEq){
        return tmpEq;
    }

    public void clean(){
        textViews = null;
        rects = null;
        path = null;
    }

    public String calc(String a0, String op, String b0){
        BigDecimal c = null;
        BigDecimal a = new BigDecimal(a0.replace("口",""));
        BigDecimal b = new BigDecimal(b0.replace("口",""));
        switch (op){
            case "+":
                c = a.add(b);
                break;
            case "-":
                c = a.subtract(b);
                break;
            case "×":
                c = a.multiply(b);
                break;
            case "÷":
                BigDecimal[] bs = a.divideAndRemainder(b);
                return bs[0].stripTrailingZeros().toPlainString() + "&&" + bs[1].stripTrailingZeros().toPlainString();
        }
        return c.stripTrailingZeros().toPlainString();
    }

    private TextView createText(String s, int size, int x, int y){
        TextView txt = new TextView(cnt);
        txt.setIncludeFontPadding(false);
        txt.setGravity(Gravity.CENTER);
        txt.setTextColor(Color.BLACK);
        txt.setTextSize(size);
        txt.setText(s);
//        txt.setPadding(1,1,1,1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
        params.setMargins(x, y, 0, 0);
        txt.setLayoutParams(params);

        return txt;
    }

    private Rect createRect(int x, int y, int w, int h){
        Rect r = new Rect(x,y, x+w, y+h);
        return r;
    }

    private String getValidNumber(String s){
        return BigDecimal.valueOf(Double.parseDouble(s)).stripTrailingZeros().toPlainString();
    }

}
