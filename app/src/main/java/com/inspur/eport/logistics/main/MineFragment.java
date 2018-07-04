package com.inspur.eport.logistics.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.eport.logistics.BaseFragment;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.account.LoginActivity;
import com.inspur.eport.logistics.bean.User;
import com.inspur.eport.logistics.utils.MyToast;
import com.inspur.eport.logistics.utils.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MineFragment extends BaseFragment {

    @BindView(R.id.mine_version)
    protected TextView mVersion;
    @BindView(R.id.mine_online)
    protected TextView mOnline;
    @BindView(R.id.mine_pwd_modify)
    protected TextView mModify;
    @BindView(R.id.mine_update_check)
    protected TextView mUpdate;
    @BindView(R.id.mine_feedback)
    protected TextView mFeedback;
    @BindView(R.id.mine_logout)
    protected Button mLogout;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_mine, container, false);

        unbinder = ButterKnife.bind(this, contentView);

        mVersion.setText(getString(R.string.version, Tools.getVersionName(mActivity)));

        mModify.setOnClickListener(onClickListener);
        mUpdate.setOnClickListener(onClickListener);
        mFeedback.setOnClickListener(onClickListener);
        mLogout.setOnClickListener(onClickListener);

        mOnline.setText(User.getUser().getAccount());

        return contentView;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.mine_pwd_modify:
                    MyToast.show(mActivity, "修改密码");
                    break;
                case R.id.mine_update_check:
                    MyToast.show(mActivity, "检查更新");
                    break;
                case R.id.mine_feedback:
                    MyToast.show(mActivity, "意见反馈");
                    break;
                case R.id.mine_logout:
                    startActivity(new Intent(mActivity, LoginActivity.class));
                    mActivity.finish();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
