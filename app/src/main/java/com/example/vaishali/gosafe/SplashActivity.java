package com.example.vaishali.gosafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent LoginActivity = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(LoginActivity);
                finish();
            }
        }, 2000);

    }
}

