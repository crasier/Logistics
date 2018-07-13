package com.inspur.eport.logistics.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.Constants;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.account.LoginActivity;
import com.inspur.eport.logistics.account.WebLoginActivity;
import com.inspur.eport.logistics.bean.User;
import com.inspur.eport.logistics.utils.Prefer;


public class SplashActivity extends BaseActivity {

    @Override
    protected void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_activity_splash);
        mHandler.postDelayed(jumpToMain, 2000);
    }

    private Runnable jumpToMain = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            String token = Prefer.getInstance().getString(Constants.KEY_PREFER_TOKEN, "");
            if (TextUtils.isEmpty(token)) {
//                intent.setClass(SplashActivity.this, WebLoginActivity.class);
                intent.setClass(SplashActivity.this, LoginActivity.class);
            }else {
//                intent.setClass(SplashActivity.this, MainActivity.class);
                intent.setClass(SplashActivity.this, LoginActivity.class);
                User.getUser().setToken(token);
            }
            startActivity(intent);
            finish();
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
