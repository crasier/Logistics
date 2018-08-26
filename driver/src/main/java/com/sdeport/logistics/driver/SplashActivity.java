package com.sdeport.logistics.driver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.account.LoginActivity;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.main.MainActivity;
import com.sdeport.logistics.driver.server.WebRequest;

public class SplashActivity extends BaseActivity{

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_splash);
        mHandler.postDelayed(jumpToMain, 1500);

    }

    private Runnable jumpToMain = new Runnable() {
        @Override
        public void run() {
//            String token = Prefer.getInstance().getString(Constants.KEY_PREFER_TOKEN, "");
            String acc = Prefer.getInstance().getString(Constants.KEY_PREFER_ACCOUNT, "");
            String pwd = Prefer.getInstance().getString(Constants.KEY_PREFER_PASSWORD, "");
            if (TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)) {
//                intent.setClass(SplashActivity.this, WebLoginActivity.class);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }else {
//                intent.setClass(SplashActivity.this, MainActivity.class);
                User.getUser().setAccount(Prefer.getInstance().getString(Constants.KEY_PREFER_ACCOUNT, ""));
                User.getUser().setPassword(Prefer.getInstance().getString(Constants.KEY_PREFER_PASSWORD, ""));
                mHandler.postDelayed(loginFailRunnable, 5000);
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
        mHandler.removeCallbacks(loginFailRunnable);
        if (result) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else {
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
        MyToast.cancel();
    }
}
