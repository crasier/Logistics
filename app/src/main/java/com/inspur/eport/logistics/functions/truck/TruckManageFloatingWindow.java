package com.inspur.eport.logistics.functions.truck;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.eport.logistics.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 委托单检索窗口
 */
public class TruckManageFloatingWindow {

    private static final int PICKER_TYPE_START = 1;
    private static final int PICKER_TYPE_END = 2;

    private Context mContext;
    private Unbinder unbinder;
    private View rootView;
    private OnButtonClickListener listener;

    private boolean isShowing = false;

    @BindView(R.id.truck_check_no)
    protected EditText mNo;
    @BindView(R.id.truck_check_type)
    protected EditText mType;
    @BindView(R.id.truck_check_license)
    protected EditText mLicense;
    @BindView(R.id.truck_check_capacity)
    protected EditText mCapacity;
    @BindView(R.id.truck_check_cancel)
    protected Button mCancel;
    @BindView(R.id.truck_check_check)
    protected Button mCheck;
    @BindView(R.id.truck_check_reset)
    protected Button mReset;

    private String mNoStr, mTypeStr, mLicenseStr, mCapacityStr;


    public TruckManageFloatingWindow(Context context, View contentView) {
        mContext = context;

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
        mNoStr = mTypeStr = mLicenseStr = mCapacityStr = "";
        mNo.setText("");
        mType.setText("");
        mLicense.setText("");
        mCapacity.setText("");
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.truck_check_cancel:
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                    break;
                case R.id.truck_check_check:
                    mNoStr = mNo.getText().toString().trim();
                    mTypeStr = mType.getText().toString().trim();
                    mLicenseStr = mLicense.getText().toString().trim();
                    mCapacityStr = mCapacity.getText().toString().trim();

                    if (listener != null) {
                        listener.onCheckClick(mNoStr, mTypeStr, mLicenseStr, mCapacityStr);
                    }
                    break;
                case R.id.truck_check_reset:
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

        public void onCheckClick(String no, String type, String license, String capacity);

        public void onResetClick();
    }
}
