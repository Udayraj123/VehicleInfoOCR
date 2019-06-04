package com.example.ocr.Jsoup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ocr.Jsoup.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

public class GetCaptcha extends AsyncTask<String, Void, Bitmap>
{

    private final String TAG = "GetCaptcha";
    private final String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private final String BASE_URL = "https://vahan.nic.in";
    private final String VEHICLE_URL="/nrservices/faces/user/searchstatus.xhtml";
    private final String FULL_URL=BASE_URL+VEHICLE_URL;
    // private final String BASE_URL = "https://parivahan.gov.in";
    // private final String VEHICLE_URL="/rcdlstatus/vahan/rcDlHome.xhtml";

    private int statusCode;
    private AsyncCaptchaResponse response = null;

    private final int SOCKET_TIMEOUT = 408;
    private String getAbsoluteURL(String url)
    {
        return BASE_URL + url;
    }
    public GetCaptcha(AsyncCaptchaResponse response)
    {
        this.response = response;
    }

    private Bitmap bitmap;
    public static String formNumber;
    public static String viewState;
    public static Map<String, String> cookies;

    public static void bigLog(String TAG,String s){
        final int chunkSize = 500;
        for (int i = 0; i < s.length(); i += chunkSize) {
            Log.d(TAG, s.substring(i, Math.min(s.length(), i + chunkSize)));
        }
    }
    @Override
    protected Bitmap doInBackground(String... params)
    {
        try{
            Log.d("GetCaptcha","Loading url: "+FULL_URL);
            Connection.Response form = Jsoup.connect(FULL_URL)
            .method(Connection.Method.GET)
            .timeout(10000)
            .header("Host", BASE_URL.substring(8))
            .userAgent(userAgent)
            .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language","en-US,en;q=0.5")
            .header("Accept-Encoding","gzip, deflate, br")
            .header("Referer", FULL_URL)
                    // .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
            .header("Cache-Control","max-age=0")
            .execute();
            cookies = form.cookies();
            String cookiesString=cookies.toString().substring(1,cookies.toString().length()-1);

            Log.d("GetCaptcha","request Done, cookies: "+cookiesString);
            if((statusCode = form.statusCode()) == 200)
            {
                Document formDocument = form.parse();
                formNumber = formDocument.select("button[class=ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only]").attr("name");
                viewState = formDocument.select("input[name=javax.faces.ViewState]").attr("value");
                String captcha = formDocument.getElementsByClass("captcha-image").get(0).attr("src");
                String captchaURL = BASE_URL + captcha;
                bigLog(TAG,"captchaURL: "+captchaURL + " formNumber: "+formNumber);
                // Note: here user agent is not there?!
                // InputStream input = new java.net.URL(captchaURL).openStream();
                // Bitmap bitmap1 = BitmapFactory.decodeStream(input);

                HttpURLConnection connection = null;
                try {
                    URL url = new URL(captchaURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.addRequestProperty("User-Agent", userAgent);
                    connection.addRequestProperty("Accept","image/webp,*/*");
                    connection.addRequestProperty("Cache-Control","max-age=0");
                    connection.addRequestProperty("Referer",FULL_URL);
                    connection.addRequestProperty("Cookie", cookiesString);
                    if (connection.getResponseCode() == 200) {
                        InputStream content = connection.getInputStream();
                        bitmap=null;
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                content, null, null);
                        } finally {
                            try {
                                content.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                    else{
                        Log.d("GetCaptcha","Server Error! code: "+connection.getResponseCode());
                    }
                } catch (IOException ignored) {
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        }
        catch (SocketTimeoutException e)
        {
            Log.d("GetCaptcha","Error in network: ");
            e.printStackTrace();
            statusCode = SOCKET_TIMEOUT;
        }
        catch (IOException e) {
            Log.d("GetCaptcha","Error in IO: ");
            e.printStackTrace();
        }
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        thresholdBitmap(bitmap,300);
        openBinaryBitmap(bitmap,5);
        return bitmap;
    }
    private int colorVal(int pixel){
        return (int)Math.sqrt(Math.pow(Color.red(pixel),2) +Math.pow(Color.green(pixel),2) +Math.pow(Color.blue(pixel),2));
    }
    private void thresholdBitmap(Bitmap bitmap,int THR ){
        for(int i=0;i<bitmap.getWidth();i++){
            for(int j=0;j<bitmap.getHeight();j++){
                if(colorVal(bitmap.getPixel(i,j)) < THR)
                    bitmap.setPixel(i,j, Color.BLACK);
                else
                    bitmap.setPixel(i,j, Color.WHITE);
            }
        }
    }
    private void openBinaryBitmap(Bitmap binaryBitmap,int KSIZE) {
        dilateBinaryBitmap(binaryBitmap,KSIZE);
        erodeBinaryBitmap(binaryBitmap,KSIZE);
    }
    private void erodeBinaryBitmap(Bitmap binaryBitmap,int KSIZE) {
        int currp, minp,w = binaryBitmap.getWidth(), h = binaryBitmap.getHeight(), mini,minj;
        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                // revolve kernel
                minp = colorVal(binaryBitmap.getPixel(i,j));
                mini = i;
                minj = j;
                for(int ki=-KSIZE/2;ki<KSIZE/2;ki++){
                    for(int kj=KSIZE/2;kj<KSIZE/2;kj++){
                        if(ki > -1 && kj < -1 && ki < w && kj < h){
                            currp = colorVal(binaryBitmap.getPixel(i+ki,j+kj));
                            if(minp > currp){
                                minp = currp;
                                mini = i+ki;
                                minj = j+kj;
                            }
                        }
                    }
                }
                binaryBitmap.setPixel(i,j, binaryBitmap.getPixel(mini,minj));  
            }
        }
    }
    private void dilateBinaryBitmap(Bitmap binaryBitmap,int KSIZE) {
        int currp, maxp,w = binaryBitmap.getWidth(), h = binaryBitmap.getHeight(), maxi,maxj;
        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                // revolve kernel
                maxp = colorVal(binaryBitmap.getPixel(i,j));
                maxi = i;
                maxj = j;
                for(int ki=-KSIZE/2;ki<KSIZE/2;ki++){
                    for(int kj=KSIZE/2;kj<KSIZE/2;kj++){
                        if(ki > -1 && kj < -1 && ki < w && kj < h){
                            currp = colorVal(binaryBitmap.getPixel(i+ki,j+kj));
                            if(maxp < currp){
                                maxp = currp;
                                maxi = i+ki;
                                maxj = j+kj;
                            }
                        }
                    }
                }
                binaryBitmap.setPixel(i,j, binaryBitmap.getPixel(maxi,maxj));  
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        response.processFinish(bitmap, statusCode);
    }
}