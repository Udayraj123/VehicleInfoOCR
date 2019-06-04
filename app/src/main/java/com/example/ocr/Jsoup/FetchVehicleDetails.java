package com.example.ocr.Jsoup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ocr.Jsoup.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class FetchVehicleDetails extends AsyncTask<String, Void, Vehicle>
{
    private final String TAG = "FetchVehicleDetails: ";
    private final String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0";

    private final String BASE_URL = "https://vahan.nic.in";
    private final String VEHICLE_URL="/nrservices/faces/user/searchstatus.xhtml";
    // private final String BASE_URL = "https://parivahan.gov.in";
    // private final String VEHICLE_URL="/rcdlstatus/vahan/rcDlHome.xhtml";
    private final String FULL_URL=BASE_URL+VEHICLE_URL;

    private Vehicle vehicle;
    private int statusCode;
    private AsyncResponse response = null;

    private final int SOCKET_TIMEOUT = 408;
    private final int CAPTCHA_FAILED = 999;
    private final int TECHNICAL_DIFFICULTY = 888;
    private String getAbsoluteURL(String url)
    {
        return BASE_URL + url;
    }
    public FetchVehicleDetails(AsyncResponse response)
    {
        this.response = response;
    }
    public void bigLog(String s){
        final int chunkSize = 500;
        for (int i = 0; i < s.length(); i += chunkSize) {
            Log.d(TAG, s.substring(i, Math.min(s.length(), i + chunkSize)));
        }
    }



    // drawer.dismiss();
    // new FetchVehicleDetails(new AsyncResponse(){
    //     //uses most recent cookies and formnumber
    //     @Override
    //     public void processFinish(Vehicle vehicle, int statusCode) {
    //         Log.d(TAG,"Finished Status code:"+statusCode);
    //         if (statusCode == OK) {
    //             if (vehicle != null){
    //                 showVehicleDetails(vehicle);
    //             }
    //             else {
    //                 logToast("Vehicle details not found");
    //
    //             }
    //         }
    //         else if (statusCode == CAPTCHA_LOAD_FAILED){
    //             // Done: reload button?!
    //             logToast("Captcha Load Failed!");
    //         }
    //         else if (statusCode == TECHNICAL_DIFFICULTY){
    //             logToast("Technical Difficulty. Failed to fetch from table!");
    //             // .title(R.string.error_technical_difficulty)
    //
    //         }
    //         else if (statusCode == SOCKET_TIMEOUT){
    //             logToast("Internet Timeout.. Slow internet?");
    //         }
    //         else{
    //             //no internet
    //             // verify using isNetworkAvailable()
    //             logToast("Internet Unavailable");
    //         }
    //     }
    // }).execute(result.substring(0, result.length() - 4), result.substring(result.length() - 4), captchaInput.getText().toString());

    @Override
    protected Vehicle doInBackground(String... params)
    {
        try
        {
            // .data("javax.faces.partial.render", "rc_Form:rcPanel") <-- changed
            // .data("form_rcdl:j_idt24:CaptchaID", params[2]) <-- param changes!
            Connection connection = Jsoup.connect(FULL_URL)
            .header("Host", BASE_URL.substring(8))
            .userAgent(userAgent)
            .header("Accept","application/xml, text/xml, */*; q=0.01")
            .header("Accept-Language","en-US,en;q=0.5")
            .header("Accept-Encoding","gzip, deflate, br")
            .header("Referer", FULL_URL)
            .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
            .header("Faces-Request","partial/ajax")
            .header("X-Requested-With","XMLHttpRequest")
            // .header("Origin", BASE_URL)
            .cookies(GetCaptcha.cookies) // cookies verified
            .data("javax.faces.partial.ajax", "true")
            .data("javax.faces.source", GetCaptcha.formNumber)
            .data("javax.faces.partial.execute", "@all")
            // .data("javax.faces.partial.render", "form_rcdl:pnl_show+form_rcdl:pg_show+form_rcdl:rcdl_pnl")
            .data("javax.faces.partial.render", "rcDetailsPanel+resultPanel+userMessages+capatcha")
            .data(GetCaptcha.formNumber, GetCaptcha.formNumber)
            // .data("form_rcdl", "form_rcdl")
            .data("masterLayout", "masterLayout")
            .data("j_idt33", "")
            .data("regn_no1_exact", (params[0]+params[1]).toLowerCase())
            // .data("form_rcdl:tf_reg_no1", params[0])
            // .data("form_rcdl:tf_reg_no2", params[1])
            // .data("form_rcdl:j_idt32:CaptchaID", params[2])
            .data("txt_ALPHA_NUMERIC", params[2])
            .data("javax.faces.ViewState", GetCaptcha.viewState)
            .timeout(10000);

            Log.d(TAG,"Sending Post request :" + connection.request().data());
            Document document = connection.post();
            statusCode = connection.response().statusCode();
            Log.d(TAG,"Response Status code:"+statusCode);
            bigLog(document.html());
            Log.d(TAG,"tables: "+document.getElementsByTag("table"));

            if (document.getElementsByTag("table").size() != 0)
            {
                String location_raw = document.getElementsByTag("div").get(4).text();
                Log.d("FetchVehicleDetails", "Raw location: "+location_raw);
                String location = "";
                for (int i = 1; i < location_raw.split(",").length; i++)
                    location += location_raw.split(",")[i] + (i==1 ? "," : "");

                Elements rows = document.getElementsByTag("table").get(0).getElementsByTag("tr");
                String[] attributes = new String[8];
                int j = 0;
                for (Element r : rows)
                {
                    Elements cols = r.getElementsByTag("td");

                    for (int i = 1; i < cols.size(); i += 2)
                        attributes[j++] = cols.get(i).text();
                }

                vehicle = new Vehicle(params[0]+params[1], attributes[7], attributes[6], attributes[5], attributes[3], attributes[2], attributes[4], location, attributes[1]);
            }
            else
            {
                if(document.getElementsByClass("ui-messages-error-detail").size() != 0)
                    statusCode = CAPTCHA_FAILED;
                vehicle = null;
            }
        }
        catch (SocketTimeoutException e){
            statusCode = SOCKET_TIMEOUT;
            e.printStackTrace();
        }
        catch (IOException e) {e.printStackTrace();}
        catch (ArrayIndexOutOfBoundsException e){ statusCode = TECHNICAL_DIFFICULTY;e.printStackTrace(); }

        return vehicle;
    }

    @Override
    protected void onPostExecute(Vehicle vehicle)
    {
        response.processFinish(vehicle, statusCode);
    }
}