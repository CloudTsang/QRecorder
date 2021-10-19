package com.meili.mnist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import com.meili.mnist.mnist.MnistSeviceConnection;
import com.meili.mnist.mnist.GlobMethods;
import com.meili.mnist.ocr.BaiduOcr;
import com.meili.mnist.widget.BmpUtil;
import com.meili.mnist.widget.PathObject;
import com.meili.mnist.widget.SimpleCanvasView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.meili.mnist.QCanvasActivity.ydclassifierlite;

//选区后的手写识别视图
public class OcrResultActivity extends Activity {
    private LinearLayout resultPanel;
    private RelativeLayout canvasPanel;

    private RadioGroup radioGroup;
    private SimpleCanvasView canvas;
    private Switch isFraction;
    private Switch isMultAns;
    private boolean booFraction;
    private TextView tipFration;
    private Bitmap pic;

    private Button btnMultConfirm;
    private TextView multAnsTxt;
    private LinearLayout lyMultAns;

    private List<RadioButton> btns;
    private ArrayList<String> arr;
    private int finalAnswer;
    private int editingIndex;
    private boolean isBd;
    private boolean isAdd;
    private boolean multAns;
    private boolean isFormula;
    private boolean isFill;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_result);

        resultPanel = (LinearLayout)findViewById(R.id.panel_result_ctrl);
        canvasPanel = (RelativeLayout)findViewById(R.id.panel_baidu_canvas);

        radioGroup = (RadioGroup)findViewById(R.id.panel_ocr_result);
        canvas = (SimpleCanvasView)findViewById(R.id.simple_canvas_view);
        isFraction = (Switch)findViewById(R.id.btn_fraction);
        tipFration = (TextView)findViewById(R.id.txt_tipfrac);
        isMultAns = (Switch)findViewById(R.id.swtich_multans);
        btnMultConfirm = (Button)findViewById(R.id.btn_multans_confirm);
        multAnsTxt = (TextView)findViewById(R.id.txt_multans);
        lyMultAns = (LinearLayout)findViewById(R.id.ly_multans);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                finalAnswer = checkedId-79000;
            }
        });

        finalAnswer = 0;

        Intent intent = getIntent();
        isBd = intent.getBooleanExtra("bdocr", true);
        isFill = intent.getBooleanExtra("fill", false);
        arr = intent.getStringArrayListExtra("result");
        finalAnswer = intent.getIntExtra("final", 0);
        editingIndex = intent.getIntExtra("edit", -1);
        isFormula = intent.getBooleanExtra("formula", false);

        if(isFormula){
            isFraction.setVisibility(View.VISIBLE);
            tipFration.setVisibility(View.VISIBLE);
            isFraction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        tipFration.setText("单行识别，支持分数");
                    }else{
                        tipFration.setText("多行识别，不支持分数");
                    }
                    booFraction = isChecked;
                }
            });
        }else{
            isFraction.setVisibility(View.INVISIBLE);
            tipFration.setVisibility(View.INVISIBLE);
        }

        isMultAns.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isMultAns.setText("多答案");
                    multAns = true;
                    lyMultAns.setVisibility(View.VISIBLE);
                    btnMultConfirm.setVisibility(View.VISIBLE);
                }else{
                    isMultAns.setText("唯一答案");
                    multAns = false;
                    lyMultAns.setVisibility(View.INVISIBLE);
                    btnMultConfirm.setVisibility(View.INVISIBLE);
                }

            }
        });
        isAdd = false;
        Button btnInput = findViewById(R.id.btn_skip_input);
        if(isBd){
            if(isFill){
                btnInput.setVisibility(View.VISIBLE);
            }else {
                btnInput.setVisibility(View.GONE);
            }

            if(arr!=null && arr.size()>0){
                showResult(true);
                createList();
            }else{
                showResult(false);
            }
        }else{
            if(isFill){
                btnInput.setVisibility(View.VISIBLE);
            }else {
                btnInput.setVisibility(View.GONE);
            }
            showResult(false);
        }

    }

    public void onConfirmResult(View view){
        Intent intent = new Intent();
        intent.putStringArrayListExtra("results", arr);
        if(finalAnswer==-1 && arr.size()>0){
            finalAnswer = arr.size()-1;
        }
        intent.putExtra("final", finalAnswer);
        intent.putExtra("edit", editingIndex);
        setResult(200, intent);
        finish();
    }

    public void onUp(View view){
        if(finalAnswer == 0){
            return;
        }
        String tmp = new String(arr.get(finalAnswer-1));
        arr.set(finalAnswer-1, arr.get(finalAnswer));
        arr.set(finalAnswer, tmp);
        finalAnswer--;
        createList();
    }

    public void onDown(View view){
        if(finalAnswer == arr.size()-1){
            return;
        }
        String tmp = new String(arr.get(finalAnswer+1));
        arr.set(finalAnswer+1, arr.get(finalAnswer));
        arr.set(finalAnswer, tmp);
        finalAnswer++;
        createList();
    }

    public void onAdd(View view){
        isAdd = true;
        showResult(false);
        canvasPanel.setVisibility(View.VISIBLE);
    }

    public void onDelete(View view){
        if(arr == null || arr.size() == 0){
            return;
        }
        arr.remove(finalAnswer);
        if(finalAnswer > 0){
            finalAnswer--;
        }
        createList();
    }

    private void createList(){
        if(arr == null){
            return ;
        }
        radioGroup.removeAllViews();
        btns = new ArrayList<>();
        if(arr.size() == 0){
            return;
        }
        Log.i("mnist", "finalAnswer = "+finalAnswer);
        for(int i=0; i<arr.size(); i++){
            String s = arr.get(i);
            Log.i("mnist", s);
            final RadioButton rb = createBtn(s);
            rb.setId(79000+i);

            if(i == finalAnswer){
                rb.setChecked(true);
            }
            btns.add(rb);
            radioGroup.addView(rb);
            Space spa = new Space(this);
            spa.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3));
            radioGroup.addView(spa);
        }
    }

    private void showResult(boolean b){
        if(b){
            resultPanel.setVisibility(View.VISIBLE);
            canvasPanel.setVisibility(View.GONE);
        }else{
            resultPanel.setVisibility(View.GONE);
            canvasPanel.setVisibility(View.VISIBLE);
        }
    }

    public void onBaiduOcr(View view){
        if(isBd && isFill){
            try {
                JSONArray points = new JSONArray(canvas.getPointsString());
                if(points.length()==0){
                    showResult(true);
                    canvas.clean();
                    return;
                }
                PathObject obj = PathObject.getPathFromAxis2(points) ;
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)4, getResources().getDisplayMetrics()));
                paint.setStyle(Paint.Style.STROKE);
                Bitmap bmp = obj.drawPath3(paint);
                byte[] bs = BmpUtil.bitmapToBytes(bmp);
                MnistSeviceConnection.save(bmp, 0);
                if(bs != null) {
                    ArrayList<String> tmparr = BaiduOcr.ocr(bs);
                    String ret = "";
                    for(String s:tmparr){
                        ret += s;
                    }
                    ret = ret.replace("百度识别", "");
                    ret = ret.replace("度识别", "");
                    ret = ret.replace("百度识", "");
                    LayoutInflater factory = LayoutInflater.from(this);
                    final View addv = factory.inflate(R.layout.edittext_alert, null);
                    final EditText edit = (EditText)addv.findViewById(R.id.editText);
                    edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    edit.setSingleLine(true);

                    edit.setText(ret);
                    final AlertDialog alert2 = new AlertDialog.Builder(view.getContext()).setTitle("确认结果").setView(addv)
                            .setPositiveButton("确定", null)
                            .create();
                    alert2.setCanceledOnTouchOutside(false);
                    alert2.setCancelable(false);
                    alert2.show();

                    alert2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String ret2 = edit.getText().toString();
                            if(multAns){

                            }else{
                                alert2.dismiss();
                                Intent intent = new Intent();
                                intent.putExtra("path", canvas.getPointsString());
                                intent.putExtra("ocrresult", ret2);
                                setResult(200, intent);
                                finish();
                            }

                        }
                    });

                }
            }catch (Exception err){
                err.printStackTrace();
            }
            return;
        }
        if(isBd && !booFraction){
            try{
                JSONArray points = new JSONArray(canvas.getPointsString());
                if(points.length()==0){
                    showResult(true);
                    canvas.clean();
                    return;
                }
                PathObject obj = PathObject.getPathFromAxis2(points) ;
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)4, getResources().getDisplayMetrics()));
                paint.setStyle(Paint.Style.STROKE);
                Bitmap bmp = obj.drawPath2(paint);
//                MnistSeviceConnection.save(bmp, 0);
                byte[] bs = BmpUtil.bitmapToBytes(bmp);
                if(bs != null) {
                    ArrayList<String> tmparr = BaiduOcr.ocr(bs);
//                    ArrayList<String> tmparr = BaiduOcr.formulaOcr(bmp);
                    if(isFormula){
                        for(int in=0; in<tmparr.size(); in++){
                            String s = tmparr.get(in);
                            s = s.replace("十","+");
                            s = s.replace("一","-");
                            s = s.replace("|", "1");
                            s = s.replace("s", "5");
                            s = s.replace("S", "5");
                            s = s.replace("q", "9");
                            s = s.replace("b","6");
                            s = s.replace("y", "4");
                            s = s.replace(">", "7");
                            s = s.replace("二","=");
                            s = s.replace("o", "0");
                            s = s.replace("O", "0");
                            s = s.replace("口", "0");
                            s = s.replace(",", ".");
                            s = s.replace("（", "(");
                            s = s.replace("）", ")");
                            s = s.replace("几", "12");
                            if(!s.startsWith("=") && in>0){
                                s = "="+s;
                            }
                            tmparr.set(in, s);
                        }
                    }
                    showResult(true);
                    if(isAdd){
                        arr.addAll(tmparr);
                    }else{
                        arr = tmparr;
                        finalAnswer = arr.size()-1;
                    }
                    if(isFormula){
                        finalAnswer = arr.size()-1;
                    }
                    createList();
                }
                canvas.clean();
            }catch(Exception err){
                err.printStackTrace();
            }
        }else if(isBd && booFraction){
            String s = GlobMethods.mnist(this, ydclassifierlite, canvas.getPointsString());
            s = s.replace(">", "7");
            showResult(true);
            if(isAdd){
                if(!s.startsWith("=")){
                    s = "="+s;
                }
                arr.add(s);
            }else{
                arr = new ArrayList<>();
                arr.add(s);
                finalAnswer = arr.size()-1;
            }
            if(isFormula){
                finalAnswer = arr.size()-1;
            }
            createList();
            canvas.clean();
        }else{
            if(multAns){
                String s = GlobMethods.mnist(this, ydclassifierlite, canvas.getPointsString());
                canvas.clean();
                addMultAns(s);
            }else{
                Intent intent = new Intent();
                intent.putExtra("path", canvas.getPointsString());
                setResult(200, intent);
                finish();
            }

        }
    }

    public void onMultAnsConfirm(View view){
//        Log.i("mnist", "onMultAnsConfirm");
        String s= "";
        if(arr!=null){
            s  = arr.toString();
        }
        Intent intent = new Intent();
        intent.putExtra("multans", s);
        setResult(203, intent);
        finish();
    }

    public void onCancel(View view){
        if(multAns){
            if(canvas.isEmpty() && arr!=null && arr.size()>0){
                arr.remove(arr.size()-1);
                String s = TextUtils.join("\n",arr);
                multAnsTxt.setText(s);
                return;
            }
        }
        canvas.stepBack();
    }

    public void onSkipInput(View view){
        if(multAns){
            Intent intent = new Intent();
            intent.setClass(this, KeyboardTestActivity.class);
            startActivityForResult(intent, 201);
            return;
        }else{
            if(isBd && isFill){
                Intent intent = new Intent();
                setResult(201, intent);
                finish();
                return;
            }
            if(isBd){
                createInputView(view);
            }else{
                Intent intent = new Intent();
                setResult(201, intent);
                finish();
            }
        }
    }

    public void onSkipInput2(View view){
        if(multAns){
            LayoutInflater factory = LayoutInflater.from(this);
            final View addv = factory.inflate(R.layout.edittext_alert, null);
            final EditText edit = (EditText)addv.findViewById(R.id.editText);
            edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            final AlertDialog alert2 = new AlertDialog.Builder(this).setTitle("输入").setView(addv)
                    .setPositiveButton("确定", null)
                    .setNeutralButton("题图", null)
                    .create();
            alert2.setCanceledOnTouchOutside(false);
            alert2.setCancelable(false);
            alert2.show();

            alert2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ns = edit.getText().toString();
                    if(ns.length() == 0){
                        alert2.dismiss();
                        return;
                    }
                    ns = ns.replace("（","(");
                    ns = ns.replace("）",")");
                    addMultAns(ns);
                    alert2.dismiss();
                }
            });
            alert2.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dia = new Dialog(addv.getContext(), R.style.edit_AlertDialog_style);
                    WindowManager.LayoutParams lp = dia.getWindow().getAttributes();
                    lp.gravity = Gravity.CENTER;
                    lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;//宽高可设置具体大小
                    lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    dia.getWindow().setAttributes(lp);
                    dia.setContentView(R.layout.image_alert);
                    final ImageView imgv = (ImageView)dia.findViewById(R.id.img_alert_view);
                    dia.setCanceledOnTouchOutside(true);
                    imgv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dia.dismiss();
                        }
                    });
                    imgv.setImageBitmap(QCanvasActivity.questionImg);
                    dia.show();
                }
            });
        }else{
            if(isBd && isFill){
                Intent intent = new Intent();
                setResult(202, intent);
                finish();
                return;
            }
            if(isBd){
                createInputView(view);
            }else{
                Intent intent = new Intent();
                setResult(202, intent);
                finish();
            }
        }
    }

    private void createInputView(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View addv = factory.inflate(R.layout.edittext_alert, null);
        final EditText edit = (EditText)addv.findViewById(R.id.editText);
        edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edit.setSingleLine(false);

        edit.setText("");
        final AlertDialog alert2 = new AlertDialog.Builder(view.getContext()).setTitle("增加一行").setView(addv)
                .setPositiveButton("确定", null)
                .setNeutralButton("题图", null)
                .create();
        alert2.setCanceledOnTouchOutside(false);
        alert2.setCancelable(false);
        alert2.show();

        alert2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ns = edit.getText().toString();
                if(ns.length() == 0){
                    showResult(true);
                    alert2.dismiss();
                    return;
                }
                String[] nss = ns.split("\n");
                List<String> tmparr = Arrays.asList(nss);
//                            Log.i("mnist", ns);
                if(arr == null){
                    arr = new ArrayList<>();
                }
                for(int j=0;j<tmparr.size();j++){
                    String s = tmparr.get(j);
                    s.replace("（","(");
                    s.replace("）",")");
                    tmparr.set(j, s);
                }
                arr.addAll(tmparr);
                if(!isAdd){
                    finalAnswer = arr.size()-1;
                }
                createList();
                showResult(true);
                alert2.dismiss();
            }
        });

        alert2.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dia = new Dialog(addv.getContext(), R.style.edit_AlertDialog_style);
                WindowManager.LayoutParams lp = dia.getWindow().getAttributes();
                lp.gravity = Gravity.CENTER;
                lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;//宽高可设置具体大小
                lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                dia.getWindow().setAttributes(lp);
                dia.setContentView(R.layout.image_alert);
                final ImageView imgv = (ImageView)dia.findViewById(R.id.img_alert_view);
                dia.setCanceledOnTouchOutside(true);
                imgv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dia.dismiss();
                    }
                });
                imgv.setImageBitmap(QCanvasActivity.questionImg);
                dia.show();
            }
        });
    }

    private RadioButton createBtn(String s){
        final RadioButton rb = new RadioButton(this);
        rb.setTextSize(26);
        rb.setTextColor(Color.BLACK);
        rb.setText(s);
        rb.setBackgroundColor(Color.GRAY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(params);
        rb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int k = (int)rb.getId() - 79000;
                LayoutInflater factory = LayoutInflater.from(v.getContext());
                final View view = factory.inflate(R.layout.edittext_alert, null);
                final EditText edit = (EditText)view.findViewById(R.id.editText);
                edit.setSingleLine(true);
                edit.setText(arr.get(k));
                AlertDialog alert = new AlertDialog.Builder(v.getContext()).setTitle("修正识别结果").setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ns = edit.getText().toString();
                                arr.set(k, ns);
                                RadioButton tmpB = btns.get(k);
                                tmpB.setText(ns);
                                btns.set(k, tmpB);
                            }
                        }).create();
                alert.show();
                return false;
            }
        });
        return rb;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("mnist", requestCode + "   " + resultCode);
        if(requestCode==200){
            if(resultCode==200){
                Bundle bundle = data.getExtras();
                String input = bundle.getString("input");
                if(isAdd){
                    arr.add(input);
                }else{
                    arr = new ArrayList<>();
                    arr.add(input);
                    finalAnswer = arr.size()-1;
                }
                createList();
                showResult(true);
                canvas.clean();
            }
        }
        else if(requestCode==201) {
            if(resultCode==200) {
                Bundle bundle = data.getExtras();
                String input = bundle.getString("input");
                addMultAns(input);

            }
        }
    }

    private void addMultAns(String a){
        if(arr == null){
            arr = new ArrayList<>();
        }
        arr.add(a);
        String s = TextUtils.join("\n",arr);
        multAnsTxt.setText(s);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
