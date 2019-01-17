package com.eyedog.aftereffect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SvgaActivity extends AppCompatActivity {

    HomeLogoImageView logoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svga);
        logoImageView = findViewById(R.id.logo_progress);
        logoImageView.setMaskOffset(480);
    }
}
