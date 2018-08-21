package com.sdeport.logistics.driver.account;

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

public class RetrieveActivity extends BaseActivity {

    private Unbinder unbinder;

    @BindView(R.id.retrieve_acc)
    protected EditText mAccEt;
    @BindView(R.id.retrieve_pwd)
    protected EditText mPwdEt;
    @BindView(R.id.retrieve_confirm)
    protected EditText mPwdConfirm;
    @BindView(R.id.retrieve_code)
    protected EditText mCode;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_retrieve);
        unbinder = ButterKnife.bind(this);

        setTopBar(R.drawable.icon_back, R.string.account_pwd_reset, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.retrieve_code_get)
    protected void getValidateCode() {
        String acc = mAccEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        String confirm = mPwdConfirm.getText().toString().trim();
        String code = mCode.getText().toString().trim();

        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        if (!pwd.equals(confirm)) {
            MyToast.show(this, R.string.confirm_fail);
            return;
        }

        //TODO 获取验证码请求发送
    }

    @OnClick(R.id.retrieve_commit)
    protected void retrieve() {
        String acc = mAccEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        String confirm = mPwdConfirm.getText().toString().trim();
        String code = mCode.getText().toString().trim();

        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        if (!pwd.equals(confirm)) {
            MyToast.show(this, R.string.confirm_fail);
            return;
        }
        if (TextUtils.isEmpty(code)) {
            MyToast.show(this, R.string.code_empty);
            return;
        }
        //TODO 重置接口调用
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
