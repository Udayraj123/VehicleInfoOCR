package com.example.ocr.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private int previewWidth;
    private float widthScaleFactor = 1.0f;
    private int previewHeight;
    private float heightScaleFactor = 1.0f;
    private int facing = CameraSource.CAMERA_FACING_BACK;
    private Set<Graphic> graphics = new HashSet<>();
    public abstract static class Graphic {
        private GraphicOverlay overlay;
        protected String type="";

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }
        public abstract void draw(Canvas canvas);
        public float scaleX(float horizontal) {
            return horizontal * overlay.widthScaleFactor;
        }
        public float scaleY(float vertical) {
            return vertical * overlay.heightScaleFactor;
        }
        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }
        public float translateX(float x) {
            if (overlay.facing == CameraSource.CAMERA_FACING_FRONT) {
                return overlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }
        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }
    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }
    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.facing = facing;
        }
        postInvalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            if ((previewWidth != 0) && (previewHeight != 0)) {
                widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
                heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
            }

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }

    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    //
    //     PointF touch = new PointF(event.getX(),event.getY());
    //     // switch (event.getAction()){
    //     //     case MotionEvent.ACTION_DOWN:
    //     //         break;
    //     //
    //     //     case MotionEvent.ACTION_MOVE:
    //     //         break;
    //     //
    //     //     case MotionEvent.ACTION_UP:
    //     //         break;
    //     // }
    //     Log.d("Graphicoverlay: ","touched : "+touch);
    //     for (Graphic graphic : graphics) {
    //         if(graphic.type.equals("text")){
    //             TextGraphic t = (TextGraphic)graphic;
    //             if(t.rect.contains(touch.x,touch.y)){
    //                 t.rectPaint.setColor(Color.YELLOW);
    //                 t.textPaint.setColor(Color.GREEN);
    //             }
    //         }
    //     }
    //     return false;
    // }

}