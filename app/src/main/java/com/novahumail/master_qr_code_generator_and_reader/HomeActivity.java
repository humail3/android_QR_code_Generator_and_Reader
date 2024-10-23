package com.novahumail.master_qr_code_generator_and_reader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.novahumail.master_qr_code_generator_and_reader.R;

public class HomeActivity extends AppCompatActivity {

    private Button generateQRButton;
    private Button scanQRButton;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        generateQRButton = findViewById(R.id.QRCodeGeneratorBtn);
        scanQRButton = findViewById(R.id.QRCodeScannerBtn);
        adView = findViewById(R.id.adView);

        generateQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GenerateQRCodeActivity.class);
                startActivity(intent);
            }
        });

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ScanQRCodeActivity.class);
                startActivity(intent);
            }
        });

        // Initialize AdMob
        MobileAds.initialize(this);

        // Load the banner ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);




    }
}