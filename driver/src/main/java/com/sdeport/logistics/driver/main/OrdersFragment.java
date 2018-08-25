package com.sdeport.logistics.driver.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Order;
import com.sdeport.logistics.driver.bean.OrderPage;
import com.sdeport.logistics.driver.server.WebRequest;

import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class OrdersFragment extends BaseFragment {

    private static final String TAG = "OrdersFragment";

    private static final int ORDER_TYPE_ALL = 0;
    private static final int ORDER_TYPE_NEW = 1;
    private static final int ORDER_TYPE_ING = 2;
    private static final int ORDER_TYPE_FINISHED = 3;

    private Unbinder unbinder;

    private LayoutInflater mInflater;

    @BindView(R.id.refresher)
    protected SmartRefreshLayout refresher;
    @BindView(R.id.header)
    protected MaterialHeader header;
    @BindView(R.id.list_emptier)
    protected TextView empty;
    @BindView(R.id.title_all)
    protected TextView all;
    @BindView(R.id.title_new)
    protected TextView unReceived;
    @BindView(R.id.title_received)
    protected TextView received;
    @BindView(R.id.title_finished)
    protected TextView finished;
//    @BindView(R.id.line)
//    protected ImageView line;

    @BindView(R.id.pager)
    protected ViewPager pager;

//    private SparseArray<ArrayList<Order>> dataList;

    private TextView tabTitles[];
    private ArrayList<ListView> tabViews;

    private PageAdapter adapter;
    private ListerAdapter listerAdapter;

    private int currentPageIndex = 0;//注意：此index与获取派车单状态码是一一对应的 页0-码0,页1-码1,页2-码2,页3-码3

    private SparseArray<OrderPage> dataLists;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_orders, container, false);

        dataLists = new SparseArray<>();
        mInflater = inflater;
        unbinder = ButterKnife.bind(this, contentView);

        empty.setText(R.string.order_list_empty);
        pager.setOffscreenPageLimit(3);
        pager.addOnPageChangeListener(pageChangeListener);

        refresher.setOnRefreshListener(refreshListener);
        all.setOnClickListener(onClickListener);
        unReceived.setOnClickListener(onClickListener);
        received.setOnClickListener(onClickListener);
        finished.setOnClickListener(onClickListener);

        tabTitles = new TextView[]{all, unReceived, received, finished};
        tabViews = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            ListView view = (ListView) inflater.inflate(R.layout.listview, null);
            view.setAdapter(new ListerAdapter(i));
            tabViews.add(view);
        }

        adapter = new PageAdapter();
        pager.setAdapter(adapter);

        getDataList(false, ORDER_TYPE_ALL);
        getDataList(false, ORDER_TYPE_NEW);
        getDataList(false, ORDER_TYPE_ING);
        getDataList(false, ORDER_TYPE_FINISHED);
        return contentView;
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            getDataList(false, currentPageIndex);
        }
    };

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            if (currentPageIndex == position) {
                return;
            }

            tabTitles[currentPageIndex].setTextColor(getResources().getColor(R.color.text_dark));
            tabTitles[currentPageIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            tabTitles[position].setTextColor(getResources().getColor(R.color.orange));
            tabTitles[position].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.drawable_line_orange);

            currentPageIndex = position;

            refreshList(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = currentPageIndex;
            switch (v.getId()) {
                case R.id.title_all:
                    index = 0;
                    break;
                case R.id.title_new:
                    index = 1;
                    break;
                case R.id.title_received:
                    index = 2;
                    break;
                case R.id.title_finished:
                    index = 3;
                    break;
            }

            if (currentPageIndex == index) {
                return;
            }
            pager.setCurrentItem(index, false);
        }
    };

    private void getDataList(final boolean add, final int target) {

        if (dataLists.get(target) == null) {
            dataLists.put(target, new OrderPage());
        }

        if (dataLists.get(target).isLoading()) {
            return;
        }

        dataLists.get(target).setLoading(true);
        WebRequest.getInstance().getOrderList(dataLists.get(target).getCurrentPage(), dataLists.get(target).getCountPerPage(), String.valueOf(target), new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "refreshList: onSubscribe = "+target);
            }

            @Override
            public void onNext(JSONObject o) {
                Log.e(TAG, "refreshList: onNext = "+o+"; target = "+target);
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }

                try {
                    JSONObject data = JSON.parseObject(o.getString("data"));
                    Log.d(TAG, "refreshList: data = "+data);
//                    dataTotal = data.getIntValue("total");
                    dataLists.get(target).setTotal(data.getIntValue("total"));
                    String listStr = data.getString("list");
                    JSONArray list = JSON.parseArray(listStr);
                    Log.d(TAG, "refreshList: list = "+list);
                    ArrayList<Order> orders = (ArrayList<Order>) JSON.parseArray(list.toJSONString(), Order.class);
                    Log.d(TAG, "refreshList: orders = "+orders);
                    if (add) {
                        dataLists.get(target).getDatas().addAll(orders);
                    }else {
                        dataLists.get(target).setDatas(orders);
                    }
                }catch (Exception e) {
                    onError(e);
                    return;
                }
                dataLists.get(target).setLoading(false);

                refreshFinished();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "refreshList: onError = "+e+"; target = "+target);
                dataLists.get(target).setLoading(false);
                mActivity.dismissDialog();
                refreshFinished();
                MyToast.show(mActivity, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });

        refreshList(currentPageIndex);

        refresher.finishRefresh(true);
    }

    private void refreshList(int pageIndex) {

        listerAdapter = (ListerAdapter) tabViews.get(pageIndex).getAdapter();
        empty.setVisibility(dataLists.get(pageIndex).getDatas().isEmpty() ? View.VISIBLE : View.GONE);
        listerAdapter.setData(dataLists.get(pageIndex).getDatas());
        listerAdapter.notifyDataSetChanged();
    }

    private void refreshFinished() {
        boolean allFinished = true;
        for (int i = 0; i < dataLists.size(); i++) {
            if (dataLists.get(dataLists.keyAt(i)).isLoading()) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            refresher.finishRefresh();
        }
    }

    private void onViewClick(View v, int position) {
        switch (v.getId()) {
            case R.id.receive:
                MyToast.show(mActivity, "page:"+currentPageIndex+"接受 "+position);
                break;
            case R.id.refuse:
                MyToast.show(mActivity, "page:"+currentPageIndex+"拒绝 "+position);
                break;
            case R.id.cancel:
                MyToast.show(mActivity, "page:"+currentPageIndex+"撤销 "+position);
                break;
            case R.id.check:
                MyToast.show(mActivity, "page:"+currentPageIndex+"查看 "+position);
                break;
            case R.id.order_simple:
                dataLists.get(currentPageIndex).getDatas().get(position).setSpread(!dataLists.get(currentPageIndex).getDatas().get(position).isSpread());
                break;
        }
        ((ListerAdapter) tabViews.get(currentPageIndex).getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(tabViews.get(position));
            return tabViews.get(position);
//            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }

    private class ListerAdapter extends BaseAdapter {

        private ArrayList<Order> orders;
        private int type;

        public ListerAdapter(int type) {
            this.type = type;
        }

        public void setData(ArrayList<Order> orders) {
            this.orders = orders;
        }

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

            Order order = dataLists.get(currentPageIndex).getDatas().get(position);
            holder.bill.setText(
                    String.format(Locale.CHINA, "%s:%s",
                            getString(R.string.order_ing_bill_no),
                            order.getBillNo()));
            holder.simple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });
            holder.status.setText(getString(R.string.not_receive));
            holder.owner.setText(order.getOwner() == null ? "" : order.getOwner());
            holder.address.setText(order.getAddress() == null ? "" : order.getAddress());

            setClicker(holder, position);

            if (!dataLists.get(currentPageIndex).getDatas().get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.spreader.setVisibility(View.GONE);
                return convertView;
            }else {
                holder.arrow.setRotation(180);
                holder.spreader.setVisibility(View.VISIBLE);
            }

//            holder.contact.setText("暂无此项信息,待删除");//TODO 货主联系方式是否需要
            holder.timeDelivery.setText(order.getDelivTime() == null ? "" : order.getDelivTime());
            holder.cntr.setText(
                    String.format(Locale.CHINA, "%s:%s  %s:%s%s",
                            getString(R.string.cntr_no),
                            order.getCntrNo() == null ? "" : order.getCntrNo(),
                            getString(R.string.cntr_detail),
                            order.getCntrSize() == null ? "" : order.getCntrSize(),
                            order.getCntrType() == null ? "" : order.getCntrType()));
            if (TextUtils.isEmpty(order.gettStatus())) {
                holder.infoGet.setVisibility(View.GONE);
            }else {
                holder.infoGet.setVisibility(View.VISIBLE);
                holder.driverGet.setText(
                        String.format(Locale.CHINA, "%s:%s",
                                getString(R.string.time_container_get),
                                order.getDelivTime()));//TODO 修改为提箱时间
                holder.addrGet.setText(
                        String.format(Locale.CHINA, "%s:%s",
                                getString(R.string.address_container_get),
                                order.getDelivPlace() == null ? "" : order.getDelivPlace()));
            }

            if (TextUtils.isEmpty(order.getrStatus())) {
                holder.infoRtn.setVisibility(View.GONE);
            }else {
                holder.infoRtn.setVisibility(View.VISIBLE);
                holder.driverRtn.setText(
                        String.format(Locale.CHINA, "%s:%s",
                                getString(R.string.time_container_rtn),
                                order.getDelivTime())//TODO 修改为还箱时间
                );
                holder.addrRtn.setText(
                        String.format(Locale.CHINA, "%s:%s",
                                getString(R.string.address_container_rtn),
                                order.getDelivPlaceRtn() == null ? "" : order.getDelivPlaceRtn()));
            }

            return convertView;
        }

        private void setClicker(ViewHolder holder, final int position) {
            holder.receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
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
        @BindView(R.id.owner)
        protected TextView owner;
        @BindView(R.id.address)
        protected TextView address;

        @BindView(R.id.order_simple)
        protected View simple;
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
}
