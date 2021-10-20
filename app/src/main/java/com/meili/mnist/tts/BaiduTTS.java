package com.meili.mnist.tts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.util.HashMap;
import java.util.Map;

public class BaiduTTS {
    private static String appId = "YOUR_APP_ID";
    private static String appKey = "YOUR_APP_KEY";
    private static String secretKey = "YOUR_SECRET_KEY";

    private static SpeechSynthesizer mSpeechSynthesizer;

    public static void init(Context context){
        LoggerProxy.printable(true); // 日志打印在logcat中

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);

        // 2. 设置listener
        SpeechSynthesizerListener listener = new MessageListener();
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

        // 3. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        if(result != 0){
            Log.i("mnist", "error code :" + result + " method:setAppId");
        };
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        if(result != 0){
            Log.i("mnist", "error code :" + result + " method:setApiKey");
        };
        mSpeechSynthesizer.auth(TtsMode.ONLINE);
        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "4");
        // 设置合成的音量，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "3");
        // 设置合成的语调，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        result = mSpeechSynthesizer.initTts(TtsMode.ONLINE);
        if(result != 0){
            Log.i("mnist", "error code :" + result + " method:initTts");
        }

       /* Map<String, String> params = new HashMap<>();
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        AutoCheck.getInstance(context).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });*/
    }

    public static void speak(String str){
        Log.i("mnist", mSpeechSynthesizer.toString());
        if(mSpeechSynthesizer != null){
            mSpeechSynthesizer.speak(str);
        }
    }


}
