package com.eport.logistics.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.TextView;

import com.eport.logistics.BaseFragment;
import com.eport.logistics.Constants;
import com.eport.logistics.R;
import com.eport.logistics.account.LoginActivity;
import com.eport.logistics.account.WebLoginActivity;
import com.eport.logistics.bean.User;
import com.eport.logistics.utils.MyToast;
import com.eport.logistics.utils.Prefer;
import com.eport.logistics.utils.Tools;

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
                    User.getUser().setToken("123123123reqwerqe");
                    break;
                case R.id.mine_logout:
                    Prefer.getInstance().putString(Constants.KEY_PREFER_TOKEN, "");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                            @Override
                            public void onReceiveValue(Boolean value) {

                            }
                        });
                    }else {
                        CookieManager.getInstance().removeAllCookie();
                    }

//                    startActivity(new Intent(mActivity, WebLoginActivity.class));
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
