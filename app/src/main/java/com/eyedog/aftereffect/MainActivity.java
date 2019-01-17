package com.eyedog.aftereffect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnController, btnSvga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnController = findViewById(R.id.btn_controller);
        btnSvga = findViewById(R.id.btn_svga);
        btnController.setOnClickListener(this);
        btnSvga.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_controller:
                startActivity(ControllerActivity.class);
                break;
            case R.id.btn_svga:
                startActivity(SvgaActivity.class);
                break;
        }
    }

    private void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
