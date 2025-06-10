package com.example.dongseonambook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // 스플래시 화면 표시 시간 (밀리초) → 2000 = 2초
    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 일정 시간 후 MainActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity로 이동
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();  // 스플래시 액티비티 종료 (뒤로 가기 못 하게)
            }
        }, SPLASH_TIME_OUT);
    }
}
