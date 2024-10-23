package com.novahumail.master_qr_code_generator_and_reader;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;
import static android.webkit.URLUtil.isValidUrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.RGBLuminanceSource;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.NotFoundException;
import com.novahumail.master_qr_code_generator_and_reader.R;

import java.io.IOException;

import eu.livotov.labs.android.camview.ScannerLiveView;
import eu.livotov.labs.android.camview.scanner.decoder.zxing.ZXDecoder;

public class ScanQRCodeActivity extends AppCompatActivity {

    private ScannerLiveView scannerLiveView;
    private TextView scannedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        scannerLiveView = findViewById(R.id.camView);
        scannedTextView = findViewById(R.id.scannedData);


        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }


        scannerLiveView.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scanner) {
                Toast.makeText(ScanQRCodeActivity.this, "Scanner Started...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScannerStopped(ScannerLiveView scanner) {
                Toast.makeText(ScanQRCodeActivity.this, "Scanner Stopped...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScannerError(Throwable err) {
                Toast.makeText(ScanQRCodeActivity.this, "Scanner Error Occurred Please Start Again...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeScanned(String data) {
                scannedTextView.setText(data);
            }
        });

        // Set an OnClickListener for the resultTextView
        scannedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the text from the TextView
                String url = scannedTextView.getText().toString();

                // Check if it's a valid URL and open it in a web browser
                if (isValidUrl(url)) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });


        Button copyButton = findViewById(R.id.copyButton);


        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToCopy = scannedTextView.getText().toString();

                if (!textToCopy.isEmpty()) {
                    // Get the clipboard manager
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                    // Create a clip data with the text to copy
                    ClipData clipData = ClipData.newPlainText("Copied Text", textToCopy);

                    // Set the clip data to the clipboard manager
                    clipboardManager.setPrimaryClip(clipData);

                    // Notify the user that the text has been copied
                    showToast("Text copied to clipboard");
                }
            }
        });


        // Handle shared images with your app
        handleSharedImages(getIntent());


        Button selectImageBTN = findViewById(R.id.selectImage_btn); // Replace with your button's ID

        selectImageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the gallery to select an image
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 123); // 123 is a request code, you can choose any number

            }
        });


    }




    //    Override the onActivityResult method to handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Perform QR code scanning on the selected image
            String qrCodeResult = scanQRCodeFromImage(selectedImageUri);

            if (qrCodeResult != null) {
                scannerLiveView.setVisibility(View.GONE);

                ImageView imageView = findViewById(R.id.imageView);
                try {
                    // Load the image from the ImageUri into a Bitmap
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    // Set the Bitmap to the ImageView
                    imageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle any exceptions that may occur while loading the image
                }
                // Update the TextView with the scan result
                scannedTextView.setText(qrCodeResult);
            } else {
                // Handle the case when no QR code is found in the selected image
                showToast("No QR code found in the selected image.");
            }
        }
    }


    // Handle shared images
    private void handleSharedImages(Intent intent) {
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if (intent.getType().startsWith("image/")) {
                Uri sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (sharedImageUri != null) {
                    scannerLiveView.setVisibility(View.GONE);

                    ImageView imageView = findViewById(R.id.imageView);
                    try {
                        // Load the image from the ImageUri into a Bitmap
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), sharedImageUri);

                        // Set the Bitmap to the ImageView
                        imageView.setImageBitmap(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle any exceptions that may occur while loading the image
                    }

                    // Perform QR code scanning on the shared image
                    String qrCodeResult = scanQRCodeFromImage(sharedImageUri);
                    if (qrCodeResult != null) {
                        // Display the result in the scannedTextView
                        scannedTextView.setText(qrCodeResult);
                    }
                }
            }
        }
    }


    // Scan a QR code from an image
    private String scanQRCodeFromImage(Uri imageUri) {
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();
            int[] pixels = new int[width * height];
            imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private boolean checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int vibratePermission = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && vibratePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, VIBRATE}, PERMISSION_CODE);
    }

    @Override
    protected void onPause() {
        scannerLiveView.stopScanner();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZXDecoder decoder = new ZXDecoder();
        decoder.setScanAreaPercent(0.8);
        scannerLiveView.setDecoder(decoder);
        scannerLiveView.startScanner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean vibrationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (cameraAccepted && vibrationAccepted) {
                Toast.makeText(this, "Permission Granted...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied \n You can't use the app without permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }


}




