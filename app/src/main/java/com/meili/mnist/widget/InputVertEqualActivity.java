package com.meili.mnist.widget;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meili.mnist.QCanvasActivity;
import com.meili.mnist.R;

import java.util.ArrayList;

public class InputVertEqualActivity extends Activity {
    private RelativeLayout panel;
    private SimpleCanvasView canvas;
    private TextView txt;
    private String str;
    private ArrayList<String> eqs = null;
    private boolean inited = false;
    private Button btnExam;
    private boolean isExam = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vert_eq_input);
        panel = (RelativeLayout)findViewById(R.id.panel_inputvert);
        canvas = (SimpleCanvasView)findViewById(R.id.simple_canvas_view_verteq_input);
        txt = (TextView)findViewById(R.id.txt_inputvert);
        btnExam = (Button)findViewById(R.id.btn_vert_exam);

        canvas.isLock = true;
        Intent intent = getIntent();
        str = intent.getStringExtra("vertstr");
        if(intent.hasExtra("eqs")){
            eqs = intent.getStringArrayListExtra("eqs");
        }

        Button btnCancel = (Button)findViewById(R.id.btn_inputvert_cancel);
        if(eqs!= null && eqs.size()>0){
            btnCancel.setVisibility(View.INVISIBLE);
        }else{
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(inited){
            return;
        }
        inited = true;
        boolean eqresult = false;
        try{
            if(eqs!= null && eqs.size()>0){
                eqresult = QCanvasActivity.eq.createEquation(eqs, 0,0,panel.getWidth(), panel.getHeight(), Configuration.ORIENTATION_LANDSCAPE);
            }else{
                eqresult = QCanvasActivity.eq.createEquation(str, 0,0,panel.getWidth(), panel.getHeight(), Configuration.ORIENTATION_LANDSCAPE);
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        if(QCanvasActivity.eq.hasExam){
            btnExam.setVisibility(View.VISIBLE);
        }else{
            btnExam.setVisibility(View.GONE);
        }
        isExam = false;

        if(!eqresult){
            txt.setText("请输入正确的式子");
        }else{
            showVertEq();
        }

    }

    private void showVertEq(){
        txt.setText(str);
        panel.removeAllViews();
        for(TextView t:QCanvasActivity.eq.textViews){
            panel.addView(t);
        }
        if(QCanvasActivity.eq.path!=null){
            canvas.path = QCanvasActivity.eq.path;
        }
        if(QCanvasActivity.eq.rects!=null){
            canvas.rects = QCanvasActivity.eq.rects;
        }
        canvas.invalidate();
    }

    public void showVertEqExam(){
        txt.setText(str);
        panel.removeAllViews();
        for(TextView t:QCanvasActivity.eq.textViews2){
            panel.addView(t);
        }
        if(QCanvasActivity.eq.path!=null){
            canvas.path = QCanvasActivity.eq.path2;
        }
        if(QCanvasActivity.eq.rects!=null){
            canvas.rects = QCanvasActivity.eq.rects2;
        }
        canvas.invalidate();

    }


    public void inputVertConfirm(View view){
        if(eqs!=null && eqs.size()>0){
            Intent intent = new Intent();
            setResult(202, intent);
        }else {
            Intent intent = new Intent();
            setResult(200, intent);
        }

        finish();
    }

    public void inputVertCancel(View view){
        Intent intent = new Intent();
        setResult(201, intent);
        finish();
    }

    public void inputVertExam(View view){
        if(!isExam){
            showVertEqExam();
            btnExam.setText("原式");
        }else{
            showVertEq();
            btnExam.setText("验算");
        }
        isExam = !isExam;

    }
}
