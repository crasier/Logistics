package com.eport.logistics.account;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eport.logistics.BaseActivity;
import com.eport.logistics.Constants;
import com.eport.logistics.R;
import com.eport.logistics.bean.User;
import com.eport.logistics.main.MainActivity;
import com.eport.logistics.utils.Prefer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 通过抓取web页地址链接来判断是否登录成功
 * */
public class WebLoginActivity extends BaseActivity{

    private static final String TAG = "WebLoginActivity";

    @BindView(R.id.webview)
    protected WebView mWebView;

    private Unbinder unbinder;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_login_web_activity);

        setTopBar(R.drawable.icon_back, 0, 0);
        unbinder = ButterKnife.bind(this);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());

        createDialog(true);
        mWebView.loadUrl(Constants.URL_LOGIN);
//        mWebView.loadUrl("http://www.baidu.com");
//        mWebView.loadUrl("https://test.sditds.gov.cn:5565/cas/login?service=http://test.sditds.gov.cn:81/logistics/security/login");
    }

    private void autoSubmit() {
        if (TextUtils.isEmpty(User.getUser().getAccount()) ||
                TextUtils.isEmpty(User.getUser().getPassword())) {
            return;
        }
        Log.e("autoSubmit", "shouldOverrideUrlLoading: auto login");
        String jsStr =
                "javascript:document.getElementById('username').value='"+User.getUser().getAccount()+"';" +
                "document.getElementById('password').value='"+User.getUser().getPassword()+"';"+
//                "document.getElementByName('submit')[0].value='hello world';";
                "document.getElementById('fm1').elements[5].click();";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.e("autoSubmit", "onReceiveValue: "+value);
                }
            });
        }
    }

    private class MyWebViewClient extends WebViewClient {
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Log.e(TAG, "shouldOverrideUrlLoading: url(21) = "+request.getUrl());
//            return super.shouldOverrideUrlLoading(view, request);
//        }


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
                User.getUser().setToken(token);
                User.getUser().setAccount("cargo");
                User.getUser().setPassword("666666");
//                Prefer.getInstance().putString(Constants.KEY_PREFER_TOKEN, token);
                Intent intent = new Intent(WebLoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mWebView.clearCache(true);
                mWebView.clearHistory();
                mWebView.destroy();
                dismissDialog();
                finish();
                return false;
            }

            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (url.equals(Constants.URL_LOGIN)) {
                autoSubmit();
            }
            dismissDialog();
            setTopBar(R.drawable.icon_back, view.getTitle(), 0);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }

        if (unbinder != null) {
            unbinder.unbind();
        }

        exitAPP();
    }

    @Override
    protected void freeMe() {

    }
}
