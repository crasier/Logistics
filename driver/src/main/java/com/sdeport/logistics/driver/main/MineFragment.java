package com.sdeport.logistics.driver.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Driver;
import com.sdeport.logistics.driver.bean.Role;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.group.BindGroupActivity;
import com.sdeport.logistics.driver.group.BindResultActivity;
import com.sdeport.logistics.driver.mine.AboutActivity;
import com.sdeport.logistics.driver.mine.SettingActivity;
import com.sdeport.logistics.driver.mine.UserInfoActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MineFragment extends BaseFragment {

    private Unbinder unbinder;

    @BindView(R.id.attach_status)
    protected TextView bindStatus;
    @BindView(R.id.attach_sub_title)
    protected TextView bindTip;
    @BindView(R.id.user_info_status)
    protected TextView userStatus;
    @BindView(R.id.user_info__sub_title)
    protected TextView userStatusTip;
    @BindView(R.id.settings_sub_title)
    protected TextView setTip;
    @BindView(R.id.user_name)
    protected TextView userName;

    private Role role;
    private Driver driver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_mine, container, false);

        unbinder = ButterKnife.bind(this, contentView);

        role = User.getUser().getRole();
        driver = role == null ? null : role.getDriver();
        userName.setText(role == null ? "" : role.getCnName());

        initBoundStatus();
        initRoleInfo();

        setTip.setText("");
        return contentView;
    }

    /**
     * 填写用户挂靠信息
     * */
    private void initBoundStatus() {
        if (driver == null || TextUtils.isEmpty(driver.getAttachStatus()) || driver.getAttachStatus().equals("0")) {//未挂靠
            bindStatus.setText(R.string.unbound_group);
            bindStatus.setTextColor(getResources().getColor(R.color.orange));
            bindStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_orange));
            bindTip.setText(R.string.bind_group_tip);
        }else if (driver.getAttachStatus().equals("1")) {//申请中
            bindStatus.setText(R.string.bind_group_ing);
            bindStatus.setTextColor(getResources().getColor(R.color.orange));
            bindStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_orange));
            bindTip.setText(R.string.bind_group_tip_2);
        }else if (driver.getAttachStatus().equals("2")){//已挂靠
            bindStatus.setText(R.string.bound_group);
            bindStatus.setTextColor(getResources().getColor(R.color.icon_green));
            bindStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_green));
            bindTip.setText(driver.getMotorcadeName());
        }else {//其他未知
            bindStatus.setText(R.string.unknown);
            bindStatus.setTextColor(getResources().getColor(R.color.orange));
            bindStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_orange));
            bindTip.setText(R.string.unknown);
        }
    }

    /**
     * 填写用户个人信息完整情况
     * */
    private void initRoleInfo() {

        SparseArray<String> infos = new SparseArray<>();
        infos.put(R.string.info_phone, User.getUser().getAccount());
        infos.put(R.string.info_name, role == null ? "" : role.getCnName());
        infos.put(R.string.info_id, driver == null ? "" : driver.getIdCardNo());
        infos.put(R.string.info_license, driver == null ? "" : driver.getLicenseNo());
        infos.put(R.string.info_license_type, driver == null ? "" : driver.getLicenseType());
        infos.put(R.string.info_truck, driver == null ? "" : driver.getTruckNo());

        for (int i = 0; i < infos.size(); i++) {
            if (TextUtils.isEmpty(infos.get(infos.keyAt(i)))) {
                userStatus.setText(R.string.bind_not_full);
                userStatus.setTextColor(getResources().getColor(R.color.orange));
                userStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_orange));

                userStatusTip.setText(getString(R.string.info_dismiss, getString(infos.keyAt(i))));
                return;
            }
        }

        userStatus.setText(R.string.personal_full);
        userStatus.setTextColor(getResources().getColor(R.color.icon_green));
        userStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_border_green));

        userStatusTip.setText(R.string.personal_info_tip);
    }

    @OnClick({R.id.about_item, R.id.attach_item, R.id.settings_item, R.id.user_info_item})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.attach_item:
                if (driver == null || TextUtils.isEmpty(driver.getAttachStatus()) || driver.getAttachStatus().equals("0")) {
                    intent = new Intent(getContext(), BindGroupActivity.class);
                }else {
                    intent = new Intent(getContext(), BindResultActivity.class);
                    intent.putExtra("name", driver.getMotorcadeName());
                }
                startActivity(intent);
                break;
            case R.id.user_info_item:
                intent = new Intent(getContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_item:
                intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.about_item:
                intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
