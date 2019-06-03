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
    private AndroidLikeButton camBtn;
    private EditText captchaInput;
    private ClipboardManager clipboard;

    public BitmapTextRecognizer bitmapProcessor;
    private com.example.ocr.util.SimplePermissions permHandler;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBottomSheetView();

        bitmapProcessor = new BitmapTextRecognizer();
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        preview = findViewById(R.id.camera_source_preview);
        graphicOverlay = findViewById(R.id.graphics_overlay);
        imageView = bottomSheetView.findViewById(R.id.captcha_img);
        vehicleDetails = bottomSheetView.findViewById(R.id.vehicle_details);
        searchBtn = bottomSheetView.findViewById(R.id.search_btn);
        setCaptchaInputListeners();
        setSearchButtonListeners();
        setVehicleNumListeners();

        permHandler = new com.example.ocr.util.SimplePermissions(this, new String[]{
                // android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        });
        if(!permHandler.hasAllPermissions())
            Toast.makeText(MainActivity.this, "Checking permissions...", Toast.LENGTH_SHORT).show();
        // should usually be the last line in init
        permHandler.grantPermissions();
    }
    //    callback from ActivityCompat.requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String PermissionsList[], @NonNull int[] grantResults) {
        // https://stackoverflow.com/questions/34342816/android-6-0-multiple-PermissionsList
        if (permHandler.hasAllPermissions()) {
            Toast.makeText(MainActivity.this, "Permissions granted", Toast.LENGTH_SHORT).show();
            camBtn = findViewById(R.id.cam_btn);
            camBtn.setOnLikeEventListener(new AndroidLikeButton.OnLikeEventListener() {
                @Override
                public void onLikeClicked(AndroidLikeButton androidLikeButton) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startLoadingCaptcha();
                            createCameraSource();
                            startCameraSource();
                        }
                    });
                }
                @Override
                public void onUnlikeClicked(AndroidLikeButton androidLikeButton){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            confirmVehicleNumber();
                            stopCameraSource();
                        }
                    });
                }
            });

            setFlashListeners();
            // default is start the camera
            camBtn.performClick();
        }
        else {
            Toast.makeText(MainActivity.this, "Please enable the permissions from settings. Exiting App!", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
    }
    // basic usage
    private void setBottomSheetView() {
        mBottomSheet = findViewById(R.id.owl_bottom_sheet);
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
    private String numPlateFilter(String s){
        return s.toUpperCase().replaceAll("[^A-Z0-9]","");
    }

    private void confirmVehicleNumber() {
        if (cameraSource != null && cameraSource.frameProcessor.textBlocks != null) {
            List<FirebaseVisionText.TextBlock> textBlocks = cameraSource.frameProcessor.textBlocks;
            for (int i = 0; i < textBlocks.size(); i++) {
                // set vehicleNum here
            }
        }
    }

    private void startLoadingCaptcha() {

        mBottomSheet.setIcon(R.drawable.bubble2);
        captchaInput.setText("");
        vehicleDetails.setVisibility(View.GONE);
        Log.d(TAG,"Started Loading Captcha");
        new GetCaptcha(new AsyncCaptchaResponse(){
            @Override
            public void processFinish(Bitmap captchaImage, int statuscode) {
                if (captchaImage != null){
                    Log.d(TAG,"Received Captcha");
                    // Toast.makeText(MainActivity.this, "Captcha image loaded", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(captchaImage);
                    //Done: preprocess the bitmap and pass to mlkit
                    final Bitmap processedCaptchaImage = Utils.preProcessBitmap(captchaImage);

                    // TEMPORARY delay code
                    // new Handler().postDelayed(new Runnable() {
                    //     @Override
                    //     public void run() {
                    //         // Toast.makeText(MainActivity.this, "Captcha image processed", Toast.LENGTH_SHORT).show();
                    //         imageView.setImageBitmap(processedCaptchaImage);
                    //     }
                    // },1000);
                    // ^TEMPORARY delay code

                    // start thread here.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            bitmapProcessor.processBitmap(processedCaptchaImage);
                            Log.d(TAG,"Setting captcha text: "+bitmapProcessor.allText);
                            String detectedCaptcha = bitmapProcessor.allText;
                            // if(captchaInput.getText().toString()=="")
                            captchaInput.setText(detectedCaptcha);
                        }
                    }).start();
                    mBottomSheet.setIcon(R.drawable.bubble_pop2);
                }
                else{
                    Log.d(TAG,"Captcha Could not be loaded");
                    Toast.makeText(MainActivity.this, "Captcha Could not be loaded! Check internet.", Toast.LENGTH_SHORT).show();
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
        // show drawer edge-
        // if(captchaInput.getText().toString()!="")
        // drawerEdge.setVisible(true)

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
    private void setFlashListeners() {
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
    }
    private void setCaptchaInputListeners() {
        captchaInput = bottomSheetView.findViewById(R.id.captcha_input);
        // submit on press enter
        captchaInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event!=null&&(event.getKeyCode()==KeyEvent.KEYCODE_ENTER))||(actionId== EditorInfo.IME_ACTION_DONE)){
                    searchBtn.performClick();
                }
                return false;
            }
        });
    }
    private void logToast(String msg) {
        Log.d(TAG,msg);
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    private void setSearchButtonListeners() {
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
                                logToast("Vehicle details not found");

                            }
                        }
                        else if (statusCode == CAPTCHA_LOAD_FAILED){
                            // Done: reload button?!
                            logToast("Captcha Load Failed!");
                        }
                        else if (statusCode == TECHNICAL_DIFFICULTY){
                            logToast("Technical Difficulty. Failed to fetch from table!");
                            // .title(R.string.error_technical_difficulty)

                        }
                        else if (statusCode == SOCKET_TIMEOUT){
                            logToast("Internet Timeout.. Slow internet?");
                        }
                        else{
                            //no internet
                            // verify using isNetworkAvailable()
                            logToast("Internet Unavailable");
                        }
                    }
                }).execute(result.substring(0, result.length() - 4), result.substring(result.length() - 4), captchaInput.getText().toString());
            }
        });
    }
    private void setVehicleNumListeners() {

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
        vehicleNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    vehicleNumber.setText(numPlateFilter(vehicleNumber.getText().toString()));
            }
        });
    }

    public void copyNumber(View view) {
        String vehicleNum = numPlateFilter(vehicleNumber.getText().toString());
        Log.d(TAG,"Copying : "+vehicleNum);
        if(vehicleNum != "") {
            ClipData clip = ClipData.newPlainText("Vehicle Number", vehicleNum);
            clipboard.setPrimaryClip(clip);
            logToast("Text Copied \ud83d\ude00 :  "+vehicleNum);
        }
        else{
            logToast("Cannot copy empty text");
        }
    }

    public void copyDetails(View view) {
        TextView t1,t2,t3,t4,t5,t6,t7,t8;
        t1 = bottomSheetView.findViewById(R.id.vehicle_name);
        t2 = bottomSheetView.findViewById(R.id.vehicle_owner);
        t3 = bottomSheetView.findViewById(R.id.vehicle_fuel);
        t4 = bottomSheetView.findViewById(R.id.vehicle_cc);
        t5 = bottomSheetView.findViewById(R.id.vehicle_engine);
        t6 = bottomSheetView.findViewById(R.id.vehicle_chasis);
        t7 = bottomSheetView.findViewById(R.id.vehicle_location);
        t8 = bottomSheetView.findViewById(R.id.vehicle_expiry);
        String s="Vehicle Number : "+vehicleNumber.getText().toString()+"\n Vehicle Name : "+t1.getText().toString()+"\n Owner Name : "+t2.getText().toString()+"\n Fuel Type : "+t3.getText().toString()+"\n Displacement : "+t4.getText().toString()+"\n Engine Number : "+t5.getText().toString()+"\n Chasis Number : "+t6.getText().toString()+"\n Location : "+t7.getText().toString()+"\n Expiry On : "+t8.getText().toString();
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Vehicle details", s);
        clipboard.setPrimaryClip(clip);
        logToast("Text Copied! "+("\ud83d\ude00"));
    }

    private boolean warningBack = false;
    // collapse bottom sheet when back button pressed
    @Override
    public void onBackPressed() {
        if (!mBottomSheet.isExpanded()){
            if(cameraSource != null){
                if(warningBack)
                    super.onBackPressed();
                else{
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                    warningBack = true;
                    // reset after delay
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            warningBack = false;
                        }
                    },2500);
                }
            }
            else{
                camBtn.performClick();
            }
        }
        else
            mBottomSheet.collapse();
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
        if(bitmapProcessor!=null)
            bitmapProcessor.stop();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
    private void stopCameraSource() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }
}