package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private MaterialButton qrCodeFoundButton;
    private MaterialToolbar toolbar;
    private String qrCode;

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        qrCode = "";

        toolbar = findViewById(R.id.toolbarScanner);
        // Set toolbar navigation
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setTitleTextColor(getResources().getColor(R.color.color_white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(ScannerActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Set up and initiate the camera preview to be displayed inside the PreviewView widget.
    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

         preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            // stopCamera() might not work for all versions(?), the line below fixed my error
            @SuppressLint("RestrictedApi")
            @Override
            public void onQRCodeFound(String _qrCode) {
                qrCode = _qrCode;
                if (_qrCode.equals(getResources().getString(R.string.qrConfirmation))) {
                    Objects.requireNonNull(preview.getCamera()).close();

                    // Confirm donation in database
                    String appointmentKey = Objects.requireNonNull(getIntent().getExtras()).getString("appointmentKey");

                    String currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid + "/Appointments/" + appointmentKey);


                    ref.child("donationConfirmed").setValue(true).addOnSuccessListener(aVoid -> {
                        //Toast.makeText(ScannerActivity.this, getResources().getString(R.string.string_donation_confirmed), Toast.LENGTH_SHORT).show();
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(ScannerActivity.this);
                        dialog.setTitle(getResources().getString(R.string.confirmation))
                                .setPositiveButton(getResources().getString(R.string.string_ok), (dialog1, which) -> startActivity(new Intent(ScannerActivity.this, MainActivity.class)))
                                .setMessage(qrCode)
                                .show();
                        Log.e("ScanConfirmSucces", "Confirmed.");
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ScannerActivity.this, getResources().getString(R.string.string_error), Toast.LENGTH_SHORT).show();
                        Log.e("ScanConfirmFailure", e.toString());
                    });

                    // Get the date from the confirmed appointment donation and add it to the user;
                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("User/" + currentUserUid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long year = (long) snapshot.child("year").getValue();
                            long month = (long) snapshot.child("month").getValue();
                            long day = (long) snapshot.child("day").getValue();

                            Log.d("allen", "long:"  + year + " " + month + " " + day);





                            LocalDate date = LocalDate.of(Integer.parseInt(String.valueOf(year)), Integer.parseInt(String.valueOf(month)), Integer.parseInt(String.valueOf(day)));

                            ref2.child("lastDonationConfirmed").setValue(date.toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("addLastDonationDateToUser", error.toString());
                        }
                    });

                }

            }

            @Override
            public void qrCodeNotFound() {

            }
        }));

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.scannerInfo) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(ScannerActivity.this);
            dialog.setTitle(getResources().getString(R.string.string_info))
                    .setMessage(getResources().getString(R.string.string_scanner_info))
                    .show();

        }

        return true;
    }
}