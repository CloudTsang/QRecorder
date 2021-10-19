package com.meili.mnist;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class KeyboardTestActivity extends Activity {
    public static final int INPUT_NORMAL = 1;
    public static final int INPUT_VERTEQ = 2;

    private KeyboardView keyboardView1;
    private KeyboardView keyboardView2;
    private RelativeLayout keyboardPanel;
    private Keyboard keyboard1;
    private Keyboard keyboard2;
    private int curKeyboard;
    private TextView txtKeyboard;
    private StringBuilder builder;
    private KeyboardView.OnKeyboardActionListener lstner;

    private int inputype;

    protected void onCreate(Bundle savedInstanceState){
        Log.i("mnist", "onCreate");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pop_up_input);

        keyboardView1 = (KeyboardView)findViewById(R.id.custom_keyboard1);
        keyboardView2 = (KeyboardView)findViewById(R.id.custom_keyboard2);
        txtKeyboard = (TextView)findViewById(R.id.txt_kb_input);
        keyboardPanel = (RelativeLayout)findViewById(R.id.ly_keyboardpanel);


        Intent intent = getIntent();
        boolean alignbottom = intent.getBooleanExtra("alignbottom", true);
        inputype = intent.getIntExtra("inputtype", INPUT_NORMAL);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) keyboardView1.getLayoutParams();
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) keyboardView2.getLayoutParams();
        if(alignbottom){
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }else{
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        keyboardView1.setLayoutParams(params);
        keyboardView2.setLayoutParams(params2);

        if(inputype == INPUT_NORMAL){
            curKeyboard = R.xml.keyboard_mnist;
        }else if(inputype == INPUT_VERTEQ){
            curKeyboard = R.xml.keyboard_verteq;
        }


        builder = new StringBuilder();
        lstner = new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {
            }
            @Override
            public void onRelease(int primaryCode) {
            }
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                switch(primaryCode){
                    case Keyboard.KEYCODE_DONE://收起
                        Intent intent = new Intent();  //创建一个Intent
                        setResult(400,intent);
                        finish();
                        break;
                    case Keyboard.KEYCODE_CANCEL://回退
                        if(builder.length()==0){
                            return;
                        }
                        builder.delete(builder.length()-1, builder.length());
                        txtKeyboard.setText(builder.toString());
                        break;
                    case Keyboard.KEYCODE_DELETE://清空
                        builder = new StringBuilder();
                        txtKeyboard.setText("");
                        break;
                    case Keyboard.KEYCODE_SHIFT://切换
                        if(curKeyboard == R.xml.keyboard_mnist){
                            curKeyboard = R.xml.keyboard_mnist2;

                        }else{
                            curKeyboard = R.xml.keyboard_mnist;
                        }
                        showKeyboard();
                        break;
                    case -100:
                        builder.append("[fra]");
                        txtKeyboard.setText(builder.toString());
                        break;
                    case -101:
                        builder.append("[/fra]");
                        txtKeyboard.setText(builder.toString());
                        break;
                    default:
                        builder.append(Character.toString((char) primaryCode));
                        txtKeyboard.setText(builder.toString());
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

        initKeyboard();
    }

    public void confirm(View view){
        Intent intent = new Intent();  //创建一个Intent
        intent.putExtra("input", builder.toString());
        setResult(200,intent);
        finish();
    }

    private void initKeyboard() {
        if(inputype == INPUT_NORMAL){
            keyboard1 = new Keyboard(this, R.xml.keyboard_mnist);
        }else if(inputype == INPUT_VERTEQ){
            keyboard1 = new Keyboard(this, R.xml.keyboard_verteq);
        }

        keyboard2 = new Keyboard(this, R.xml.keyboard_mnist2);
        keyboardView1.setKeyboard(keyboard1);
        keyboardView1.setEnabled(true);
        keyboardView1.setPreviewEnabled(false);
        keyboardView1.setOnKeyboardActionListener(lstner);
        keyboardView2.setKeyboard(keyboard2);
        keyboardView2.setEnabled(true);
        keyboardView2.setPreviewEnabled(false);
        keyboardView2.setOnKeyboardActionListener(lstner);
        showKeyboard();
    }

    public void showKeyboard() {
        if(curKeyboard == R.xml.keyboard_mnist || curKeyboard == R.xml.keyboard_verteq){
            keyboardView1.setVisibility(View.VISIBLE);
            keyboardView2.setVisibility(View.GONE);
        }else{
            keyboardView1.setVisibility(View.GONE);
            keyboardView2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
//        keyboard1 = new Keyboard(this, R.xml.keyboard_mnist);
//        keyboard2 = new Keyboard(this, R.xml.keyboard_mnist2);
        initKeyboard();
//        showKeyboard();
        Log.i("mnist", "onresume");
    }

}
