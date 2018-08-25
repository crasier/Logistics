package com.sdeport.logistics.driver.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.account.LoginActivity;
import com.sdeport.logistics.driver.account.ModifyPwdActivity;
import com.sdeport.logistics.driver.main.HomeFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SettingActivity extends BaseActivity {

    private Unbinder unbinder;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        View view = LayoutInflater.from(this).inflate(R.layout.activity_settings, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        addContentView(view);
        setTopBar(R.drawable.icon_back, R.string.title_settings, -1);
        unbinder = ButterKnife.bind(this);
    }

    @OnClick(R.id.modify_passwd_item)
    public void onModifyPwdClick(View view) {
        Intent intent = new Intent(this, ModifyPwdActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.logout)
    protected void logout() {
        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
        HomeFragment.getInstance().getActivity().finish();
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
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
