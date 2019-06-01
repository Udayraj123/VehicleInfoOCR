package com.example.ocr.utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;

/**
 * This class provides utilities for camera.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static int KSIZE_BLUR = 3;
    private static int KSIZE_CLOSE = 10;
    private static int GAMMA_HIGH = 125 ;
    private static int CANNY_THRESHOLD_L = 85;
    private static int CANNY_THRESHOLD_U = 185;
    private static int TRUNC_THRESH = 100;
    private static int ZERO_THRESH = 155;

    public static Bitmap preProcessBitmap(Bitmap image){
        Mat inputMat = new Mat();//image.getHeight(),image.getWidth(),CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(image, inputMat);
        Utils.preProcessMat(inputMat);
        Bitmap outImage = Bitmap.createBitmap(image.getWidth(),image.getHeight(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(inputMat, outImage);
        inputMat.release();
        return outImage;
    }
    private static byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    private static void gamma(Mat mat, double gammaValue){
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total()*lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(Math.pow(i / 255f, 1f/gammaValue) * 255f);
        }
        lookUpTable.put(0, 0, lookUpTableData);
        Core.LUT(mat, lookUpTable, mat);
    }

    private static void preProcessMat(Mat processedMat ){
        normalize(processedMat);
        Imgproc.threshold(processedMat,processedMat,TRUNC_THRESH,255,Imgproc.THRESH_TRUNC);

        if(KSIZE_BLUR > 0) {
            Imgproc.blur(processedMat, processedMat, new Size(KSIZE_BLUR, KSIZE_BLUR));
        }
        //        normalize(processedMat);
//        morph(processedMat);
//        return processedMat;
    }

    private static void normalize(Mat processedMat){
        Core.normalize(processedMat, processedMat, 0, 255, Core.NORM_MINMAX);
    }

    // private static void threshBinary(Mat processedMat) {
    //     Imgproc.threshold(processedMat, processedMat, CANNY_THRESH, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
    // }
    private static void canny(Mat processedMat) {
        Imgproc.Canny(processedMat, processedMat, CANNY_THRESHOLD_U, CANNY_THRESHOLD_L);
        // threshBinary(processedMat);
    }
    private static Mat morph_kernel = new Mat(new Size(KSIZE_CLOSE, KSIZE_CLOSE), CvType.CV_8UC1, new Scalar(255));

    private static void thresh(Mat processedMat) {
        Imgproc.threshold(processedMat,processedMat,ZERO_THRESH,255,Imgproc.THRESH_TOZERO);
    }
    private static void morph(Mat processedMat) {
        // reduce noisy lines
        Imgproc.morphologyEx(processedMat, processedMat, Imgproc.MORPH_OPEN, morph_kernel, new Point(-1,-1),1);
    }
    private static void logShape(String name, Mat m) {
        Log.d("custom"+TAG, "matrix: "+name+" shape: "+m.rows()+"x"+m.cols());
    }

    private static Bitmap decodeBitmapFromFile(String path, String imageName) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(new File(path, imageName).getAbsolutePath(),
                options);
    }

}
