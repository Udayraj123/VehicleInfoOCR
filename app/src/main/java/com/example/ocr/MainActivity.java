package com.example.ocr;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edittextpicker.aliazaz.EditTextPicker;
import com.example.ocr.Jsoup.AsyncCaptchaResponse;
import com.example.ocr.Jsoup.AsyncResponse;
import com.example.ocr.Jsoup.FetchVehicleDetails;
import com.example.ocr.Jsoup.GetCaptcha;
import com.example.ocr.Jsoup.Vehicle;

import java.io.IOException;
import com.example.ocr.text_detection.*;
import com.example.ocr.camera.*;
import com.example.ocr.others.*;
import com.example.ocr.utils.Utils;
import com.jackandphantom.androidlikebutton.AndroidLikeButton;

import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java3");
    }
    private final int OK = 200;
    private final int SOCKET_TIMEOUT = 408;
    private final int CAPTCHA_LOAD_FAILED = 999;
    private final int TECHNICAL_DIFFICULTY = 888;
    public final int ACTION_NULL = -1;

    //  ----- Instance Variables -----
    private EditTextPicker edittext;
    private ImageView imageView;
    private Button searchBtn;
    private View vehicleDetails;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private EditText captchaInput;
    private com.example.ocr.util.SimplePermissions permHandler;
    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preview = findViewById(R.id.camera_source_preview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphics_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        imageView = findViewById(R.id.captcha_img);
        vehicleDetails = findViewById(R.id.vehicle_details);
        searchBtn = findViewById(R.id.search_btn);
        //FirebaseApp.initializeApp(this);
        captchaInput = findViewById(R.id.captcha_input);
        captchaInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event!=null&&(event.getKeyCode()==KeyEvent.KEYCODE_ENTER))||(actionId== EditorInfo.IME_ACTION_DONE)){
                    searchBtn.performClick();
                }
                return false;
            }
        });
        setSearchButtonListner();
        //Auto cap input
        edittext = findViewById(R.id.vehicle_number);
        edittext.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                searchBtn.setEnabled(edittext.isTextEqualToPattern());
            }

            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void afterTextChanged(Editable et) {}
        });
        searchBtn.setEnabled(edittext.isTextEqualToPattern());

        //Done: add permission handler from omr here
        Toast.makeText(MainActivity.this, "Checking permissions...", Toast.LENGTH_SHORT).show();
        permHandler = new com.example.ocr.util.SimplePermissions(this, new String[]{
                // android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        });
        // should usually be the last line in init
        permHandler.grantPermissions();
    }
    //    callback from ActivityCompat.requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String PermissionsList[], @NonNull int[] grantResults) {
        // https://stackoverflow.com/questions/34342816/android-6-0-multiple-PermissionsList
        if (permHandler.hasAllPermissions()) {
            Toast.makeText(MainActivity.this, "Permissions granted", Toast.LENGTH_SHORT).show();
            AndroidLikeButton btn = findViewById(R.id.cam_btn);
            btn.setOnLikeEventListener(new AndroidLikeButton.OnLikeEventListener() {
                @Override
                public void onLikeClicked(AndroidLikeButton androidLikeButton) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopCameraSource();
                        }
                    });
                }
                @Override
                public void onUnlikeClicked(AndroidLikeButton androidLikeButton){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startLoadingCaptcha();
                            createCameraSource();
                            startCameraSource();
                        }
                    });
                }
            });

            AndroidLikeButton flashBtn = findViewById(R.id.flash_btn);
            flashBtn.setOnLikeEventListener(new AndroidLikeButton.OnLikeEventListener() {
                @Override
                public void onLikeClicked(AndroidLikeButton androidLikeButton) {
                    if(cameraSource!=null)
                        cameraSource.turnOnTheFlash();
                }
                public void onUnlikeClicked(AndroidLikeButton androidLikeButton){
                    if(cameraSource!=null)
                        cameraSource.turnOffTheFlash();
                }
            });
            startLoadingCaptcha();
            createCameraSource();
            startCameraSource();
        }
        else {
            Toast.makeText(MainActivity.this, "Please manually enable the permissions from settings. Exiting App!", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void startLoadingCaptcha() {
        captchaInput.setText("");
        vehicleDetails.setVisibility(View.GONE);

        Log.d(TAG,"Started Loading Captcha");
        new GetCaptcha(new AsyncCaptchaResponse(){
            @Override
            public void processFinish(Bitmap captchaImage, int statuscode) {
                Toast.makeText(MainActivity.this, "Sent Request", Toast.LENGTH_SHORT).show();
                if (captchaImage != null){
                    Log.d(TAG,"Received Captcha");
                    Toast.makeText(MainActivity.this, "Captcha image loaded", Toast.LENGTH_SHORT).show();
                    // show popup message here:

                    // show drawer edge-
                    // drawer.setVisible(true)

                    //Done: preprocess the bitmap and pass to mlkit
                    captchaImage = Utils.preProcessBitmap(captchaImage);
                    Toast.makeText(MainActivity.this, "Captcha image processed", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(captchaImage);
                    String detectedCaptcha = cameraSource.frameProcessor.processBitmap(captchaImage,
                        cameraSource.rotation, cameraSource.facing,graphicOverlay);
                    if(captchaInput.getText().toString()=="")
                        captchaInput.setText(detectedCaptcha);

                    //TODO:  add Cancel button to drawer

                }
                else{
                    Log.d(TAG,"Captcha Could not be loaded");
                    Toast.makeText(MainActivity.this, "Captcha Could not be loaded", Toast.LENGTH_SHORT).show();
                    // fragmentCallback.showSnackBar(R.string.error_server_busy
                }
            }
        }).execute();
    }
    private void showVehicleDetails(Vehicle vehicle) {

        ((TextView)findViewById(R.id.vehicle_name)).setText(vehicle.getName());
        ((TextView)findViewById(R.id.vehicle_fuel)).setText(vehicle.getFuel());
        ((TextView)findViewById(R.id.vehicle_cc)).setText(vehicle.getCc());
        ((TextView)findViewById(R.id.vehicle_engine)).setText(vehicle.getEngine());
        ((TextView)findViewById(R.id.vehicle_chasis)).setText(vehicle.getChassis());
        ((TextView)findViewById(R.id.vehicle_owner)).setText(vehicle.getOwner());
        ((TextView)findViewById(R.id.vehicle_location)).setText(vehicle.getLocation());
        ((TextView)findViewById(R.id.vehicle_expiry)).setText(vehicle.getExpiry());
    }
    private void createCameraSource() {

        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        }

        cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                stopCameraSource();
            }
        }
    }
    private void setSearchButtonListner() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
                String result = edittext.getText().toString();
                // drawer.dismiss();
                new FetchVehicleDetails(new AsyncResponse(){
                    //uses most recent cookies and formnumber
                    @Override
                    public void processFinish(Vehicle vehicle, int statusCode) {
                        Log.d(TAG,"Finished Status code:"+statusCode);
                        if (statusCode == OK) {
                            if (vehicle != null){
                                showVehicleDetails(vehicle);
                            }
                            else {
                                String msg = "Vehicle details not found";
                                Log.d(TAG,msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                
                            }
                        }
                        else if (statusCode == CAPTCHA_LOAD_FAILED){
                            // Done: reload button?!
                            String msg = "Captcha Load Failed!";
                            Log.d(TAG,msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        else if (statusCode == TECHNICAL_DIFFICULTY){
                            String msg = "Technical Difficulty. Failed to fetch from table!";
                            Log.d(TAG,msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            // .title(R.string.error_technical_difficulty)

                        }
                        else if (statusCode == SOCKET_TIMEOUT){
                            String msg = "Internet Timeout.. Slow internet?";
                            Log.d(TAG,msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //no internet
                            // verify using isNetworkAvailable()
                            Log.d(TAG,"Internet Unavailable");
                            Toast.makeText(MainActivity.this, "Internet Unavailable", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute(result.substring(0, result.length() - 4), result.substring(result.length() - 4), captchaInput.getText().toString());
            }
        });
    }
    private void stopCameraSource() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }
}