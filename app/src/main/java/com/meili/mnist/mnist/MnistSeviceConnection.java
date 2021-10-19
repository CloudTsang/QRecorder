package com.meili.mnist.mnist;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.common.StringUtils;
import com.meili.mnist.QCanvasActivity;
import com.meili.mnist.widget.VertEqualObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MnistSeviceConnection {
    public static boolean upload(String Authorization, int questionId, String btAnswerJson){
        String upload_url = "上传接口";
//        questionId = 893074;
//        btAnswerJson = "{\"ans\":[{\"area\":[53,376,80,67],\"as\":\"+\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[605,371,69,69],\"as\":\"÷\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[1118,371,78,69],\"as\":\"÷\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[1343,367,77,72],\"as\":\"÷\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[57,495,77,69],\"as\":\"×\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[602,497,75,65],\"as\":\"÷\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[1113,488,80,73],\"as\":\"÷\",\"idx\":1,\"pgas\":\"\",\"ps\":[[[[0,0]]]]},{\"area\":[1353,488,68,69],\"as\":\"-\",\"idx\":1,\"ps\":[[[[0,0]]]]}],\"cn\":{\"pg\":[],\"rc\":[0,240,1480,340],\"rl\":\"https://qres.k12china.com/qlib/q/202001/09/1422421742_50p_.png\",\"sm\":\"\",\"tx\":\"\",\"vd\":[]},\"id\":893074,\"st\":[1600,1600],\"ty\":\"1\",\"vn\":\"1.0.0\"}";

        HttpURLConnection connection = null;
        try{
//            Log.i("mnist", "upload q json : " + btAnswerJson);

            OkHttpClient client = new OkHttpClient();
            Log.i("mnist", "qid = "+questionId);
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            btAnswerJson = URLEncoder.encode(btAnswerJson, "utf-8");
            RequestBody body = RequestBody.create(mediaType, "questionId="+questionId+"&btAnswerJson="+btAnswerJson);
            Request request = new Request.Builder()
                    .url(upload_url)
                    .put(body)
                    .addHeader("authorization", "token "+Authorization)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();

            Response response = client.newCall(request).execute();
            Log.i("mnist", response.code()+response.body().string());

        }catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean uploadQ(int uid, int qid, JSONObject qdata, boolean ans){        
        String upload_url = "http://localhost/pyocr/saveqdata";
        HttpURLConnection connection = null;
        try{
            String dataJson = qdata.toString();
//            Log.i("mnist:","上传信息："+dataJson);

            URL url = new URL(upload_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setFixedLengthStreamingMode(dataJson.length());
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Charset", "utf-8");

            DataOutputStream dop = new DataOutputStream(
                    httpURLConnection.getOutputStream());
            if(ans){
                dop.writeBytes("uid="+uid+"&qid="+qid+"&qdata="+dataJson+"&ans="+1);
            }else{
                dop.writeBytes("uid="+uid+"&qid="+qid+"&qdata="+dataJson);
            }
            int responseCode = httpURLConnection.getResponseCode();
            Log.i("mnist:","upload code :"+responseCode);
            DataInputStream dip = new DataInputStream(
                    httpURLConnection.getInputStream());
            Log.i("mnist:","result:"+dip.toString());
            dop.flush();
            dop.close();

        }catch (MalformedURLException e) {

//            e.printStackTrace();
            return false;
        } catch (IOException e) {
//            e.printStackTrace();
            return false;
        }catch (Exception e){
//            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static String downloadQ(String Authorization, ArrayList<Integer> qids, final QCanvasActivity act) {
        String downloadUrl = "下载接口";

        for(int i=0; i<qids.size(); i++){
            downloadUrl += qids.get(i);
            if(i!=qids.size()-1){
                downloadUrl+=",";
            }
        }
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .addHeader("authorization", "token "+Authorization)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();
//            Response response = client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respStr = new String(response.body().string());
                    Log.i("mnist","response = "+respStr);
                    try {

                        JSONObject resp = new JSONObject(respStr);
                        JSONArray data = resp.getJSONArray("data");
                        if(data == null){
                            return;
                        }
                        JSONArray res = new JSONArray();
                        for(int i=0;i<data.length();i++){
                            if(!data.getJSONObject(i).isNull("btAnswerJson")){
                                res.put(data.getJSONObject(i).getJSONObject("btAnswerJson"));
                            }
                        }
                        act.GetQJsonData(res, true);
                    }catch (Exception err){
                        err.printStackTrace();
                    }
                }
            });
//            Log.i("mnist", response.code()+response.body().string());
        }catch(Exception err){
            err.printStackTrace();
        }
        return "";
    }

    public static VertEqualObject verteqDetect(String paths){
        String upload_url = "竖式识别接口";

        HttpURLConnection connection = null;
        try{
//            Log.i("mnist", "upload q json : " + btAnswerJson);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "paths="+paths);
            Request request = new Request.Builder()
                    .url(upload_url)
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();

            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            Log.i("mnist", response.code()+"  "+bodyStr);
            JSONObject jobj = new JSONObject(bodyStr);
            JSONArray jstrs = jobj.getJSONArray("msg");
            int jtype = jobj.getInt("type");
            if(jstrs == null || jstrs.length()==0){
                return null;
            }
            ArrayList<String> eqs = new ArrayList<>();
            for(int i=0; i<jstrs.length(); i++){
                eqs.add(jstrs.getString(i));
            }
            return new VertEqualObject(jtype, eqs);

        }catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String save(Bitmap bmp, int type){
        try{
            String dir = Environment.getExternalStorageDirectory().toString() + "/mnist";
            File dirF = new File(dir);
            if(!dirF.exists()){
                dirF.mkdirs();
            }
            dir = dir+"/mnist_" + type + "_" +getRandomString(6)+ ".jpg";
            File file = new File(dir);
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            FileOutputStream out;
            out = new FileOutputStream(file);
            if(bmp.compress(Bitmap.CompressFormat.JPEG, 100, out))
            {
                out.flush();
                out.close();
            }
            return dir;
        }catch(Exception err){
            err.printStackTrace();
            return "";
        }
    }

    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}



