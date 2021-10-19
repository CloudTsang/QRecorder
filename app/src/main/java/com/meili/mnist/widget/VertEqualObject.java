package com.meili.mnist.widget;


import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;

public class VertEqualObject {
    public static final int ADD_MULT_SUB = 0;
    public static final int ADD = 1;
    public static final int SUB = 2;
    public static final int MULT = 3;
    public static final int DIVIDE = 4;

    public int qtype;
    public ArrayList<String> eqs;
    public ArrayList<String> eqsExam;
    public String answer;
    public String remainder;

    public VertEqualObject(ArrayList<String> strs){
        eqs = strs;
        if(eqs.size()==0){
            return;
        }
        String sig = eqs.get(1);
        if(sig.equals("+")){
            qtype = ADD;
        }else if(sig.equals("-")){
            qtype = SUB;
        }else if(sig.equals("×")){
            qtype = MULT;
        }else if(sig.equals("÷")){
            qtype = DIVIDE;
        }
        try{
            String tmp = eqs.get(eqs.size()-1);
            tmp = tmp.replace(" ", "");
            tmp = tmp.replace("口", "");
            BigDecimal a = new BigDecimal(tmp);
            answer = a.toPlainString();
            if(qtype == DIVIDE){
                tmp = eqs.get(eqs.size()-2);
                tmp = tmp.replace(" ", "");
                tmp = tmp.replace("口", "");
                BigDecimal b = new BigDecimal(tmp);
                if(!b.toPlainString().equals("0")){
                    remainder = b.toPlainString();
                }
            }
        }catch (Exception err){
            err.printStackTrace();
        }

    }

    public VertEqualObject(int qt, ArrayList<String> strs){
        qtype = qt;
        eqs = strs;
        if(eqs.size()==0){
            return;
        }
        try{
            if(qt == ADD_MULT_SUB){
                String tmp = eqs.get(eqs.size()-1);
                tmp = tmp.replace(" ", "");
                tmp = tmp.replace("口", "");
                BigDecimal a = new BigDecimal(tmp);
                answer = a.toPlainString();
            }else{
                String tmp = eqs.get(0);
                tmp = tmp.replace(" ", "");
                tmp = tmp.replace("口", "");
                BigDecimal a = new BigDecimal(tmp);
                answer = a.toPlainString();
            }
        }catch (Exception err){
            err.printStackTrace();
        }
    }

}
