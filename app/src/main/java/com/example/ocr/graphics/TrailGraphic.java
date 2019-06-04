// package com.example.ocr.graphics;
//
// import android.graphics.Canvas;
// import android.graphics.Color;
// import android.graphics.LinearGradient;
// import android.graphics.Paint;
// import android.graphics.Path;
// import android.graphics.PathMeasure;
// import android.graphics.Point;
// import android.graphics.RectF;
// import android.graphics.Shader;
// import android.util.Log;
//
// import java.util.concurrent.ConcurrentLinkedQueue;
//
// public class TrailGraphic extends GraphicOverlay.Graphic {
//     static final int TRAIL_MAX_COUNT = 50; //maximum trail array size
//     static final int TRAIL_DRAW_POINT = 30; //number of points to split the trail for draw
//     static final String TAG = "TrailGraphic";
//     private ConcurrentLinkedQueue<Point> trail;
//     private Paint trailPaint;
//     private int radius = 5;
//     private Path trailPath;
//     public void addPoint(Point position) {
//         trail.add(position);
//         if(trail.size() > TRAIL_MAX_COUNT) {
//             trail.remove();
//             // Log.d(TAG,"Trail Point Removed");
//         }
//         // Log.d(TAG,"Trail Point Added: "+position);
//         // fillTrail();
//     }
//     // draw trail from here
//     public void fillTrail() {
//         trailPath.reset();
//         boolean isFirst = true;
//         Point prev = new Point(0,0);
//         for(Point p : trail) {
//             if(isFirst) {
//                 trailPath.moveTo(p.x, p.y);
//                 isFirst = false;
//             } else {
//                 trailPath.lineTo(p.x, p.y);
//                 // trailPath.addArc(new RectF(p.x,prev.y,prev.x,p.y),0,35);
//             }
//             prev=p;
//         }
//     }
//     public TrailGraphic(GraphicOverlay overlay) {
//         super(overlay);
//         Log.d(TAG,"new TrailGraphic");
//
//         trail = new ConcurrentLinkedQueue<Point>();
//         trailPath = new Path();
//         trailPaint = new Paint();
//         // int x1 = 0, y1 = 0, x2 = 0,  y2 = 40;
//         // Shader shader = new LinearGradient(0, 0, 0, 40, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
//         // trailPaint.setShader(shader);
//         trailPaint.setStyle(Paint.Style.STROKE);
//         trailPaint.setColor(Color.BLUE);
//         trailPaint.setStrokeWidth(radius * 2);
//         trailPaint.setAlpha(150);
//         // Redraw the overlay, as this graphic has been added.
//         postInvalidate();
//     }
//
//     @Override
//     public void draw(Canvas canvas) {
//         canvas.drawPath(trailPath, trailPaint);
//     }
// }