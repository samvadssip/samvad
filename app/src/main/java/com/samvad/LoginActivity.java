package com.samvad;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    LinearLayout btnGoogle;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    public static final int GOOGLE_SIGN_IN_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(checkForSignIn()){
            navigateToHomeActivity();
        }
        btnGoogle = findViewById(R.id.btn_google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);

        btnGoogle.setOnClickListener(v -> signIn());
    }

    private boolean checkForSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        return account!=null;
    }

    private void signIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent,GOOGLE_SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_CODE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                Toast.makeText(this,"hello "+account.getDisplayName(), Toast.LENGTH_SHORT).show();
                navigateToHomeActivity();
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this,"Something went Wrong.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToHomeActivity() {
        startActivity(new Intent(this,HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}