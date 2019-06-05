package com.example.ocr.text_detection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import com.example.ocr.graphics.*;

import android.widget.EditText;


public class TextRecognitionProcessor {

    private static final String TAG = "TextRecProc";
    public final String NUMPLATE_PATTERN = "[A-Z]{2}[0-9]{2}[A-Z]+[0-9]+";
    public final String NUMPLATE_PATTERN_TIGHT = "[A-Z]{2}[0-9]{2}[A-Z]+[0-9]{4}";
    private int MAX_BOXES = 5;
    private final FirebaseVisionTextRecognizer detector;
    public String allText="";
    public String majorText="";
    public List<FirebaseVisionText.TextBlock> textBlocks;
    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public TextRecognitionProcessor() {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    //region ----- Exposed Methods -----


    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay){
        if (shouldThrottle.get()) { // nice atomic approach to skip frames!
            return;
        }

        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), graphicOverlay);
    }

    //endregion

    //region ----- Helper Methods -----

    private Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }
    private String numPlateFilter(String s){
        return s.toUpperCase().replaceAll("[^A-Z0-9]","");
    }
    private void autoUpdateMajorText(String line){
        line = numPlateFilter(line);
        if( majorText.matches(NUMPLATE_PATTERN_TIGHT) && line.matches(NUMPLATE_PATTERN_TIGHT)
                || !majorText.matches(NUMPLATE_PATTERN_TIGHT) && line.matches(NUMPLATE_PATTERN)){
            majorText = line;
            Log.d(TAG,"Updated majorText: "+line);
        }
    }
    private void onSuccess( @NonNull FirebaseVisionText results, @NonNull GraphicOverlay graphicOverlay) {

        //Done: set a public variable from here containing the "main" text. ( for captcha)
        graphicOverlay.clear();
        allText = "";
        textBlocks = results.getTextBlocks();
        // textBlocks = new ArrayList<>(results.getTextBlocks());
        // Collections.sort(textBlocks, new Comparator<FirebaseVisionText.TextBlock>(){
        //     @Override
        //     public int compare(FirebaseVisionText.TextBlock t1, FirebaseVisionText.TextBlock t2) {
        //         return t2.getBoundingBox().width() * t2.getBoundingBox().height()
        //         - t1.getBoundingBox().width() * t1.getBoundingBox().height();
        //     }
        // });

        for (int i = 0; i < Math.min(textBlocks.size(), MAX_BOXES); i++) {
            List<FirebaseVisionText.Line> lines = textBlocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);
                }
                autoUpdateMajorText(lines.get(j).getText());
            }
            //preference to block than line VERIFY?!
            autoUpdateMajorText(textBlocks.get(i).getText());
            allText += textBlocks.get(i).getText();
        }
        if(!allText.equals(""))
            Log.d(TAG,"Success read: "+allText);
        allText = numPlateFilter(allText);
        autoUpdateMajorText(allText);
    }

    private void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }

    private void detectInVisionImage( FirebaseVisionImage image, final GraphicOverlay graphicOverlay) {

        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText results) {
                                shouldThrottle.set(false);
                                TextRecognitionProcessor.this.onSuccess(results, graphicOverlay);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                shouldThrottle.set(false);
                                TextRecognitionProcessor.this.onFailure(e);
                            }
                        });
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true);
    }

}