package com.sdeport.logistics.driver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_splash));
        super.onCreate(savedInstanceState);
    }
}
