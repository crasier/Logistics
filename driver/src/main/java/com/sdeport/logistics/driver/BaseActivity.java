package com.sdeport.logistics.driver;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.bean.EventBean;

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
    protected TextView mRightTV;

    private boolean hasTap = false;

    private Dialog loadingDialog;

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
}
