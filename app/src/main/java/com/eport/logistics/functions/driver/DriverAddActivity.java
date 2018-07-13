package com.eport.logistics.functions.driver;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.BaseActivity;
import com.eport.logistics.R;
import com.eport.logistics.bean.Driver;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DriverAddActivity extends BaseActivity {

    private static final String TAG = "DriverAddActivity";

    private Unbinder unbinder;

    @BindView(R.id.driver_add_view_account)
    protected View mAccView;
    @BindView(R.id.driver_add_account)
    protected EditText mAccountEt;//车队号
    @BindView(R.id.driver_add_view_password)
    protected View mPwdView;
    @BindView(R.id.driver_add_pwd)
    protected EditText mPwdEt;//密码
    @BindView(R.id.driver_add_name)
    protected EditText mNameEt;//车队账号
    @BindView(R.id.driver_add_card_id)
    protected EditText mCardIdEt;//账号密码
    @BindView(R.id.driver_add_phone)
    protected EditText mPhoneEt;//车队联系人
    @BindView(R.id.driver_add_nick)
    protected EditText mNickEt;//车队常用车牌号
    @BindView(R.id.driver_add_truck)
    protected EditText mTruckEt;//车队账号名称

    private Driver mDriver;

    @Override
    protected void initUI(Bundle savedInstanceState) {

        addContentView(R.layout.layout_driver_add_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.driver_add_title, 0);

        if (getIntent() != null) {
            mDriver = (Driver) getIntent().getSerializableExtra("driver");
        }

        if (mDriver == null) {
            mAccView.setVisibility(View.VISIBLE);
            mPwdView.setVisibility(View.VISIBLE);
        }else {
            mAccView.setVisibility(View.GONE);
            mPwdView.setVisibility(View.GONE);
            mNameEt.setText(mDriver.getAccountName());
            mCardIdEt.setText(mDriver.getIdCardNo());
            mPhoneEt.setText(mDriver.getPhone());
            mNickEt.setText(mDriver.getNick());
            mTruckEt.setText(mDriver.getTruckNo());
        }
    }

    @OnClick(R.id.driver_add_commit)
    protected void commit() {
        String account = mAccountEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        String name = mNameEt.getText().toString().trim();
        String phone = mPhoneEt.getText().toString().trim();
        String cardNo = mCardIdEt.getText().toString().trim();
        if (TextUtils.isEmpty(account) && mDriver == null) {
            MyToast.show(this, R.string.driver_add_account_empty);
            return;
        }

        if (TextUtils.isEmpty(pwd) && mDriver == null) {
            MyToast.show(this, R.string.driver_add_pwd_empty);
            return;
        }

        if (TextUtils.isEmpty(name)) {
            MyToast.show(this, R.string.driver_add_name_empty);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            MyToast.show(this, R.string.driver_add_phone_empty);
            return;
        }

        WebRequest.getInstance().modifyDriverInfo(
                mDriver == null ? "" : mDriver.getId(),
                mDriver == null ? "" : mDriver.getMotocadeId(),
                mDriver == null ? account : "",
                mDriver == null ? pwd : "",
                name,
                mCardIdEt.getText().toString().trim(),
                phone,
                mNickEt.getText().toString().trim(),
                mTruckEt.getText().toString().trim(),
                new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        createDialog(false);
                    }

                    @Override
                    public void onNext(JSONObject o) {
                        if (o == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }

                        Log.e(TAG, "onNext: " + o);

                        if (!o.getBooleanValue("success")) {
                            onError(new Throwable(o.getString("failReason")));
                            return;
                        }
                        dismissDialog();
                        MyToast.show(DriverAddActivity.this, R.string.operation_success);
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissDialog();
                        MyToast.show(DriverAddActivity.this,
                                e == null ? getString(R.string.operation_failed) : e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }
        );
    }

    @Override
    protected void freeMe() {

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
        if (unbinder != null) {
            unbinder.unbind();
        }
        finish();
    }
}
