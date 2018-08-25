package com.sdeport.logistics.driver.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.server.WebRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ModifyPwdActivity extends BaseActivity {

    private static final String TAG = "ModifyPwdActivity";

    private Unbinder unbinder;

    @BindView(R.id.pwd_old)
    protected EditText mPwdOld;
    @BindView(R.id.pwd_new)
    protected EditText mPwdNew;
    @BindView(R.id.pwd_confirm)
    protected EditText mPwdConfirm;

    private Disposable request;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_pwd_modify);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.pwd_modify, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.base_top_left:
                onBackPressed();
                break;
        }
    }

    @OnClick(R.id.modify)
    protected void modify() {
        final String pwdOld = mPwdOld.getText().toString().trim();
        final String pwdNew = mPwdNew.getText().toString().trim();
        String conf = mPwdConfirm.getText().toString().trim();
        if (TextUtils.isEmpty(pwdOld)) {
            MyToast.show(this, R.string.pwd_old_hint);
            return;
        }

        if (TextUtils.isEmpty(pwdNew)) {
            MyToast.show(this, R.string.pwd_new_hint);
            return;
        }

        if (!conf.equals(pwdNew)) {
            MyToast.show(this, R.string.confirm_fail);
            return;
        }

        WebRequest.getInstance().resetPassword(User.getUser().getAccount(), pwdOld, pwdNew, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(true);
                if (request != null && !request.isDisposed()) {
                    request.dispose();
                }
                request = d;
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

                dismissDialog();
                MyToast.show(ModifyPwdActivity.this, R.string.reset_password_success);
                User.getUser().setPassword(pwdNew);
                Prefer.getInstance().putString(Constants.KEY_PREFER_PASSWORD, pwdNew);
            }

            @Override
            public void onError(Throwable e) {
                dismissDialog();
                MyToast.show(ModifyPwdActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void freeMe() {
        if (request != null && !request.isDisposed()) {
            request.dispose();
        }
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
