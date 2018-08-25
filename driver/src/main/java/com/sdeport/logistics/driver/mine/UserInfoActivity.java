package com.sdeport.logistics.driver.mine;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Driver;
import com.sdeport.logistics.driver.bean.Role;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.server.WebRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class UserInfoActivity extends BaseActivity {

    @BindView(R.id.phone)
    protected EditText phone;
    @BindView(R.id.name)
    protected EditText name;
    @BindView(R.id.id)
    protected EditText id;
    @BindView(R.id.driving_licence)
    protected EditText license;
    @BindView(R.id.truck)
    protected EditText truck;
    @BindView(R.id.spinner)
    protected Spinner spinner;

    private String[] licenseTypes;
    private Role role;
    private Driver driver;

    private String licenseType;
    @Override
    protected void initUI(Bundle savedInstanceState) {
        licenseTypes = getResources().getStringArray(R.array.plate_type);
        addContentView(R.layout.activity_user_info);
        setTopBar(R.drawable.icon_back, R.string.title_personal, -1);
        ButterKnife.bind(this);

        role = User.getUser().getRole();
        driver = role == null ? null : role.getDriver();

        phone.setEnabled(false);
        phone.setText(User.getUser().getAccount());
        name.setText(role == null ? "" : role.getCnName());
        id.setText(driver == null ? "" : driver.getIdCardNo());
        license.setText(driver == null ? "" : driver.getLicenseNo());
        truck.setText(driver == null ? "" : driver.getTruckNo());

        if (driver == null || TextUtils.isEmpty(driver.getLicenseType())) {
            spinner.setSelection(0);
        }else {
            boolean inUse = false;
            for (int i = 0; i < licenseTypes.length; i++) {
                if (licenseTypes[i].equals(driver.getLicenseType())) {
                    spinner.setSelection(i);
                    inUse = true;
                    break;
                }
            }
            if (!inUse) {
                licenseTypes[0] = driver.getLicenseType();
                spinner.setSelection(0);
            }
        }

        spinner.setOnItemSelectedListener(onItemSelectedListener);
    }


    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            licenseType = licenseTypes[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @OnClick(R.id.save)
    protected void save() {
        String phoneStr = phone.getText().toString().trim();
        String nameStr = name.getText().toString().trim();
        String idStr = id.getText().toString().trim();
        String licenseStr = license.getText().toString().trim();
        String truckStr = truck.getText().toString().trim();

        if (TextUtils.isEmpty(nameStr)) {
            MyToast.show(this, R.string.info_name_hint);
            return;
        }
        if (TextUtils.isEmpty(idStr)) {
            MyToast.show(this, R.string.info_id_hint);
            return;
        }


        WebRequest.getInstance().modifyRoleInfo(nameStr, idStr, licenseStr, licenseType, truckStr, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject o) {

            }

            @Override
            public void onError(Throwable e) {

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                finish();
                break;
        }

    }
}
