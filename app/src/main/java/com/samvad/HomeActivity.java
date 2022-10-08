package com.samvad;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "HomeActivity";
    private static final int CAMERA_PERMISSION = 100;
    private static final int VIDEO_RECORD = 101;
    public static final String Heroku_URL = "https://smapi2.herokuapp.com/";
    public static final String Local_URL = "http://192.168.185.216:5000/";
    public static final String ML_URL = Heroku_URL + "translate";

    LinearLayout speakBtnOff, speakBtnOn;
    TextView translatedText;
    TextToSpeech textToSpeech;
    Button record;
    ProgressDialog progressDialog;

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
        progressDialog = new ProgressDialog(HomeActivity.this);
        record = findViewById(R.id.record);
        record.setOnClickListener((v) -> recordVideo());
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
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private Uri videoPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_RECORD) {
            if (resultCode == RESULT_OK) {
                videoPath = data.getData();
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                uploadvideo();
                Log.i("Path", videoPath.getPath());
            } else if (resultCode == RESULT_CANCELED) {

            } else {

            }
        }
    }

    private String getfiletype(Uri videouri) {
        ContentResolver r = getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri));
    }

    private void uploadvideo() {
        if (videoPath != null) {
            // save the selected video in Firebase storage

            final StorageReference reference = FirebaseStorage.getInstance().getReference("Files/" + System.currentTimeMillis() + "." + getfiletype(videoPath));
            reference.putFile(videoPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    // get the link of video
                    String downloadUri = uriTask.getResult().toString();
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Video");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("videolink", downloadUri);
                    reference1.child("" + System.currentTimeMillis()).setValue(map);
                    // Video uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Video Uploaded!!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onSuccess: " + downloadUri);
                    callML(downloadUri);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error, Image not uploaded
                    progressDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    // show the progress bar
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    private void callML(String pathToFirebase) {
        findViewById(R.id.pro_btn).setVisibility(View.VISIBLE);
//        findViewById(R.id.prog);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ML_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    translatedText.setText(MessageFormat.format("{0}|{1}", jsonObject.getString("english"), jsonObject.getString("gujarati")));
                    findViewById(R.id.pro_btn).setVisibility(View.INVISIBLE);
//                    findViewById(R.id.prog);
                } catch (JSONException e) {
                    e.printStackTrace();
                    findViewById(R.id.pro_btn).setVisibility(View.INVISIBLE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);
                findViewById(R.id.pro_btn).setVisibility(View.INVISIBLE);
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("video", pathToFirebase);
                return params;
            }
        };
        int MY_SOCKET_TIMEOUT_MS = 90000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
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

}