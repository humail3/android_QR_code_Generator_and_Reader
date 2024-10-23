package com.novahumail.master_qr_code_generator_and_reader;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.novahumail.master_qr_code_generator_and_reader.R;

public class GenerateQRCodeActivity extends AppCompatActivity {

    private TextView qrCodeTextView;
    private ImageView qrCodeImageView;
    private TextInputEditText qrCodeTextInputEditText;
    private Button qrCodeGeneratorButton,shareBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);
        qrCodeTextView = findViewById(R.id.frameText);
        qrCodeImageView = findViewById(R.id.QRCodeImg);
        qrCodeTextInputEditText = findViewById(R.id.inputData);
        qrCodeGeneratorButton = findViewById(R.id.QRCodeGeneratorBtn);
        shareBTN = findViewById(R.id.shareButton);


        qrCodeGeneratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = qrCodeTextInputEditText.getText().toString().trim();
                if (data.isEmpty()) {
                    Toast.makeText(GenerateQRCodeActivity.this, "Please Enter Some Data to Generate QR Code", Toast.LENGTH_SHORT).show();
                } else {

                    // Initialize multi format writer
                    MultiFormatWriter writer = new MultiFormatWriter();

                    // Initialize bit matrix
                    try {
                        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 250, 250);

                        // Initialize barcode encoder
                        BarcodeEncoder encoder = new BarcodeEncoder();

                        // Initialize Bitmap
                        Bitmap bitmap = encoder.createBitmap(matrix);

                        //set bitmap on image view
                        qrCodeImageView.setImageBitmap(bitmap);

                        Bitmap bitmapp = getBitmapFromImageView(qrCodeImageView);
                        if (bitmapp != null) {
                            saveImageToGallery(bitmapp, "MyImage");
                        }

                        // Initialize input manager
                        InputMethodManager manager1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        //Hide soft Keyboard
                        manager1.hideSoftInputFromWindow(qrCodeTextInputEditText.getApplicationWindowToken(), 0);

                        qrCodeTextView.setVisibility(View.GONE);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the Bitmap from the ImageView
                qrCodeImageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = qrCodeImageView.getDrawingCache();

                // Create an Intent to share the image
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(bitmap));

                // Launch the sharing activity
                startActivity(Intent.createChooser(shareIntent, "Share Image"));

            }
        });


    }


    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "Image",
                null
        );
        return Uri.parse(path);
    }


    public Bitmap getBitmapFromImageView(ImageView qrCodeImageView) {
        if (qrCodeImageView.getDrawable() instanceof BitmapDrawable) {
            return ((BitmapDrawable) qrCodeImageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
    }


    public void saveImageToGallery(Bitmap bitmapp, String title) {
        String savedImagePath = null;

        // Create a directory for your images
        String imageFileName = "MyApp_" + title + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyAppImages");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        // If the directory was created or exists, save the image
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmapp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add the image to the gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }

        // Notify the user about the image save location
        if (savedImagePath != null) {
            Toast.makeText(this, "Image saved to " + savedImagePath, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}