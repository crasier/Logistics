package com.sdeport.logistics.driver.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.server.WebRequest;
import com.sdeport.logistics.driver.tools.CD;

import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class RetrieveActivity extends BaseActivity {

    private static final long DURATION = 60 * 1000;
    private Unbinder unbinder;

    @BindView(R.id.retrieve_acc)
    protected EditText mAccEt;
    @BindView(R.id.retrieve_pwd)
    protected EditText mPwdEt;
    @BindView(R.id.retrieve_confirm)
    protected EditText mPwdConfirm;
    @BindView(R.id.retrieve_code)
    protected EditText mCode;
    @BindView(R.id.retrieve_code_get)
    protected Button codeGet;

    private HashMap<String, Disposable> requests;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_activity_retrieve);
        unbinder = ButterKnife.bind(this);

        setTopBar(R.drawable.icon_back, R.string.account_pwd_reset, 0);

        requests = new HashMap<>();

        checkRemainTime();
    }

    private void checkRemainTime() {
        long currentTime = System.currentTimeMillis();
        long preferTime = Prefer.getInstance().getLong(Constants.KEY_PREFER_FUTURE_RETRIEVE, 0);
        long duration = currentTime - preferTime;

        if (duration <= 0 || duration > DURATION) {
            Prefer.getInstance().putLong(Constants.KEY_PREFER_FUTURE_RETRIEVE, 0);
            return;
        }

        startCD(DURATION - duration);
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
        final String acc = mAccEt.getText().toString().trim();

        if (TextUtils.isEmpty(acc)) {
            MyToast.show(this, R.string.acc_empty);
            return;
        }

        WebRequest.getInstance().getValidateCode(acc, "2", new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(true);
                if (requests.containsKey("code") && !requests.get("code").isDisposed()) {
                    requests.get("code").dispose();
                    requests.remove("code");
                }
                requests.put("code", d);
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
                if (requests.containsKey("code")) {
                    requests.remove("code");
                }
                dismissDialog();
                MyToast.show(RetrieveActivity.this, R.string.register_code_success);
                startCD(DURATION);
            }

            @Override
            public void onError(Throwable e) {
                if (requests.containsKey("code")) {
                    requests.remove("code");
                }
                dismissDialog();
                MyToast.show(RetrieveActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }


    private CD.Action action = new CD.Action() {
        @Override
        public void onStep(long step) {
            codeGet.setText(String.format(Locale.CHINA, "%s\n(%d)",
                    getString(R.string.account_validate_code), step / 1000));
        }

        @Override
        public void onStop() {
            codeGet.setText(R.string.account_validate_code);
            codeGet.setEnabled(true);
            codeGet.setTextColor(getResources().getColor(R.color.orange));
            codeGet.setBackgroundResource(R.drawable.bg_border_orange);
        }
    };

    private void startCD(long duration) {

        //重新开始倒计时时，记录开始时间
        if (duration == DURATION)
            Prefer.getInstance().putLong(Constants.KEY_PREFER_FUTURE_RETRIEVE, System.currentTimeMillis());

        codeGet.setTextColor(getResources().getColor(R.color.text_hint));
        codeGet.setBackgroundResource(R.drawable.bg_border_gray);
        codeGet.setEnabled(false);

        action.usable = true;
        action.duration = duration;
        action.tag = "retrieve";
        CD.getInstance().addAction(action);
    }

    @OnClick(R.id.retrieve_commit)
    protected void retrieve() {
        final String acc = mAccEt.getText().toString().trim();
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
        WebRequest.getInstance().retrieve(acc, pwd, code, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(true);
                if (requests.containsKey("retrieve") && !requests.get("retrieve").isDisposed()) {
                    requests.get("retrieve").dispose();
                    requests.remove("retrieve");
                }
                requests.put("retrieve", d);
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
                if (requests.containsKey("retrieve")) {
                    requests.remove("retrieve");
                }
                dismissDialog();
                MyToast.show(RetrieveActivity.this, R.string.retrieve_success);
                Intent retData = new Intent();
                retData.putExtra("account", acc);
                setResult(RESULT_OK, retData);
            }

            @Override
            public void onError(Throwable e) {
                if (requests.containsKey("retrieve")) {
                    requests.remove("retrieve");
                }
                dismissDialog();
                MyToast.show(RetrieveActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void freeMe() {

        for (Disposable dis : requests.values()) {
            if (!dis.isDisposed()) {
                dis.dispose();
            }
        }

        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
