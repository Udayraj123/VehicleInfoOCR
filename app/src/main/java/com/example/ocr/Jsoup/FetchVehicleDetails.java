package com.example.ocr.Jsoup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
    private final String BASE_URL = "https://parivahan.gov.in";
    private final String VEHICLE_URL="/rcdlstatus/vahan/rcstatus.xhtml";

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

    @Override
    protected Vehicle doInBackground(String... params)
    {
        try
        {
            Connection connection = Jsoup.connect(getAbsoluteURL(VEHICLE_URL))
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36")
                    .data("javax.faces.partial.ajax", "true")
                    .data("javax.faces.source", GetCaptcha.formNumber)
                    .data("javax.faces.partial.execute", "@all")
                    .data("javax.faces.partial.render", "rc_Form:rcPanel")
                    .data(GetCaptcha.formNumber, GetCaptcha.formNumber)
                    .data("rc_Form", "rc_Form")
                    .data("rc_Form:tf_reg_no1", params[0])
                    .data("rc_Form:tf_reg_no2", params[1])
                    .data("rc_Form:j_idt24:CaptchaID", params[2])
                    .data("javax.faces.ViewState", GetCaptcha.viewState)
                    .timeout(10000)
                    .cookies(GetCaptcha.cookies);

            Document document = connection.post();
            statusCode = connection.response().statusCode();

            if (Jsoup.parse(document.text()).body().getElementsByTag("table").size() != 0)
            {
                String location_raw = Jsoup.parse(document.text()).body().getElementsByTag("div").get(4).text();
                String location = "";
                for (int i = 1; i < location_raw.split(",").length; i++)
                    location += location_raw.split(",")[i] + (i==1 ? "," : "");

                Elements rows = Jsoup.parse(document.text()).body().getElementsByTag("table").get(0).getElementsByTag("tr");
                String[] attributes = new String[8];
                int j = 0;
                for (Element r : rows)
                {
                    Elements cols = r.getElementsByTag("td");

                    for (int i = 1; i < cols.size(); i += 2)
                        attributes[j++] = cols.get(i).text();
                }

                vehicle = new Vehicle(params[0]+params[1], attributes[7], attributes[6], attributes[5], attributes[3], attributes[2], attributes[4], location, attributes[1], true);
            }
            else
            {
                if(Jsoup.parse(document.text()).body().getElementsByClass("ui-messages-error-detail").size() != 0)
                    statusCode = CAPTCHA_FAILED;

                vehicle = null;
            }
        }
        catch (SocketTimeoutException e)
        {
            statusCode = SOCKET_TIMEOUT;
        }
        catch (IOException e) {e.printStackTrace();}
        catch (ArrayIndexOutOfBoundsException e){ statusCode = TECHNICAL_DIFFICULTY; }

        return vehicle;
    }

    @Override
    protected void onPostExecute(Vehicle vehicle)
    {
        response.processFinish(vehicle, statusCode);
    }
}