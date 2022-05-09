package com.udayraj.vehicleinfolive;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.crashlytics.android.Crashlytics;
import com.edittextpicker.aliazaz.EditTextPicker;
import com.udayraj.vehicleinfolive.text_detection.*;
import com.udayraj.vehicleinfolive.camera.*;
import com.udayraj.vehicleinfolive.graphics.*;
import com.udayraj.vehicleinfolive.utils.*;
import com.udayraj.vehicleinfolive.webscraper.*;
import com.rbddevs.splashy.Splashy;
//import android.view.animation.Animation;
//import com.google.firebase.ml.vision.text.FirebaseVisionText;
// import nl.dionsegijn.konfetti.KonfettiView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity implements AppEvents {
    public final String NUMPLATE_PATTERN = "[A-Z]{2}[0-9]+[A-Z]+[0-9]+";
    private final String CRYING_EMOJI = "\ud83d\ude36";
    private final String SLEEP_EMOJI = "\ud83d\udca4";
    private final String SMILE_EMOJI = "\ud83d\ude00";
//    private final String PROCESSING_EMOJI = "⏳";
//    private final String NOPE_EMOJI = "\u26d4";
//    private final String DONE_EMOJI = "\ud83d\udc4c";
//    private final String POPPER_EMOJI = "\ud83c\udf89";
//    private final String HEART_EMOJI = "\ud83d\udc9b";
//    private final String LOVE_EMOJI = "\ud83d\udc96";

//    Set OMRChecker running.
    // Upgrade to androidX

    // Remove run2 and other messups from the crawler

//    JS clean up on request done...
// Remove concept of pausing camera. Pressing camera opens the box?

// Automate publish and test

// Keep next feature ready - open from gallery!
    // Design to make it intuitive to use Text typing as well
    //                <!--icon credits: flaticons -->
    //    Toggle captcha letters on tap to switch in m-M, 0-o-O, etc. And separate edit button
    // Webview loading bar
// Manage Tags on console


    // private DrawingArea drawingArea;
    private ViewGroup superContainer;
    private EditTextPicker vehicleNumber;
    private ImageView captchaImageView;
    private View supportView;
    // private KonfettiView viewKonfetti;
//    private View fruitNinja;
    private View drawerView;
    private CameraSource cameraSource = null;
    private CameraSourcePreview cameraPreview;
    private GraphicOverlay graphicOverlay;
    private EditText captchaInput;
    private ClipboardManager clipboard;
    private WebScraper webScraper;
    private ImageBitmapGetter captchaBitmapGetter;
    public BitmapTextRecognizer bitmapProcessor;
    private SimplePermissions permHandler;
    private ImageButton camBtn;
    private ImageButton flashBtn;
    private ImageButton drawerBtn;
    private Button searchBtn;

    private boolean canDoTransitions;
    private Element eltCaptchaImage;
    private Element eltVehicleNumber;
    private Element eltCaptchaInput;
    private Element eltSubmitBtn;
    private static String TAG = "MainActivity";
    private final String BASE_URL = "https://vahan.nic.in";
    private final String VEHICLE_URL="/nrservices/faces/user/searchstatus.xhtml";
    private final String FULL_URL=BASE_URL+VEHICLE_URL;

    private Set<String> platesHash = new HashSet<>();
    private Queue<String> platesQueue = new LinkedList<>();
    private List<Button> buttons = new ArrayList<>();
    private int NUM_PLATES;

    private Fade mFade;
    private String APP_LINK;
    private Resources resources;
    @Override @TargetApi(19)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"Begin Mainactivity User Init");
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        resources = getResources();
        canDoTransitions = resources.getString(R.string.can_do_transitions).equals("true");
        if(canDoTransitions){
            mFade = new Fade(Fade.IN);
            mFade.setDuration(800);
        }

        // heavy?!
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        superContainer = findViewById(R.id.super_container);

        cameraPreview = findViewById(R.id.camera_source_preview);
        graphicOverlay = findViewById(R.id.graphics_overlay);
        // viewKonfetti = findViewById(R.id.viewKonfetti);
        supportView = findViewById(R.id.support_view);
        drawerView = findViewById(R.id.drawer);
        captchaImageView = findViewById(R.id.captcha_img);
        captchaInput = findViewById(R.id.captcha_input);
        vehicleNumber = findViewById(R.id.vehicle_plate);
        // drawerView.getBackground().setAlpha(242);
        searchBtn = findViewById(R.id.search_btn);
        flashBtn = findViewById(R.id.flash_btn);
        drawerBtn = findViewById(R.id.drawer_btn);
        camBtn = findViewById(R.id.cam_btn);

        //TODO move stuff into diff functions

        new Splashy(this)
                .setLogo(R.drawable.splash_guide)
                .setLogoWHinDp(350,350) // default is 200x200
                .setTitle(R.string.app_name_orig)
                // .setTitleColor("#FFFFFF")
                .setSubTitle("Simple. Fast. Ad-free.")
                .setTitleSize(25f)
                .setSubTitleSize(20f)
                // .setProgressColor(R.color.black)
                .showProgress(true)
                .setFullScreen(true)
                .setTime(3000)
                .show();

        setCaptchaInputListeners();
        setSearchButtonListeners();
        setVehicleNumListeners();
        APP_LINK = "http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
        int[] dialog_ids = {R.id.support_btn,R.id.dont_support_btn, R.id.share_btn};
        for(int id : dialog_ids) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // viewKonfetti.setVisibility(INVISIBLE);
                    if(id == R.id.support_btn){
                        // https://stackoverflow.com/questions/10816757/rate-this-app-link-in-google-play-store-app-on-the-phone
                        Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        // To count with Play market backstack, After pressing back button,
                        // to taken back to our application, we need to add following flags to intent.
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
//                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
//                            Log.e(TAG, "ActivityNotFoundException",e);
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(APP_LINK)));
                        }
                    }else if(id == R.id.share_btn){
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Scan a number plate and get owner information in seconds - use "+resources.getString(R.string.app_name_orig)+": "+APP_LINK;
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name_orig)+": A Camera Based Vehicle Info App");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
//                    else if (id == R.id.dont_support_btn){
//                        MainActivity.this.finish();
//                    }
                    supportView.setVisibility(INVISIBLE);
                }
            });
        }


        int[] plate_ids = {R.id.plate1,R.id.plate2,R.id.plate3,R.id.plate4,R.id.plate5};
        NUM_PLATES = plate_ids.length;
        for(int id : plate_ids){
            Button btn = findViewById(id);
            buttons.add(btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleNumber.setText(btn.getText());
                    drawerBtn.performClick();
                }
            });
        }
        // Make the href links clickable
        int[] text_ids = {R.id.support_btn,R.id.developer_line1,R.id.developer_line2,R.id.developer_line3,R.id.developer_line4};//,R.id.developer_line5};
        for(int id : text_ids) {
            TextView t = findViewById(id);
            t.setMovementMethod(LinkMovementMethod.getInstance());
        }
        //easter egg
        View spacer = findViewById(R.id.spacer);
        TableLayout easterTable = findViewById(R.id.easter_table);

        final R.drawable drawableResources = new R.drawable();
        Field[] allDrawables = R.drawable.class.getFields();
        for (Field f : allDrawables){
            try {
                if(f.getName().contains("easter")){
//                    easter.add
                    TableRow row = new TableRow(MainActivity.this);
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

                    row.setBackgroundResource(f.getInt(drawableResources));
                    easterTable.addView(row);
//                    ImageView meme = new ImageView(MainActivity.this);
//                    meme.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,180));
//                    meme.setImageResource(f.getInt(drawableResources));//null also works
//                    row.addView(meme);
                    Log.d(TAG,"Added drawable "+f.getName()+" with id : "+f.getInt(drawableResources ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        spacer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                logToast("Easter egg!");

                findViewById(R.id.easter).setVisibility(VISIBLE);
                return false;
            }
        });

        // should usually be the last line in init
        getPermissionsAfterInit();
    }
    private void getPermissionsAfterInit(){
        permHandler = new SimplePermissions(this, new String[]{
                // android.Manifest.permission.WRITE_EXTERNAL_STORAGE, <- for error reporting
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE
        });
        if(!permHandler.hasAllPermissions())
            Toast.makeText(MainActivity.this, "Checking permissions...", Toast.LENGTH_SHORT).show();
        permHandler.grantPermissions();
    }
    //    callback from ActivityCompat.requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String PermissionsList[], @NonNull int[] grantResults) {
        // https://stackoverflow.com/questions/34342816/android-6-0-multiple-PermissionsList
        if (permHandler.hasAllPermissions()) {
//            Toast.makeText(MainActivity.this, "Permissions granted", Toast.LENGTH_SHORT).show();
            //todo: show this somewhere else
//             logToast("Press the Camera Button when the number plate is detected correctly");
            setCamButtonListeners();
            setFlashListeners();
            // Attempt for faster start :

            webScraper = new WebScraper(this);
            captchaBitmapGetter = new ImageBitmapGetter(MainActivity.this);
            webScraper.setUserAgentToDesktop(true); //default: false
            webScraper.setLoadImages(true); //default: false
            LinearLayout webview= findViewById(R.id.webview);
            webview.addView(webScraper.getView());
            bitmapProcessor = new BitmapTextRecognizer(MainActivity.this);
            startLoadingCaptcha();
            // start the camera
            camBtn.performClick();
        }
        else {
            Toast.makeText(MainActivity.this, "Please enable the permissions from settings. Exiting App!", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
    }
    private String captchaFilter(String s){
        // NOTE: small j and small i replaced if at 3rd pos!
        String filtered = s.replaceAll("[^a-zA-Z0-9]","");
        if(filtered.length() > 2 && filtered.charAt(2)=='j'){
            filtered = filtered.substring(0,2)+'J'+filtered.substring(3);
        }
        if(filtered.length() > 2 && filtered.charAt(2)=='i'){
            filtered = filtered.substring(0,2)+'I'+filtered.substring(3);
        }
        return filtered;
    }

    private void confirmVehicleNumber() {
        if (cameraSource != null && cameraSource.frameProcessor.textBlocks != null) {
            if(!cameraSource.frameProcessor.majorText.equals(""))
                vehicleNumber.setText(cameraSource.frameProcessor.majorText);
            // show vehicleNum draw interface here
//            List<FirebaseVisionText.TextBlock> textBlocks = cameraSource.frameProcessor.textBlocks;
//            for (int i = 0; i < textBlocks.size(); i++) {
//                // find and club nearby boxes?!
//            }
        }
    }
    //Todo: move these to IP class
    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }

            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private int colorVal(int pixel){
        return (int)Math.sqrt(Math.pow(Color.red(pixel),2) +Math.pow(Color.green(pixel),2) +Math.pow(Color.blue(pixel),2));
    }
    public Bitmap pad(Bitmap Src, int padding_x, int padding_y) {
        Bitmap outputimage = Bitmap.createBitmap(Src.getWidth() + padding_x,Src.getHeight() + padding_y, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputimage);
        can.drawARGB(0xFF,0xFF,0xFF,0xFF); //This represents White color
        can.drawBitmap(Src, padding_x, padding_y, null);
        return outputimage;
    }
    private Bitmap dilateBinaryBitmap(Bitmap binaryBitmap,int KX,int KY) {
        Bitmap copyBitmap = binaryBitmap.copy(Bitmap.Config.ARGB_8888,true);
        int currp, maxp,w = binaryBitmap.getWidth(), h = binaryBitmap.getHeight(), maxi,maxj;
        // revolve kernel
        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                maxp = colorVal(binaryBitmap.getPixel(i,j));
                maxi = i;
                maxj = j;
                for(int ki=i-KX/2;ki<=i+KX/2;ki++){
                    for(int kj=j-KY/2;kj<=j+KY/2;kj++){
                        if(ki > -1 && kj > -1 && ki < w && kj < h){
                            currp = colorVal(binaryBitmap.getPixel(ki,kj));
                            if(maxp < currp){
                                maxp = currp;
                                maxi = ki;
                                maxj = kj;
                            }
                        }
                    }
                }
                copyBitmap.setPixel(i,j, binaryBitmap.getPixel(maxi,maxj));
            }
        }
        Log.d(TAG,"Dilated: "+copyBitmap.getWidth());
        return copyBitmap;
    }



    public boolean checkInternetConnection() {
        ConnectivityManager con_manager = (ConnectivityManager)
                MainActivity.this.getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        if(con_manager.getActiveNetworkInfo() != null && con_manager.getActiveNetworkInfo().isConnected()){
            return true;
        }
        else{
            logToast(CRYING_EMOJI+"  No internet available!");
            return false;
        }
    }

    private void startLoadingCaptcha() {
        if(checkInternetConnection()) {
            // bottomSheetOn();

            captchaInput.setText("");
            // vehicleDetails.setVisibility(View.INVISIBLE);
            // vehicleDetails.setVisibility(View.GONE);
            webScraper.setOnPageLoadedListener(new WebScraper.onPageLoadedListener(){
                @Override
                public void loaded(String URL) {
                    Log.d(TAG, "Loaded!");
//                    findViewById(R.id.loading_webview).setVisibility(INVISIBLE);

                    eltCaptchaImage = webScraper.findElementByClassName("captcha-image", 0);
                    eltVehicleNumber = webScraper.findElementById("regn_no1_exact");
                    eltCaptchaInput = webScraper.findElementById("txt_ALPHA_NUMERIC");
                    eltSubmitBtn = webScraper.findElementByClassName("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only", 0);
                    webScraper.setOnPageLoadedListener(null);
                    String focusScript=
                            "var children = document.getElementById('wrapper').children; "+
                                    "for (var i=0; i<children.length; i++) {children[i].style.display='none';} "+
                                    "document.getElementById('page-wrapper').style.display='block'; "+
                                    "var children = document.getElementsByClassName('container')[2].children; "+
                                    "for (var i=0; i<children.length; i++) {children[i].style.display='none';}"+
                                    "document.getElementsByClassName('row bottom-space')[0].style.display='block';"+
                                    "document.getElementsByClassName('logo-header-section display-print-none')[0].style.display='block';";
                    Log.d(TAG,"Running focusScript");
                    webScraper.loadURL("javascript:{" + focusScript + "}void(0);");
                    //TODO: fix- this run2 currently needs api 19
                    Log.d(TAG, "Got image from: " + eltCaptchaImage.getAttribute("src"));
                    webScraper.injectJSAndGetCaptcha(captchaBitmapGetter);
                }
            });
             logToast("Loading Website...");
            webScraper.loadURL(FULL_URL);
        }
    }
    public class ImageBitmapGetter implements Img2Bitmap
    {
        private AppEvents listener;
        ImageBitmapGetter(AppEvents listener){
            this.listener = listener;
        }
        private void processAndRead(Bitmap bitmap){
            // The image is about 120x40
            Bitmap resizedCaptcha  =  resizeBitmap(bitmap, 90, 30);
            // Blur it out by resizing
            Bitmap captchaImage = resizeBitmap(resizedCaptcha, 195, 65);
            Bitmap dilated = dilateBinaryBitmap(captchaImage,1,5);
            Bitmap padded = pad(dilated,30,30);
            resizedCaptcha.recycle();
            captchaImage.recycle();
            dilated.recycle();
            listener.onBitmapProcessed(padded);
        }

        @Override
        public void showMessage(String message){
            if(!message.equals(""))
                logToast("Note: "+message);
            // doFade();
            // viewKonfetti.setVisibility(VISIBLE);
            // supportView.setVisibility(VISIBLE);
            // viewKonfetti.build()
            //         .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            //         .setDirection(0.0, 359.0)
            //         .setSpeed(1f, 5f)
            //         .setFadeOutEnabled(true)
            //         .setTimeToLive(2000L)
            //         .addShapes(Shape.RECT, Shape.CIRCLE)
            //         .addSizes(new Size(12, 5))
            //         .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
            //         .streamFor(300, 5000L);
        }
        @Override
        public void onConvertComplete(final byte[] mDecodedImage){
            Log.d(TAG,"Conversion complete.");
            if (mDecodedImage == null || mDecodedImage.length == 0) {
                return;
            }
            final Bitmap bitmap = BitmapFactory.decodeByteArray(mDecodedImage, 0, mDecodedImage.length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    captchaImageView.setImageBitmap(bitmap);
                }

            });
            // logToast(PROCESSING_EMOJI + " Processing Captcha");
            new Thread(new Runnable() {
                public void run() {
                    processAndRead(bitmap);
                }
            }).start();
        }
    }

    @Override
    public void onBitmapProcessed(final Bitmap processedCaptchaImage){
        bitmapProcessor.processBitmap(processedCaptchaImage);
        // runOnUiThread(new Runnable() {
        //     @Override
        //     public void run() {
        //         captchaImageView.setImageBitmap(processedCaptchaImage);
        //     }
        //
        // });
        // processedCaptchaImage.recycle();
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        }

        cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(MainActivity.this));
    }
    @Override
    public void onCaptchaUpdate(String detectedCaptcha){
        captchaInput.setText(captchaFilter(detectedCaptcha));
        //TODO: Fix THROTTLE FOR PERF
        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
        logToast(SMILE_EMOJI + " Captcha read finished!");
        drawerBtn.animate().scaleX(1.25f).scaleY(1.25f).start();
    }
    @Override
    public void onMajorTextUpdate(String majorText){
        if(platesHash.contains(majorText))
            return;

        // new entries
        platesHash.add(majorText);
        platesQueue.add(majorText);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                platesHash.remove(platesQueue.remove());
                updatePlates();
            }
        },30*1000);
        if(platesQueue.size() > NUM_PLATES) {
            platesHash.remove(platesQueue.remove());
        }
        if(vehicleNumber.getText()!=null && vehicleNumber.getText().toString().equals("") && !majorText.equals("")) {
            vehicleNumber.setText(majorText);
        }
        updatePlates();
        Log.d(TAG,"Updated majorText: "+majorText);
    }

    public void updatePlates(){
        doFade();
        int i = 0;
        for(String plate : platesQueue){
            buttons.get(i).setText(plate);
            buttons.get(i).setVisibility(VISIBLE);
            i++;
        }
        while(i<NUM_PLATES){
            buttons.get(i).setVisibility(INVISIBLE);
            i++;
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                cameraPreview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                stopCameraSource();
            }
        }
    }
    private void setCamButtonListeners(){
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraSource==null) {
                    createCameraSource();
                    startCameraSource();
                    camBtn.animate().scaleX(1.1f).scaleY(1.1f).start();
                    camBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.wheel));
                    drawerBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.search_off));
                    // Crash prone:
                    // camBtn.startAnimation(mRotation);
                    // ^Better keep a gif
                    // drawingArea.setVisibility(View.INVISIBLE);
                }
                else {
                    flashBtn.animate().scaleX(1/1.1f).scaleY(1/1.1f).start();
                    flashBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.flash_off));
                    confirmVehicleNumber();
                    stopCameraSource();
                    camBtn.animate().scaleX(1/1.1f).scaleY(1/1.1f).start();
                    camBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.wheel_off));
                    drawerBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.search_on));
                    // Based on user feedback-
                    drawerBtn.performClick();
                    // mRotation.cancel();
                    // camBtn.clearAnimation();
                    // drawingArea.setVisibility(View.VISIBLE);
                    // camBtn.animate().setDuration(600).rotation(camBtn.getRotation() + 360).start();
                }
            }
        });
    }

    private boolean flashOn=false;
    private void setFlashListeners() {
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraSource!=null){
                    if(flashOn) {
                        cameraSource.turnOffTheFlash();
                        flashBtn.animate().scaleX(1/1.1f).scaleY(1/1.1f).start();
                        flashBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.flash_off));
                    }
                    else {
                        cameraSource.turnOnTheFlash();
                        flashBtn.animate().scaleX(1.1f).scaleY(1.1f).start();
                        flashBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.flash_on));
                    }
                    flashOn = !flashOn;
                }
            }
        });
    }
    private void setCaptchaInputListeners() {
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
                //todo: make this run on api 16
                if(vehicleNumber.getText()!=null && eltVehicleNumber!=null){
                    eltVehicleNumber.setText(vehicleNumber.getText().toString());
                    eltVehicleNumber.setAttribute("style","background-color:lightgreen !important");
                }
                else{
                    logToast("UNEXPECTED!! VehicleNumber Text is NULL!");
                    Crashlytics.log("UNEXPECTED!! VehicleNumber Text is NULL!");
                }
                if(eltCaptchaInput == null){
                    logToast(CRYING_EMOJI + " Unexpected error, please report to my creator Udayraj");
                    Crashlytics.log(" Unexpected error, please report to my creator Udayraj");
                }
                eltCaptchaInput.setText(captchaInput.getText().toString());
                eltCaptchaInput.setAttribute("style","background-color:lightgreen !important");
                if(checkInternetConnection()) {
                    eltSubmitBtn.click();
                    logToast("Submitting...");
                    // wait for submit to finish
                    webScraper.setOnPageLoadedListener(new WebScraper.onPageLoadedListener() {
                        @Override
                        public void loaded(String URL) {
                            logToast("Form Submitted");
                            String tableScript = "var table = document.getElementsByClassName('table')[0];" +
                                    " for (var i = 0, row; typeof(table) != 'undefined' && row = table.rows[i]; i++) {" +
                                    " row.style = 'display: table;  width:100%; word-break:break-all;';" +
                                    " for (var j = 0, col; col = row.cells[j]; j++) {" +
                                    " col.style='display: table-row;'" +
                                    " }" +
                                    " }";
                            webScraper.setOnPageLoadedListener(null);
                            webScraper.loadURL("javascript:{" + tableScript + "}void(0)");
                        }
                    });
                }
            }
        });
    }

    //  drawerBtn connected via onclick in xml
    @TargetApi(19)
    public void onDrawerButtonClicked(View v) {
        // drawer fade
        if(canDoTransitions)
            TransitionManager.beginDelayedTransition(superContainer,mFade);
        if(drawerView.getVisibility()== VISIBLE){
            vehicleNumber.setText("");
            drawerView.setVisibility(INVISIBLE);
            drawerBtn.animate().scaleX(1.1f).scaleY(1.1f).start();
        }
        else{
            checkInternetConnection();
            drawerView.setVisibility(VISIBLE);
            drawerBtn.animate().scaleX(1/1.1f).scaleY(1/1.1f).start();
        }
        // camBtn.performClick();
    }
    private void setVehicleNumListeners() {
        vehicleNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        vehicleNumber.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            @Override public void afterTextChanged(Editable s) {
                //validate
                vehicleNumber.isTextEqualToPattern();
                // Remove spaces
                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    // logToast(NOPE_EMOJI +" No spaces allowed");
                    vehicleNumber.setText(result);
                    // setSelection will set the cursor at the end
                    vehicleNumber.setSelection(result.length());
                }
                // enable/disable search button
                if(vehicleNumber.getText() != null)
                    searchBtn.setEnabled(vehicleNumber.getText().toString().matches(NUMPLATE_PATTERN));
            }
        });
        if(vehicleNumber.getText() != null)
            searchBtn.setEnabled(vehicleNumber.getText().toString().matches(NUMPLATE_PATTERN));
    }

    public void copyNumber(View view) {
        String vehicleNum = (vehicleNumber.getText() != null) ? vehicleNumber.getText().toString() : "";
        Log.d(TAG,"Copying : "+vehicleNum);
        if(!vehicleNum.equals("")) {
            ClipData clip = ClipData.newPlainText("Vehicle Number", vehicleNum);
            clipboard.setPrimaryClip(clip);
            logToast(SMILE_EMOJI + " Text Copied:  "+vehicleNum);
        }
        else{
            logToast(CRYING_EMOJI + " Empty text");
        }
    }
    @TargetApi(19)
    public void doFade() {
        if(canDoTransitions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Get the root view and create a transition
            // Start recording changes to the view hierarchy
            TransitionManager.beginDelayedTransition(superContainer, mFade);
        }
    }
    private boolean warningBack = false;
    // collapse bottom sheet when back button pressed
    @Override
    public void onBackPressed() {
        if (findViewById(R.id.easter).getVisibility() != VISIBLE) {

            if (drawerView.getVisibility() != VISIBLE) {
                if (warningBack) {
                    super.onBackPressed();
                } else {
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                    doFade();
                    supportView.setVisibility(VISIBLE);
                    warningBack = true;
                    // reset after delay
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            warningBack = false;
                        }
                    }, 2500);
                }
                // if(cameraSource != null){
                // }
                // else{
                //     camBtn.performClick();
                // }
            }
            else {
                doFade();
                vehicleNumber.setText("");
                drawerView.setVisibility(INVISIBLE);
            }
        }
        else {
            // Crashlytics.getInstance().crash();
            findViewById(R.id.easter).setVisibility(INVISIBLE);
        }
    }

    private void initDrawingArea() {
        // if (drawingArea == null) {
        //     drawingArea = (DrawingArea) findViewById(R.id.drawing_area);
        //     drawingArea.initTrailDrawer();
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= 27)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        Log.d(TAG, "onResume");

        initDrawingArea();
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        // drawingArea.trimMemory();
        cameraPreview.stop();
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
//            Toast.makeText(MainActivity.this, "Camera paused. Search when ready.", Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, "Camera paused.", Toast.LENGTH_LONG).show();
            cameraSource.release();
            cameraSource = null;
        }
    }
}

/*
* private Bitmap thresholdBitmap(Bitmap orig, int THR ){
        Bitmap bitmap = orig.copy(Bitmap.Config.ARGB_8888,true);
        for(int i=0;i<bitmap.getWidth();i++){
            for(int j=0;j<bitmap.getHeight();j++){
                if(colorVal(bitmap.getPixel(i,j)) < THR)
                    bitmap.setPixel(i,j, Color.BLACK);
                else
                    bitmap.setPixel(i,j, Color.WHITE);
            }
        }
        return bitmap;
    }
    private Bitmap openBinaryBitmap(Bitmap binaryBitmap,int KX,int KY) {
        Bitmap copyBitmap1  = dilateBinaryBitmap(binaryBitmap,KX,KY);
        // binaryBitmap.recycle();
        Bitmap copyBitmap2 = erodeBinaryBitmap(copyBitmap1,KX,KY);
        copyBitmap1.recycle();
        return copyBitmap2;
    }
    private Bitmap erodeBinaryBitmap(Bitmap binaryBitmap,int KX,int KY) {
        Bitmap copyBitmap = binaryBitmap.copy(Bitmap.Config.ARGB_8888,true);
        int currp, minp,w = binaryBitmap.getWidth(), h = binaryBitmap.getHeight(), mini,minj;
        // revolve kernel
        for(int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                minp = colorVal(binaryBitmap.getPixel(i,j));
                mini = i;
                minj = j;
                for(int ki=i-KX/2;ki<=i+KX/2;ki++){
                    for(int kj=j-KY/2;kj<=j+KY/2;kj++){
                        if(ki > -1 && kj > -1 && ki < w && kj < h){
                            currp = colorVal(binaryBitmap.getPixel(ki,kj));
                            if(minp > currp){
                                minp = currp;
                                mini = ki;
                                minj = kj;
                            }
                        }
                    }
                }
                copyBitmap.setPixel(i,j, binaryBitmap.getPixel(mini,minj));
            }
        }
        Log.d(TAG,"Eroded: "+copyBitmap.getWidth());
        return copyBitmap;
    }
public void copyDetails(View view) {
        TextView t1,t2,t3,t4,t5,t6,t7,t8;
        t1 = findViewById(R.id.vehicle_name);
        t2 = findViewById(R.id.vehicle_owner);
        t3 = findViewById(R.id.vehicle_fuel);
        t4 = findViewById(R.id.vehicle_cc);
        t5 = findViewById(R.id.vehicle_engine);
        t6 = findViewById(R.id.vehicle_chasis);
        t7 = findViewById(R.id.vehicle_location);
        t8 = findViewById(R.id.vehicle_expiry);
        String s="Vehicle Number : "+vehicleNumber.getText().toString()+"\n Vehicle Name : "+t1.getText().toString()+"\n Owner Name : "+t2.getText().toString()+"\n Fuel Type : "+t3.getText().toString()+"\n Displacement : "+t4.getText().toString()+"\n Engine Number : "+t5.getText().toString()+"\n Chasis Number : "+t6.getText().toString()+"\n Location : "+t7.getText().toString()+"\n Expiry On : "+t8.getText().toString();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Vehicle details", s);
        clipboard.setPrimaryClip(clip);
        logToast("Text Copied! "+SMILE_EMOJI);
    }
        private Animation mRotation;

        // mRotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
//        mRotation= new RotateAnimation(
//                0, 360,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f
//        );
//        mRotation.setDuration(10000);
//        mRotation.setRepeatCount(Animation.INFINITE);

    */