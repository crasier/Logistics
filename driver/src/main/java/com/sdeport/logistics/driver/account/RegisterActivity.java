package com.sdeport.logistics.driver.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";

    private Unbinder unbinder;

    @BindView(R.id.register_acc)
    protected EditText mAccEt;
    @BindView(R.id.register_pwd)
    protected EditText mPwdEt;
    @BindView(R.id.register_confirm)
    protected EditText mConfirmEt;
    @BindView(R.id.register_code)
    protected EditText mCodeEt;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_register);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.register, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.base_top_left:
                onBackPressed();
                break;
        }
    }

    @OnClick(R.id.register_code_get)
    protected void getValidateCode() {
        final String acc = mAccEt.getText().toString().trim();
        final String pwd = mPwdEt.getText().toString().trim();
        final String conf = mConfirmEt.getText().toString().trim();

        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }

        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        if (!conf.equals(pwd)) {
            MyToast.show(this, R.string.confirm_fail);
            return;
        }

        //TODO get validate code
    }

    @OnClick(R.id.register_register)
    protected void register() {
        final String acc = mAccEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        String conf = mConfirmEt.getText().toString().trim();
        String code = mCodeEt.getText().toString().trim();
        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }

        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        if (!conf.equals(pwd)) {
            MyToast.show(this, R.string.confirm_fail);
            return;
        }

        if (TextUtils.isEmpty(code)) {
            MyToast.show(this, R.string.code_empty);
            return;
        }

        createDialog(false);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("account", acc);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, 2000);

        //TODO register
    }

    private Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        mHandler.removeCallbacks(exitRunnable);
        finish();
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
