package com.inspur.eport.logistics.functions.team;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Team;
import com.inspur.eport.logistics.server.WebRequest;
import com.inspur.eport.logistics.utils.MyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TeamAddActivity extends BaseActivity {

    private Unbinder unbinder;
    private Team mTeam;

    @BindView(R.id.team_add_code)
    protected EditText mCodeEt;//车队号
    @BindView(R.id.team_add_name)
    protected EditText mNameEt;//车队名称
    @BindView(R.id.team_add_account)
    protected EditText mAccountEt;//车队账号
    @BindView(R.id.team_add_view_account)
    protected View viewAccount;
    @BindView(R.id.team_add_pwd)
    protected EditText mPwdEt;//账号密码
    @BindView(R.id.team_add_view_pwd)
    protected View viewPwd;
    @BindView(R.id.team_add_contact)
    protected EditText mContactEt;//车队联系人
    @BindView(R.id.team_add_truck)
    protected EditText mTruckEt;//车队常用车牌号
    @BindView(R.id.team_add_account_name)
    protected EditText mAccNameEt;//车队账号名称
    @BindView(R.id.team_add_nick)
    protected EditText mNickEt;//车队昵称
    @BindView(R.id.team_add_phone)
    protected EditText mPhoneEt;//车队联系人手机号
    @BindView(R.id.team_add_card_id)
    protected EditText mCardIdEt;//身份证号

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_team_add_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.team_add_title, 0);
        if (getIntent() != null) {
            mTeam = (Team) getIntent().getSerializableExtra("team");
        }

        if (mTeam == null) {
            viewAccount.setVisibility(View.VISIBLE);
            viewPwd.setVisibility(View.VISIBLE);
        }else {
            viewAccount.setVisibility(View.GONE);
            viewPwd.setVisibility(View.GONE);
            mCodeEt.setText(mTeam.getCode());
            mNameEt.setText(mTeam.getTypeName());
            mContactEt.setText(mTeam.getContacter());
            mTruckEt.setText(mTeam.getTruckNo());
            mAccNameEt.setText(mTeam.getAccount());
            mNickEt.setText(mTeam.getNick());
            mPhoneEt.setText(mTeam.getPhone());
            mCardIdEt.setText(mTeam.getCardId());
        }
    }

    @OnClick(R.id.team_add_commit)
    protected void commit() {

        String code = mCodeEt.getText().toString().trim();
        String name = mNameEt.getText().toString().trim();
        String account = mAccountEt.getText().toString().trim();
        String pwd = mPwdEt.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            MyToast.show(this, R.string.team_add_code_empty);
            return;
        }

        if (TextUtils.isEmpty(name)) {
            MyToast.show(this, R.string.team_add_name_empty);
            return;
        }

        if (TextUtils.isEmpty(account) && mTeam == null) {
            MyToast.show(this, R.string.team_add_account_empty);
            return;
        }

        if (TextUtils.isEmpty(pwd) && mTeam == null) {
            MyToast.show(this, R.string.team_add_pwd_empty);
            return;
        }

        String contacter = mContactEt.getText().toString().trim();
        String truckNo = mTruckEt.getText().toString().trim();
        String accName = mAccNameEt.getText().toString().trim();
        String nick = mNickEt.getText().toString().trim();
        String phone = mPhoneEt.getText().toString().trim();
        String cardId = mCardIdEt.getText().toString().trim();

        WebRequest.getInstance().modifyTeamInfo(
                mTeam == null ? "" : mTeam.getStaffId(),
                code,
                name,
                contacter,
                truckNo,
                mTeam == null ? account : mTeam.getAccount(),
                mTeam == null ? pwd : "",
                cardId,
                accName,
                nick,
                phone,
                mTeam == null,//true:添加账号，false:修改账号
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
                        if (!o.getBoolean("success")) {
                            onError(new Throwable(o.getString("failReason")));
                            return;
                        }

                        dismissDialog();
                        MyToast.show(TeamAddActivity.this, getString(R.string.operation_success));
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissDialog();
                        MyToast.show(TeamAddActivity.this,
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
