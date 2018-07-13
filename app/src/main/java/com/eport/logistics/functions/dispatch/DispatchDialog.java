package com.eport.logistics.functions.dispatch;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.eport.logistics.R;
import com.eport.logistics.bean.DispatchOrder;
import com.eport.logistics.utils.ViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DispatchDialog extends Dialog{

    private LayoutInflater inflater;
    private Context context;

    @BindView(R.id.lister)
    protected ListView lister;
    @BindView(R.id.btn_negative)
    protected Button negative;
    @BindView(R.id.btn_positive)
    protected Button positive;
    @BindView(R.id.dispatch_driver)
    protected TextView driverInfo;
    @BindView(R.id.dispatch_truck)
    protected TextView truckInfo;

    private ArrayList<DispatchOrder> orders;
    private String driver;
    private String truck;

    private OrderAdapter orderAdapter;

    private onSureClickListener sureClickListener;
    private onCancelClickListener cancelClickListener;

    public DispatchDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public DispatchDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_order_dispatch_dialog);
        ButterKnife.bind(this);
        orderAdapter = new OrderAdapter();

        positive.setOnClickListener(onClickListener);
        negative.setOnClickListener(onClickListener);

        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_negative:
                    if (cancelClickListener != null) {
                        cancelClickListener.onCancelClick();
                    }
                    break;
                case R.id.btn_positive:
                    if (sureClickListener != null) {
                        sureClickListener.onSureClick();
                    }
                    break;
            }
        }
    };

    @Override
    public void show() {
        super.show();
        if (driver != null) {
            driverInfo.setText(driver);
        }

        if (truck != null) {
            truckInfo.setText(truck);
        }

        lister.setAdapter(orderAdapter);
        orderAdapter.notifyDataSetChanged();
    }

    public DispatchDialog setOrders(ArrayList<DispatchOrder> orders) {
        this.orders = orders;
        if (orderAdapter != null) orderAdapter.notifyDataSetChanged();

        return this;
    }

    public DispatchDialog setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public DispatchDialog setTruck(String truck) {
        this.truck = truck;
        return this;
    }

    public DispatchDialog setOnSureClickListener(onSureClickListener listener) {
        sureClickListener = listener;
        return this;
    }

    public DispatchDialog setOnCancelClickListener(onCancelClickListener listener) {
        this.cancelClickListener = listener;
        return this;
    }

    interface onSureClickListener{
        void onSureClick();
    }

    interface onCancelClickListener{
        void onCancelClick();
    }

    private class OrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return orders == null ? 0 : orders.size();
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
                convertView = inflater.inflate(R.layout.view_text_spinner, null);
            }
            ViewHolder.<TextView> get(convertView, R.id.text)
                    .setText(context.getString(R.string.dispatch_name,
                            orders.get(position).getContainerNo() == null ? "" : orders.get(position).getContainerNo()));
            return convertView;
        }
    }
}
