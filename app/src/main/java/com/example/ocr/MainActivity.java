package com.example.ocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ocr.Jsoup.AsyncCaptchaResponse;
import com.example.ocr.Jsoup.AsyncResponse;
import com.example.ocr.Jsoup.FetchVehicleDetails;
import com.example.ocr.Jsoup.GetCaptcha;
import com.example.ocr.Jsoup.Vehicle;

import java.io.IOException;
import java.util.List;

import com.example.ocr.text_detection.*;
import com.example.ocr.camera.*;
import com.example.ocr.others.*;
import com.example.ocr.utils.Utils;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.jackandphantom.androidlikebutton.AndroidLikeButton;

import br.vince.owlbottomsheet.OwlBottomSheet;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java3");
    }
    private final int OK = 200;
    private final int SOCKET_TIMEOUT = 408;
    private final int CAPTCHA_LOAD_FAILED = 999;
    private final int TECHNICAL_DIFFICULTY = 888;
    public final int ACTION_NULL = -1;
    public final String NUMPLATE_PATTERN = "[A-Z]{2}[0-9]{2}[A-Z]+[0-9]{3}[0-9]+";

    //  ----- Instance Variables -----
    // private EditTextPicker vehicleNumber;

    // the bottom sheet
    private OwlBottomSheet mBottomSheet;
    private EditText vehicleNumber;
    private ImageView imageView;
    private Button searchBtn;
    private View bottomSheetView;
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
        mBottomSheet = findViewById(R.id.owl_bottom_sheet);
        setupView();
        imageView = bottomSheetView.findViewById(R.id.captcha_img);
        vehicleDetails = bottomSheetView.findViewById(R.id.vehicle_details);
        searchBtn = bottomSheetView.findViewById(R.id.search_btn);

        //FirebaseApp.initializeApp(this);

        captchaInput = bottomSheetView.findViewById(R.id.captcha_input);
        captchaInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event!=null&&(event.getKeyCode()==KeyEvent.KEYCODE_ENTER))||(actionId== EditorInfo.IME_ACTION_DONE)){
                    searchBtn.performClick();
                }
                return false;
            }
        });
        setSearchButtonListener();
        //Auto cap input
        vehicleNumber = bottomSheetView.findViewById(R.id.vehicle_plate);
        vehicleNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        vehicleNumber.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void afterTextChanged(Editable et) {
                searchBtn.setEnabled(et.toString().matches(NUMPLATE_PATTERN));
            }
        });
        searchBtn.setEnabled(vehicleNumber.getText().toString().matches(NUMPLATE_PATTERN));

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
                            confirmVehicleNumber();
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

    // collapse bottom sheet when back button pressed
    @Override
    public void onBackPressed() {
        if (!mBottomSheet.isExpanded()){
            // warningBack = true, set timer
            super.onBackPressed();
        }
        else
            mBottomSheet.collapse();
    }

    // basic usage
    private void setupView() {
    
        //used to calculate some animations. it's required
        mBottomSheet.setActivityView(this);
        
        //icon to show in collapsed sheet
        mBottomSheet.setIcon(R.drawable.bubble2);

        //bottom sheet color
        mBottomSheet.setBottomSheetColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        //view shown in bottom sheet
        mBottomSheet.attachContentView(R.layout.main_content);
        bottomSheetView = mBottomSheet.getContentView();
        //getting close button from view shown
        bottomSheetView.findViewById(R.id.vehicle_details_close_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheet.collapse();
                }});
    }

    private void confirmVehicleNumber() {
        if (cameraSource != null) {
        // show overlay display here
            List<FirebaseVisionText.TextBlock> textBlocks = cameraSource.frameProcessor.textBlocks;
            for (int i = 0; i < textBlocks.size(); i++) {

            }
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

                    imageView.setImageBitmap(captchaImage);
                    //Done: preprocess the bitmap and pass to mlkit
                    final Bitmap processedCaptchaImage = Utils.preProcessBitmap(captchaImage);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Captcha image processed", Toast.LENGTH_SHORT).show();
                            imageView.setImageBitmap(processedCaptchaImage);
                        }
                    },1000);
                    String detectedCaptcha = cameraSource.frameProcessor.processBitmap(processedCaptchaImage, cameraSource.rotation, cameraSource.facing,graphicOverlay);

                    // if(captchaInput.getText().toString()=="")
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

        // THIS CAUSES INPUTS TO DESTROY AND CAUSE POSSIBLE CRASH
        // final Handler handler = new Handler();
        // Runnable runnable = new Runnable(){
        //     @Override
        //     public void run(){
        //         if (cameraSource != null) {
        //             vehicleNumber.setText(cameraSource.frameProcessor.majorText);
        //             handler.postDelayed(this,1000);
        //         }
        //     }
        // };
        // handler.post(runnable);
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
    private void setSearchButtonListener() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
                String result = vehicleNumber.getText().toString();
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

    public void cpno(View view) {
        ImageButton b = (ImageButton) findViewById(R.id.vhecp);
        if (edittext.getText().toString() != null) {
            b.setVisibility(View.VISIBLE);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Vehicle Number", edittext.getText().toString());
        }
        else{
            b.setVisibility(View.GONE);
        }
    }

    public void detailscp(View view) {
        TextView t1,t2,t3,t4,t5,t6,t7,t8;
        t1=(TextView)findViewById(R.id.vehicle_name);
        t2=(TextView)findViewById(R.id.vehicle_owner);
        t3=(TextView)findViewById(R.id.vehicle_fuel);
        t4=(TextView)findViewById(R.id.vehicle_cc);
        t5=(TextView)findViewById(R.id.vehicle_engine);
        t6=(TextView)findViewById(R.id.vehicle_chasis);
        t7=(TextView)findViewById(R.id.vehicle_location);
        t8=(TextView)findViewById(R.id.vehicle_expiry);
        String s="Vehicle Number : "+edittext.getText().toString()+"\n Vehicle Name : "+t1.getText().toString()+"\n Owner Name : "+t2.getText().toString()+"\n Fuel Type : "+t3.getText().toString()+"\n Displacement : "+t4.getText().toString()+"\n Engine Number : "+t5.getText().toString()+"\n Chasis Number : "+t6.getText().toString()+"\n Location : "+t7.getText().toString()+"\n Expiry On : "+t8.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Vehicle details", s);
    }
}