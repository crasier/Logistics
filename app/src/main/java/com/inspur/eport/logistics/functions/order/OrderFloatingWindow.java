package com.inspur.eport.logistics.functions.order;

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
 * 预约题、还箱检索窗口
 * */
public class OrderFloatingWindow {

    private static final int PICKER_TYPE_START = 1;
    private static final int PICKER_TYPE_END = 2;

    private Context mContext;
    private Unbinder unbinder;
    private View rootView;
    private OnButtonClickListener listener;
    private ArrayList<Dicts> dictsList;
    private String[] backArray;
    private SpinnerAdapter mAdapter;
    private BackSpinnerAdapter mBackSpinnerAdapter;
    private LayoutInflater mInflater;
    private AlertDialog datePickerDialog;
    private int pickerType;

    private boolean isShowing = false;

    @BindView(R.id.check_order_no)
    protected EditText mNo;
    @BindView(R.id.check_order_delegate)
    protected EditText mDelegate;
    @BindView(R.id.check_order_buyer)
    protected EditText mBuyer;
    @BindView(R.id.check_date_start)
    protected TextView mDateStart;
    @BindView(R.id.check_date_end)
    protected TextView mDateEnd;
    @BindView(R.id.check_order_status)
    protected Spinner mStatus;
//    @BindView(R.id.check_order_back)
//    protected Spinner mBack;
    @BindView(R.id.check_order_cancel)
    protected Button mCancel;
    @BindView(R.id.check_order_check)
    protected Button mCheck;
    @BindView(R.id.check_order_reset)
    protected Button mReset;

    private String mNoStr, mDeleStr, mBuyerStr, mDateStartStr, mDateEndStr, mStatusStr;


    public OrderFloatingWindow(Context context, View contentView) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        rootView = contentView;
        unbinder = ButterKnife.bind(this, rootView);

        mDateStart.setOnClickListener(onClickListener);
        mDateEnd.setOnClickListener(onClickListener);
        mCancel.setOnClickListener(onClickListener);
        mCheck.setOnClickListener(onClickListener);
        mReset.setOnClickListener(onClickListener);

        backArray = context.getResources().getStringArray(R.array.dispatch_order_back);
    }

    public void setDictsList(ArrayList<Dicts> dicts) {
        this.dictsList = dicts;
        Dicts d = new Dicts();
        d.setLabel(mContext.getString(R.string.all));//增加一个全部选项
        d.setValue("");
        dictsList.add(0, d);
        mAdapter = new SpinnerAdapter();
        mStatus.setAdapter(mAdapter);
        mBackSpinnerAdapter = new BackSpinnerAdapter();
//        mBack.setAdapter(mBackSpinnerAdapter);
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
        mNoStr = mDeleStr = mBuyerStr = mDateStartStr = mDateEndStr = mStatusStr = "";
        mNo.setText("");
        mDelegate.setText("");
        mBuyer.setText("");
        mDateStart.setText("");
        mDateEnd.setText("");
        mStatus.setSelection(0);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check_date_start:
                    pickerType = PICKER_TYPE_START;
                    pickDate();
                    break;
                case R.id.check_date_end:
                    pickerType = PICKER_TYPE_END;
                    pickDate();
                    break;
                case R.id.check_order_cancel:
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                    break;
                case R.id.check_order_check:
                    mNoStr = mNo.getText().toString().trim();
                    mDeleStr = mDelegate.getText().toString().trim();
                    mBuyerStr = mBuyer.getText().toString().trim();
                    mDateStartStr = mDateStart.getText().toString().trim();
                    mDateEndStr = mDateEnd.getText().toString().trim();
                    mStatusStr = dictsList.get(mStatus.getSelectedItemPosition()).getValue();
//                    mBackStr = String.valueOf(mBack.getSelectedItemPosition());

                    if (listener != null) {
                        listener.onCheckClick(mNoStr, mDeleStr, mBuyerStr, mDateStartStr, mDateEndStr, mStatusStr);
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

    /**
     * 选择日期
     * */
    private void pickDate() {
        if (datePickerDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyDialogStyle);
            View pickerView = mInflater.inflate(R.layout.view_datepicker, null);
            final DatePicker picker = pickerView.findViewById(R.id.picker);
            builder.setTitle(R.string.order_check_picker);
            builder.setView(pickerView);
            builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    setPickedDate(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                }
            })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            datePickerDialog = builder.create();
        }

        datePickerDialog.show();
    }

    private void setPickedDate(int year, int month, int day) {
        switch (pickerType) {
            case PICKER_TYPE_START:
                mDateStart.setText(String.format(Locale.CHINA,
                        "%d-%d-%d",
                        year,month + 1,day));
                break;
            case PICKER_TYPE_END:

                mDateEnd.setText(String.format(Locale.CHINA,
                        "%d-%d-%d",
                        year,month + 1,day));
                break;
        }
    }

    public void setButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnButtonClickListener {
        public void onCancelClick();

        public void onCheckClick(String no, String delegate, String buyer, String dateStart, String dateEnd, String status);

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

    private class BackSpinnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return backArray == null ? 0 : backArray.length;
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
                    .setText(backArray[position]);

            return convertView;
        }
    }
}
