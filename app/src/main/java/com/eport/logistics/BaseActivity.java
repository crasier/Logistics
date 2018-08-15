package com.eport.logistics;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eport.logistics.bean.EventBean;
import com.eport.logistics.bean.User;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.eport.logistics.utils.Prefer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();

    private LayoutInflater inflater;

    protected LinearLayout mRootView;
    protected View topBar;
    protected ImageView mLeft;
    protected TextView mTitle;
    protected ImageView mRight;
    protected WebView mWebView;

    private boolean hasTap = false;

    private Dialog loadingDialog;

    private boolean needAutoLogin = false;
    private EventBean mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = LayoutInflater.from(this);

        setContentView(R.layout.activity_base);

        mRootView = findViewById(R.id.base_root);
        topBar = findViewById(R.id.base_top);
        mLeft = findViewById(R.id.base_top_left);
        mTitle = findViewById(R.id.base_top_title);
        mRight = findViewById(R.id.base_top_right);

        initUI(savedInstanceState);
    }

    protected Handler mHandler = new Handler();

    private Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            hasTap = false;
        }
    };

    protected void addContentView(int viewId) {
        mRootView.addView(inflater.inflate(viewId, null));
    }

    protected void addContentView(View view) {
        mRootView.addView(view);
    }

    protected abstract void initUI(Bundle savedInstanceState);

    protected abstract void freeMe();

    protected void exitAPP() {
        System.exit(0);
    }

    protected View getTopBar() {
        return topBar;
    }

    protected void setTopBar(int leftId, int titleId, int rightId) {
        setTopBar(leftId, titleId == 0 ? null : getString(titleId), rightId);
    }

    protected void setTopBar(int leftId, String title, int rightId) {
        boolean showTop = false;

        if (leftId <= 0) {
            mLeft.setVisibility(View.INVISIBLE);
        }else {
            showTop = true;
            mLeft.setVisibility(View.VISIBLE);
            mLeft.setImageResource(leftId);
        }

        if (TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.INVISIBLE);
        }else {
            showTop = true;
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
        }

        if (rightId <= 0) {
            mRight.setVisibility(View.INVISIBLE);
        }else {
            showTop = true;
            mRight.setVisibility(View.VISIBLE);
            mRight.setImageResource(rightId);
        }

        if (showTop) {
            topBar.setVisibility(View.VISIBLE);
            mLeft.setOnClickListener(this);
            mTitle.setOnClickListener(this);
            mRight.setOnClickListener(this);
        }else {
            topBar.setVisibility(View.GONE);
        }
    }

    /**
     * 弹loading dialog
     *
     */
    public void createDialog(final boolean cancel) {
        try {
            if (!BaseActivity.this.isFinishing()) {
                if (null == loadingDialog) {
                    loadingDialog = createLoadingDialog(BaseActivity.this);
                }

                loadingDialog.setCancelable(cancel);

                if (loadingDialog.isShowing()) {

                }else {
                    loadingDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @return
     */
    public Dialog createLoadingDialog(Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_dialog_loading, null);// 得到加载view
        LinearLayout layout = v.findViewById(R.id.dialog_view);// 加载布局

        loadingDialog = new Dialog(context, R.style.my_loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;

    }

    /**
     * 关闭dialog
     */
    public void dismissDialog() {
        /**
         * 用这个!this.isFinishing()做为条件,容易出现窗口泄露<br/>
         * 例:创建了Dialog,在Activity结束跳转的时候,Dialog无法关闭.
         */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                if (isFinishing()) {
//                    return;
//                }
                if (null != loadingDialog && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBean event) {
        switch (event.getTag()) {
            case EventBean.TAG_SESSION_INVALID:
//                startActivity(new Intent(this, WebLoginActivity.class));
                mEvent = event;
                login();
                break;
        }
    }

    protected void login() {
        needAutoLogin = true;

        if (mWebView == null) {
            mWebView = findViewById(R.id.base_webview);
            WebSettings settings = mWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setSupportZoom(true);
            mWebView.setWebViewClient(new MyWebViewClient());
            mWebView.setWebChromeClient(new MyWebChromeClient());
            mWebView.addJavascriptInterface(new JavaScript(), "log_cont");
        }

        mWebView.loadUrl(Constants.URL_LOGIN);
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
//                    User.getUser().setToken(token);

                Prefer.getInstance().putString(Constants.KEY_PREFER_USER, User.getUser().getAccount());
                Prefer.getInstance().putString(Constants.KEY_PREFER_PWD, User.getUser().getPassword());

                User.getUser().setToken(token);
//                Prefer.getInstance().putString(Constants.KEY_PREFER_TOKEN, token);

                mWebView.clearHistory();
                mWebView.clearCache(true);
                mWebView.destroy();

                onLoginResult(true);
                return false;
            }

            onLoginResult(false);
            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (isFinishing()) {
                return;
            }
            createDialog(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dismissDialog();
            if (url.equals(Constants.URL_LOGIN) && needAutoLogin) {
                autoSubmit();
                needAutoLogin = false;
            }
            getMsgFromHtml();
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

    private void autoSubmit() {
        if (TextUtils.isEmpty(User.getUser().getAccount()) ||
                TextUtils.isEmpty(User.getUser().getPassword())) {
            return;
        }
        Log.e("autoSubmit", "shouldOverrideUrlLoading: auto login");
        String jsStr =
                "javascript:document.getElementById('username').value='"+User.getUser().getAccount()+"';" +
                        "document.getElementById('password').value='"+User.getUser().getPassword()+"';"+
                        "document.getElementById('fm1').submit.click();";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.e("autoSubmit", "onReceiveValue: "+value);
                }
            });
        }else {
            mWebView.loadUrl(jsStr);
        }
    }

    private void getMsgFromHtml() {
        String jsStr = "javascript:window.log_cont.dataFromHtml(document.getElementById('msg').innerHTML);";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.e("autoSubmit", "onReceiveValue msg = "+value);
                }
            });
        }else {
            mWebView.loadUrl(jsStr);
        }
    }

    public void onLoginResult(boolean result) {
        if (result && mEvent != null) {
            WebRequest.getInstance().withToken(mEvent.getUrl(), mEvent.getObserver(),mEvent.getType(),mEvent.getMethod(),mEvent.getBody());
        }else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissDialog();
        EventBus.getDefault().unregister(this);
        MyToast.cancel();
    }

    @Override
    public void onBackPressed() {
        if (hasTap) {
            mHandler.removeCallbacks(exitRunnable);
            exitAPP();
            hasTap = false;
        }else {
            hasTap = true;
            mHandler.removeCallbacks(exitRunnable);
            MyToast.show(this, R.string.tap_exit);
            mHandler.postDelayed(exitRunnable, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        freeMe();
        super.onDestroy();
    }

    private class JavaScript {

        @JavascriptInterface
        public void dataFromHtml(String content) {
            Log.e(TAG, "dataFromHtml: " + content);
            if (!TextUtils.isEmpty(content)) {
                MyToast.show(BaseActivity.this, content);
            }
        }
    }
}
