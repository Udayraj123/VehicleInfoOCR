package com.example.ocr.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;

import com.example.ocr.graphics.*;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TrailGraphic extends GraphicOverlay.Graphic {
    static final int TRAIL_MAX_COUNT = 50; //maximum trail array size
    static final int TRAIL_DRAW_POINT = 30; //number of points to split the trail for draw

    private ConcurrentLinkedQueue<Point> trail;
    private Paint[] trailPaints;
    private float[][] trailPoss, trailTans;
    private Path trailPath;
    // draw trail from here
    private void FillTrail() {
        trailPath.reset();
        boolean isFirst = true;
        for(Point p : trail) {
            if(isFirst) {
                trailPath.moveTo(p.x, p.y);
                trailPoss[0][0] = p.x;
                trailPoss[0][1] = p.y;
                isFirst = false;
            } else {
                trailPath.lineTo(p.x, p.y);
            }
        }
        PathMeasure path = new PathMeasure(trailPath, false);
        float step = path.getLength() / TRAIL_DRAW_POINT;
        for(int i=0; i<TRAIL_DRAW_POINT; i++) {
            path.getPosTan(step * i, trailPoss[i], trailTans[i]);
        }
    }

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 5.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionText.Element text;

    TrailGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
        super(overlay);

        this.text = text;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
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
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        // Renders the text at the bottom of the box.
        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);
    }
}