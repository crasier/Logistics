package com.sdeport.logistics.driver.group;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BindResultActivity extends BaseActivity {

    private Unbinder unbinder;

    @BindView(R.id.motorcade_name)
    protected TextView bound;
    @BindView(R.id.icon)
    protected ImageView icon;
    @BindView(R.id.result)
    protected TextView result;
    @BindView(R.id.motorcade_label)
    protected TextView label;
    @BindView(R.id.cancel)
    protected TextView cancel;


    private String status;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.activity_bindresult);
        setTopBar(R.drawable.icon_back, R.string.group_bound_info, -1);

        unbinder = ButterKnife.bind(this);

        if (getIntent() != null) {
            bound.setText(getIntent().getStringExtra("name"));
            status = getIntent().getStringExtra("status");
        }

        if (status.equals("2")) {
            icon.setImageResource(R.drawable.icon_bound);
            result.setText(R.string.bound_group);
            result.setTextColor(getResources().getColor(R.color.colorPrimary));
            label.setText(R.string.group_bound);
            cancel.setVisibility(View.INVISIBLE);
        }else {
            icon.setImageResource(R.drawable.icon_ing);
            result.setText(R.string.bind_group_ing);
            result.setTextColor(getResources().getColor(R.color.orange));
            label.setText(R.string.group_bound_ing);
            cancel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 取消挂靠申请（只有状态是正在申请时司机端才可以主动撤销）
     * */
    @OnClick(R.id.cancel)
    protected void cancelApply() {
        //TODO 撤销挂靠申请
        MyToast.show(this, "撤销挂靠申请");
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
