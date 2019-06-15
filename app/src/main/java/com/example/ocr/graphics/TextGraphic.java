package com.example.ocr.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class TextGraphic extends GraphicOverlay.Graphic {

//    private static final int OFFSET = 20;
    private static final float TEXT_SIZE = 34.0f;
    private static final float STROKE_WIDTH = 5.0f;
    private final FirebaseVisionText.Element text;

    private final Paint rectPaint;
    private final Paint textPaint;
    private RectF rect;

    public TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
        super(overlay);
        this.type="text";
        this.text = text;
        this.rect = new RectF(text.getBoundingBox());

        rectPaint = new Paint();
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setStrokeWidth(9f);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        if (text == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        // Renders the text at the bottom of the box.
        canvas.drawText(" " +text.getText(), rect.left, rect.bottom-10, textPaint);
    }
}