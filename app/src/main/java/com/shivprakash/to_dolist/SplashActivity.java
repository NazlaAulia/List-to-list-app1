package com.shivprakash.to_dolist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // layout logo

        // Delay 2 detik lalu pindah ke Login
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, login.class);
            startActivity(intent);
            finish(); // biar splash nggak bisa di-back
        }, 2000); // 2000 ms = 2 detik
    }
}
