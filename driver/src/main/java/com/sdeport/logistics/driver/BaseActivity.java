package com.sdeport.logistics.driver;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.bean.Driver;
import com.sdeport.logistics.driver.bean.Event;
import com.sdeport.logistics.driver.bean.Role;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.constant.Urls;
import com.sdeport.logistics.driver.server.WebRequest;
import com.sdeport.logistics.driver.tools.CD;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();

    private LayoutInflater inflater;

    protected LinearLayout mRootView;
    protected View topBar;
    protected ImageView mLeft;
    protected TextView mTitle;
    protected ImageView mRight;
    protected TextView mRightTV;

    protected WebView mWebView;

    private boolean hasTap = false;
    private boolean needAutoLogin = false;

    private Dialog loadingDialog;

    private Event mEvent;

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
        mRightTV = findViewById(R.id.base_top_right_text);

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
        CD.getInstance().stop();
        System.exit(0);
    }

    protected View getTopBar() {
        return topBar;
    }

    protected void setTopBar(int leftId, int titleId, int rightId) {
        setTopBar(leftId, titleId == 0 ? null : getString(titleId), rightId);
    }

    protected void setTopBar(int leftId, int titleId, int rightId, int rightStr, int bgColor) {
        setTopBar(leftId, titleId, rightId);
        if (rightStr >= 0) {
            mRight.setVisibility(View.GONE);
            mRightTV.setVisibility(View.VISIBLE);
            mRightTV.setText(rightStr);
            mRightTV.setOnClickListener(this);
        }else {
            mRightTV.setVisibility(View.GONE);
        }

        Log.e(TAG, "setTopBar: bgColor = "+bgColor);
        if (bgColor == 0) {
            topBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }else {
            topBar.setBackgroundColor(bgColor);
            mTitle.setTextColor(getResources().getColor(R.color.text_dark));
        }
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
    public void onMessageEvent(Event event) {
        switch (event.getTag()) {
            case Event.TAG_SESSION_INVALID:
//                startActivity(new Intent(this, WebLoginActivity.class));
                mEvent = event;
                login();
                break;
        }
    }

    protected void login() {

        if (Constants.DEBUG) {
            getRoleDetail();
            return;
        }

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

        mWebView.loadUrl(Urls.URL_LOGIN);
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

            if (url.equals(Urls.URL_LOGISTICS) ||
                    url.equals(Urls.URL_LOGISTICS_MAINPAGE)) {

                CookieManager manager = CookieManager.getInstance();

                String token = manager.getCookie(Urls.URL_LOGIN);
                Log.e(TAG, "shouldOverrideUrlLoading: token = "+ token);
                if (TextUtils.isEmpty(token)) {
                    manager.setCookie(Urls.URL_LOGIN, null);
                    mWebView.clearCache(false);
                    mWebView.loadUrl(Urls.URL_LOGIN);
                    return false;
                }

                dismissDialog();
//                    User.getUser().setToken(token);

                Prefer.getInstance().putString(Constants.KEY_PREFER_ACCOUNT, User.getUser().getAccount());
                Prefer.getInstance().putString(Constants.KEY_PREFER_PASSWORD, User.getUser().getPassword());

                User.getUser().setToken(token);
//                Prefer.getInstance().putString(Constants.KEY_PREFER_TOKEN, token);

                mWebView.clearHistory();
                mWebView.clearCache(true);

                if (mEvent == null) {//需要获取用户角色详情
                    onLoginResult(false);
                    getRoleDetail();
                    return false;
                }

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
            if (url.equals(Urls.URL_LOGIN) && needAutoLogin) {
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
                        "document.getElementById('inputCode').value=document.getElementById('checkCode').value;"+
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
            mEvent = null;
        }else {

        }
    }

    protected void getRoleDetail() {
        WebRequest.getInstance().getRoleInfo(User.getUser().getAccount(), new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }


                Log.e("roleDetail", "data = "+o.getString("data"));

                JSONObject roleObj = JSON.parseObject(o.getString("data"));

                if (roleObj == null) {
                    onError(new Throwable("role is invalid"));
                    return;
                }
                Role role = JSON.parseObject(roleObj.toJSONString(), Role.class);

                if (role == null) {
                    onError(new Throwable("role is invalid"));
                    return;
                }

                if (!TextUtils.isEmpty(role.getDriverInfo())) {
                    Driver driver = JSON.parseObject(role.getDriverInfo(), Driver.class);
                    role.setDriver(driver);
                }

                User.getUser().setRole(role);

                if (role.getRoleInfo() != null && role.getRoleInfo().contains("11111")) {
                    onLoginResult(true);
                }else {
                    Prefer.getInstance().putString(Constants.KEY_PREFER_ACCOUNT, "");
                    Prefer.getInstance().putString(Constants.KEY_PREFER_PASSWORD, "");
                    onError(new Throwable(getString(R.string.login_role_fail)));
                }
            }

            @Override
            public void onError(Throwable e) {
                onLoginResult(false);
                User.getUser().setAccount("");
                User.getUser().setPassword("");

                MyToast.show(BaseActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {
                dismissDialog();
            }
        });
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
