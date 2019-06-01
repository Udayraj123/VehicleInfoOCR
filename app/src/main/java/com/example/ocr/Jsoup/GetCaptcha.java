package com.example.ocr.Jsoup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ocr.Jsoup.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Map;

public class GetCaptcha extends AsyncTask<String, Void, Bitmap>
{
    private final String BASE_URL = "https://parivahan.gov.in";
    private final String VEHICLE_URL="/rcdlstatus/vahan/rcstatus.xhtml";

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

    @Override
    protected Bitmap doInBackground(String... params)
    {
        try
        {
            Connection.Response form = Jsoup.connect(getAbsoluteURL(VEHICLE_URL))
                    .method(Connection.Method.GET)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36")
                    .execute();

            cookies = form.cookies();

            if((statusCode = form.statusCode()) == 200)
            {
                Document formDocument = form.parse();
                formNumber = formDocument.select("button[class=ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only]").attr("name");
                viewState = formDocument.select("input[name=javax.faces.ViewState]").attr("value");

                String captcha = formDocument.getElementsByTag("img").get(1).attr("src");
                String captchaURL = BASE_URL + captcha;
                InputStream input = new java.net.URL(captchaURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
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

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        response.processFinish(bitmap, statusCode);
    }
}