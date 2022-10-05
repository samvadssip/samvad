package com.samvad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;

public class HomeActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int CAMERA_PERMISSION = 104;
    SurfaceView surfaceView;
    private CameraSource cameraSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        surfaceView = findViewById(R.id.surface_view);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
        startCameraSource();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Toast.makeText(this, "About Clicked!!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.logout:
                Toast.makeText(this, "Logout Clicked!!", Toast.LENGTH_SHORT).show();
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

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        float ratio = ((float) metrics.heightPixels / (float) metrics.widthPixels);

            /*cameraSource = new CameraSource.Builder()
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1300, 1080)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();*/
    }

//    private void setSideMenu() {
//        PopupMenu menu = new PopupMenu(this, sideMenu);
//        menu.getMenuInflater().inflate(R.menu.home_menu, menu.getMenu());
//        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Toast.makeText(HomeActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//        logout();
//    }

    private void logout() {

    }
}