package com.example.ocr.Jsoup;

import android.graphics.Bitmap;

public interface AsyncCaptchaResponse
{
    void processFinish(Bitmap bitmap, int statuscode);
}