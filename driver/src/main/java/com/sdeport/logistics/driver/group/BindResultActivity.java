package com.sdeport.logistics.driver.group;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BindResultActivity extends BaseActivity {

    private Unbinder unbinder;

    @BindView(R.id.motorcade_name)
    protected TextView bound;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.activity_bindresult);
        setTopBar(R.drawable.icon_account, R.string.group_bound_info, -1);

        unbinder = ButterKnife.bind(this);

        if (getIntent() != null) {
            bound.setText(getIntent().getStringExtra("name"));
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

    }
}
