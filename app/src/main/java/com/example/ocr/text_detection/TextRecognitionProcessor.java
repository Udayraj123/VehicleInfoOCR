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
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import com.example.ocr.others.*;
public class TextRecognitionProcessor {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;
    public String majorText="";
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


    public String processBitmap(final Bitmap bitmap, int rotation, int facing, GraphicOverlay graphicOverlay){
        // FrameMetadata frameMetadata)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        process(byteBuffer,new FrameMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setRotation(rotation)
                .setCameraFacing(facing)
                .build(), graphicOverlay);
        return majorText;
    }

    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay){

        if (shouldThrottle.get()) {
            return;
        }

        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay);
    }

    //endregion

    //region ----- Helper Methods -----

    private Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }


    private void onSuccess( @NonNull FirebaseVisionText results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

        //TODO: set a public variable from here containing the "main" text. ( for captcha)
         majorText = "?";

        graphicOverlay.clear();

        List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                String line="";
                for (int k = 0; k < elements.size(); k++) {
                    line = line + elements.get(k).getText();
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);
                }
                majorText = line;
                Log.d(TAG,"Read: "+majorText);
            }
        }
    }

    private void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }

    private void detectInVisionImage( FirebaseVisionImage image, final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {

        detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText results) {
                                shouldThrottle.set(false);
                                TextRecognitionProcessor.this.onSuccess(results, metadata, graphicOverlay);
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

    //endregion


}