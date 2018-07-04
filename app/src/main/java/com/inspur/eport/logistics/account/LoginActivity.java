package com.inspur.eport.logistics.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.Constants;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.User;
import com.inspur.eport.logistics.main.MainActivity;
import com.inspur.eport.logistics.server.TestData;
import com.inspur.eport.logistics.server.WebRequest;
import com.inspur.eport.logistics.utils.MyToast;
import com.inspur.eport.logistics.utils.Prefer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LoginActivity extends BaseActivity {

    private Unbinder unbinder;

    @BindView(R.id.login_account)
    protected EditText mAccountEt;
    @BindView(R.id.login_pwd)
    protected EditText mPwdEt;

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
    }

    @Optional
    @OnClick(R.id.login_login)
    protected void login() {

        final String acc = mAccountEt.getText().toString().trim();
        final String pwd = mPwdEt.getText().toString().trim();
        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            MyToast.show(this, R.string.pwd_empty);
            return;
        }

        createDialog(false);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                dismissDialog();

                User.getUser().setAccount(acc);
                User.getUser().setPassword(pwd);
                User.getUser().setToken(TestData.TOKEN);

                Prefer.getInstance().putString(Constants.KEY_PREFER_USER, acc);
                Prefer.getInstance().putString(Constants.KEY_PREFER_PWD, pwd);

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                LoginActivity.this.finish();
            }
        }, 1000);


        if (true) {
            return;
        }

        WebRequest.getInstance().login(acc, pwd, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject jsonObject) {
                if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                    dismissDialog();
                    String token = jsonObject.getJSONObject("data").getString("token");

                    User.getUser().setAccount(acc);
                    User.getUser().setPassword(pwd);
                    User.getUser().setToken(token);

                    Prefer.getInstance().putString(Constants.KEY_PREFER_USER, acc);
                    Prefer.getInstance().putString(Constants.KEY_PREFER_PWD, pwd);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    LoginActivity.this.finish();
                }else {
                    onError(new Throwable(jsonObject.getString("message")));
                }
            }

            @Override
            public void onError(Throwable e) {
                dismissDialog();
                MyToast.show(LoginActivity.this, TextUtils.isEmpty(e.getMessage())
                        ? getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
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

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
