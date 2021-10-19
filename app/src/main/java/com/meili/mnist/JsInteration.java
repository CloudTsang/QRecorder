package com.meili.mnist;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;
import org.json.JSONArray;

public class JsInteration {
    public JsInteration() {

    }

    @JavascriptInterface
    public String JsTest(){
        Log.i("mnist", "js test success");
        return "js test success";
    }

    @JavascriptInterface
    public void QSelected(final String jsonStr, final int selectedIndex, final String token){
//        QCanvasActivity.self.setQContent(qid, url);
        Log.i("mnist", "token = "+token);
        Log.i("mnist", "data = "+jsonStr);

        try {
            final JSONArray data = new JSONArray(jsonStr);

            QCanvasActivity.self.token = token;
            QCanvasActivity.self.qUrls = data;
        }catch (Exception err){
            err.printStackTrace();
            return;
        }

        final Handler handler =  new Handler(Looper.getMainLooper()){
            public void handleMessage(Message msg){
//                Log.i("java.hx:","msg.what = "+msg.what);
//                QCanvasActivity.self.setQContent(Integer.parseInt(qid), url);
                QCanvasActivity.self.setQContent(selectedIndex);
            }
        };
        Message message = new Message();
        handler.sendMessage(message);
    }
}
