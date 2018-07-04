package com.inspur.eport.logistics.functions.order;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
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
import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Dicts;
import com.inspur.eport.logistics.bean.DispatchOrder;
import com.inspur.eport.logistics.functions.dispatch.DispatchFloatingWindowManager;
import com.inspur.eport.logistics.functions.dispatch.DispatchOrderManageActivity;
import com.inspur.eport.logistics.server.TestData;
import com.inspur.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

    private int itemTotal;//总条数
    private int pageTotal;//总页数
    private int pageCurrent;//当前所在页

    private DispatchOrder operatingOrder;

    private final int itemPerPage = 10;

    private OrderFloatingWindow floatingWindowManager;


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
        mLister.setOnScrollListener(onScrollListener);

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

    private ListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };

    private void refresh() {
        createDialog(false);
        if (dictsMap == null) {
            dictsMap = new LinkedHashMap<>();
        } else {
            dictsMap.clear();
        }

        if (ordersList == null) {
            ordersList = new ArrayList<>();
        } else {
            ordersList.clear();
        }

        requestDictsFinish = false;
        requestDataListFinish = false;

        getDicts();
        getDataList(false);
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        if (!requestDataListFinish) {
            return;
        }
        requestDataListFinish = false;
        getDataList(true);
    }

    private boolean requestDictsFinish = false;
    private boolean requestDataListFinish = false;

    private void refreshFinished() {
        if (isFinishing()) {
            return;
        }
        if (requestDictsFinish && requestDataListFinish) {
            mRefresher.finishRefresh(true);
            dismissDialog();
            mAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(ordersList != null && ordersList.size() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    private void getDicts() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseDicts(TestData.getdicts);
            }
        }, 1000);
    }

    private void parseDicts(String data) {
        JSONObject rootJson = JSON.parseObject(data);
        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("ForwardStatus");
        for (int i = 0; i < dataArray.size(); i++) {
            Dicts dict = JSON.parseObject(dataArray.get(i).toString(), Dicts.class);
            dictsMap.put(dict.getValue(), dict);
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseDataList(TestData.orderDataList, add);
            }
        }, new Random().nextInt(3) * 1000);
    }

    /**
     * 解析数据
     *
     * @param add 获取后是更新还是添加
     */
    private void parseDataList(String data, boolean add) {
        JSONObject rootJson = JSON.parseObject(data);
        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("list");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");
        pageTotal = rootJson.getJSONObject("data").getInteger("pages");
        pageCurrent = rootJson.getJSONObject("data").getInteger("pageNum");

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
                }

                @Override
                public void onResetClick() {
                    //TODO get data list without limits
                }
            });
        }
        mFloatingBtn.setVisibility(View.GONE);
        floatingWindowManager.show();
    }

    /**
     * 点击展开项中的功能按钮操作
     */
    private void onOperationClick(View view, DispatchOrder order) {

        operatingOrder = order;

        switch (view.getId()) {
            case R.id.order_get_change:
                Log.e(TAG, "onOperationClick check : " + order);
                MyToast.show(OrderContainerActivity.this, "提箱改派");
                break;
            case R.id.order_return_change:
                Log.e(TAG, "onOperationClick receive : " + order);
                MyToast.show(OrderContainerActivity.this, "还箱改派");
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

            setTitle(holder, position);

            if (!ordersList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.child.setVisibility(View.GONE);
                return convertView;
            }


            holder.arrow.setRotation(180);
            holder.child.setVisibility(View.VISIBLE);

            DispatchOrder order = ordersList.get(position);

            setDate(holder.date, order);
            holder.buyer.setText(order.getBuyerCN());
            holder.delegate.setText(order.getForwarder());
            holder.addr.setText(order.getAddress());
            //箱信息
            setContainer(holder.container, order);
            //提箱
            setGetInfo(holder.deliGet, order);
            //还箱
            setRtnInfo(holder.deliRtn, order);
//            if (TextUtils.isEmpty(order.getOriBack())) {
//                holder.originRtn.setText("");
//            } else {
//                holder.originRtn.setText(order.getOriBack().equals("1") ? R.string.yes : R.string.no);
//            }
            setStatus(holder.status, order);
            setOrderGetStatus(holder.statusGet, order);
            setOrderRtnStatus(holder.statusRtn, order);
            setOperation(holder, order);
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

            if (TextUtils.isEmpty(order.getDelivTime())) {

            } else {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(Long.parseLong(order.getDelivTime())));
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

            builder.append(TextUtils.isEmpty(order.getContainerNo()) ? "" : order.getContainerNo())
                    .append(TextUtils.isEmpty(order.getContainerSize()) || TextUtils.isEmpty(builder) ? "" : "\n")
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
            view.setText(dictsMap.containsKey(order.getStatus()) ? dictsMap.get(order.getStatus()).getLabel() : "");
        }

        //提箱预约发送状态
        private void setOrderGetStatus(TextView view, DispatchOrder order) {
            if (TextUtils.isEmpty(order.getTransStatus()) || TextUtils.isEmpty(order.getTransTime())) {
                view.setText("");
                return;
            }
            String status = "";
            try {
                status = getString(order.getTransStatus().equals("2") ? R.string.order_send_success : R.string.order_send_fail)
                        + "  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(Long.parseLong(order.getTransTime())));
            }catch (Exception e) {
                e.printStackTrace();
            }

            view.setText(status);
        }

        //还箱预约发送状态
        private void setOrderRtnStatus(TextView view, DispatchOrder order) {
            if (TextUtils.isEmpty(order.getTransStatusRtn()) || TextUtils.isEmpty(order.getTransTimeRtn())) {
                view.setText("");
                return;
            }
            String status = "";
            try {
                status = getString(order.getTransStatusRtn().equals("2") ? R.string.order_send_success : R.string.order_send_fail)
                        + "  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(Long.parseLong(order.getTransTimeRtn())));
            }catch (Exception e) {
                e.printStackTrace();
            }

            view.setText(status);
        }

        /**
         * 根据订单状态，设置哪些操作功能按钮可以显示
         */
        private void setOperation(ViewHolder holder, final DispatchOrder order) {

            boolean get = false;
            boolean rtn = false;

            if (TextUtils.isEmpty(order.getStatus())) {

            } else if (order.getStatus().equals(Dicts.STATUS_5400)) {//待派发
                get = true;
                rtn = true;
            } else if (order.getStatus().equals(Dicts.STATUS_5500)) {//已派发待接单
            } else if (order.getStatus().equals(Dicts.STATUS_5510)) {//车队拒绝接单

            } else if (order.getStatus().equals(Dicts.STATUS_5520)) {//已接单待派车
            } else if (order.getStatus().equals(Dicts.STATUS_5525)) {//货代取消派发

            } else if (order.getStatus().equals(Dicts.STATUS_5530)) {//车队撤销接单

            } else if (order.getStatus().equals(Dicts.STATUS_5550)) {//派车中
                get = true;
                rtn = true;
            } else if (order.getStatus().equals(Dicts.STATUS_5600)) {//已派车
                rtn = true;
            } else if (order.getStatus().equals(Dicts.STATUS_5650)) {//提箱中
                rtn = true;
            } else if (order.getStatus().equals(Dicts.STATUS_5700)) {//已提箱
                rtn = true;
            } else if (order.getStatus().equals(Dicts.STATUS_5750)) {//还箱中

            } else if (order.getStatus().equals(Dicts.STATUS_5800)) {//已还箱

            } else {

            }

            holder.changeGet.setVisibility(get ? View.VISIBLE : View.GONE);
            holder.changeRtn.setVisibility(rtn ? View.VISIBLE : View.GONE);

            if (get) {
                holder.changeGet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order);
                    }
                });
            }

            if (rtn) {
                holder.changeRtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order);
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
