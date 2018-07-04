package com.inspur.eport.logistics.functions.team;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Dicts;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 委托单检索窗口
 */
public class TeamManageFloatingWindow {

    private static final int PICKER_TYPE_START = 1;
    private static final int PICKER_TYPE_END = 2;

    private Context mContext;
    private Unbinder unbinder;
    private View rootView;
    private OnButtonClickListener listener;
    private ArrayList<Dicts> dictsList;
    private LayoutInflater mInflater;
    private AlertDialog datePickerDialog;
    private int pickerType;

    private boolean isShowing = false;

    @BindView(R.id.team_check_name)
    protected EditText mName;
    @BindView(R.id.team_check_code)
    protected EditText mCode;
    @BindView(R.id.team_check_contact)
    protected EditText mContact;
    @BindView(R.id.team_check_phone)
    protected EditText mPhone;
    @BindView(R.id.team_check_account)
    protected EditText mAccount;
    @BindView(R.id.team_check_account_name)
    protected EditText mAccountName;
    @BindView(R.id.team_check_card)
    protected EditText mCard;
    @BindView(R.id.team_check_nick)
    protected EditText mNick;
    @BindView(R.id.team_check_cancel)
    protected Button mCancel;
    @BindView(R.id.team_check_check)
    protected Button mCheck;
    @BindView(R.id.team_check_reset)
    protected Button mReset;

    private String mNameStr, mCodeStr, mContactStr, mPhoneStr, mAccountStr, mAccountNameStr, mCardStr, mNickStr;


    public TeamManageFloatingWindow(Context context, View contentView) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        rootView = contentView;
        unbinder = ButterKnife.bind(this, rootView);

        mCancel.setOnClickListener(onClickListener);
        mCheck.setOnClickListener(onClickListener);
        mReset.setOnClickListener(onClickListener);
    }

    public void dismiss() {
        if (!isShowing) return;
        rootView.setVisibility(View.GONE);
        isShowing = false;
    }

    public void show() {
        if (isShowing) return;
        rootView.setVisibility(View.VISIBLE);
        isShowing = true;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void reset() {
        mNameStr = mCodeStr = mContactStr = mPhoneStr = mAccountStr = mAccountNameStr = mCardStr = mNickStr = "";
        mName.setText("");
        mCode.setText("");
        mContact.setText("");
        mPhone.setText("");
        mAccount.setText("");
        mAccountName.setText("");
        mCard.setText("");
        mNick.setText("");
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.team_check_cancel:
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                    break;
                case R.id.team_check_check:
                    mNameStr = mName.getText().toString().trim();
                    mCodeStr = mCode.getText().toString().trim();
                    mContactStr = mContact.getText().toString().trim();
                    mPhoneStr = mPhone.getText().toString().trim();
                    mAccountStr = mAccount.getText().toString().trim();
                    mAccountNameStr = mAccountName.getText().toString().trim();
                    mCardStr = mCard.getText().toString().trim();
                    mNickStr = mNick.getText().toString().trim();

                    if (listener != null) {
                        listener.onCheckClick(mNameStr, mCodeStr, mContactStr, mPhoneStr, mAccountStr, mAccountNameStr, mCardStr, mNickStr);
                    }
                    break;
                case R.id.team_check_reset:
                    reset();
                    if (listener != null) {
                        listener.onResetClick();
                    }
                    break;
            }
        }
    };


    public void setButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnButtonClickListener {
        public void onCancelClick();

        public void onCheckClick(String name, String code, String contact, String phone, String account, String accountName, String card, String nick);

        public void onResetClick();
    }
}
