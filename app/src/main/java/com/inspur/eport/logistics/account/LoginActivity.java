package com.inspur.eport.logistics.account;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.Constants;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.User;
import com.inspur.eport.logistics.main.MainActivity;
import com.inspur.eport.logistics.server.WebRequest;
import com.inspur.eport.logistics.utils.EncryptUtil;
import com.inspur.eport.logistics.utils.MyToast;
import com.inspur.eport.logistics.utils.Prefer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private Unbinder unbinder;

    @BindView(R.id.login_account)
    protected EditText mAccountEt;
    @BindView(R.id.login_pwd)
    protected EditText mPwdEt;
    @BindView(R.id.login_webview)
    protected WebView mWebView;

    private String acc;
    private String pwd;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_login);
        unbinder = ButterKnife.bind(this);
        if (!TextUtils.isEmpty(Prefer.getInstance().getString(Constants.KEY_PREFER_USER, ""))) {
            mAccountEt.setText(Prefer.getInstance().getString(Constants.KEY_PREFER_USER, ""));
        }
        if (!TextUtils.isEmpty(Prefer.getInstance().getString(Constants.KEY_PREFER_PWD, ""))) {
            mPwdEt.setText(Prefer.getInstance().getString(Constants.KEY_PREFER_PWD, ""));
        }


        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    @Optional
    @OnClick(R.id.login_login)
    protected void login() {

        acc = mAccountEt.getText().toString().trim();
        pwd = mPwdEt.getText().toString().trim();
        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        AssetManager am = getAssets();
        InputStream pfxInputStream = null;
        try {
             pfxInputStream = am.open("sso.pfx");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pfxInputStream == null) {
            MyToast.show(LoginActivity.this, "加密文件读取失败");
            return;
        }

        if (TextUtils.isEmpty(User.getUser().getToken())) {
            MyToast.show(LoginActivity.this, "登录失败，请重试");
            return;
        }

        String ssoInfo = EncryptUtil.getEncryptInfo(
                acc,
                pwd,
                null,
                pfxInputStream,
                "62236644");

        Log.e("getEncryptInfo", "after encrypt: ssoInfo = "+ssoInfo);
        String url = String.format(Locale.CHINA, Constants.URL_LOGIN_CAS,
                "sso",
                ssoInfo);

        mWebView.loadUrl(url);

//        WebRequest.getInstance().login(acc, pwd, User.getUser().getToken(), pfxInputStream, new Observer<String>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                createDialog(false);
//            }
//
//            @Override
//            public void onNext(String o) {
//                Log.e(TAG, "onNext: login result = "+o);
//                if (!TextUtils.isEmpty(o)) {
//                    dismissDialog();
////                    String token = o.getJSONObject("data").getString("token");
//
//                    User.getUser().setAccount(acc);
//                    User.getUser().setPassword(pwd);
////                    User.getUser().setToken(token);
//
//                    Prefer.getInstance().putString(Constants.KEY_PREFER_USER, acc);
//                    Prefer.getInstance().putString(Constants.KEY_PREFER_PWD, pwd);
//
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    mWebView.clearHistory();
//                    mWebView.clearCache(true);
//                    mWebView.destroy();
//                    LoginActivity.this.finish();
//                }else {
//                    onError(new Throwable(getString(R.string.operation_failed)));
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                dismissDialog();
//                MyToast.show(LoginActivity.this, TextUtils.isEmpty(e.getMessage())
//                        ? getString(R.string.operation_failed) : e.getMessage());
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
    }
    /**
     * 注册账号
     * */
    @Optional
    @OnClick(R.id.login_register)
    protected void register() {
        startActivityForResult(new Intent(this, RegisterActivity.class), Constants.CODE_ACTIVITY_REGISTER);
    }

    /**
     * 找回密码
     * */
    @OnClick(R.id.login_retrieve)
    protected void retrieve() {
        startActivityForResult(new Intent(this, RetrieveActivity.class), Constants.CODE_ACTIVITY_RETRIEVE);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String acc = "";
            switch (requestCode) {
                case Constants.CODE_ACTIVITY_REGISTER:
                    if (data == null) {
                        break;
                    }
                    acc = data.getStringExtra("account");
                    if (TextUtils.isEmpty(acc)) {
                        break;
                    }
                    mAccountEt.setText(acc);
                    mPwdEt.setText("");
                    break;
                case Constants.CODE_ACTIVITY_RETRIEVE:
                    if (data == null) {
                        return;
                    }
                    acc = data.getStringExtra("account");
                    if (TextUtils.isEmpty(acc)) {
                        break;
                    }
                    mAccountEt.setText(acc);
                    mPwdEt.setText("");
                    break;
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.e(TAG, "shouldInterceptRequest: url = "+url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading: url = "+url);
            // you want to catch when an URL is going to be loaded

            if (url.equals("http://test.sditds.gov.cn:81/logistics/") ||
                    url.equals("http://test.sditds.gov.cn:81/logistics/mainPage")) {

                CookieManager manager = CookieManager.getInstance();

                String token = manager.getCookie(Constants.URL_LOGIN);
                Log.e(TAG, "shouldOverrideUrlLoading: token = "+ token);
                if (TextUtils.isEmpty(token)) {
                    manager.setCookie(Constants.URL_LOGIN, null);
                    mWebView.clearCache(false);
                    mWebView.loadUrl(Constants.URL_LOGIN);
                    return false;
                }

                dismissDialog();

                User.getUser().setAccount(acc);
                User.getUser().setPassword(pwd);
//                    User.getUser().setToken(token);

                Prefer.getInstance().putString(Constants.KEY_PREFER_USER, acc);
                Prefer.getInstance().putString(Constants.KEY_PREFER_PWD, pwd);

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                mWebView.clearHistory();
                mWebView.clearCache(true);
                mWebView.destroy();

                User.getUser().setToken(token);
                Prefer.getInstance().putString(Constants.KEY_PREFER_TOKEN, token);
                LoginActivity.this.finish();
                return false;
            }
            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            createDialog(true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dismissDialog();
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "onReceivedSslError: "+error.toString());
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }

    }

    private class MyWebChromeClient extends WebChromeClient {

    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
