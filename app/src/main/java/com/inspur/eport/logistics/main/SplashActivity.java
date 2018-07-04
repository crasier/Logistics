package com.inspur.eport.logistics.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.account.LoginActivity;


public class SplashActivity extends BaseActivity {

    @Override
    protected void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_activity_splash);
        mHandler.postDelayed(jumpToMain, 2000);
    }

    private Runnable jumpToMain = new Runnable() {
        @Override
        public void run() {
            finish();
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
    };

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void freeMe() {

    }
}
