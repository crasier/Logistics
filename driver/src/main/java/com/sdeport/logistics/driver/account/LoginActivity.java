package com.sdeport.logistics.driver.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.constant.Codes;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private Unbinder unbinder;

    @BindView(R.id.login_account)
    protected EditText mAccountEt;
    @BindView(R.id.login_pwd)
    protected EditText mPwdEt;

    private String acc;
    private String pwd;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_login);

        unbinder = ButterKnife.bind(this);
        if (!TextUtils.isEmpty(Prefer.getInstance().getString(Constants.KEY_PREFER_ACCOUNT, ""))) {
            mAccountEt.setText(Prefer.getInstance().getString(Constants.KEY_PREFER_ACCOUNT, ""));
        }
        if (!TextUtils.isEmpty(Prefer.getInstance().getString(Constants.KEY_PREFER_PASSWORD, ""))) {
            mPwdEt.setText(Prefer.getInstance().getString(Constants.KEY_PREFER_PASSWORD, ""));
        }
    }

    @Optional
    @OnClick(R.id.login_login)
    protected void doLogin() {

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

        User.getUser().setAccount(acc);
        User.getUser().setPassword(pwd);

        super.login();
    }

    @Override
    public void onLoginResult(boolean result) {
        if (result) {
            startActivity(new Intent(this, MainActivity.class));
            LoginActivity.this.finish();
        }else {
//            MyToast.show(this, getString(R.string.login_fail));
        }
    }

    /**
     * 注册账号
     * */
    @OnClick(R.id.login_register)
    protected void register() {
        startActivityForResult(new Intent(this, RegisterActivity.class), Codes.CODE_ACTIVITY_REGISTER);
    }

    /**
     * 重置密码
     * */
    @OnClick(R.id.login_retrieve)
    protected void retrieve() {
        startActivityForResult(new Intent(this, RetrieveActivity.class), Codes.CODE_ACTIVITY_RETRIEVE);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String acc = "";
            switch (requestCode) {
                case Codes.CODE_ACTIVITY_REGISTER:
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
                case Codes.CODE_ACTIVITY_RETRIEVE:
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

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
