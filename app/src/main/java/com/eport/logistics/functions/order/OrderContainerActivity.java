package com.eport.logistics.functions.order;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.BaseActivity;
import com.eport.logistics.R;
import com.eport.logistics.bean.Dicts;
import com.eport.logistics.bean.DispatchOrder;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 预约提箱、还箱
 * */
public class OrderContainerActivity extends BaseActivity {
    public static final String TAG = "OrderContainerActivity";

    private Unbinder unbinder;
    private Adapter mAdapter;
    private LayoutInflater mInflater;

    @BindView(R.id.refresher)
    protected SmartRefreshLayout mRefresher;
    @BindView(R.id.lister)
    protected ListView mLister;
    @BindView(R.id.header)
    protected MaterialHeader mHeader;
    @BindView(R.id.empty)
    protected TextView mEmpty;
    @BindView(R.id.floating_bar)
    protected FloatingActionButton mFloatingBtn;
    @BindView(R.id.floating_window)
    protected View mFloatingWindow;

    private TextView footView;

    private LinkedHashMap<String, Dicts> dictsMap;
    private ArrayList<DispatchOrder> ordersList;

    private DispatchOrder operatingOrder;

    private int pageSize;//每页数据条数
    private int pageNum;//当前所在页
    private int pageTotal;//总页数
    private int itemTotal;//总条数

    private String billNo = "";//提单号
    private String forwarderName= "";//货代
    private String consigneeCName= "";//收货人
    private String delivTimeStart= "";//送货日期始
    private String delivTimeEnd= "";//送货日期终
    private String flowStatus= "";//状态
    private String oriBack="";//是否原车返回

    private final int itemPerPage = 10;

    private OrderFloatingWindow floatingWindowManager;

    private boolean requestDictsFinish = true;
    private boolean requestDataListFinish = true;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_order_container_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, getIntent().getStringExtra("menuName"), 0);

        mInflater = LayoutInflater.from(this);
        mAdapter = new Adapter();
        mLister.setAdapter(mAdapter);

        footView = mInflater.inflate(R.layout.view_text, null).findViewById(R.id.text);
        mLister.addFooterView(footView);

        mRefresher.setOnRefreshListener(refreshListener);
        mFloatingBtn.setOnClickListener(this);

        refresh();
    }

    /**
     * 下拉刷新
     */
    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            refresh();
        }
    };

    private void refresh() {
        if (dictsMap == null) {
            dictsMap = new LinkedHashMap<>();
        } else {
            dictsMap.clear();
        }

        if (ordersList == null) {
            ordersList = new ArrayList<>();
        }

        getDicts();
        getDataList(false);
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        getDataList(true);
    }

    private void refreshFinished() {
        if (isFinishing()) {
            return;
        }
        if (requestDictsFinish && requestDataListFinish) {
            mRefresher.finishRefresh(true);
            dismissDialog();
            mAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(ordersList != null && ordersList.size() > 0 ? View.GONE : View.VISIBLE);
            if (ordersList == null || ordersList.size() == 0) {
                footView.setText("");
            }
        }
    }

    private void getDicts() {

        if (!requestDictsFinish || isFinishing()) {
            return;
        }

        WebRequest.getInstance().getDicts("ReceiptStatus", new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                requestDictsFinish = false;
                createDialog(false);
            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(new Throwable(getString(R.string.operation_failed)));
                    return;
                }
                if (!o.getBoolean("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }

                parseDicts(o);
            }

            @Override
            public void onError(Throwable e) {
                requestDictsFinish = true;
                refreshFinished();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void parseDicts(JSONObject rootJson) {

        if (requestDictsFinish || isFinishing()) {
            return;
        }

        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("ReceiptStatus");
        if (dataArray == null) {

        }else {
            for (int i = 0; i < dataArray.size(); i++) {
                Dicts dict = JSON.parseObject(dataArray.get(i).toString(), Dicts.class);
                dictsMap.put(dict.getValue(), dict);
            }
        }

        requestDictsFinish = true;
        Log.e(TAG, "parseDicts: " + dictsMap);
        refreshFinished();
    }

    /**
     * 加载数据
     *
     * @param add 获取后是更新还是添加
     */
    private void getDataList(final boolean add) {

        if (!requestDataListFinish) {
            return;
        }

        WebRequest.getInstance().getDispatchList(
                add ? pageNum + 1 : 1,
                pageSize == 0 ? itemPerPage : pageSize,
                billNo,
                forwarderName,
                consigneeCName,
                delivTimeStart,
                delivTimeEnd,
                flowStatus,
                oriBack,
                new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        requestDataListFinish = false;
                        createDialog(false);
                        Log.e(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(JSONObject o) {
                        Log.e(TAG, "onNext: "+o);
                        if (o == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }
                        if (!o.getBoolean("success")) {
                            onError(new Throwable(o.getString("failReason")));
                            return;
                        }
                        parseDataList(o, add);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: "+e);
                        requestDataListFinish = true;
                        refreshFinished();
                        MyToast.show(OrderContainerActivity.this,
                                e == null ? getString(R.string.operation_failed) : e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");
                    }
                }
        );
    }

    /**
     * 解析数据
     *
     * @param add 获取后是更新还是添加
     */
    private void parseDataList(JSONObject rootJson, boolean add) {
        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("list");

        pageTotal = rootJson.getJSONObject("data").getInteger("pages");
        pageNum = rootJson.getJSONObject("data").getInteger("pageNum");
        pageSize = rootJson.getJSONObject("data").getInteger("size");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");

        if (add) {
            ordersList.addAll(JSON.parseArray(dataArray.toJSONString(), DispatchOrder.class));
        } else {
            ordersList = (ArrayList<DispatchOrder>) JSON.parseArray(dataArray.toJSONString(), DispatchOrder.class);
        }

        for (int i = 0; i < ordersList.size(); i++) {
            if (operatingOrder != null && operatingOrder.getId().equals(ordersList.get(i).getId())) {
                ordersList.get(i).setSpread(operatingOrder.isSpread());
                break;
            }
        }

        Log.e(TAG, "parseDataList: " + ordersList);
        requestDataListFinish = true;

        refreshFinished();
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                onBackPressed();
                break;
            case R.id.floating_bar:
                showCheckAction();
                break;
        }
    }

    /**
     * 打开检索信息悬浮窗
     */
    private void showCheckAction() {
        if (floatingWindowManager == null) {
            floatingWindowManager = new OrderFloatingWindow(this, mFloatingWindow);
            floatingWindowManager.setDictsList(new ArrayList<>(dictsMap.values()));
            floatingWindowManager.setButtonClickListener(new OrderFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindowManager.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String no, String delegate, String buyer, String dateStart, String dateEnd, String status) {
                    //TODO get data list within limits
                    Log.e(TAG, "onCheckClick: no = " + no + ";delegate=" + delegate + ";buyer=" + buyer
                            + ";dateStart=" + dateStart + ";dateEnd=" + dateEnd + ";status=" + status);
                    billNo = no;
                    forwarderName = delegate;
                    consigneeCName = buyer;
                    delivTimeStart = dateStart;
                    delivTimeEnd = dateEnd;
                    flowStatus = status;
                    oriBack = "";

                    getDataList(false);
                }

                @Override
                public void onResetClick() {
                    //TODO get data list without limits

                    billNo = "";
                    forwarderName = "";
                    consigneeCName = "";
                    delivTimeStart = "";
                    delivTimeEnd = "";
                    flowStatus = "";
                    oriBack = "";

                    getDataList(false);
                }
            });
        }
        mFloatingBtn.setVisibility(View.GONE);
        floatingWindowManager.show();
    }

    /**
     * 点击展开项中的功能按钮操作
     */
    private void onOperationClick(View view, final int position) {

        operatingOrder = ordersList.get(position);

        final long millis = Calendar.getInstance().getTimeInMillis();

        switch (view.getId()) {
            case R.id.order_get_send://预约提箱发送
                Log.e(TAG, "onOperationClick check : " + operatingOrder);
//                MyToast.show(OrderContainerActivity.this, "预约提箱");
                WebRequest.getInstance().containerGetAppoint(","+operatingOrder.getId(), millis, new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        createDialog(false);
                    }

                    @Override
                    public void onNext(JSONObject object) {
                        Log.e(TAG, "appoint onNext: "+object);
                        if (object == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }
                        if (object.getBooleanValue("success")) {
                            ordersList.get(position).setTransTime(millis);
                            ordersList.get(position).setTransStatus("2");
                            mAdapter.notifyDataSetChanged();
                            MyToast.show(OrderContainerActivity.this, getString(R.string.operation_success));
                        }else {
                            onError(new Throwable(TextUtils.isEmpty(object.getString("failReason")) ?
                            getString(R.string.operation_failed) : object.getString("failReason")));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "appoint get onNext: "+e);
                        MyToast.show(OrderContainerActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                            getString(R.string.operation_failed) : e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissDialog();
                    }
                });
                break;
            case R.id.order_return_send://预约还箱发送
                Log.e(TAG, "onOperationClick receive : " + operatingOrder);
//                MyToast.show(OrderContainerActivity.this, "预约还箱");

                WebRequest.getInstance().containerRtnAppoint(","+operatingOrder.getId(), millis, new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        createDialog(false);
                    }

                    @Override
                    public void onNext(JSONObject object) {
                        Log.e(TAG, "appoint rtn onNext: "+object);
                        if (object == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }
                        if (object.getBooleanValue("success")) {
                            ordersList.get(position).setTransTimeRtn(millis);
                            ordersList.get(position).setTransStatusRtn("2");
                            mAdapter.notifyDataSetChanged();
                            MyToast.show(OrderContainerActivity.this, getString(R.string.operation_success));
                        }else {
                            onError(new Throwable(TextUtils.isEmpty(object.getString("failReason")) ?
                                    getString(R.string.operation_failed) : object.getString("failReason")));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "appoint onNext: "+e);
                        MyToast.show(OrderContainerActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                                getString(R.string.operation_failed) : e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissDialog();
                    }
                });
                break;
        }
    }

    private void onItemClick(int position) {
        DispatchOrder order = ordersList.get(position);
        order.setSpread(!order.isSpread());
        operatingOrder = order;
        ordersList.set(position, order);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (floatingWindowManager != null && floatingWindowManager.isShowing()) {
            floatingWindowManager.dismiss();
            mFloatingBtn.setVisibility(View.VISIBLE);
            return;
        }
        finish();
    }

    protected class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ordersList == null ? 0 : ordersList.size();
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
                convertView = mInflater.inflate(R.layout.item_container_order_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= ordersList.size() - 1) {
                if (ordersList.size() < itemTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                } else {
                    footView.setText(R.string.foot_no_more);
                }
            }

            DispatchOrder order = ordersList.get(position);

            setTitle(holder, position);
            //箱信息
            setContainer(holder.container, order);
            setStatus(holder.status, order);
            setOperation(holder, position);

            if (!order.isSpread()) {
                holder.arrow.setRotation(0);
                holder.child.setVisibility(View.GONE);
                return convertView;
            }

            holder.arrow.setRotation(180);
            holder.child.setVisibility(View.VISIBLE);

            setDate(holder.date, order);
            holder.buyer.setText(order.getBuyerCN());
            holder.delegate.setText(order.getForwarder());
            holder.addr.setText(order.getAddress());
            //提箱
            setGetInfo(holder.deliGet, order);
            //还箱
            setRtnInfo(holder.deliRtn, order);
//            if (TextUtils.isEmpty(order.getOriBack())) {
//                holder.originRtn.setText("");
//            } else {
//                holder.originRtn.setText(order.getOriBack().equals("1") ? R.string.yes : R.string.no);
//            }
            setOrderGetStatus(holder.statusGet, order);
            setOrderRtnStatus(holder.statusRtn, order);
            return convertView;
        }

        private void setTitle(ViewHolder holder, final int position) {
            holder.title.setText(getString(R.string.order_title, ordersList.get(position).getBillNo()));
            holder.top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }

        private void setDate(TextView view, DispatchOrder order) {
            String date = "";

            if (order.getDelivTime() == null) {

            } else {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(order.getDelivTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            view.setText(date);
        }

        /**
         * 设置箱信息
         * */
        private void setContainer(TextView view, DispatchOrder order) {
            StringBuilder builder = new StringBuilder();

            builder.append(getString(R.string.dispatch_container_detail))
                    .append(": ")
                    .append(TextUtils.isEmpty(order.getContainerNo()) ? "" : order.getContainerNo())
                    .append(TextUtils.isEmpty(order.getContainerSize()) || TextUtils.isEmpty(builder) ? "" : "  ")
                    .append(TextUtils.isEmpty(order.getContainerSize()) ? "" : order.getContainerSize())
                    .append(TextUtils.isEmpty(order.getContainerType()) ? "" : order.getContainerType());

            view.setText(builder);
        }

        /**
         * 设置提箱派车信息
         * */
        private void setGetInfo(TextView view, DispatchOrder order) {
            StringBuilder builder = new StringBuilder();
            builder.append(TextUtils.isEmpty(order.getDriver()) ? "" : order.getDriver())
                    .append(TextUtils.isEmpty(order.getTruckNo()) || TextUtils.isEmpty(builder) ? "" : "\n")
                    .append(TextUtils.isEmpty(order.getTruckNo()) ? "" : order.getTruckNo())
                    .append(TextUtils.isEmpty(order.getDelivPlace()) || TextUtils.isEmpty(builder) ? "" : "\n")
                    .append(TextUtils.isEmpty(order.getDelivPlace()) ? "" : order.getDelivPlace());

            view.setText(builder);
        }

        /**
         * 设置还箱派车信息
         * */
        private void setRtnInfo(TextView view, DispatchOrder order) {

            StringBuilder builder = new StringBuilder();
            builder.append(TextUtils.isEmpty(order.getRtnDriver()) ? "" : order.getRtnDriver())
                    .append(TextUtils.isEmpty(order.getRtnTruckNo()) || TextUtils.isEmpty(builder) ? "" : "\n")
                    .append(TextUtils.isEmpty(order.getRtnTruckNo()) ? "" : order.getRtnTruckNo())
                    .append(TextUtils.isEmpty(order.getRtnPlace()) || TextUtils.isEmpty(builder) ? "" : "\n")
                    .append(TextUtils.isEmpty(order.getRtnPlace()) ? "" : order.getRtnPlace());
            view.setText(builder);
        }

        /**
         * 根据状态接口返回的状态值和名称，设置当前订单状态
         */
        private void setStatus(TextView view, DispatchOrder order) {
            if (dictsMap == null) {
                view.setText("");
                return;
            }
            view.setText(dictsMap.containsKey(order.getFoStatus()) ? dictsMap.get(order.getFoStatus()).getLabel() : "");
        }

        //提箱预约发送状态
        private void setOrderGetStatus(TextView view, DispatchOrder order) {
            if (TextUtils.isEmpty(order.getTransStatus()) || order.getTransTime() == null) {
                view.setText("");
                return;
            }
            String status = "";
            try {
                status = getString(order.getTransStatus().equals("2") ? R.string.order_send_success : R.string.order_send_fail)
                        + "  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(order.getTransTime()));
            }catch (Exception e) {
                e.printStackTrace();
            }

            view.setText(status);
        }

        //还箱预约发送状态
        private void setOrderRtnStatus(TextView view, DispatchOrder order) {
            if (TextUtils.isEmpty(order.getTransStatusRtn()) || order.getTransTimeRtn() == null) {
                view.setText("");
                return;
            }
            String status = "";
            try {
                status = getString(order.getTransStatusRtn().equals("2") ? R.string.order_send_success : R.string.order_send_fail)
                        + "  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(order.getTransTimeRtn()));
            }catch (Exception e) {
                e.printStackTrace();
            }

            view.setText(status);
        }

        /**
         * 根据订单状态，设置哪些操作功能按钮可以显示
         */
        private void setOperation(ViewHolder holder, final int position) {

            DispatchOrder order = ordersList.get(position);

            boolean get = false;
            boolean rtn = false;

            if (TextUtils.isEmpty(order.getFoStatus())) {

            } else if (order.getFoStatus().equals(Dicts.STATUS_5400)) {//待派发
                get = true;
                rtn = true;
            } else if (order.getFoStatus().equals(Dicts.STATUS_5500)) {//已派发待接单
            } else if (order.getFoStatus().equals(Dicts.STATUS_5510)) {//车队拒绝接单

            } else if (order.getFoStatus().equals(Dicts.STATUS_5520)) {//已接单待派车
            } else if (order.getFoStatus().equals(Dicts.STATUS_5525)) {//货代取消派发

            } else if (order.getFoStatus().equals(Dicts.STATUS_5530)) {//车队撤销接单

            } else if (order.getFoStatus().equals(Dicts.STATUS_5550)) {//派车中
                get = true;
                rtn = true;
            } else if (order.getFoStatus().equals(Dicts.STATUS_5600)) {//已派车
                get = true;
                rtn = true;
            } else if (order.getFoStatus().equals(Dicts.STATUS_5650)) {//提箱中
//                rtn = true;
            } else if (order.getFoStatus().equals(Dicts.STATUS_5700)) {//已提箱
//                rtn = true;
            } else if (order.getFoStatus().equals(Dicts.STATUS_5750)) {//还箱中

            } else if (order.getFoStatus().equals(Dicts.STATUS_5800)) {//已还箱

            } else {

            }

            holder.changeGet.setVisibility(get ? View.VISIBLE : View.GONE);
            holder.changeRtn.setVisibility(rtn ? View.VISIBLE : View.GONE);

            if (get) {
                holder.changeGet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, position);
                    }
                });
            }

            if (rtn) {
                holder.changeRtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, position);
                    }
                });
            }
        }

        public class ViewHolder {
            @BindView(R.id.order_top)
            protected View top;
            @BindView(R.id.order_main_name)
            protected TextView title;
            @BindView(R.id.order_main_arrow)
            protected ImageView arrow;
            @BindView(R.id.order_main_child)
            protected View child;
            @BindView(R.id.deliver_date)
            protected TextView date;
            @BindView(R.id.order_buyer)
            protected TextView buyer;
            @BindView(R.id.delegate)
            protected TextView delegate;
            @BindView(R.id.address)
            protected TextView addr;
            @BindView(R.id.container)
            protected TextView container;
            @BindView(R.id.deliver_get)
            protected TextView deliGet;
            @BindView(R.id.deliver_return)
            protected TextView deliRtn;
            @BindView(R.id.dispatch_status)
            protected TextView status;
            @BindView(R.id.dispatch_status_get)
            protected TextView statusGet;
            @BindView(R.id.dispatch_status_rtn)
            protected TextView statusRtn;
//            @BindView(R.id.dispatch_return_origin)
//            protected TextView originRtn;
            @BindView(R.id.order_get_send)
            protected TextView changeGet;
            @BindView(R.id.order_return_send)
            protected TextView changeRtn;
        }
    }
}
