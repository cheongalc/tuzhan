package com.tuzhan;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Handler splash_delay = new Handler();
        splash_delay.postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
            intent.putExtra("isFirstStart", true);
            startActivity(intent);
            finish();
        }, 2500);
    }
}
