package com.inspur.eport.logistics.functions.transport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Order;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TransportOrderDetailActivity extends BaseActivity {

    private Unbinder unbinder;
    private Order order;
    private LayoutInflater mInflater;
    private Adapter mAdapter;

    @BindView(R.id.detail_no)
    protected TextView mNo;
    @BindView(R.id.detail_delegate)
    protected TextView mDelegate;
    @BindView(R.id.detail_contact)
    protected TextView mContact;
    @BindView(R.id.detail_address)
    protected TextView mAddress;
    @BindView(R.id.detail_remark)
    protected TextView mRemark;
    @BindView(R.id.lister)
    protected ListView mLister;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_order_transport_detail);

        order = (Order) getIntent().getSerializableExtra("order");
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.order_detail_title, 0);
        mInflater = LayoutInflater.from(this);
        mAdapter = new Adapter();
        mLister.setAdapter(mAdapter);

        mNo.setText(order.getBillNo());
        mDelegate.setText(order.getDelegate());
        mContact.setText(order.getBuyerCN());
        mAddress.setText(order.getAddress());
    }

    @Override
    protected void freeMe() {

    }

    @Override
    public void onBackPressed() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_title:
                onBackPressed();
                break;
        }
    }

    private Order.Container getTitleContainer() {
        Order.Container cont = new Order.Container();
        cont.setNo(getString(R.string.order_container_no));
        cont.setType(getString(R.string.order_container_type));
        cont.setSize(getString(R.string.order_container_size));
        cont.setDate(getString(R.string.order_container_date));
        cont.setPlace(getString(R.string.order_container_place));
        return cont;
    }

    public class Adapter extends BaseAdapter{

        private ArrayList<Order.Container> containersList;

        public Adapter() {
            if (order == null || order.getContainers() == null) {
                containersList = new ArrayList<>();
                containersList.add(getTitleContainer());
            }else {
                containersList = order.getContainers();
                containersList.add(0, getTitleContainer());
            }
        }

        @Override
        public int getCount() {
            return containersList.size();
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_transport_order_detail, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == 0) {
                holder.no.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.type.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.size.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.date.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.place.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else {
                holder.no.setTextColor(getResources().getColor(R.color.text_main));
                holder.type.setTextColor(getResources().getColor(R.color.text_main));
                holder.size.setTextColor(getResources().getColor(R.color.text_main));
                holder.date.setTextColor(getResources().getColor(R.color.text_main));
                holder.place.setTextColor(getResources().getColor(R.color.text_main));
            }

            holder.no.setText(containersList.get(position).getNo());
            holder.type.setText(containersList.get(position).getType());
            holder.size.setText(containersList.get(position).getSize());
            holder.date.setText(containersList.get(position).getDate());
            holder.place.setText(containersList.get(position).getPlace());

            return convertView;
        }

        public class ViewHolder {
            @BindView(R.id.detail_container_no)
            TextView no;
            @BindView(R.id.detail_container_type)
            TextView type;
            @BindView(R.id.detail_container_size)
            TextView size;
            @BindView(R.id.detail_container_date)
            TextView date;
            @BindView(R.id.detail_container_place)
            TextView place;
        }
    }
}
