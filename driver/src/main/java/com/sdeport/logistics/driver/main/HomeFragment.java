package com.sdeport.logistics.driver.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Order;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @description 首页
 * @author Winfred Yin
 * @date 2018-08-15
 * */
public class HomeFragment extends BaseFragment {

    private final String TAG = "HomeFragment";

    @BindView(R.id.refresher)
    protected SmartRefreshLayout refresher;
    @BindView(R.id.header)
    protected MaterialHeader header;
    @BindView(R.id.emptier)
    protected TextView empty;

    @BindView(R.id.order_ing)
    protected ListView ingLister;
    @BindView(R.id.order_not_received)
    protected ListView notLister;

    private Unbinder unbinder;
    private LayoutInflater mInflater;
    private IngAdapter ingAdapter;
    private NotAdapter notAdapter;

    private ArrayList<Order> ingDataList;
    private ArrayList<Order> notDataList;

    private static HomeFragment instance;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        instance = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_home, container, false);

        unbinder = ButterKnife.bind(this, contentView);
        mInflater = inflater;

        refresher.setOnRefreshListener(onRefreshListener);
        ingAdapter = new IngAdapter();
        notAdapter = new NotAdapter();

        ingLister.setAdapter(ingAdapter);
        notLister.setAdapter(notAdapter);

        return contentView;
    }

    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            if (ingDataList == null) {
                ingDataList = new ArrayList<>();
            }else {
                ingDataList.clear();
            }

            if (notDataList == null) {
                notDataList = new ArrayList<>();
            }else {
                notDataList.clear();
            }

            int size = new Random().nextInt(5);
            for (int i = 0; i < size; i++) {
                Order order = new Order();
                order.setId(String.valueOf(i));

                if (i < 1) {
                    order.setSpread(true);
                    ingDataList.add(order);
                }else {
                    order.setSpread(false);
                    notDataList.add(order);
                }

            }

            ingAdapter.notifyDataSetChanged();
            notAdapter.notifyDataSetChanged();

            refresher.finishRefresh();
        }
    };

    private AbsListView.OnScrollListener onScrollListener =
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    switch (i) {
                        case SCROLL_STATE_IDLE:
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            };

    private void ingBtnClick(View v, int position) {
        switch (v.getId()) {
            case R.id.receive:
                MyToast.show(mActivity, "进行中 接受"+position);
                break;
            case R.id.refuse:
                MyToast.show(mActivity, "进行中 拒绝"+position);
                break;
            case R.id.cancel:
                MyToast.show(mActivity, "进行中 撤销"+position);
                break;
            case R.id.check:
                MyToast.show(mActivity, "进行中 查看"+position);
                break;
            case R.id.bill_no:
                ingDataList.get(position).setSpread(!ingDataList.get(position).isSpread());
                ingAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void notBtnClick(View v, int position) {
        switch (v.getId()) {
            case R.id.receive:
                MyToast.show(mActivity, "未接单 接受"+position);
                break;
            case R.id.refuse:
                MyToast.show(mActivity, "未接单 拒绝"+position);
                break;
            case R.id.cancel:
                MyToast.show(mActivity, "未接单 撤销"+position);
                break;
            case R.id.check:
                MyToast.show(mActivity, "未接单 查看"+position);
                break;
            case R.id.bill_no:
                notDataList.get(position).setSpread(!notDataList.get(position).isSpread());
                notAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onStart() {
        try {
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        try {
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    public static HomeFragment getInstance() {
        return instance;
    }

    private class IngAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ingDataList == null ? 0 : ingDataList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.included_order_detail, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.bill.setText(getString(R.string.order_ing_bill_no)+":"+"CNTU2030132345");
            holder.bill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingBtnClick(v, position);
                }
            });
            holder.status.setText("已提箱");
            holder.buyer.setText("山东华中股份有限公司");
            holder.address.setText("山东省青岛市即墨区寂寞路");
            setClicker(holder, position);

            if (!ingDataList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.spreader.setVisibility(View.GONE);
                return convertView;
            }else {
                holder.arrow.setRotation(180);
                holder.spreader.setVisibility(View.VISIBLE);
            }

            holder.contact.setText("15622220000");
            holder.timeDelivery.setText("2018-08-22 12:30:00");
            holder.cntr.setText("20GP");
            holder.driverGet.setText("张三,  鲁A12345,  2018-08-23 12:33:00");
            holder.addrGet.setText("提箱地："+"青岛港");
            holder.driverRtn.setText("李四,  鲁B54321,  2018-08-23 12:00:33");
            holder.addrRtn.setText("还箱地："+"青岛港2");

            return convertView;
        }

        private void setClicker(ViewHolder holder, final int position) {
            holder.receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingBtnClick(v, position);
                }
            });

            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingBtnClick(v, position);
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingBtnClick(v, position);
                }
            });

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ingBtnClick(v, position);
                }
            });
        }
    }


    private class NotAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return notDataList == null ? 0 : notDataList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.included_order_detail, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.bill.setText(getString(R.string.order_ing_bill_no)+":"+"CNTU2030132345");
            holder.bill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });
            holder.status.setText("已提箱");
            holder.buyer.setText("山东华中股份有限公司");
            holder.address.setText("山东省青岛市即墨区寂寞路");

            setClicker(holder, position);

            if (!notDataList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.spreader.setVisibility(View.GONE);
                return convertView;
            }else {
                holder.arrow.setRotation(180);
                holder.spreader.setVisibility(View.VISIBLE);
            }

            holder.contact.setText("15622220000");
            holder.timeDelivery.setText("2018-08-22 12:30:00");
            holder.cntr.setText("20GP");
            holder.driverGet.setText("张三,  鲁A12345,  2018-08-23 12:33:00");
            holder.addrGet.setText("提箱地："+"青岛港");
            holder.driverRtn.setText("李四,  鲁B54321,  2018-08-23 12:00:33");
            holder.addrRtn.setText("还箱地："+"青岛港2");

            return convertView;
        }

        private void setClicker(ViewHolder holder, final int position) {
            holder.receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });

            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });
        }
    }

    protected class ViewHolder{
        @BindView(R.id.arrow)
        protected ImageView arrow;
        @BindView(R.id.bill_no)
        protected TextView bill;
        @BindView(R.id.status)
        protected TextView status;
        @BindView(R.id.buyer)
        protected TextView buyer;
        @BindView(R.id.address)
        protected TextView address;

        @BindView(R.id.order_spread)
        protected View spreader;

        @BindView(R.id.contact)
        protected TextView contact;
        @BindView(R.id.time_delivery)
        protected TextView timeDelivery;
        @BindView(R.id.container_info)
        protected TextView cntr;
        @BindView(R.id.info_get)
        protected View infoGet;
        @BindView(R.id.driver_get)
        protected TextView driverGet;
        @BindView(R.id.address_get)
        protected TextView addrGet;
        @BindView(R.id.info_rtn)
        protected View infoRtn;
        @BindView(R.id.driver_rtn)
        protected TextView driverRtn;
        @BindView(R.id.address_rtn)
        protected TextView addrRtn;

        @BindView(R.id.functions)
        protected View funs;
        @BindView(R.id.receive)
        protected TextView receive;
        @BindView(R.id.refuse)
        protected TextView refuse;
        @BindView(R.id.cancel)
        protected TextView cancel;
        @BindView(R.id.check)
        protected TextView check;
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
