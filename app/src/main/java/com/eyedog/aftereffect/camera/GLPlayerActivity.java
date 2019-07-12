package com.eyedog.aftereffect.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.eyedog.aftereffect.R;

public class GLPlayerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 2;

    private static final int PERMISSION_REQUEST_INTERNET = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glplayer);
        checkCameraPermission();
    }

    private void startCameraFragment() {
        CameraFragment cameraFragment = new CameraFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_view,cameraFragment).commit();
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                    requestPermissions(new String[]{
                            Manifest.permission.INTERNET
                    }, PERMISSION_REQUEST_INTERNET);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA
                }, PERMISSION_REQUEST_CAMERA);
            } else {
                checkWritePermission();
            }
        } else {
            startCameraFragment();
        }
    }

    private void checkWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                startCameraFragment();
            }
        }
    }

    private void checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.RECORD_AUDIO
                }, PERMISSION_REQUEST_RECORD_AUDIO);
            } else {
                startCameraFragment();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkWritePermission();
            } else {
                Toast.makeText(this, "Camera权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //checkAudioPermission();
                startCameraFragment();
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "麦克风权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
