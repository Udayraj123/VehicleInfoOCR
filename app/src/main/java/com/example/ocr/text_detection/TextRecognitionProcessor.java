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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.annotation.NonNull;
import android.util.Log;
import com.example.ocr.graphics.*;


public class TextRecognitionProcessor{

    private static final String TAG = "TextRecProc";
    /*
        The first two letters indicate the state or Union Territory to which the vehicle is registered.
        The next two digit numbers are the sequential number of a district. Due to heavy volume of vehicle registration, the numbers were given to the RTO offices of registration as well.
        The third part consists of one ,two or three letters. This shows the ongoing series of an RTO (Also as a counter of the number of vehicles registered) and/or vehicle classification
        The fourth part is a 4 digit number unique to each plate. A letter is prefixed when the 4 digit number runs out and then two letters and so on.
    */
    // This pattern is for prompting user to correct
    public final String NUMPLATE_PATTERN = "[A-Z]{2}[0-9]+[A-Z]+[0-9]+";
    // This is the pattern accepted by the site
    public final String NUMPLATE_PATTERN_STRICT = "[A-Z]{2}[0-9]{2}[A-Z]{1,3}[0-9]{4}";
    private final Pattern NUMPLATE_GROUPS = Pattern.compile("([A-Z]{2})([0-9]+)([A-Z]+)([0-9]+)");
    // private final Pattern NUMPLATE_GROUPS_STRICT = Pattern.compile("([A-Z]{2})([0-9]{2})([A-Z]{1,3})([0-9]{4})");
    private int MAX_BOXES = 5;
    private final FirebaseVisionTextRecognizer detector;
    public String allText="";
    public String majorText="";
    public List<FirebaseVisionText.TextBlock> textBlocks;
    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);
    private AppEvents listener;

    public TextRecognitionProcessor(AppEvents listener) {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        this.listener = listener;
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
        // nice atomic approach to skip frames!
        if (shouldThrottle.get() || haveToThrottle()) {
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

    private String convertToStrict(String line){
        if(line.matches(NUMPLATE_PATTERN_STRICT))
            return line;
        // assumes NUMPLATE_PATTERN is matched
        Matcher m = NUMPLATE_GROUPS.matcher(line);
        String newplate="";
        if(m.find()){
            newplate += m.group(1);
            // add missing 0s to middle part
            if(m.group(2).length() < 2)
                newplate += String.format("%0" + (2 - m.group(2).length()) + "d", 0);
            newplate += m.group(2);
            newplate += m.group(3);
            // add missing 0s to last part
            if(m.group(4).length() < 4)
                newplate += String.format("%0" + (4 - m.group(4).length()) + "d", 0);
            newplate += m.group(4);
            return newplate;
        }
        Log.d(TAG,"UNEXPECTED no match for numplate regex");
        return line;
    }
    private void autoUpdateMajorText(String line){
        line = numPlateFilter(line);
        if( majorText.matches(NUMPLATE_PATTERN_STRICT) && line.matches(NUMPLATE_PATTERN_STRICT)
                || !majorText.matches(NUMPLATE_PATTERN_STRICT) && line.matches(NUMPLATE_PATTERN)){
            majorText = convertToStrict(line);
            listener.onMajorTextUpdate(line);
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
    private Random haveToObj = new Random();
    private int THROTTLE_SPEED = 2;// be integer > 1
    private boolean haveToThrottle(){
        // 1/THROTTLE_SPEED chance to drop frame
        if(haveToObj.nextInt(1+THROTTLE_SPEED) == 0){
            // Log.d(TAG,"Throttled frame!");
            return true;
        }
        return false;
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