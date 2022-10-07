package com.samvad;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "HomeActivity";
    private static final int CAMERA_PERMISSION = 104;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private CameraSource cameraSource;
    private UIUpdater uiUpdater;

    LinearLayout speakBtnOff,speakBtnOn;
    TextView translatedText;
    TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        surfaceView = findViewById(R.id.surface_view);
        speakBtnOff = findViewById(R.id.speak_btn_off);
        speakBtnOn = findViewById(R.id.speak_btn_on);
        translatedText = findViewById(R.id.translated_text);

        startCameraSource();
        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {
//            textToSpeech.setSpeechRate(0.85f);
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("gu_IN"));
//              textToSpeech.setLanguage(Locale.US);
            }
        });
        speakBtnOff.setOnClickListener((v)->{
            speakBtnOff.setVisibility(View.INVISIBLE);
            speakBtnOn.setVisibility(View.VISIBLE);
            textToSpeech.speak(translatedText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                speakBtnOff.setVisibility(View.VISIBLE);
                speakBtnOn.setVisibility(View.INVISIBLE);
            }, 1000);
        });
    }

    /**
     * Take a screenshot of the view
     *
     * @param view the view to capture
     * @return the bitmap representing the pixels of the given view
     */
    @Nullable
    public static Bitmap loadBitmapFromView(View view) {
        try {
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;
        } catch (OutOfMemoryError error) {
            Log.d(TAG, "Out of Memory while loadBitmapFromView");
            return null;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
//                Toast.makeText(this, "About Clicked!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.logout:
//                Toast.makeText(this, "Logout Clicked!!", Toast.LENGTH_SHORT).show();
                Dialog customDialog = new Dialog(this);
                customDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customDialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                customDialog.setContentView(R.layout.push_dialog);
                customDialog.show();
                LinearLayout btn_yes = customDialog.findViewById(R.id.btn_yes);
                btn_yes.setOnClickListener(v -> {
                    logout();
                    customDialog.cancel();
                });
                LinearLayout btn_no = customDialog.findViewById(R.id.btn_no);
                btn_no.setOnClickListener((v) -> {
                    customDialog.cancel();
                });
                return true;
            default:
                return false;
        }
    }

    public void showMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.home_menu);
        popup.show();
    }

    private void startCameraSource() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(HomeActivity.this, "Cannot start camera", Toast.LENGTH_SHORT).show();
            return;
        }

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer).setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(width * 4 / 3, width).setAutoFocusEnabled(true).setRequestedFps(2.0f).build();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFixedSize(width, width * 4 / 3);

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());
                    startProcessing();
                } catch (Exception e) {
                    Log.d(TAG, "surfaceCreated: " + e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {//Release source for cameraSourc

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }

            //            Log.d(TAG, "surfaceChanged: "+bitmap);
        });
    }

    private void startProcessing() {

        uiUpdater = new UIUpdater(() -> {
            Bitmap bitmap = loadBitmapFromView(surfaceView);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            StringBuffer sb = new StringBuffer();
            Log.d(TAG, "startProcessing: " + byteArray.length);
//            for (byte b:byteArray) {
//                sb.append(b);
//            }
            Log.d(TAG, "" + Collections.singletonList(byteArray));
//            Log.d(TAG, ""+sb.toString());

//            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
//            Log.d(TAG, "startProcessing: " + scaled.getHeight());
//            Log.d(TAG, "startProcessing: " + scaled.getWidth());
//            Log.d(TAG, "startProcessing: " + );
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            scaled.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            Log.d(TAG, "startProcessing: "+byteArray.length);
//            scaled.recycle();
            scaled.getAllocationByteCount();

            Log.d(TAG, "startProcessing: " + scaled.getAllocationByteCount());
        }, 100);
        uiUpdater.startUpdates();
    }

    private void logout() {

        GoogleSignInOptions gso;
        GoogleSignInClient gsc;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        gsc.signOut().addOnCompleteListener(task -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        uiUpdater.startUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiUpdater.stopUpdates();
    }

    /**
     * A class used to perform periodical updates,
     * specified inside a runnable object. An update interval
     * may be specified (otherwise, the class will perform the
     * update every 2 seconds).
     *
     * @author Carlos Simões
     */
    public class UIUpdater {
        // Create a Handler that uses the Main Looper to run in
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        private final Runnable mStatusChecker;
        private int UPDATE_INTERVAL = 1000;

        /**
         * Creates an UIUpdater object, that can be used to
         * perform UIUpdates on a specified time interval.
         *
         * @param uiUpdater A runnable containing the update routine.
         */
        public UIUpdater(final Runnable uiUpdater) {
            mStatusChecker = new Runnable() {
                @Override
                public void run() {
                    // Run the passed runnable
                    uiUpdater.run();
                    // Re-run it after the update interval
                    mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };
        }

        /**
         * The same as the default constructor, but specifying the
         * intended update interval.
         *
         * @param uiUpdater A runnable containing the update routine.
         * @param interval  The interval over which the routine
         *                  should run (milliseconds).
         */
        public UIUpdater(Runnable uiUpdater, int interval) {
            this(uiUpdater);
            UPDATE_INTERVAL = interval;
        }

        /**
         * Starts the periodical update routine (mStatusChecker
         * adds the callback to the handler).
         */
        public synchronized void startUpdates() {
            mStatusChecker.run();
        }

        /**
         * Stops the periodical update routine from running,
         * by removing the callback.
         */
        public synchronized void stopUpdates() {
            mHandler.removeCallbacks(mStatusChecker);
        }
    }
}