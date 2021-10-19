package com.meili.mnist.ocr;

import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.aip.ocr.AipOcr;
import com.meili.mnist.widget.BmpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Path;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaiduOcr {
    private static Path supPath;
    public static final String APP_ID = "18656651";
    public static final String API_KEY = "wnuXkv4AAPhRFRUwYK1UIhiC";
    public static final String SECRET_KEY = "jGKl8ZBfiDBB5V06qnoGsvbuvEgzIv1F";
    private static String accessToken = "";
    private static AipOcr client =  new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    public static ArrayList<String> ocr(byte[] bs){
        if(client == null){
           client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        }
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("probability", "true");
        JSONObject res = client.handwriting(bs,options);

        ArrayList<String> arr = new ArrayList<>();
        Log.i("mnist","baiduocr result = "+res.toString());
        try{
            JSONArray result = res.getJSONArray("words_result");

            for(int i=0; i<result.length(); i++){
                JSONObject obj = result.getJSONObject(i);
                arr.add(obj.getString("words"));
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return arr;
    }

    public static ArrayList<String> ocr2(Bitmap bmp){
        if(accessToken.length()==0){
            getAccessToken();
        }
        ArrayList<String> arr = new ArrayList<>();
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/handwriting?access_token="+accessToken;
        try{
            String b64 = BmpUtil.bitmapToBase64(bmp);
            String encImg = URLEncoder.encode(b64, "UTF-8") ;
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "image="+encImg+"&probability=true");
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
            String respStr = new String(response.body().string());
            Log.i("mnist","baiduocr result = "+respStr);
            JSONObject res = new JSONObject(respStr);
            JSONArray result = res.getJSONArray("words_result");

            for(int i=0; i<result.length(); i++){
                JSONObject obj = result.getJSONObject(i);
                arr.add(obj.getString("words"));
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return arr;
    }

    public static ArrayList<String> formulaOcr(Bitmap bmp){
        if(accessToken.length()==0){
            getAccessToken();
        }
        Log.i("mnist", "formulaOcr");
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/formula?access_token="+accessToken;
//        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/handwriting?access_token="+accessToken;
        ArrayList<String> arr = new ArrayList<>();
        try{
            String b64 = BmpUtil.bitmapToBase64(bmp);
            String encImg = URLEncoder.encode(b64, "UTF-8") ;
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "image="+encImg+"&probability=true");
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
            String respStr = new String(response.body().string());
            Log.i("mnist","baiduocr result = "+respStr);
            JSONObject res = new JSONObject(respStr);
//            JSONArray result = res.getJSONArray("formula_result");
            JSONArray result = res.getJSONArray("words_result");
            for(int i=0; i<result.length(); i++){
                JSONObject obj = result.getJSONObject(i);
                arr.add(obj.getString("words"));
            }
        }catch (Exception err){
            err.printStackTrace();
        }
        return arr;
    }

    private static void getAccessToken(){
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        try{
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id="+API_KEY+"&client_secret="+SECRET_KEY);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
            String respStr = new String(response.body().string());
            Log.i("mnist", respStr);
            JSONObject resp = new JSONObject(respStr);
            accessToken  = resp.getString("access_token");
        }catch (Exception err){
            err.printStackTrace();
        }
    }

    public static Path getSupPath(){
        if(supPath == null){
            supPath = new Path();
            String s = "[[[629,292],[665,283],[688,273],[729,260],[744,258]],[[690,279],[673,304],[656,330],[639,354],[625,373],[612,390],[613,391],[614,392],[616,393],[622,397],[634,418],[638,434],[642,449],[643,462],[645,478],[645,480],[645,480]],[[636,365],[673,347],[694,340],[711,337],[726,338],[734,341],[743,368],[746,392],[746,417],[751,466],[750,479],[750,486],[750,489]],[[661,406],[672,401],[686,398],[698,396],[707,398],[708,398]],[[671,472],[690,470],[706,466],[726,465]],[[937,253],[940,264],[940,276]],[[843,343],[853,339],[922,306],[975,284],[993,280],[1000,279],[1000,281]],[[893,338],[881,370],[873,395],[866,420],[860,442],[854,459],[845,484],[843,487]],[[880,380],[899,369],[926,360],[947,352],[962,347],[974,343],[980,340],[982,339]],[[934,336],[931,363],[927,379],[925,392],[924,400],[924,403],[924,403]],[[972,348],[974,373],[972,384],[972,393],[967,402]],[[903,411],[924,407],[937,404],[948,401],[958,398],[971,393],[976,393],[980,393],[983,396],[984,398],[980,404],[965,422],[944,444],[905,476],[889,482],[888,483]],[[884,444],[917,469],[941,483],[964,495],[1015,516],[1035,525],[1044,528]],[[1146,312],[1166,321],[1176,324],[1182,329],[1186,334],[1186,338]],[[1134,370],[1141,393],[1143,403],[1143,413],[1143,420],[1143,428],[1144,445],[1144,453],[1145,461],[1145,465],[1146,468],[1150,467],[1164,455],[1180,436],[1192,420]],[[1216,308],[1217,321],[1217,339],[1217,353],[1217,364],[1216,376],[1215,376],[1215,376]],[[1218,301],[1239,292],[1255,289],[1269,286],[1292,289],[1298,292],[1299,296],[1295,306],[1261,342],[1248,352],[1243,357],[1241,358],[1243,358],[1250,356],[1271,352],[1278,353],[1279,355],[1278,364],[1265,386],[1247,415],[1226,442],[1203,479],[1202,486],[1202,490]],[[1271,429],[1278,443],[1284,456],[1289,468],[1294,479],[1300,488],[1303,492]],[[1408,305],[1406,327],[1404,339],[1401,355],[1399,362]],[[1403,299],[1425,288],[1439,283],[1452,279],[1460,278],[1466,278],[1471,279],[1469,284],[1463,294],[1457,309],[1450,321],[1443,332],[1435,338],[1433,340]],[[1380,372],[1407,360],[1438,345],[1447,341],[1451,342]],[[1385,387],[1395,386],[1409,384],[1434,382],[1441,385],[1448,388],[1452,396],[1454,407],[1453,424],[1443,455],[1439,465],[1434,472],[1433,476],[1429,476],[1429,476]],[[1417,375],[1414,390],[1409,407],[1403,435],[1400,447],[1400,458],[1400,462]],[[1483,352],[1483,374],[1486,404],[1487,413],[1489,421],[1490,422]],[[1543,268],[1541,300],[1535,394],[1534,446],[1532,486],[1531,514],[1530,530],[1521,531],[1519,531]]]";
            try{
                JSONArray paths = new JSONArray(s);
                for(int i=0; i<paths.length();i++){
                    JSONArray points = paths.getJSONArray(i);
                    for(int j=0; j<points.length(); j++){
                        JSONArray point = points.getJSONArray(j);
                        if(j==0){
                            supPath.moveTo(point.getInt(0), point.getInt(1));
                        }else{
                            supPath.lineTo(point.getInt(0), point.getInt(1));
                        }
                    }

                }
            }catch (Exception err){
                err.printStackTrace();
            }
        }
        return supPath;
    }
}
