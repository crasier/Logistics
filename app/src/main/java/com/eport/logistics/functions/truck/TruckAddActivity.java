package com.eport.logistics.functions.truck;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.BaseActivity;
import com.eport.logistics.R;
import com.eport.logistics.bean.Truck;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TruckAddActivity extends BaseActivity {

    private static final String TAG = "TruckAddActivity";

    private Unbinder unbinder;

    @BindView(R.id.truck_add_no)
    protected EditText mNo;//车牌号
    @BindView(R.id.truck_add_license)
    protected EditText mLicense;//行驶证号
    @BindView(R.id.truck_add_type)
    protected EditText mType;//车型
    @BindView(R.id.truck_add_capacity)
    protected EditText mCapacity;//载重

    private Truck mTruck;

    @Override
    protected void initUI(Bundle savedInstanceState) {

        addContentView(R.layout.layout_truck_add_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.truck_add_title, 0);

        if (getIntent() != null) {
            mTruck = (Truck) getIntent().getSerializableExtra("truck");
        }

        if (mTruck == null) {

        }else {
            mNo.setText(mTruck.getTruckNo());
            mLicense.setText(mTruck.getLicenseId());
            mType.setText(mTruck.getTruckType());
            mCapacity.setText(mTruck.getCarryCap());
        }
    }

    @OnClick(R.id.truck_add_commit)
    protected void commit() {
        String no = mNo.getText().toString().trim();
        String license = mLicense.getText().toString().trim();
        String type = mType.getText().toString().trim();
        String capacity = mCapacity.getText().toString().trim();
        if (TextUtils.isEmpty(no)) {
            MyToast.show(this, R.string.truck_add_code_empty);
            return;
        }

        WebRequest.getInstance().modifyTruckInfo(
                mTruck == null ? "" : mTruck.getId(),
                mTruck == null ? "" : mTruck.getMotocadeId(),
                mTruck == null ? "" : mTruck.getMotocadeCode(),
                no,
                license,
                type,
                capacity,
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
                        MyToast.show(TruckAddActivity.this, R.string.operation_success);
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissDialog();
                        MyToast.show(TruckAddActivity.this,
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
