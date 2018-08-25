package com.sdeport.logistics.driver.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Order;
import com.sdeport.logistics.driver.bean.OrderPage;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.server.WebRequest;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.flowable.FlowableInternalHelper;

/**
 * @description 首页
 * @author Winfred Yin
 * @date 2018-08-15
 * */
public class HomeFragment extends BaseFragment {

    private final String TAG = "HomeFragment";

    private static final int ORDER_TYPE_NEW = 1;
    private static final int ORDER_TYPE_ING = 2;

    @BindView(R.id.refresher)
    protected SmartRefreshLayout refresher;
    @BindView(R.id.header)
    protected MaterialHeader header;
    @BindView(R.id.ing_emptier)
    protected TextView ingEmpty;
    @BindView(R.id.not_emptier)
    protected TextView notEmpty;

    @BindView(R.id.order_ing)
    protected ListView ingLister;
    @BindView(R.id.order_not_received)
    protected ListView notLister;

    private Unbinder unbinder;
    private LayoutInflater mInflater;
    private IngAdapter ingAdapter;
    private NotAdapter notAdapter;

    private SparseArray<OrderPage> dataLists;

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

        dataLists = new SparseArray<>();

        unbinder = ButterKnife.bind(this, contentView);
        mInflater = inflater;

        refresher.setOnRefreshListener(onRefreshListener);
        ingAdapter = new IngAdapter();
        notAdapter = new NotAdapter();

        ingLister.setAdapter(ingAdapter);
        notLister.setAdapter(notAdapter);

        refreshList(false, ORDER_TYPE_ING);
        refreshList(false, ORDER_TYPE_NEW);
        return contentView;
    }

    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            refreshList(false, ORDER_TYPE_ING);
            refreshList(false, ORDER_TYPE_NEW);
        }
    };

    /**
     * 拉取正在进行列表
     * @param add 是更新还是追加
     * @param target 2:正在进行；1：待接单
     * */
    private void refreshList(final boolean add, final int target) {

        if (dataLists.get(target) == null) {
            dataLists.put(target, new OrderPage());
        }

        if (dataLists.get(target).isLoading()) {
            return;
        }

        dataLists.get(target).setLoading(true);

        if (target == ORDER_TYPE_ING) {//根据保存的ID号（正在进行中）查询派车单详情

            String id = "";
            if (TextUtils.isEmpty(Prefer.getInstance().getString(Constants.KEY_ORDER_ING, ""))) {
                dataLists.get(target).setLoading(false);
                return;
            }
            try {
                id = JSON.parseObject(Prefer.getInstance().getString(Constants.KEY_ORDER_ING, ""), Order.class).getId();
            }catch (Exception e) {
                dataLists.get(target).setLoading(false);
                return;
            }
            WebRequest.getInstance().getOrderDetail(id, new Observer<JSONObject>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(JSONObject o) {
                    if (o == null) {
                        onError(null);
                        return;
                    }
                    if (!o.getBooleanValue("success")) {
                        onError(new Throwable(o.getString("failReason")));
                        return;
                    }
                    try {
                        String data = o.getString("data");
                        Log.d(TAG, "refreshList: data = "+data);
                        Order ingOrder = JSON.parseObject(data, Order.class);
                        ingOrder.setSpread(true);
                        Log.d(TAG, "refreshList: ingOrder = "+ingOrder);
                        dataLists.get(target).getDatas().add(ingOrder);
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
            return;
        }
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

        ingAdapter.notifyDataSetChanged();
        notAdapter.notifyDataSetChanged();

        ingEmpty.setVisibility(dataLists.get(ORDER_TYPE_ING).getDatas().isEmpty() ? View.VISIBLE : View.GONE);
        notEmpty.setVisibility(dataLists.get(ORDER_TYPE_NEW).getDatas().isEmpty() ? View.VISIBLE : View.GONE);
    }

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
//                MyToast.show(mActivity, "进行中 查看"+position);
                break;
            case R.id.order_simple:
                dataLists.get(ORDER_TYPE_ING).getDatas().get(position).setSpread(!dataLists.get(ORDER_TYPE_ING).getDatas().get(position).isSpread());
                ingAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void notBtnClick(View v, int position) {
        switch (v.getId()) {
            case R.id.receive:
//                MyToast.show(mActivity, "未接单 接受"+position);
                acceptOrder(dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).getId(), Constants.TAG_ORDER_OPERATION_ALL);
                break;
            case R.id.refuse:
//                MyToast.show(mActivity, "未接单 拒绝"+position);
                refuseOrCancelOrder(dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).getId(), Constants.TAG_ORDER_OPERATION_ALL);
                break;
            case R.id.cancel:
//                MyToast.show(mActivity, "未接单 撤销"+position);
                confirmOrder(dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).getId(), Constants.TAG_ORDER_OPERATION_ALL);
                break;
            case R.id.check:
//                MyToast.show(mActivity, "未接单 查看"+position);
                break;
            case R.id.order_simple:
                dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).setSpread(!dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).isSpread());
                notAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * 接单
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态
     * */
    private void acceptOrder(String id, String type) {
        WebRequest.getInstance().acceptOrder(id, type, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }
                mActivity.dismissDialog();

                MyToast.show(mActivity, R.string.operation_success);
                refreshList(false, ORDER_TYPE_NEW);
            }

            @Override
            public void onError(Throwable e) {
                mActivity.dismissDialog();
                MyToast.show(mActivity, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 拒绝、撤销
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态,A-同时操作
     * */
    private void refuseOrCancelOrder(String id, String type) {
        WebRequest.getInstance().RefuseOrCancelOrder(id, type, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }
                mActivity.dismissDialog();

                MyToast.show(mActivity, R.string.operation_success);
                refreshList(false, ORDER_TYPE_NEW);
            }

            @Override
            public void onError(Throwable e) {
                mActivity.dismissDialog();
                MyToast.show(mActivity, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 确认提箱、还箱（司机端自行操作）
     * @param id 派车单ID
     * @param type T-提箱状态，R-还箱状态
     * */
    private void confirmOrder(String id, String type) {
        WebRequest.getInstance().confirmOrder(id, type, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }
                mActivity.dismissDialog();

                MyToast.show(mActivity, R.string.operation_success);
                refreshList(false, ORDER_TYPE_NEW);
            }

            @Override
            public void onError(Throwable e) {
                mActivity.dismissDialog();
                MyToast.show(mActivity, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
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
            return dataLists.get(ORDER_TYPE_ING) == null ? 0 : dataLists.get(ORDER_TYPE_ING).getDatas().size();
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

            Order order = dataLists.get(ORDER_TYPE_ING).getDatas().get(position);
            holder.bill.setText(
                    String.format(Locale.CHINA, "%s:%s",
                            getString(R.string.order_ing_bill_no),
                            order.getBillNo() == null ? "" : order.getBillNo()));
//            holder.simple.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ingBtnClick(v, position);
//                }
//            });
            setStatus(holder.status, order);
            holder.owner.setText(order.getOwner() == null ? "" : order.getOwner());
            holder.address.setText(order.getAddress() == null ? "" : order.getAddress());
            setClicker(holder, position);
            holder.spreader.setVisibility(View.VISIBLE);
            holder.arrow.setVisibility(View.INVISIBLE);

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

        /**
         * 设置状态
         * */
        private void setStatus(View v, Order order) {

        }

        private void setClicker(ViewHolder holder, final int position) {

            Order order = dataLists.get(ORDER_TYPE_ING).getDatas().get(position);

            boolean receive;
            boolean refuse;
            boolean cancel;

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
            return dataLists.get(ORDER_TYPE_NEW) == null ? 0 : dataLists.get(ORDER_TYPE_NEW).getDatas().size();
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

            if (dataLists.get(ORDER_TYPE_NEW).getDatas().size() < dataLists.get(ORDER_TYPE_NEW).getTotal()
                    && position >= dataLists.get(ORDER_TYPE_NEW).getDatas().size()) {
                refreshList(true, ORDER_TYPE_NEW);
            }

            Order order = dataLists.get(ORDER_TYPE_NEW).getDatas().get(position);
            holder.bill.setText(
                    String.format(Locale.CHINA, "%s:%s",
                            getString(R.string.order_ing_bill_no),
                            order.getBillNo()));
            holder.simple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notBtnClick(v, position);
                }
            });
            holder.status.setText(getString(R.string.not_receive));
            holder.owner.setText(order.getOwner() == null ? "" : order.getOwner());
            holder.address.setText(order.getAddress() == null ? "" : order.getAddress());

            setClicker(holder, position);

            if (!dataLists.get(ORDER_TYPE_NEW).getDatas().get(position).isSpread()) {
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

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
