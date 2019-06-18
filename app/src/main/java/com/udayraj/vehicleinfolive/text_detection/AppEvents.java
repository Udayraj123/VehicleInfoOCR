package com.udayraj.vehicleinfolive.text_detection;

import android.graphics.Bitmap;

public interface AppEvents {
    void onMajorTextUpdate(String a);
    void onCaptchaUpdate(String b);
    void onBitmapProcessed(Bitmap a);
}