package com.samvad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    public static final int GOOGLE_SIGN_IN_CODE = 100;
    private static final String TAG = "LoginActivity";
    LinearLayout btnGoogle;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    private static final int CAMERA_PERMISSION = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        transparentStatusBar();

        if (checkForSignIn()) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            } else {
                navigateToHomeActivity();
            }
        }
        btnGoogle = findViewById(R.id.btn_google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> signIn());
    }

    public void transparentStatusBar() {
        if ((Build.VERSION.SDK_INT >= 19) && (Build.VERSION.SDK_INT < 21)) {
            setWindowFlag(true);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    public void setWindowFlag(boolean on) {
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) {
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        } else {
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        window.setAttributes(winParams);
    }

    private boolean checkForSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        return account != null;
    }

    private void signIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, GOOGLE_SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                Toast.makeText(this, "hello " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                } else {
                    navigateToHomeActivity();
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went Wrong.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            navigateToHomeActivity();
        } else {
            Log.d(TAG, "onRequestPermissionsResult: ");
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
//            navigateToHomeActivity();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void navigateToHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}