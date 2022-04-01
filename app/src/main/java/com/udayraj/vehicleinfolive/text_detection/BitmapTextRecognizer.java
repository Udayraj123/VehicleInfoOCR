package com.udayraj.vehicleinfolive.text_detection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;


public class BitmapTextRecognizer {

    private static final String TAG = "BitmapTextRec";
    private final FirebaseVisionTextRecognizer detector;
    private List<FirebaseVisionText.TextBlock> textBlocks;
    private String allText="";

    private AppEvents listener;

    public BitmapTextRecognizer(AppEvents listener) {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        this.listener = listener;
    }
    private String filterCaptcha(String s){
        return s.replaceAll("[^a-zA-Z0-9]","");
    }
    public void processBitmap(final Bitmap bitmap){
        Log.d(TAG,"sent image to mlkit process");
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap));
    }
    private void detectInVisionImage( FirebaseVisionImage image) {
        detector.processImage(image)
        .addOnSuccessListener(
            new OnSuccessListener<FirebaseVisionText>() {
                // Note: StringBuilder is MUCH MUCH FASTER THAN Strings
                @Override
                public void onSuccess(FirebaseVisionText results) {
                   textBlocks = results.getTextBlocks();
                   StringBuilder builder = new StringBuilder();
                   for (int i = 0; i < textBlocks.size(); i++) {
                       builder.append(textBlocks.get(i).getText());
                    }
                 allText = builder.toString();
                Log.d(TAG,"Bitmap Success read: "+allText);
                allText = filterCaptcha(allText);
                listener.onCaptchaUpdate(allText);
            }
        })
        .addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Bitmap Text detection failed." + e);
                }
            });
    }
    public void stop() {
        try {
        // maybe cancel tasks here
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }
}