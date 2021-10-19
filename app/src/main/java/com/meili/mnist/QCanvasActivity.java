package com.meili.mnist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.meili.mnist.mnist.MnistSeviceConnection;
import com.meili.mnist.mnist.GlobMethods;
import com.meili.mnist.mnist.YdMnistClassifierLite;
import com.meili.mnist.ocr.BaiduOcr;
import com.meili.mnist.tts.BaiduTTS;
import com.meili.mnist.widget.AnswerCanvasView;
import com.meili.mnist.widget.AnswerObject;
import com.meili.mnist.widget.AnswerRecord;
import com.meili.mnist.widget.BmpUtil;
import com.meili.mnist.widget.CanvasPaints;
import com.meili.mnist.widget.CanvasView;
import com.meili.mnist.widget.InputVertEqualActivity;
import com.meili.mnist.widget.PathObject;
import com.meili.mnist.widget.VertEqCreator;
import com.meili.mnist.widget.VertEqualObject;
import com.tencent.smtt.sdk.QbSdk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Pattern;

import extensions.webview.WebViewActivity;

public class QCanvasActivity extends Activity {
    private static final String spechar = "(.*)[-一+十=二D0>7]+(.*)";
    public static final int QTYPE_ANY =  -1;
    public static final int QTYPE_PIC = 1;
    public static final int QTYPE_TXT = 2;
    public static final int QTYPE_MP4 = 3;
    public static final int QTYPE_GIF = 4;
    public static final int QTYPE_KIT = 5;
    public static final int QTYPE_LINE = 6;
    public static final int QTYPE_MULT_LINE = 61;
    public static final int QTYPE_SOLVE = 7;
    public static final int QTYPE_EQUALS = 8;
    public static final int QTYPE_DRAW_H = 9;
    public static final int QTYPE_VERT_EQUAL = 10;
    public static final int QTYPE_DRAW_CIRCLE = 11;
    public static final String[] qTypes = {"图片题","文本题","视频题","动图题","组件题",
            "连线题", "解答题", "递等式","画图-作高题", "竖式题", "画图-圈选题"};
    public static final String[] rSettings = {"水平","竖直","宽","高"};

    public String token = "";
    private ArrayList<Integer> qids;
    private ArrayList<Integer> recordedQids;
    private int qid = -1;
    private String qurl = "";
    private String originAnsUrl = "";

    private CanvasView canvasView;
    private AnswerCanvasView answerCanvasView;

    private TextView textTip;
    private TextView textQty;
    private TextView textRecorded;

    private AlertDialog alertDialog;
    private Button btnAnsRec;
    private Button btnAreas;
    private ToggleButton btnBaiduAreas;
    private ToggleButton btnVertEq;
    private boolean useBaiduOcr;

    private ToggleButton btnPointSize;

    private ToggleButton btnDrawH;
    private ToggleButton btnDrawD;
    private Switch btnDashLine;
    private ToggleButton btnLine;
    private ToggleButton btnDrawCircle;
    private CheckBox cbPhoto;

    private LinearLayout layHidBtn;
    private ViewGroup layCharBtn;
    private ViewGroup layText;

    private ImageView qView;
    public static Bitmap questionImg;

    private int qWidth = 0;
    private int qHeight = 0;
    private int qOffsetX = 0;
    private int qOffsetY = 0;

    public static YdMnistClassifierLite ydclassifierlite;
    private static boolean isFirst = true;

    public static QCanvasActivity self;

    private AnswerRecord ansRecord;
    private JSONObject ansObj;
    private int qType = QTYPE_PIC;

    public static final int REQUEST_EXTERNAL_PERMISSION = 12346;

    private String isSelect = "";

    public JSONArray qUrls;
    public int qIndex = 0;
//    public List<TextView> charBtns;
    public List<Button> charBtns;
    public List<TextView> txtViews;
    public TextView inputText;
    public List<TextView> inputTexts;

    private String inputStr;
    private ArrayList<String> solveResults;
    private int solveIndex;

    private VertEqualObject vertObj;
    public static VertEqCreator eq;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_canvas_q);
        this.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bgcolor));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        charBtns = new ArrayList<>();
        txtViews = new ArrayList<>();
        inputTexts = new ArrayList<>();

        canvasView = (CanvasView) findViewById(R.id.canvas_view);
        answerCanvasView = (AnswerCanvasView)findViewById(R.id.answer_canvas_view);
        canvasView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(canvasView.mode == CanvasView.MODE_PEN){

                    }else if(canvasView.mode == CanvasView.MODE_RECT|| canvasView.mode == CanvasView.MODE_BAIDU_RECT || canvasView.mode==CanvasView.MODE_VERT_RECT) {
                        btnAreas.setText("选区");
                        if (inputText == null) {
                            inputText = new TextView(v.getContext());
                            inputText.setPadding(5, 5, 1, 1);
                            inputText.setTextColor(Color.BLACK);
                            inputText.setSingleLine(true);
                            inputText.setTextSize(24);
                        }
                        onInput();
                    }else{

                    }
                }
                return false;
            }
        });
        layHidBtn = (LinearLayout)findViewById(R.id.ly_spread);
        layHidBtn.setVisibility(View.GONE);
        layText = (ViewGroup)findViewById(R.id.ly_canvas);

        layCharBtn = (ViewGroup) findViewById(R.id.ly_char_btns);
        textTip = (TextView) findViewById(R.id.txtMnist);
        textQty = (TextView) findViewById(R.id.txtQtype);
        textRecorded = (TextView)findViewById(R.id.txt_recorded);

        qView = (ImageView) findViewById(R.id.q_view);

        btnAreas = (Button)findViewById(R.id.btn_areas);
        btnDrawH = (ToggleButton)findViewById(R.id.btn_hline);
        btnDrawH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useBaiduOcr){
                    useBaiduOcr = false;
                    btnBaiduAreas.setChecked(false);
                }
                btnDrawD.setChecked(false);
                boolean b = btnDrawH.isChecked();
                if(b){
                    canvasView.mode = CanvasView.MODE_DRAW_H;
                }else{
                    canvasView.mode = CanvasView.MODE_PEN;
                }
            }
        });
        btnDrawD = (ToggleButton)findViewById(R.id.btn_dline);
        btnDrawD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useBaiduOcr){
                    useBaiduOcr = false;
                    btnBaiduAreas.setChecked(false);
                }
                btnDrawH.setChecked(false);
                boolean b = btnDrawD.isChecked();
                if(b){
                    canvasView.mode = CanvasView.MODE_DRAW_D;
                }else{
                    canvasView.mode = CanvasView.MODE_PEN;
                }
            }
        });
        btnDashLine = (Switch)findViewById(R.id.btn_dashline);
        btnDashLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canvasView.isDashLine = isChecked;
            }
        });

        btnLine = (ToggleButton) findViewById(R.id.btn_line);
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useBaiduOcr){
                    useBaiduOcr = false;
                    btnBaiduAreas.setChecked(false);
                }
                boolean b = btnLine.isChecked();
                if(b){
                    canvasView.mode = CanvasView.MODE_LINE;
                }else{
                    canvasView.mode = CanvasView.MODE_PEN;
                }
            }
        });
        btnDrawCircle = (ToggleButton)findViewById(R.id.btn_circle);
        btnDrawCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useBaiduOcr){
                    useBaiduOcr = false;
                    btnBaiduAreas.setChecked(false);
                }
                boolean b = btnDrawCircle.isChecked();
                if(b){
                    canvasView.mode = CanvasView.MODE_DRAW_CIRCLE;
                }else{
                    canvasView.mode = CanvasView.MODE_PEN;
                }
            }
        });

        btnAnsRec = (Button)findViewById(R.id.btn_ans);
        btnPointSize = (ToggleButton)findViewById(R.id.btn_psize);
        btnPointSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    canvasView.setPointSize(30);
                }else{
                    canvasView.setPointSize(20);
                }
            }
        });


        btnVertEq = (ToggleButton) findViewById(R.id.btn_vert_q);
        btnVertEq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                boolean b = btnVertEq.isChecked();
                btnLine.setChecked(false);
                btnDrawH.setChecked(false);
                btnDrawD.setChecked(false);
                btnBaiduAreas.setChecked(false);
                if(b){
                    canvasView.mode = CanvasView.MODE_VERT_RECT;
                }else {
                    canvasView.mode = canvasView.MODE_PEN;
                    btnAreas.setText("选区");
                }
            }
        });
        btnBaiduAreas = (ToggleButton) findViewById(R.id.btn_baidu_area);
        btnBaiduAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                boolean b = btnBaiduAreas.isChecked();
                useBaiduOcr = b;
                btnLine.setChecked(false);
                btnDrawH.setChecked(false);
                btnDrawD.setChecked(false);
                btnVertEq.setChecked(false);
                if(b){
                    canvasView.mode = CanvasView.MODE_BAIDU_RECT;
                }else {
                    canvasView.mode = canvasView.MODE_PEN;
                    btnAreas.setText("选区");
                }
            }
        });

        btnAnsRec.setLongClickable(true);
        btnAnsRec.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(self);
                builder.setTitle("记录答案");// 设置标题
                // builder.setIcon(R.drawable.ic_launcher);//设置图标
                builder.setMessage("如本机已存有答案数据则会被覆盖");// 为对话框设置内容
                builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        return;
                    }
                });
                builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        onCommitAnswer(null);
                        return;
                    }
                });
                builder.create().show();
                return false;
            }
        });
        cbPhoto = (CheckBox)findViewById(R.id.cb_takephoto);
        cbPhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(ansRecord!=null){
                        ansRecord.isPhto = isChecked;
                    }
            }
        });

        ydclassifierlite = new YdMnistClassifierLite(getAssets());

        requestPermissions(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        BaiduTTS.init(this.getApplicationContext());

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                Log.i("mnist", " onViewInitFinished is " + arg0);
            }
            @Override
            public void onCoreInitFinished() {
                Log.i("mnist", " onCoreInitFinished is " );
            }
        };
        QbSdk.initX5Environment(getApplicationContext(), cb);
        CanvasPaints.initPaints(self);

        eq = new VertEqCreator(this.getApplicationContext());
    }

    private void initAnswerRecord(){
        setSize();
        ansRecord = new AnswerRecord();
        ansRecord.qtype = qType;
        ansRecord.wid = qWidth;
        ansRecord.hei = qHeight;
        ansRecord.offsetX = qOffsetX;
        ansRecord.offsetY = qOffsetY;
        ansRecord.qid = qid;
        ansRecord.url = qurl;
        ansRecord.canvasHei = canvasView.getHeight();
        ansRecord.canvasWid = canvasView.getWidth();
        ansRecord.isPhto = cbPhoto.isChecked();
    }

    private void setSize(){
        Matrix mat = qView.getImageMatrix();
        float[] values = new float[9];
        mat.getValues(values);
        qOffsetX = (int)values[2];
        qOffsetY = (int)values[5];
        if(qView.getDrawable() == null){
            return ;
        }

        int dw = qView.getDrawable().getBounds().width();
        int dh = qView.getDrawable().getBounds().height();
        float sx = values[0];
        float sy = values[4];
        qWidth = (int)(dw * sx );
        qHeight = (int)(dh * sy);
    }

    public void onClean(View view) {
        canvasView.clean();
        answerCanvasView.clean();
        textTip.setText(null);
        ansObj = null;
        inputText = null;
        layCharBtn.removeAllViews();
        layText.removeAllViews();
        charBtns = new ArrayList<>();
        inputTexts = new ArrayList<>();
        inputStr = null;
        initAnswerRecord();
    }

    public void onBack(View view){
        if(!canvasView.isEmpty()){
            if(qType == QTYPE_LINE){
                if(canvasView.linePaths!=null && canvasView.linePaths.size()>0){
                    canvasView.linePaths.remove(canvasView.linePaths.size()-1);
                    canvasView.lineRects.remove(canvasView.lineRects.size()-1);
                    canvasView.lineRects.remove(canvasView.lineRects.size()-1);
                    if(canvasView.linePaths.size()==0){
                        canvasView.linePaths = null;
                        canvasView.lineRects = null;
                    }
                    canvasView.adjustPoint = null;
                }else if(canvasView.path!=null || canvasView.rect!=null){
                    canvasView.clean();
                }
                canvasView.invalidate();
            }else if(qType == QTYPE_PIC){
                canvasView.clean();
            }else if(qType == QTYPE_DRAW_H){
                canvasView.clean();
            }else if(qType == QTYPE_EQUALS || qType==QTYPE_SOLVE){
                canvasView.clean();
                Button removedBtn = charBtns.remove(charBtns.size() - 1);
                ((ViewGroup) removedBtn.getParent()).removeView(removedBtn);
                String ansStr = "";
                for (AnswerObject obj : ansRecord.objs) {
                    ansStr += obj.solves.get(obj.solveAnswer) + ",";
                }
                textTip.setText(ansStr);
                return;
            }else if(qType == QTYPE_VERT_EQUAL){
                canvasView.clean();

                if(inputText!=null && inputText.getParent()!=null){
                    ((ViewGroup)inputText.getParent()).removeView(inputText);
                    textTip.setText("");
                    return;
                }
                if(vertObj!=null && charBtns!=null && charBtns.size()>0){
                    Button removedBtn = charBtns.remove(charBtns.size() - 1);
                    if(removedBtn.getParent()!=null){
                        ((ViewGroup) removedBtn.getParent()).removeView(removedBtn);
                    }
                }
                String ansStr = "";
                for (AnswerObject obj : ansRecord.objs) {
                    if(obj.vobj!=null){
                        ansStr += obj.vobj.answer + ",";
                    }
                }
                vertObj = null;
                textTip.setText(ansStr);
                return;
            }
        }else{
            if(ansRecord==null || ansRecord.ops==null || ansRecord.ops.size()==0){
                return;
            }
            int op = ansRecord.ops.remove(ansRecord.ops.size()-1);
            if(op == QTYPE_PIC){
                if(ansRecord.objs.size() > 0){
                    AnswerObject obj = ansRecord.objs.remove(ansRecord.objs.size()-1);
                    ansRecord.rans.remove(ansRecord.rans.length()-1);
                    for(View v :obj.viewGroups){
                        if(v.getParent()!=null){
                            ((ViewGroup)v.getParent()).removeView(v);
                        }
                    }
                }
                answerCanvasView.drawAnswer(ansRecord);
                answerCanvasView.drawAnswerLine(ansRecord);
                answerCanvasView.drawAnswerDrawH(ansRecord);
                answerCanvasView.invalidate();
                createCharBtn();
            }else if(op == QTYPE_LINE){
                if(ansRecord.ansPaths!=null || ansRecord.ansPaths.size()>0){
                    ArrayList<Rect> rs = ansRecord.ansRects.remove(ansRecord.ansRects.size()-1);
                    ArrayList<Path> ps = ansRecord.ansPaths.remove(ansRecord.ansPaths.size()-1);
                    rs.remove(rs.size()-1);
                    rs.remove(rs.size()-1);
                    ps.remove(ps.size()-1);
                    ansRecord.groupRects.remove(ansRecord.groupRects.size()-1);
                    canvasView.linePaths = ps;
                    canvasView.lineRects = rs;
                    canvasView.invalidate();
                }
                answerCanvasView.drawAnswer(ansRecord);
                answerCanvasView.drawAnswerLine(ansRecord);
                answerCanvasView.invalidate();
                createCharBtn();
            }else if(op==QTYPE_DRAW_H){
                if(ansRecord!=null && ansRecord.ansPoints!=null && ansRecord.ansPoints.size()>0){
                    ansRecord.ansPoints.remove(ansRecord.ansPoints.size()-1);
                    ansRecord.lineObjects.remove(ansRecord.lineObjects.size()-1);
                }
                answerCanvasView.drawAnswer(ansRecord);
                answerCanvasView.drawAnswerDrawH(ansRecord);
                answerCanvasView.invalidate();
                createCharBtn();

            }else if(op == QTYPE_EQUALS || op==QTYPE_SOLVE){
                if(ansRecord.objs!=null && ansRecord.objs.size()>0){
                    ansRecord.objs.remove(ansRecord.objs.size()-1);
                    Button removedBtn = charBtns.remove(charBtns.size()-1);
                    ((ViewGroup)removedBtn.getParent()).removeView(removedBtn);
                    String ansStr = "";
                    for(AnswerObject obj:ansRecord.objs){
                        ansStr += obj.solves.get(obj.solveAnswer) + ",";
                    }
                    textTip.setText(ansStr);
                    answerCanvasView.drawAnswerSolveEqual(ansRecord);
                    createCharBtnSolve(true);
                    answerCanvasView.invalidate();
                    return;
                }
            }else if(op == QTYPE_VERT_EQUAL ){
                if(ansRecord.objs!=null && ansRecord.objs.size()>0){
                    AnswerObject robj = ansRecord.objs.remove(ansRecord.objs.size()-1);
                    for(View v :robj.viewGroups){
                        if(v.getParent()!=null){
                            ((ViewGroup)v.getParent()).removeView(v);
                        }
                    }
                    Button removedBtn = charBtns.remove(charBtns.size()-1);
                    ((ViewGroup)removedBtn.getParent()).removeView(removedBtn);
                    String ansStr = "";
                    for (AnswerObject obj : ansRecord.objs) {
                        if(obj.vobj!=null){
                            ansStr += obj.vobj.answer + ",";
                        }
                    }
                    textTip.setText(ansStr);
                    answerCanvasView.drawAnswer(ansRecord);
                    createChatBtnVertEq(false);
                    answerCanvasView.invalidate();
                    return;
                }
            }

        }
        if(inputText!=null && inputText.getParent()!=null){
            ((ViewGroup)inputText.getParent()).removeView(inputText);
        }
        textTip.setText("");
    }

    public void onGroup(View view){
        if(ansRecord == null){
            initAnswerRecord();
        }
        AnswerObject obj = null;
        int tmpQtype = qType;
        if((tmpQtype==QTYPE_LINE||tmpQtype==QTYPE_DRAW_H )
                && (canvasView.mode==CanvasView.MODE_PEN || canvasView.mode ==CanvasView.MODE_RECT
                || canvasView.mode==CanvasView.MODE_BAIDU_RECT)){
            tmpQtype = QTYPE_PIC;
        }
        if(tmpQtype==QTYPE_VERT_EQUAL && canvasView.mode!=CanvasView.MODE_VERT_RECT){
            tmpQtype = QTYPE_PIC;
        }

        if(tmpQtype == QTYPE_PIC){
            if((inputStr == null || inputStr.length()==0) && canvasView.isEmpty()){
                return;
            }
            if((inputStr == null || inputStr.length()==0) && !canvasView.isEmpty()){
//                Log.i("mnist", inputStr);
                Log.i("mnist", canvasView.getPoints().toString());
                Hashtable resTable = GlobMethods.mnist(this, ydclassifierlite, canvasView.getPoints());
                String res = (String)resTable.get("res");
                List<PathObject> pobjs = (List<PathObject>)resTable.get("list");
                if(canvasView.path==null){
                    obj = new AnswerObject(new Rect(canvasView.rect), new Path(), res);
                }else{
                    obj = new AnswerObject(new Rect(canvasView.rect), new Path(canvasView.path), res);
                }
                JSONArray pointsArr = new JSONArray();
                for(PathObject p : pobjs){
                    JSONArray jarr = new JSONArray();
                    for(JSONArray a : p.points){
                        jarr.put(a);
                    }
                    pointsArr.put(jarr);
                }
                obj.points = pointsArr; //canvasView.getPoints();
            }else{
                obj = new AnswerObject(new Rect(canvasView.rect), new Path(), inputStr);
                obj.viewGroups.add(inputText);
                inputText = null;
            }
            if(useBaiduOcr){
                obj.mnistMeth = "bd";
            }else{
                obj.mnistMeth = "yd";
            }
            obj.qtype = QTYPE_PIC;
            ansRecord.objs.add(obj);
            ansRecord.rans.put(obj.result);

//            answerCanvasView.drawAnswer(ansRecord);
            createCharBtn();
        }else if(tmpQtype==QTYPE_LINE){
            if(canvasView.lineRects == null || canvasView.lineRects.size()==0){
                return;
            }
            boolean ismult = false;
            for(int i=0; i<canvasView.lineRects.size()-1; i++){
                Rect r = canvasView.lineRects.get(i);
                for(int j=i+1; j<canvasView.lineRects.size(); j++){
                    Rect r2 = canvasView.lineRects.get(j);
                    if(r.top == r2.top && r.left == r2.left){
                        ismult = true;
                        break;
                    }
                }
                if(ismult){
                    break;
                }
            }
            ansRecord.mainQString = "mult";
            ArrayList<Rect> rects = new ArrayList<>();
            ArrayList<Path> paths = new ArrayList<>();

            int minx = 100000;
            int miny = 100000;
            int maxx = -1;
            int maxy = -1;
            for(int i=0; i<canvasView.lineRects.size(); i++){
                Rect r = canvasView.lineRects.get(i);
                if(r.top<=0 && r.left<=0){
                    r = new Rect(0,0,0,0);
                    rects.add(new Rect(r));
                    continue;
                }
                rects.add(new Rect(r));
                minx = Math.min(r.left, minx);
                miny = Math.min(r.top, miny);
                maxx = Math.max(r.right, maxx);
                maxy = Math.max(r.bottom, maxy);
            }
            for(int i=0; i<canvasView.linePaths.size(); i++){
                paths.add(new Path(canvasView.linePaths.get(i)));
            }
            ansRecord.ansRects.add(rects);
            ansRecord.ansPaths.add(paths);
            minx -= 20;
            miny -= 20;
            maxx += 20;
            maxy += 20;
            Rect r = new Rect(minx,miny,maxx,maxy);
            ansRecord.groupRects.add(r);
        }else if(tmpQtype == QTYPE_DRAW_H){
            if(canvasView.lineStartPoint==null || canvasView.lineEndPoint==null){
                return;
            }
            ArrayList<Point> pts = new ArrayList<>();
            pts.add(canvasView.lineStartPoint);
            pts.add(canvasView.lineEndPoint);
            if(canvasView.lineStartPoint2!=null && canvasView.lineEndPoint2!=null){
                pts.add(canvasView.lineStartPoint2);
                pts.add(canvasView.lineEndPoint2);
            }
            ansRecord.ansPoints.add(pts);
            ansRecord.lineObjects.add(canvasView.lineObjects);
            ansRecord.dash.add(canvasView.isDashLine);
        }else if(tmpQtype == QTYPE_SOLVE || tmpQtype == QTYPE_EQUALS){
            if(canvasView.rect != null ){
                obj = new AnswerObject(canvasView.rect.left,canvasView.rect.top,canvasView.rect.right,canvasView.rect.bottom,
                        solveResults,solveIndex);
                obj.qtype = qType;
                ansRecord.objs.add(obj);
                solveResults = null;
                solveIndex = -1;
            }
            createCharBtnSolve(false);
            String ansStr = "";
            for(AnswerObject ao:ansRecord.objs){
                ansStr += ao.solves.get(ao.solveAnswer) + ",";
            }
            textTip.setText(ansStr);
        }else if(tmpQtype == QTYPE_VERT_EQUAL){
            if(vertObj == null){
                return;
            }
            if(canvasView.rect != null ){
                obj = new AnswerObject(canvasView.rect.left,canvasView.rect.top,canvasView.rect.right,canvasView.rect.bottom,
                        vertObj);
                obj.qtype = qType;
                ansRecord.objs.add(obj);
                vertObj = null;
            }
//            String ansStr = "";
//            for(AnswerObject ao:ansRecord.objs){
//                if(ao.vobj!=null){
//                    ansStr += ao.vobj.answer + ",";
//                }
//            }
//            textTip.setText(ansStr);
            createChatBtnVertEq(false);
        }
        ansRecord.ops.add(tmpQtype);

        answerCanvasView.drawAnswer(ansRecord);
        answerCanvasView.drawAnswerDrawH(ansRecord);
        answerCanvasView.drawAnswerLine(ansRecord);
//        answerCanvasView.drawAnswerSolveEqual(ansRecord);

        answerCanvasView.invalidate();
        canvasView.clean();
    }


    private void createCharBtn(){
//        textTip.setText("");
        try{
            layCharBtn.removeAllViews();
            if(charBtns!=null) {
                for(Button b:charBtns){
                    if(b.getParent()!=null){
                        ((ViewGroup)b.getParent()).removeView(b);
                    }
                }
            }
            charBtns = new ArrayList<>();
            for(int i=0; i<ansRecord.objs.size(); i++){
                if(ansRecord.objs.get(i).qtype==QTYPE_SOLVE
                        ||ansRecord.objs.get(i).qtype==QTYPE_EQUALS
                       // || ansRecord.objs.get(i).qtype == QTYPE_VERT_EQUAL
                ){
                    continue;
                }
                String str = ansRecord.objs.get(i).result;
                final Boolean isVert = ansRecord.objs.get(i).qtype == QTYPE_VERT_EQUAL;
                if(isVert && ansRecord.objs.get(i).vobj!=null){
                    VertEqualObject obj = ansRecord.objs.get(i).vobj;
                    if(obj.remainder!=null && !obj.remainder.equals("0")){
                        str=obj.answer+"..."+obj.remainder;
                    }else{
                        str=obj.answer+"";
                    }
                    if(ansRecord.objs.get(i).viewGroups.size()==0){
                        TextView t = new TextView(this);
                        t.setPadding(5, 5, 1, 1);
                        t.setTextColor(Color.BLACK);
                        t.setSingleLine(true);
                        t.setTextSize(24);
                        t.setText(str + "    ");
                        t.requestLayout();
                        layText.addView(t);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) t.getLayoutParams();
                        params.height = canvasView.rect.bottom - canvasView.rect.top;
                        params.width = canvasView.rect.right - canvasView.rect.left;
                        params.setMargins(canvasView.rect.left, canvasView.rect.top, canvasView.rect.right, canvasView.rect.bottom);
                        t.setLayoutParams(params);
                        t.requestLayout();
                        ansRecord.objs.get(i).viewGroups.add(t);
                    }
                }
                final Button charbtn = new Button(this);
                charbtn.setPadding(1,1,1,1);
                charbtn.setTextSize(16);
                charbtn.setText(str);
                charbtn.setSingleLine(true);
                charbtn.setIncludeFontPadding(false);
                float fontScale = this.getResources().getDisplayMetrics().scaledDensity;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);

                charbtn.setLayoutParams(params);
                layCharBtn.addView(charbtn);
                Space spa = new Space(this);
                spa.setLayoutParams(new LinearLayout.LayoutParams(5, textTip.getMeasuredHeight()));
                layCharBtn.addView(spa);

                charbtn.setId(89000+i);
                charbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int k = (int)charbtn.getId() - 89000;
                        try{
                            if (isVert) {
                                onShowVerEq(k);
                            }else{
                                checkSpecialChar(ansRecord.objs.get(k).result, k);
                            }

                        }catch (Exception err){
                            err.printStackTrace();
                        }
                    }
                });
                charBtns.add(charbtn);
            }
        }catch (Exception err){
            err.printStackTrace();
        }
    }

    private void createCharBtnSolve(boolean ischeck){
        if(charBtns!=null) {
            for(Button b:charBtns){
                if(b.getParent()!=null){
                    layText.removeView(b);
                }
            }
        }
        charBtns = new ArrayList<>();
        for(int i=0; i<ansRecord.objs.size(); i++){
            if(ansRecord.objs.get(i).qtype==QTYPE_PIC || ansRecord.objs.get(i).qtype == QTYPE_VERT_EQUAL){
                continue;
            }
            AnswerObject obj = ansRecord.objs.get(i);
            final Button charbtn = new Button(this);
            charbtn.setPadding(0,0,0,0);
            charbtn.setTextSize(16);
            if(qType == QTYPE_SOLVE){
                if(ischeck){
                    charbtn.setText("查看");
                }else{
                    charbtn.setText("编辑");
                }
            }else if(qType == QTYPE_EQUALS){
                charbtn.setText(obj.solves.get(obj.solves.size()-1));
            }
            charbtn.setSingleLine(true);
            charbtn.setIncludeFontPadding(false);
            charbtn.setBackgroundColor(Color.BLACK);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 100);
            params.setMargins(obj.rect.left ,obj.rect.top, obj.rect.left+250, obj.rect.bottom);

            layText.addView(charbtn, params);
            charBtns.add(charbtn);

            charbtn.setLayoutParams(params);
            charbtn.setId(89000+i);
            charbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int k = (int)charbtn.getId() - 89000;
                    AnswerObject tmpObj = ansRecord.objs.get(k);
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(), OcrResultActivity.class);
                    intent.putExtra("bdocr", true);
                    intent.putStringArrayListExtra("result", tmpObj.solves);
                    intent.putExtra("final", tmpObj.solveAnswer);
                    Log.i("mnist", "onclick edit:"+k);
                    intent.putExtra("edit", k);
                    startActivityForResult(intent, 104);
                }
            });
        }
    }

    private void createChatBtnVertEq(final boolean showEq){
        createCharBtn();
        if(true){
            return;
        }
        if(charBtns!=null) {
            for(Button b:charBtns){
                if(b.getParent()!=null){
                    layText.removeView(b);
                }
            }
        }
        charBtns = new ArrayList<>();
        for(int i=0; i<ansRecord.objs.size(); i++){
            if(ansRecord.objs.get(i).qtype==QTYPE_PIC){
                continue;
            }
            AnswerObject obj = ansRecord.objs.get(i);
            if(obj.vobj==null){
                continue;
            }
            final Button charbtn = new Button(this);
            charbtn.setPadding(0,0,0,0);
            charbtn.setTextSize(12);
            if(showEq){
                String showT = "";
                for(String s:obj.vobj.eqs){
                    s = s.replace(" ", "  ");
                    s = s.replace("口", "  ");
                    s = s.replace("×","x");
                    showT+= s +"\n";
                }
                charbtn.setText(showT);
                charbtn.setSingleLine(false);
            }else{
                if(obj.vobj.remainder!=null && !obj.vobj.remainder.equals("0")){
                    charbtn.setText(obj.vobj.answer+"..."+obj.vobj.remainder);
                }else{
                    charbtn.setText(obj.vobj.answer+"");
                }
//                charbtn.setText(obj.vobj.eqs.get(obj.vobj.eqs.size()-1));
                charbtn.setSingleLine(true);
            }

            charbtn.setIncludeFontPadding(false);
            charbtn.setTextColor(Color.WHITE);
            charbtn.setBackgroundColor(Color.BLACK);
            int tmp_h = 100;
            if(showEq){
                tmp_h = obj.vobj.eqs.size()*50;
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, tmp_h);
            params.setMargins(obj.rect.left ,obj.rect.top, obj.rect.left+300, obj.rect.top+100);
            layText.addView(charbtn, params);
            charBtns.add(charbtn);

            charbtn.setLayoutParams(params);
            charbtn.setId(89000+i);
            charbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int k = (int)charbtn.getId() - 89000;
                    if(showEq){
                        onInputVerEq(k, charbtn);
                    }else{
                        onShowVerEq(k);
                    }

                }
            });
        }
    }

    public void onCommitAnswer(View view){
        if(qType == QTYPE_LINE && !canvasView.isEmpty()){
            onGroup(null);
        }
        if(ansRecord == null){
            return ;
        }

//        try {
//            View v = qView;
//            Bitmap b = Bitmap.createBitmap( qView.getMeasuredWidth(), qView.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
//            Canvas c = new Canvas(b);
//            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//            v.draw(c);
//            answerCanvasView.draw(c);
////            canvasView.draw(c);
//
//            String sdCardDir0 = Environment.getExternalStorageDirectory().toString() + "/shenbi/";
//            File dirF0 = new File(sdCardDir0);
//            if(!dirF0.exists()){
//                dirF0.mkdirs();
//                textRecorded.setText("已录:0");
//                return;
//            }
//
//            OutputStream os = new FileOutputStream(sdCardDir0+"test.jpg");
//            b.compress(Bitmap.CompressFormat.JPEG, 100, os);
//            os.flush();
//            os.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if(true){
//            return;
//        }

        ansRecord.isPhto = cbPhoto.isChecked();
        ansObj = ansRecord.getJsonObj();

        Log.i("mnist", ansObj.toString());
        /*if(true){
            return;
        }*/
        boolean uploadResult = false;
        if(qid!=-1){
            uploadResult = MnistSeviceConnection.upload(token, ansRecord.qid, ansObj.toString());
        }

        try{
            String sdCardDir = Environment.getExternalStorageDirectory().toString() + "/shenbi/";
            File dirF = new File(sdCardDir);
            if(!dirF.exists()){
//                Log.i("mnist", dir);
                dirF.mkdirs();
            }
            File saveFile = new File(sdCardDir, qid+".txt");
            if(saveFile.exists()){
                saveFile.delete();
                saveFile.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(saveFile);
            outStream.write(ansObj.toString().getBytes());
            outStream.close();
//            canvasView.clean();
            if(!uploadResult){
                textTip.setText(qid+"上传答案失败");

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("警告");
                alertBuilder.setMessage(qid+"上传答案失败");
                alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog = alertBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);

                alertDialog.show();

            }else{
                textTip.setText(qid+"记录答案完毕");

                if(recordedQids==null){
                    recordedQids = new ArrayList<>();
                }
                for(int i=0; i<recordedQids.size(); i++){
                    if(recordedQids.get(i) == qid){
                        recordedQids.remove(i);
                        recordedQids.add(qid);
                    }
                }

            }
        }catch (Exception err){
            Log.i("mnist", err.getStackTrace().toString());
        }

        checkRecorded(qids, false);
    }

    public void onAreaMode(View view){
        if(useBaiduOcr){
            useBaiduOcr = false;
            btnBaiduAreas.setChecked(false);
        }
        btnVertEq.setChecked(false);
        btnLine.setChecked(false);
        btnDrawH.setChecked(false);
        btnDrawD.setChecked(false);

        if(canvasView.mode == canvasView.MODE_PEN){
            canvasView.mode = CanvasView.MODE_RECT;
            btnAreas.setText("写字");
        }
        else if(canvasView.mode != canvasView.MODE_PEN && canvasView.mode!=canvasView.MODE_RECT){
            canvasView.mode = CanvasView.MODE_RECT;
            btnAreas.setText("写字");
        }else if(canvasView.mode == canvasView.MODE_RECT){
            canvasView.mode = canvasView.MODE_PEN;
            btnAreas.setText("选区");
        }
    }

    public void onWebSelectQ(View view){
        Intent intent = new Intent(self, WebViewActivity.class);
        if(isSelect.length() > 0){
            intent.putExtra(WebViewActivity.EXTRA_URL, isSelect);
        }else{
            intent.putExtra(WebViewActivity.EXTRA_URL, "");
        }
        self.startActivity(intent);
    }

    public void onAppTest(View view){
        Intent intent = new Intent(self, WebViewActivity.class);
        String u = "";
        Log.i("mnist", u);
        intent.putExtra(WebViewActivity.EXTRA_URL, u);
        self.startActivity(intent);
    }

    public void onQTypeSelect(View view){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选择题型");

        alertBuilder.setSingleChoiceItems(qTypes, qType-1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                qType = i+1;
                textQty.setText(qTypes[i]);
                alertDialog.dismiss();
                setQtype();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void setQtype(){
        useBaiduOcr = false;
        if(qType == QTYPE_LINE) {
            btnLine.setVisibility(View.VISIBLE);
            btnLine.setChecked(false);
            btnDrawH.setVisibility(View.GONE);
            btnDrawD.setVisibility(View.GONE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnDrawCircle.setVisibility(View.GONE);
            btnDrawCircle.setChecked(false);
            btnDashLine.setVisibility(View.GONE);
            btnVertEq.setVisibility(View.GONE);
            btnVertEq.setChecked(false);
            Button btnGroup = findViewById(R.id.btn_group);
            btnGroup.setText("下一组");
            Button btnInput = findViewById(R.id.btn_input);
            btnInput.setVisibility(View.GONE);
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.GONE);
        }else if(qType == QTYPE_DRAW_H){
            btnDrawH.setVisibility(View.VISIBLE);
            btnDrawD.setVisibility(View.VISIBLE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnDashLine.setVisibility(View.VISIBLE);
            btnLine.setVisibility(View.GONE);
            btnLine.setChecked(false);
            btnDrawCircle.setVisibility(View.GONE);
            btnDrawCircle.setChecked(false);
            btnVertEq.setVisibility(View.GONE);
            btnVertEq.setChecked(false);
            Button btnInput = findViewById(R.id.btn_input);
            btnInput.setVisibility(View.GONE);
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.GONE);
        }else if(qType==QTYPE_SOLVE || qType==QTYPE_EQUALS){
            btnDrawCircle.setVisibility(View.GONE);
            btnDrawCircle.setChecked(false);
            btnDrawH.setVisibility(View.GONE);
            btnDrawD.setVisibility(View.GONE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnDashLine.setVisibility(View.GONE);
            btnLine.setVisibility(View.GONE);
            btnLine.setChecked(false);
            btnVertEq.setVisibility(View.GONE);
            btnVertEq.setChecked(false);
            Button btnInput = findViewById(R.id.btn_input);
            btnInput.setVisibility(View.GONE);
            Button btnGroup = findViewById(R.id.btn_group);
            btnGroup.setText("下一空");
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.GONE);
        }
        else if(qType==QTYPE_PIC){
            btnAreas.setVisibility(View.VISIBLE);
            btnDrawCircle.setVisibility(View.GONE);
            btnDrawCircle.setChecked(false);
            btnLine.setVisibility(View.GONE);
            btnLine.setChecked(false);
            btnDrawH.setVisibility(View.GONE);
            btnDrawD.setVisibility(View.GONE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnVertEq.setVisibility(View.GONE);
            btnVertEq.setChecked(false);
            Button btnGroup = findViewById(R.id.btn_group);
            btnGroup.setText("下一空");
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.VISIBLE);
            btnBaiduAreas.setVisibility(View.VISIBLE);
        }else if(qType == QTYPE_VERT_EQUAL){
            btnAreas.setVisibility(View.VISIBLE);
            btnLine.setVisibility(View.GONE);
            btnLine.setChecked(false);
            btnDrawCircle.setVisibility(View.GONE);
            btnDrawCircle.setChecked(false);
            btnDrawH.setVisibility(View.GONE);
            btnDrawD.setVisibility(View.GONE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnVertEq.setVisibility(View.VISIBLE);
            btnVertEq.setChecked(false);
            btnBaiduAreas.setVisibility(View.VISIBLE);
            btnBaiduAreas.setChecked(false);
            Button btnGroup = findViewById(R.id.btn_group);
            btnGroup.setText("下一空");
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.VISIBLE);
        }else if(qType == QTYPE_DRAW_CIRCLE){
            btnAreas.setVisibility(View.VISIBLE);
            btnDrawCircle.setVisibility(View.VISIBLE);
            btnLine.setVisibility(View.GONE);
            btnLine.setChecked(false);
            btnDrawH.setVisibility(View.GONE);
            btnDrawD.setVisibility(View.GONE);
            btnDrawH.setChecked(false);
            btnDrawD.setChecked(false);
            btnVertEq.setVisibility(View.GONE);
            btnVertEq.setChecked(false);
            btnBaiduAreas.setVisibility(View.GONE);
            Button btnGroup = findViewById(R.id.btn_group);
            btnGroup.setText("下一空");
            Button btnRectSet = findViewById(R.id.btn_rectsetting);
            btnRectSet.setVisibility(View.VISIBLE);
        }
        onClean(null);
    }


    public void onRectSetting(View view){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选框数值固定");
        alertBuilder.setMultiChoiceItems(rSettings, canvasView.rSettingChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                canvasView.rSettingChecked[i] = b;
                canvasView.setRectSize();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void checkSpecialChar(final String str,  final int index){
        if(!Pattern.matches(spechar, str)){
            return;
        }
        final StringBuilder strBuilder = new StringBuilder(str);
        final List<Object[]> choicesList = new ArrayList<>();
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        for(int i=str.length()-1; i>=0; i--){
            String c1 = String.valueOf(str.charAt(i));
            String c2 = "";
            switch (c1){
                case "-":
                    c2 = "一";
                    break;
                case "一":
                    c2 = "-";
                    break;
                case "+":
                    c2 = "十";
                    break;
                case "十":
                    c2 = "+";
                    break;
                case "=":
                    c2 = "二";
                    break;
                case "二":
                    c2 = "=";
                    break;
                case "D":
                    c2 = "0";
                    break;
                case "0":
                    c2 = "D";
                    break;
                case ">":
                    c2 = "7";
                    break;
                case "7":
                    c2 = ">";
                    break;
                case "∠":
                    c2 = "<";
                    break;
                case "<":
                    c2 ="∠";
                    break;
            }
            if(c2.equals("")){
                continue;
            }
            String[] choices = {c1,c2};
            Object[] datas = {i, c1, c2};
            choicesList.add(datas);
            alertBuilder.setTitle("选择："+str.substring(0,i)+"\""+c1+"\""+str.substring(i+1));
            alertBuilder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            int cindex = (int)(choicesList.get(choicesList.size()-1)[0]);
                            String ctoRpl = (String)(choicesList.get(choicesList.size()-1)[j+1]);
                            strBuilder.replace(cindex, cindex+1, ctoRpl);
//                            Log.i("mnist", strBuilder.toString());
                        }
                    }
            );
            alertBuilder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int j){
                    choicesList.remove(choicesList.size()-1);
                    if(choicesList.size() == 0){
//                    if(!alertDialog.isShowing()){
//                        alertDialog.dismiss();
                        try {
                            String tmpS = strBuilder.toString();
                            ansRecord.rans.put(index, tmpS);
                            int starti = 0;
                            for(int s=0; s<index; s++){
                                starti += ansRecord.rans.getString(s).length();
                            }
                            for(int s=starti; s<starti+tmpS.length(); s++){
                                AnswerObject tmpP = ansRecord.objs.get(s);
                                tmpP.result = String.valueOf(tmpS.charAt(s-starti));
                                ansRecord.objs.set(s, tmpP);
                            }
                            ansObj = ansRecord.getJsonObj();
                            Log.i("mnist", ansObj.toString());
//                            tmpS = ansRecord.rans.getString(index);
                            charBtns.get(index).setText(tmpS);
                        }catch (Exception err){
                            err.printStackTrace();
                        }
                    }
                    return;
                }
            });
            alertDialog = alertBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
        return;
    }

    public void onSpread(View view){
        if(layHidBtn.getVisibility() == View.VISIBLE){
            layHidBtn.setVisibility(View.GONE);
        }else{
            layHidBtn.setVisibility(View.VISIBLE);
        }
    }

    public void onInput(View view){
        Intent intent = new Intent();
        intent.setClass(this, KeyboardTestActivity.class);
        if(canvasView.rect!=null){
            if((float)canvasView.rect.top < (float)qView.getMeasuredHeight()*0.5){
                intent.putExtra("alignbottom", true);
            }else{
                intent.putExtra("alignbottom", false);
            }
        }else{
            textTip.setText("先选框再输入");
            return;
        }
        startActivityForResult(intent, 100);
    }

    public void onInput(){
        Intent intent = new Intent();
        if(qType == QTYPE_SOLVE){
            intent.setClass(this, OcrResultActivity.class);
            intent.putExtra("bdocr", true);
            intent.putExtra("fill", false);
            startActivityForResult(intent, 104);
        }else if(qType == QTYPE_EQUALS){
            intent.setClass(this, OcrResultActivity.class);
            intent.putExtra("bdocr", true);
            intent.putExtra("formula", true);
            intent.putExtra("fill", false);
            startActivityForResult(intent, 104);
        }
        else if(qType == QTYPE_PIC){
            intent.setClass(this, OcrResultActivity.class);
            if(useBaiduOcr){
                intent.putExtra("bdocr", true);
                intent.putExtra("fill", true);
            }else{
                intent.putExtra("bdocr", false);
                intent.putExtra("fill", true);
            }
            startActivityForResult(intent, 103);
        }else if(qType == QTYPE_VERT_EQUAL){
           if(canvasView.mode == CanvasView.MODE_VERT_RECT){
               intent.setClass(this, KeyboardTestActivity.class);
               intent.putExtra("inputtype", KeyboardTestActivity.INPUT_VERTEQ);
               startActivityForResult(intent, 101);
           }else{
               intent.setClass(this, OcrResultActivity.class);
               if(useBaiduOcr){
                   intent.putExtra("bdocr", true);
                   intent.putExtra("fill", true);
               }else{
                   intent.putExtra("bdocr", false);
                   intent.putExtra("fill", true);
               }
               startActivityForResult(intent, 103);
            }
        }
    }

    private void onInput2(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View addv = factory.inflate(R.layout.edittext_alert, null);
        final EditText edit = (EditText)addv.findViewById(R.id.editText);
        edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        if(useBaiduOcr && qType!=QTYPE_PIC){
            edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            edit.setSingleLine(false);
        }else{
            edit.setSingleLine(true);
        }
        edit.setText("");
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
                inputStr = ns;
                alert2.dismiss();

                if(inputText == null){
                    inputText = new TextView(v.getContext());
                    inputText.setPadding(0, 0, 0, 0);
                    inputText.setTextColor(Color.BLACK);
                    inputText.setSingleLine(true);
                    inputText.setTextSize(24);
                    inputText.setIncludeFontPadding(false);
                }
                inputText.setText(inputStr + "   ");
                inputText.requestLayout();
                if(inputText.getParent()==null){
                    layText.addView(inputText);
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)inputText.getLayoutParams();
                params.height = canvasView.rect.bottom-canvasView.rect.top;
                params.width = canvasView.rect.right-canvasView.rect.left;
                params.setMargins(canvasView.rect.left ,canvasView.rect.top, canvasView.rect.right, canvasView.rect.bottom);
                inputText.setLayoutParams(params);
                inputText.requestLayout();
//                onGroup(null);
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

    public void onTTSTest(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View addv = factory.inflate(R.layout.edittext_alert, null);
        final EditText edit = (EditText)addv.findViewById(R.id.editText);
        edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edit.setSingleLine(true);
        edit.setText("");

        final AlertDialog alert2 = new AlertDialog.Builder(this).setTitle("输入").setView(addv)
                .setPositiveButton("确定", null)
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
                BaiduTTS.speak(ns);
                alert2.dismiss();

            }
        });
    }

    public void onInputVerEq(final int index, final Button charbtn){
        final VertEqualObject verteq;
        if(index < ansRecord.objs.size()){
            verteq = ansRecord.objs.get(index).vobj;
        }else{
            verteq = vertObj;
        }
        if(verteq == null){
            return;
        }

        LayoutInflater factory = LayoutInflater.from(this);
        final View addv = factory.inflate(R.layout.edittext_verteq2, null);
        final EditText edit = (EditText)addv.findViewById(R.id.editText_verteq2);
        edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edit.setSingleLine(false);
//        edit.setShowSoftInputOnFocus(false);

        KeyboardView keyboardView = (KeyboardView)addv.findViewById(R.id.verteq_keyboard2);
        Keyboard keyboard = new Keyboard(this, R.xml.keyboard_verteq2);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setVisibility(View.VISIBLE);

        KeyboardView.OnKeyboardActionListener lstner = new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {
            }
            @Override
            public void onRelease(int primaryCode) {
            }
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                int selected = edit.getSelectionStart();
                Editable editingText = edit.getText();
                Log.i("mnist", "selected+"+selected+ "   "+String.valueOf(editingText.charAt(selected)));
                switch(primaryCode){
                    case Keyboard.KEYCODE_CANCEL://回退
                        if(selected == 0){
                            break;
                        }
                      /*  if(String.valueOf(editingText.charAt(selected-1)).equals("\n")){
                            break;
                        }*/
                        editingText = editingText.delete(selected-1,selected);
                        edit.setText(editingText);
                        edit.setSelection(selected-1);
                        break;
                    case 65532: // 换行
                        editingText.insert(selected, "\n");
                        edit.setSelection(selected+1);
                        break;
                    case 65533: // _
                        editingText.insert(selected, "_");
                        edit.setSelection(selected+1);
                        break;
                    case 65534: // /
                        editingText.insert(selected, "/");
                        edit.setSelection(selected+1);
                        break;
                    case 65535: // 空格
                        editingText.insert(selected, "口");
                        edit.setSelection(selected+1);
                        break;
                    default:
                        editingText.insert(selected, Character.toString((char) primaryCode));
                        edit.setSelection(selected+1);
                        break;
                }
            }
            @Override
            public void onText(CharSequence text) {
            }
            @Override
            public void swipeLeft() {
            }
            @Override
            public void swipeRight() {
            }
            @Override
            public void swipeDown() {
            }
            @Override
            public void swipeUp() {
            }
        };
        keyboardView.setOnKeyboardActionListener(lstner);

        String editText = "";
        for(String s : verteq.eqs){
            editText += s + "\n";
        }
        editText = editText.replace("×","x");
        editText = editText.replace(" ","口");
        edit.setText(editText);

        final AlertDialog alert2 = new AlertDialog.Builder(this).setTitle("编辑").setView(addv)
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
                String[] arr = ns.split("\n");
                ArrayList<String> arr2 = new ArrayList<>();
                for(String s : arr){
//                    s = s.replace("口", " ");
                    arr2.add(s);
                }
                int qt = verteq.qtype;
                VertEqualObject verteq2 = new VertEqualObject(qt, arr2);
                if(index < ansRecord.objs.size()){
                    AnswerObject aobj = ansRecord.objs.get(index);
                    aobj.vobj = verteq2;
                    ansRecord.objs.set(index, aobj);
                }else{
                    vertObj = verteq2;
                }

                String showT = "";
                for(String s:verteq2.eqs){
                    s = s.replace("×","x");
                    s = s.replace("口", "  ");
                    showT+= s +"\n";
                }
                charbtn.setText(showT);

                String ansStr = "";
                for(AnswerObject ao:ansRecord.objs){
                    if(ao.vobj!=null){
                        ansStr += ao.vobj.answer;
                    }
                }
                if(vertObj!=null){
                    ansStr+=vertObj.answer;
                }
                textTip.setText(ansStr);
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

    public void onShowVerEq(int index){
        VertEqualObject verteq;
        if(index < ansRecord.objs.size()){
            verteq = ansRecord.objs.get(index).vobj;
        }else{
            verteq = vertObj;
        }
        if(verteq == null){
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, InputVertEqualActivity.class);
        intent.putStringArrayListExtra("eqs", verteq.eqs);
        startActivityForResult(intent, 102);
    }

    public void onShowOriginAnswer(View view){
        if(originAnsUrl==null || originAnsUrl.length() == 0){
            return;
        }
        final Dialog dia = new Dialog(this, R.style.edit_AlertDialog_style);
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

        new Handler().postDelayed(new Runnable(){
            public void run(){
                Bitmap bmp = getNetworkBmp(originAnsUrl);
                if(bmp!=null){
                    imgv.setImageBitmap(bmp);
                }else{
                    textTip.setText("加载图片失败");
                }
                setSize();
            }
        },500);
        dia.show();
    }

    public void onShowRecordedAnswer(View view){
        ansRecord = readAnswerRecord(qid);
        if(ansRecord == null){
            if(ansRecord == null){
                textTip.setText("没有本地储存答案数据");
                return;
            }
        }
        cbPhoto.setChecked(ansRecord.isPhto);
        float scaleX = (float)qWidth * qView.getScaleX() / (float)ansRecord.wid;
        float scaleY = (float)qHeight * qView.getScaleY() / (float)ansRecord.hei;
        int offx = (int)(qOffsetX )- (int)((float)ansRecord.offsetX * scaleX);
        int offy = (int)(qOffsetY ) - (int)((float)ansRecord.offsetY * scaleY);

        for(AnswerObject aobj:ansRecord.objs){
            if(aobj.qtype==QTYPE_PIC){
                TextView t = createTxt(aobj.result, aobj.rect, layText);
                aobj.viewGroups.add(t);
            }else if(aobj.qtype==QTYPE_VERT_EQUAL){
                String s = aobj.vobj.answer;
                if(aobj.vobj.remainder!=null && aobj.vobj.remainder.length()>0){
                    s+="..."+aobj.vobj.remainder;
                }
                TextView t = createTxt(s, aobj.rect, layText);
                aobj.viewGroups.add(t);
            }
        }
        if(qType==QTYPE_SOLVE || qType==QTYPE_EQUALS){
            createCharBtnSolve(true);
        }else{
            createCharBtn();
        }
        answerCanvasView.drawAnswer(ansRecord, scaleX, scaleY, offx, offy);
        answerCanvasView.drawAnswerLine(ansRecord, scaleX, scaleY, offx, offy);
        answerCanvasView.drawAnswerDrawH(ansRecord, scaleX, scaleY, offx, offy);
        answerCanvasView.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100) {
            if (resultCode == 200) {
                Bundle bundle = data.getExtras();
                String input = bundle.getString("input");
                if (canvasView.rect == null) {
                    return;
                }
                if (inputText == null) {
                    inputText = new TextView(this);
                    inputText.setPadding(5, 5, 1, 1);
                    inputText.setTextColor(Color.BLACK);
                    inputText.setSingleLine(true);
                    inputText.setTextSize(24);
                }
                inputText.setText(input + "    ");
                inputText.requestLayout();

                if (inputText.getParent() == null) {
                    layText.addView(inputText);
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inputText.getLayoutParams();
                params.height = canvasView.rect.bottom - canvasView.rect.top;
                params.width = canvasView.rect.right - canvasView.rect.left;
                params.setMargins(canvasView.rect.left, canvasView.rect.top, canvasView.rect.right, canvasView.rect.bottom);
                inputText.setLayoutParams(params);
                inputText.requestLayout();
                inputStr = input;
                textTip.setText(" " + inputStr);
            } else if (resultCode == 400) {
                Log.i("mnist", "input cancel");
            }

        }else if(requestCode == 101) {
            Bundle bundle = data.getExtras();
            String input = bundle.getString("input");
            Intent intent = new Intent();
            intent.setClass(this, InputVertEqualActivity.class);
            intent.putExtra("vertstr", input);
            startActivityForResult(intent, 102);

        }else if(requestCode == 102){
            if(resultCode == 200){
                vertObj = new VertEqualObject(eq.eq);
                if(eq.hasExam){
                    vertObj.eqsExam = eq.eq2;
                }
                final Button charbtn = new Button(this);
                charbtn.setPadding(0,0,0,0);
                charbtn.setTextSize(12);
                if(vertObj.remainder!=null && !vertObj.remainder.equals("0")){
                    charbtn.setText(vertObj.answer+"..."+vertObj.remainder);
                }else{
                    charbtn.setText(vertObj.answer+"");
                }
                charbtn.setTextColor(Color.WHITE);
                charbtn.setSingleLine(true);
                charbtn.setIncludeFontPadding(false);
                charbtn.setBackgroundColor(Color.BLACK);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(400, 100);
                params.setMargins(canvasView.rect.left ,canvasView.rect.top, canvasView.rect.left+300, canvasView.rect.top+100);
                layText.addView(charbtn, params);
                charBtns.add(charbtn);
                charbtn.setLayoutParams(params);
                charbtn.setId(89000+ansRecord.objs.size());
                charbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int k = (int)charbtn.getId() - 89000;
                        onShowVerEq(k);
                    }
                });
                if(charBtns==null){
                    charBtns = new ArrayList<>();
                }
                charBtns.add(charbtn);
            }else if(resultCode == 201){
                eq.clean();
                canvasView.clean();
            }else if(requestCode == 202){

            }

        }else if(requestCode == 103){
            if(resultCode == 200){
                Bundle bundle = data.getExtras();
                String inputPath = bundle.getString("path");
                //补完输入路径
               /* if(canvasView.points.length()>1){
                    canvasView.points = canvasView.points.substring(0, canvasView.points.length() - 1);
                    canvasView.points+=",";
                }
                canvasView.points+= inputPath.substring(1);
                try{
                    JSONArray arr0 = new JSONArray(inputPath);
                    curPathNum = arr0.length();
                    Log.i("mnist", "inputPaht = "+inputPath);
                }catch (Exception err){
                    err.printStackTrace();
                }*/

                if(useBaiduOcr){
                    inputStr = bundle.getString("ocrresult", "");
                }else{
                    inputStr = GlobMethods.mnist(this, ydclassifierlite, inputPath);
                }
                if(canvasView.rect == null){
                    return;
                }
                inputText = createTxt(inputStr, canvasView.rect, layText);
                textTip.setText(" "+inputStr);

            }else if(resultCode == 201){
                onInput(null);
            }else if(resultCode == 202){
                onInput2();
            }else if(resultCode == 203){
                Bundle bundle = data.getExtras();
                inputStr = bundle.getString("multans", "");
                if(canvasView.rect == null){
                    return;
                }
                inputText = createTxt(inputStr, canvasView.rect, layText);
                textTip.setText(" "+inputStr);
            }
        }else if(requestCode == 104){
            if(resultCode == 200){
                Bundle bundle = data.getExtras();
                solveResults = bundle.getStringArrayList("results");
                solveIndex = bundle.getInt("final",-1);
                int editingIndex = bundle.getInt("edit", -1);
                if(solveResults==null || solveResults.size() == 0){
                    return;
                }
                if(ansRecord==null){
                    initAnswerRecord();
                }
                Log.i("mnist", "edit="+editingIndex);
                if(editingIndex!=-1 && editingIndex<ansRecord.objs.size()){
                    AnswerObject obj = ansRecord.objs.get(editingIndex);
                    obj.solves = solveResults;
                    obj.solveAnswer = solveIndex;
                    ansRecord.objs.set(editingIndex, obj);
                }else{
                    final Button charbtn = new Button(this);
                    charbtn.setPadding(0,0,0,0);
                    charbtn.setTextSize(16);
                    if(qType == QTYPE_SOLVE){
                        charbtn.setText("编辑");
                    }else if(qType == QTYPE_EQUALS){
                        charbtn.setText(solveResults.get(solveResults.size()-1));
                    }
                    charbtn.setSingleLine(true);
                    charbtn.setIncludeFontPadding(false);
                    charbtn.setBackgroundColor(Color.BLACK);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 100);
                    params.setMargins(canvasView.rect.left ,canvasView.rect.top, canvasView.rect.left+250, canvasView.rect.bottom);
                    layText.addView(charbtn, params);
                    charBtns.add(charbtn);
                    charbtn.setLayoutParams(params);
                    charbtn.setId(89000+ansRecord.objs.size());
                    charbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int k = (int)charbtn.getId() - 89000;
                            AnswerObject tmpObj = ansRecord.objs.get(k);
                            Intent intent = new Intent();
                            intent.setClass(view.getContext(), OcrResultActivity.class);
                            intent.putExtra("bdocr", true);
                            intent.putStringArrayListExtra("result", tmpObj.solves);
                            intent.putExtra("final", tmpObj.solveAnswer);
                            Log.i("mnist", "onclick edit:"+k);
                            intent.putExtra("edit", k);
                            startActivityForResult(intent, 104);
                        }
                    });
                    if(charBtns==null){
                        charBtns = new ArrayList<>();
                    }
                    charBtns.add(charbtn);
                }
//                createCharBtnSolve(false);
                String ansStr = "";
                for(AnswerObject obj:ansRecord.objs){
                    ansStr += obj.solves.get(obj.solveAnswer) + ",";
                }
                if(editingIndex==-1 && solveResults!=null &&solveIndex!=-1){
                    ansStr += solveResults.get(solveIndex);
                }
                textTip.setText(ansStr);
            }
        }else if(requestCode == 105){
            if(resultCode == 200){
                Bundle bundle = data.getExtras();
                int vqtype = bundle.getInt("veqtype", -1);
                ArrayList<String> arr = bundle.getStringArrayList("veqs");
                if(arr == null || arr.size()==0){
                    return;
                }

                vertObj = new VertEqualObject(vqtype, arr);
                final Button charbtn = new Button(this);
                charbtn.setPadding(0,0,0,0);
                charbtn.setTextSize(12);
                String showT = "";
                for(String s:vertObj.eqs){
//                    s = s.replace(" ", "  ");
                    s = s.replace(" ", "  ");
                    s = s.replace("×","x");
                    s = s.replace("口", "  ");
                    showT+= s +"\n";
                }
                charbtn.setText(showT);
//                charbtn.setTextSize(16);
//                charbtn.setText("编辑");
                charbtn.setSingleLine(false);
                charbtn.setIncludeFontPadding(false);
                charbtn.setBackgroundColor(Color.BLACK);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300, vertObj.eqs.size()*50);
                params.setMargins(canvasView.rect.left ,canvasView.rect.top, canvasView.rect.left+250, canvasView.rect.bottom);
                layText.addView(charbtn, params);
                charBtns.add(charbtn);
                charbtn.setLayoutParams(params);
                charbtn.setId(89000+ansRecord.objs.size());
                charbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int k = (int)charbtn.getId() - 89000;
                        onInputVerEq(k, charbtn);
                    }
                });
                if(charBtns==null){
                    charBtns = new ArrayList<>();
                }
                charBtns.add(charbtn);
            }
        }
    }

    private TextView createTxt(String s, Rect r, ViewGroup parent){
        TextView ret = new TextView(this);
        ret.setPadding(0, 0, 0, 0);
        ret.setTextColor(Color.BLACK);
        ret.setSingleLine(true);
        ret.setTextSize(24);
        ret.setIncludeFontPadding(false);
        ret.setText(s + "   ");
        ret.requestLayout();
        parent.addView(ret);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)ret.getLayoutParams();
        params.height = r.bottom-r.top;
        params.width = r.right-r.left;
        params.setMargins(r.left ,r.top, r.right, r.bottom);
        ret.setLayoutParams(params);
        ret.requestLayout();
        return ret;
    }

    public static AnswerRecord readAnswerRecord(int qid){
//        String sdCardDir =Environment.getExternalStorageDirectory().getAbsolutePath();
        String sdCardDir = Environment.getExternalStorageDirectory().toString() + "/shenbi/";
        File loadFile = new File(sdCardDir, qid+".txt");
        if(!loadFile.exists()){
            return null;
        }
        String ansJson = txt2String(loadFile);
        Log.i("mnist", "ansJson:"+ansJson);
        try{
            JSONObject obj = new JSONObject(ansJson);
            return AnswerRecord.getRecord(obj);
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }


    public static String txt2String(File file){
        Long fileLengthLong = file.length();
        byte[] fileContent = new byte[fileLengthLong.intValue()];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileContent);
            inputStream.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        String string = new String(fileContent);
        return string;
    }

    public void prevQ(View view){
        if(qUrls == null){
            return;
        }
        if(qIndex <= 0){
            textTip.setText("这是该类型第一道题");
            return;
        }
        try {
            qIndex--;
            JSONObject obj = qUrls.getJSONObject(qIndex);
            int qid = obj.getInt("questionId");//Integer.parseInt(obj.getString("questionId"));
            String url = obj.getString("quesUrl");
            originAnsUrl = obj.getString("anwserUrl");           
            setQContent(qid, url);
//            textTip.setText("开始录题");
            inputStr = "";
            textTip.setText("");
        }catch (Exception err){
            err.printStackTrace();
        }
    }

    public void nextQ(View view){
        if(qUrls == null){
            return;
        }
        if(qIndex+1 >= qUrls.length()){
            textTip.setText("这是该类型最后一道题");
            return;
        }
        try{
            qIndex++;
            JSONObject obj = qUrls.getJSONObject(qIndex);
            int qid = obj.getInt("questionId");//Integer.parseInt(obj.getString("questionId"));
            String url = obj.getString("quesUrl");
            originAnsUrl = obj.getString("anwserUrl");            
            setQContent(qid, url);
//            textTip.setText("开始录题");
            inputStr = "";
            textTip.setText("");
        }catch (Exception err){
            err.printStackTrace();
        }
    }

    public boolean setQContent(final int q, final String url){
        if(WebViewActivity.self!=null && WebViewActivity.self.webView!=null){
            isSelect = WebViewActivity.self.webView.getUrl();
            WebViewActivity.self.finish();
        }
        onClean(null);
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Bitmap bmp = getNetworkBmp(url);
                if(bmp!=null){
                    Log.i("mnist", "load qid = "+q);
                    qid = q;
                    qurl = url;
                    qView.setImageBitmap(bmp);
                    ansRecord = readAnswerRecord(qid);
                    questionImg = bmp;
                    Button btnConf = (Button)findViewById(R.id.btnConfirn);
                    if(ansRecord != null){
                        btnConf.setVisibility(View.VISIBLE);
                        qType = ansRecord.qtype;
//                        btnQType.setText("类型:"+qTypes[qType-1]);
                        textQty.setText(qTypes[qType-1]);
                        setQtype();
                    }else{
                        btnConf.setVisibility(View.GONE);
                    }
                }else{
                    textTip.setText("加载图片失败");
                }
                /*curPathNum = 0;
                pathNumArr = new ArrayList<>();
                ansObj = null;
                txtQInput1.setText("");
                canvasView.clean();*/
                setSize();
            }
        },500);

        return true;
    }

    public boolean setQContent(final int index){
        if(qUrls == null || qUrls.length() < index){
            return false;
        }
        try {
            qids = new ArrayList<>();
            for(int i=0;i<qUrls.length(); i++){
                JSONObject obj = qUrls.getJSONObject(i);
                int qid = obj.getInt("questionId");
                qids.add(qid);
            }
            MnistSeviceConnection.downloadQ(token, qids, this);

            qIndex = index;
            JSONObject obj = qUrls.getJSONObject(index);
            int qid = obj.getInt("questionId");//Integer.parseInt(obj.getString("questionId"));
            String url = obj.getString("quesUrl");
            originAnsUrl = obj.getString("anwserUrl");            
            return setQContent(qid, url);
        }catch (Exception err){
            err.printStackTrace();
            return false;
        }
    }

    public void GetQJsonData(JSONArray arr, boolean isresp){
        recordedQids = new ArrayList<>();
        for(int i=0; i<arr.length(); i++){
            try{
                int tmpQid = arr.getJSONObject(i).getInt("id");
                recordedQids.add(tmpQid);
                String sdCardDir = Environment.getExternalStorageDirectory().toString() + "/shenbi/";
                File dirF = new File(sdCardDir);
                if(!dirF.exists()){
//                Log.i("mnist", dir);
                    dirF.mkdirs();
                }
                File saveFile = new File(sdCardDir, tmpQid+".txt");
                if(saveFile.exists()){
                    continue;
//                    saveFile.delete();
//                    saveFile.createNewFile();
                }
                FileOutputStream outStream = new FileOutputStream(saveFile);
                outStream.write(arr.getJSONObject(i).toString().getBytes());
                outStream.close();
            }catch (Exception err){
                Log.i("mnist", err.getStackTrace().toString());
            }
        }
        checkRecorded(qids, isresp);
    }

    private void checkRecorded(ArrayList<Integer> arr, boolean isresp){
        if(arr == null || arr.size()==0){
            return;
        }
        String sdCardDir = Environment.getExternalStorageDirectory().toString() + "/shenbi/";
        File dirF = new File(sdCardDir);
        if(!dirF.exists()){
            dirF.mkdirs();
            textRecorded.setText("已录:0");
            return;
        }
        int numRecorded = 0;
        for(int tmpQid:arr){
            File saveFile = new File(sdCardDir, tmpQid+".txt");
            if(saveFile.exists()){
                numRecorded++;
            }
        }
        if(!isresp){
            textRecorded.setText("已录:"+numRecorded);
        }else{
            final int nr = numRecorded;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textRecorded.setText("已录:"+nr);
                }
            });
        }
    }

    public static Bitmap getNetworkBmp(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(600000);
            //连接设置获得数据流
            conn.setDoInput(true);
//            conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static boolean requestPermissions(Activity activity) {
        Log.i("mnist", "读写储存权限 ："+(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
        Log.i("mnist", "启动相机权限 ："+(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED));
        Log.i("mnist", "读取手机状态权限 ："+(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED));
        Log.i("mnist", "扬声器使用权限 ："+(ContextCompat.checkSelfPermission(activity, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED));
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, android.Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED
                ) {
            Log.e("CameraActivity.java.hx:", "尝试获取储存以及相机权限");
            //请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_PHONE_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                    ) {
                showToast("请开启 储存权限。", activity);
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_PHONE_STATE,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.MODIFY_AUDIO_SETTINGS}, REQUEST_EXTERNAL_PERMISSION);
                return false;
            }else{
                showToast("请开启 储存权限。", activity);
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_PHONE_STATE,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.MODIFY_AUDIO_SETTINGS}, REQUEST_EXTERNAL_PERMISSION);
                return true;
            }
        }
        return true;
    }
    public static void  showToast(final String text, final Activity act) {
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
