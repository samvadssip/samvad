package com.samvad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "HomeActivity";
    private static final int CAMERA_PERMISSION = 104;
    LinearLayout sideMenu;
    SurfaceView surfaceView;
    private CameraSource cameraSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sideMenu = findViewById(R.id.side_menu);
        sideMenu.setOnClickListener(v -> setSideMenu());

        surfaceView = findViewById(R.id.surface_view);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
        startCameraSource();
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
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize((width/2)*4/3, width/2)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build();
        surfaceView.getHolder().setFixedSize(width/2, (width/2)*4/3);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());
                } catch (Exception e) {
                    Log.d(TAG, "surfaceCreated: " + e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {//Release source for cameraSource

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    private void setSideMenu() {
        PopupMenu menu = new PopupMenu(this, sideMenu);
        menu.getMenuInflater().inflate(R.menu.home_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(HomeActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        logout();
    }

    private void logout() {

    }
}