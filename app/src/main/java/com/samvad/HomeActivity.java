package com.samvad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

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

    private void startCameraSource(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(HomeActivity.this,"Cannot start camera",Toast.LENGTH_SHORT).show();
            return;
        }

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        float ratio = ((float)metrics.heightPixels / (float)metrics.widthPixels);

            /*cameraSource = new CameraSource.Builder()
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1300, 1080)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();*/
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