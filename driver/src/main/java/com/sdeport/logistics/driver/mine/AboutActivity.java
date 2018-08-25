package com.sdeport.logistics.driver.mine;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.edtion)
    TextView edition;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        View view = LayoutInflater.from(this).inflate(R.layout.activity_about, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        addContentView(view);
        setTopBar(R.drawable.icon_back, "关于我们", -1);
        ButterKnife.bind(this);
        try {
            edition.setText(String.format("版本号:%s", getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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
