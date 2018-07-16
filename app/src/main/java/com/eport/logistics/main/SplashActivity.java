package com.eport.logistics.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.eport.logistics.BaseActivity;
import com.eport.logistics.Constants;
import com.eport.logistics.R;
import com.eport.logistics.account.LoginActivity;
import com.eport.logistics.account.WebLoginActivity;
import com.eport.logistics.bean.User;
import com.eport.logistics.utils.MyToast;
import com.eport.logistics.utils.Prefer;


public class SplashActivity extends BaseActivity {

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_splash);
        mHandler.postDelayed(jumpToMain, 2000);
    }

    private Runnable jumpToMain = new Runnable() {
        @Override
        public void run() {
//            String token = Prefer.getInstance().getString(Constants.KEY_PREFER_TOKEN, "");
            String acc = Prefer.getInstance().getString(Constants.KEY_PREFER_USER, "");
            String pwd = Prefer.getInstance().getString(Constants.KEY_PREFER_PWD, "");
            if (TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)) {
//                intent.setClass(SplashActivity.this, WebLoginActivity.class);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }else {
//                intent.setClass(SplashActivity.this, MainActivity.class);
                User.getUser().setAccount(Prefer.getInstance().getString(Constants.KEY_PREFER_USER, ""));
                User.getUser().setPassword(Prefer.getInstance().getString(Constants.KEY_PREFER_PWD, ""));

                SplashActivity.super.login();
            }
        }
    };

    private Runnable loginFailRunnable = new Runnable() {
        @Override
        public void run() {
            MyToast.show(SplashActivity.this, R.string.login_fail);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    };

    /**
     * false的情况可能有多次，因为网页跳转不止一次
     * */
    @Override
    public void onLoginResult(boolean result) {
        if (result) {
            mHandler.removeCallbacks(loginFailRunnable);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else {
            mHandler.removeCallbacks(loginFailRunnable);
            mHandler.postDelayed(loginFailRunnable, 3000);
        }
    }

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
