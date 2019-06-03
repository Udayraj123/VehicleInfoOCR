package com.example.ocr.text_detection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;



public class BitmapTextRecognizer {

    private static final String TAG = "BitmapTextRec";
    private final FirebaseVisionTextRecognizer detector;
    private List<FirebaseVisionText.TextBlock> textBlocks;
    public String allText="";
    public BitmapTextRecognizer() {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }
    private String filterCaptcha(String s){
        return s.replaceAll("[^a-zA-Z0-9]","");
    }
    public void processBitmap(final Bitmap bitmap){
        Log.d(TAG,"sent image to mlkit process");

        Task<FirebaseVisionText> result =detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap));

        try{
            Tasks.await(result);
        }
        catch (ExecutionException e){
            Log.d(TAG,"Error during recognition task?!");
            e.printStackTrace();
        }
        catch (InterruptedException e){
            Log.d(TAG,"Error during recognition task?!");
            e.printStackTrace();
        }
        Log.d(TAG,"Finished waiting, read: "+allText);

    }
    private Task<FirebaseVisionText> detectInVisionImage( FirebaseVisionImage image) {
        return
        detector.processImage(image)
        .addOnSuccessListener(
            new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText results) {
                 allText = "";
                 textBlocks = results.getTextBlocks();
                 for (int i = 0; i < textBlocks.size(); i++) {
                    allText += textBlocks.get(i).getText();
                }
                Log.d(TAG,"Bitmap Success read: "+allText);
                allText = filterCaptcha(allText);
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