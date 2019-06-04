// package com.example.ocr.utils;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.util.Log;
// import org.opencv.core.Core;
// import org.opencv.core.CvType;
// import org.opencv.core.Mat;
// import org.opencv.core.Point;
// import org.opencv.core.Rect;
// import org.opencv.core.Scalar;
// import org.opencv.core.Size;
// import org.opencv.imgproc.Imgproc;
// import java.io.File;
//
// import static org.opencv.core.Core.BORDER_CONSTANT;
// import static org.opencv.core.Core.copyMakeBorder;
//
// /**
//  * This class provides utilities for camera.
//  */
//
// public class Utils {
//     private static final String TAG = Utils.class.getSimpleName();
//     private static int U_WIDTH = 300;
//     private static int KSIZE_CLOSE = 5;
//     private static int KSIZE_BLUR = 5;
//     private static int TRUNC_THRESH = 60;
//     private static int ZERO_THRESH = 30;
//
//     public static Bitmap preProcessBitmap(Bitmap image){
//         Mat inputMat = new Mat();//image.getHeight(),image.getWidth(),CvType.CV_8UC1);
//         org.opencv.android.Utils.bitmapToMat(image, inputMat);
//         inputMat = Utils.preProcessMat(inputMat);
//         Bitmap outImage = Bitmap.createBitmap(inputMat.width(),inputMat.height(), Bitmap.Config.ARGB_8888);
//         org.opencv.android.Utils.matToBitmap(inputMat, outImage);
//         inputMat.release();
//         return outImage;
//     }
//     private static Mat morph_kernel = new Mat(new Size(1, KSIZE_CLOSE), CvType.CV_8UC1, new Scalar(255));
//
//     private static Mat preProcessMat(Mat processedMat ){
//         resize_util_inplace(processedMat, U_WIDTH);
//         if(KSIZE_BLUR > 0) {
//             Imgproc.blur(processedMat, processedMat, new Size(KSIZE_BLUR, KSIZE_BLUR));
//         }
//         Imgproc.threshold(processedMat,processedMat,TRUNC_THRESH,255,Imgproc.THRESH_TRUNC);
//         Core.normalize(processedMat, processedMat, 0, 255, Core.NORM_MINMAX);
//         Imgproc.threshold(processedMat,processedMat,ZERO_THRESH,255,Imgproc.THRESH_TOZERO);
//         Imgproc.morphologyEx(processedMat, processedMat, Imgproc.MORPH_CLOSE, morph_kernel, new Point(-1,-1),1);
//         Imgproc.dilate(processedMat,processedMat,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
//         Imgproc.erode(processedMat,processedMat,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
//         //pad at last
//         Mat padded = new Mat();
//         int padding = U_WIDTH/2;
//         copyMakeBorder(processedMat,padded,padding,padding,padding,padding,BORDER_CONSTANT,new Scalar(255,255,255));
//         processedMat.release();
//         return padded;
//     }
//     public static void resize_util_inplace(Mat image, int u_width, int u_height) {
//         Size sz = new Size(u_width,u_height);
//         if(image.cols() > u_width)
//                 // for downscaling
//             Imgproc.resize(image,image ,sz, 0,0 ,Imgproc.INTER_AREA);
//         else
//                 // for upscaling
//             Imgproc.resize(image,image ,sz, 0,0 ,Imgproc.INTER_CUBIC);
//     }
//
//     public static void resize_util_inplace(Mat image, int u_width) {
//         if(image.cols() == 0)return;
//         int u_height = (image.rows() * u_width)/image.cols();
//         resize_util_inplace(image,u_width,u_height);
//     }
//
//     private static void logShape(String name, Mat m) {
//         Log.d("custom"+TAG, "matrix: "+name+" shape: "+m.rows()+"x"+m.cols());
//     }
//
//     private static Bitmap decodeBitmapFromFile(String path, String imageName) {
//         // First decode with inJustDecodeBounds=true to check dimensions
//         final BitmapFactory.Options options = new BitmapFactory.Options();
//         options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//         return BitmapFactory.decodeFile(new File(path, imageName).getAbsolutePath(),
//             options);
//     }
//
// }
