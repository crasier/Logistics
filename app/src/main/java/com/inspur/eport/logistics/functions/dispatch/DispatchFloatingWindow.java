package com.inspur.eport.logistics.functions.dispatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Dicts;
import com.inspur.eport.logistics.utils.ViewHolder;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 提箱派车、还箱派车、提箱改派、还箱改派 检索窗口
 * */
public class DispatchFloatingWindow {

    private static final int PICKER_TYPE_START = 1;
    private static final int PICKER_TYPE_END = 2;

    private Context mContext;
    private Unbinder unbinder;
    private View rootView;
    private OnButtonClickListener listener;
    private ArrayList<Dicts> dictsList;
    private SpinnerAdapter mAdapter;
    private LayoutInflater mInflater;

    private boolean isShowing = false;

    @BindView(R.id.check_order_no)
    protected EditText mNo;
    @BindView(R.id.check_deliv_place)
    protected EditText mDelivPlace;
    @BindView(R.id.check_rtn_place)
    protected EditText mRtnPlace;
    @BindView(R.id.check_order_delegate)
    protected EditText mDelegate;
    @BindView(R.id.check_order_buyer)
    protected EditText mBuyer;
    @BindView(R.id.check_order_status)
    protected Spinner mStatus;
    @BindView(R.id.check_order_cancel)
    protected Button mCancel;
    @BindView(R.id.check_order_check)
    protected Button mCheck;
    @BindView(R.id.check_order_reset)
    protected Button mReset;

    private String mNoStr, mDelivStr, mRtnStr, mDeleStr, mBuyerStr, mStatusStr;


    public DispatchFloatingWindow(Context context, View contentView) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        rootView = contentView;
        unbinder = ButterKnife.bind(this, rootView);

        mCancel.setOnClickListener(onClickListener);
        mCheck.setOnClickListener(onClickListener);
        mReset.setOnClickListener(onClickListener);

        mStatus.setPrompt(String.format(Locale.CHINA, "<%s>", mContext.getString(R.string.all)));
    }

    public void setDictsList(ArrayList<Dicts> dicts) {
        this.dictsList = dicts;
        Dicts d = new Dicts();
        d.setLabel(mContext.getString(R.string.all));//增加一个全部选项
        d.setValue("");
        dictsList.add(0, d);
        mAdapter = new SpinnerAdapter();
        mStatus.setAdapter(mAdapter);
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
        mNoStr = mDeleStr = mBuyerStr = mDelivStr = mRtnStr = mStatusStr = "";
        mNo.setText("");
        mDelegate.setText("");
        mBuyer.setText("");
        mDelivPlace.setText("");
        mRtnPlace.setText("");
        mStatus.setSelection(0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check_order_cancel:
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                    break;
                case R.id.check_order_check:
                    mNoStr = mNo.getText().toString().trim();
                    mDelivStr = mDelivPlace.getText().toString().trim();
                    mRtnStr = mRtnPlace.getText().toString().trim();
                    mDeleStr = mDelegate.getText().toString().trim();
                    mBuyerStr = mBuyer.getText().toString().trim();
                    mStatusStr = dictsList.get(mStatus.getSelectedItemPosition()).getValue();

                    if (listener != null) {
                        listener.onCheckClick(mNoStr, mDelivStr, mRtnStr, mDeleStr, mBuyerStr, mStatusStr);
                    }
                    break;
                case R.id.check_order_reset:
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

        public void onCheckClick(String no, String deliv, String rtn, String delegate, String buyer, String status);

        public void onResetClick();
    }

    private class SpinnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dictsList == null ? 0 : dictsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_text_spinner, null);
            }

            ViewHolder.<TextView> get(convertView, R.id.text)
                    .setText(dictsList.get(position).getLabel());

            return convertView;
        }
    }
}
