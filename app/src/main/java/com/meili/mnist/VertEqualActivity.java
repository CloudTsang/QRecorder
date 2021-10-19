package com.meili.mnist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meili.mnist.mnist.MnistSeviceConnection;
import com.meili.mnist.mnist.VertEqualDetect;
import com.meili.mnist.widget.SimpleCanvasView;
import com.meili.mnist.widget.VertEqualObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VertEqualActivity extends Activity {
    private VertEqualObject verteq;
    private TextView txt;
    private TextView txt2;
    private Button btnConfirm;
    private Button btnEdit;
    private String showT;

    private Keyboard keyboard;
    private KeyboardView keyboardView;
    private KeyboardView.OnKeyboardActionListener lstner;

    private SimpleCanvasView canvas;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_vert_eq);

        canvas = (SimpleCanvasView)findViewById(R.id.simple_canvas_view_verteq);
        txt = (TextView)findViewById(R.id.txt_vert_result);
        txt2 = (TextView)findViewById(R.id.txt_vert_format);

        btnConfirm = (Button)findViewById(R.id.btn_verteq_confirm);
        btnEdit = (Button)findViewById(R.id.btn_vertq_edit);
    }

    public void onVertEqDetect(View view){
       /* finish();
        if(true){
            return;
        }*/
        Log.i("mnist", canvas.getPointsString());
        verteq = VertEqualDetect.vertEqualMnist(canvas.getPointsString(), this, QCanvasActivity.ydclassifierlite);

        if(verteq!= null && verteq.eqs.size()>0){
            Log.i("mnist", verteq.toString());
            showT = "";
            for(String s : verteq.eqs){
                showT += s + "\n";
            }
            showT = showT.replace("×","x");
            showT = showT.replace(" ", "口");
            txt.setText(showT);
            String showT2 = showT.replace("口", "  ");
            txt2.setText(showT2);
            btnConfirm.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
        }else{
            Log.i("mnist", "识别竖式失败");
            txt.setText("识别竖式失败");
        }
    }

    public void onVerEqBack(View view){
        canvas.stepBack();
    }

    public void onVerEqConfirm(View view){
        Intent intent = new Intent();
        if(verteq!=null){
            intent.putExtra("veqtype", verteq.qtype);
            intent.putExtra("veqs", verteq.eqs);
        }
        setResult(200, intent);
        finish();
    }

    public void onVerEqEdit(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View addv = factory.inflate(R.layout.edittext_verteq, null);
        final EditText edit = (EditText)addv.findViewById(R.id.editText_verteq);
        keyboardView = (KeyboardView)addv.findViewById(R.id.verteq_keyboard1);
        edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edit.setSingleLine(false);
        edit.setShowSoftInputOnFocus(false);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(),0);


        String editText = "";
        for(String s : verteq.eqs){
            editText += s + "\n";
        }
        editText = editText.replace("×","x");
        editText = editText.replace(" ","口");
        edit.setText(editText);

        keyboard = new Keyboard(this, R.xml.keyboard_verteq);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setVisibility(View.VISIBLE);


        lstner = new KeyboardView.OnKeyboardActionListener() {
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

        final AlertDialog alert2 = new AlertDialog.Builder(view.getContext()).setTitle("编辑").setView(addv)
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
                verteq = new VertEqualObject(qt, arr2);

                showT = "";
                for(String s : verteq.eqs){
                    showT += s + "\n";
                }
                showT = showT.replace("×","x");
                showT = showT.replace(" ", "口");
                txt.setText(showT);
                String showT2 = showT.replace("口", "  ");
                txt2.setText(showT2);

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
