package com.eport.logistics.functions.driver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eport.logistics.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 委托单检索窗口
 */
public class DriverManageFloatingWindow {

    private static final int PICKER_TYPE_START = 1;
    private static final int PICKER_TYPE_END = 2;

    private Context mContext;
    private Unbinder unbinder;
    private View rootView;
    private OnButtonClickListener listener;
    private LayoutInflater mInflater;

    private boolean isShowing = false;

    @BindView(R.id.driver_check_account)
    protected EditText mAccount;
    @BindView(R.id.driver_check_name)
    protected EditText mName;
    @BindView(R.id.driver_check_card_id)
    protected EditText mCardId;
    @BindView(R.id.driver_check_phone)
    protected EditText mPhone;
    @BindView(R.id.driver_check_nick)
    protected EditText mNick;
    @BindView(R.id.driver_check_truck)
    protected EditText mTruckNo;
    @BindView(R.id.driver_check_cancel)
    protected Button mCancel;
    @BindView(R.id.driver_check_check)
    protected Button mCheck;
    @BindView(R.id.driver_check_reset)
    protected Button mReset;

    private String mAccountStr, mNameStr, mCardIdStr, mPhoneStr, mNickStr, mTruckNoStr;


    public DriverManageFloatingWindow(Context context, View contentView) {
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
        mAccountStr = mNameStr = mCardIdStr = mPhoneStr = mNickStr = mTruckNoStr = "";
        mAccount.setText("");
        mName.setText("");
        mCardId.setText("");
        mPhone.setText("");
        mNick.setText("");
        mTruckNo.setText("");
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.driver_check_cancel:
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                    break;
                case R.id.driver_check_check:
                    mAccountStr = mAccount.getText().toString().trim();
                    mNameStr = mName.getText().toString().trim();
                    mCardIdStr = mCardId.getText().toString().trim();
                    mPhoneStr = mPhone.getText().toString().trim();
                    mNickStr = mNick.getText().toString().trim();
                    mTruckNoStr = mTruckNo.getText().toString().trim();

                    if (listener != null) {
                        listener.onCheckClick(mAccountStr, mNameStr, mCardIdStr, mPhoneStr, mNickStr, mTruckNoStr);
                    }
                    break;
                case R.id.driver_check_reset:
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

        public void onCheckClick(String account, String name, String cardId, String phone, String nick, String truckNo);

        public void onResetClick();
    }
}
