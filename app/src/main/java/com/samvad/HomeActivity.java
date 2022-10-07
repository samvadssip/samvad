package com.samvad;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "HomeActivity";
    private static int CAMERA_PERMISSION = 100;
    private static int VIDEO_RECORD = 101;

    private Uri videoPath;

    LinearLayout speakBtnOff, speakBtnOn;
    TextView translatedText;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        speakBtnOff = findViewById(R.id.speak_btn_off);
        speakBtnOn = findViewById(R.id.speak_btn_on);
        translatedText = findViewById(R.id.translated_text);

        if (isCameraPresentInPhone()) {
            getCameraPermission();
        }

        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {
//            textToSpeech.setSpeechRate(0.85f);
            // if No error is found then only it will run
            if (i != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("gu_IN"));
//              textToSpeech.setLanguage(Locale.US);
            }
        });
        speakBtnOff.setOnClickListener((v) -> {
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

    public void recordVideoButtonPressed(View view) {
        recordVideo();
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_RECORD);
    }

    private boolean isCameraPresentInPhone() {
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_RECORD) {
            if (resultCode == RESULT_OK) {
                videoPath = data.getData();
                Log.i("Path",videoPath.getPath().toString());
            }else if (resultCode == RESULT_CANCELED) {

            }else {

            }
        }
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

//    private void startCameraSource() {
//        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(HomeActivity.this, "Cannot start camera", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int width = displayMetrics.widthPixels;
//
//
//    }

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
    protected void onResume() {
        super.onResume();
//        uiUpdater.startUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
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