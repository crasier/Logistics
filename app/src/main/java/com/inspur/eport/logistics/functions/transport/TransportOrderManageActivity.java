package com.inspur.eport.logistics.functions.transport;

import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.ETC1;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
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
import com.inspur.eport.logistics.bean.Order;
import com.inspur.eport.logistics.functions.dispatch.TransportOrderDispatchActivity;
import com.inspur.eport.logistics.server.WebRequest;
import com.inspur.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 运输委托单管理
 * */
public class TransportOrderManageActivity extends BaseActivity {

    public static final String TAG = "TransportOrderManage";

    Unbinder unbinder;
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
    private ArrayList<Order> ordersList;

    private int pageSize;//每页数据条数
    private int pageNum;//当前所在页
    private int pageTotal;//总页数
    private int itemTotal;//总条数

    private String billNo = "";
    private String forwarderName= "";
    private String consigneeCName= "";
    private String delivTimeStart= "";
    private String delivTimeEnd= "";
    private String flowStatus= "";

    private Order operatingOrder;

    private final int itemPerPage = 10;

    private TransportFloatingWindow floatingWindowManager;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_transport_order_activity);
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
     * */
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
        }else {
            dictsMap.clear();
        }

        if (ordersList == null) {
            ordersList = new ArrayList<>();
        }else {
            ordersList.clear();
        }

        requestDictsFinish = false;
        requestDataListFinish = false;

        getDicts();
        getDataList(false);
    }

    /**
     * 加载更多
     * */
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

        WebRequest.getInstance().getDicts("ForwardStatus", new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                requestDictsFinish = false;
                createDialog(false);
            }

            @Override
            public void onNext(JSONObject object) {
                if (object == null || !object.getBooleanValue("success")) {
                    onError(new Throwable(getString(R.string.operation_failed)));
                    return;
                }
                parseDicts(object);
            }

            @Override
            public void onError(Throwable e) {
                requestDictsFinish = true;
                refreshFinished();
                MyToast.show(TransportOrderManageActivity.this,
                        e == null ? getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void parseDicts(JSONObject rootJson) {

        if (requestDictsFinish) {
            return;
        }

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
     * @param add 获取后是更新还是添加
     * */
    private void getDataList(final boolean add) {

        WebRequest.getInstance().getOrderList(
                add ? pageNum + 1 : 1,
                pageSize == 0 ? itemPerPage : pageSize,
                billNo,
                forwarderName,
                consigneeCName,
                delivTimeStart,
                delivTimeEnd,
                flowStatus,
                new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        requestDataListFinish = false;
                        createDialog(false);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {

                        Log.e(TAG, "onNext:getOrderList= "+jsonObject);
                        if (jsonObject == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }

                        if (!jsonObject.getBooleanValue("success")) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }

                        parseDataList(jsonObject, add);
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestDataListFinish = true;
                        refreshFinished();
                    }

                    @Override
                    public void onComplete() {

                    }
                }
        );
    }

    /**
     * 解析数据
     * @param add 获取后是更新还是添加
     * */
    private void parseDataList(JSONObject rootJson, boolean add) {

        if (requestDataListFinish) {
            return;
        }

        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("list");
        pageTotal = rootJson.getJSONObject("data").getInteger("pages");
        pageNum = rootJson.getJSONObject("data").getInteger("pageNum");
        pageSize = rootJson.getJSONObject("data").getInteger("size");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");

        if (add) {
            ordersList.addAll(JSON.parseArray(dataArray.toJSONString(), Order.class));
        }else {
            ordersList = (ArrayList<Order>) JSON.parseArray(dataArray.toJSONString(), Order.class);
        }

        for (int i = 0; i < ordersList.size(); i++) {
            if (operatingOrder != null && operatingOrder.getId().equals(ordersList.get(i).getId())) {
                ordersList.get(i).setSpread(operatingOrder.isSpread());
                break;
            }
        }

        Log.e(TAG, "parseDataList: "+ordersList);
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
     * */
    private void showCheckAction() {
        if (floatingWindowManager == null) {
            floatingWindowManager = new TransportFloatingWindow(this, mFloatingWindow);
            floatingWindowManager.setDictsList(new ArrayList<>(dictsMap.values()));
            floatingWindowManager.setButtonClickListener(new TransportFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindowManager.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String no, String delegate, String buyer, String dateStart, String dateEnd, String status) {
                    Log.e(TAG, "onCheckClick: no = "+no+";delegate="+delegate+";buyer="+buyer+";dateStart="+dateStart+";dateEnd="+dateEnd+";status="+status);
                    billNo = no;
                    forwarderName = delegate;
                    consigneeCName = buyer;
                    delivTimeStart = dateStart;
                    delivTimeEnd = dateEnd;
                    flowStatus = status;

                    getDataList(false);
                }

                @Override
                public void onResetClick() {
                    billNo = "";
                    forwarderName = "";
                    consigneeCName = "";
                    delivTimeStart = "";
                    delivTimeEnd = "";
                    flowStatus = "";

                    getDataList(false);
                }
            });
        }
        mFloatingBtn.setVisibility(View.GONE);
        floatingWindowManager.show();
    }

    /**
     * 点击展开项中的功能按钮操作
     * */
    private void onOperationClick(View view, Order order, int position) {

        operatingOrder = order;

        switch (view.getId()) {
            case R.id.order_check:
                Log.e(TAG, "onOperationClick check : "+order);
                Intent detailIntent = new Intent(TransportOrderManageActivity.this, TransportOrderDetailActivity.class);
                detailIntent.putExtra("order", operatingOrder);
                startActivity(detailIntent);
                break;
            case R.id.order_receive:
                Log.e(TAG, "onOperationClick receive : "+order);
                MyToast.show(TransportOrderManageActivity.this, "接单请求");
                modifyOrderStatus(order, "5520", position);
                break;
            case R.id.order_refuse:
                Log.e(TAG, "onOperationClick refuse : "+order);
                MyToast.show(TransportOrderManageActivity.this, "拒绝接单");
                modifyOrderStatus(order, "5510", position);
                break;
            case R.id.order_cancel:
                Log.e(TAG, "onOperationClick cancel : "+order);
                MyToast.show(TransportOrderManageActivity.this, "取消委托单");
                modifyOrderStatus(order, "5530", position);
                break;
            case R.id.order_dispatch:
                Log.e(TAG, "onOperationClick dispatch : "+order);
//                MyToast.show(TransportOrderManageActivity.this, "请求派车");
                Intent dispatchIntent = new Intent(TransportOrderManageActivity.this, TransportOrderDispatchActivity.class);
                dispatchIntent.putExtra("fkForwardingId", order.getId());
                dispatchIntent.putExtra("menuName", getString(R.string.dispatch_title));
                startActivity(dispatchIntent);
                break;
        }
    }

    private void modifyOrderStatus(Order order, final String status, final int position) {
        WebRequest.getInstance().modifyOrderStatus(order.getId(), status, new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(false);
            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(new Throwable(getString(R.string.operation_failed)));
                    return;
                }

                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }

                dismissDialog();
                MyToast.show(TransportOrderManageActivity.this, R.string.operation_success);
                ordersList.get(position).setStatus(status);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                dismissDialog();
                MyToast.show(TransportOrderManageActivity.this,
                        e == null ? getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void onItemClick(int position) {
        Order order = ordersList.get(position);
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
                convertView = mInflater.inflate(R.layout.item_transport_order_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= ordersList.size() - 1) {
                if (ordersList.size() < itemTotal && pageNum < pageTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                }else {
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

            holder.buyer.setText(ordersList.get(position).getBuyerCN());
            holder.delegate.setText(ordersList.get(position).getDelegate());
            holder.addr.setText(ordersList.get(position).getAddress());
            setContainer(holder.container, ordersList.get(position));
            setStatus(holder.status, ordersList.get(position));
            setOperation(holder, ordersList.get(position), position);
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

        /**
         * 设置箱型箱量
         * */
        private void setContainer(TextView view, Order order) {
            if (order.getContainers() == null) {
                view.setText("");
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (Order.Container container: order.getContainers()) {
                stringBuilder.append(String.format(Locale.CHINA,
                        "%s * %s %s ,",
                        container.getTotal(), container.getSize(), container.getType()));
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            view.setText(stringBuilder.toString());
        }

        /**
         * 根据状态接口返回的状态值和名称，设置当前订单状态
         * */
        private void setStatus(TextView view, Order order) {
            if (dictsMap == null) {
                view.setText("");
                return;
            }
            view.setText(dictsMap.containsKey(order.getStatus()) ? dictsMap.get(order.getStatus()).getLabel() : "");
        }

        /**
         * 根据订单状态，设置哪些操作功能按钮可以显示
         * */
        private void setOperation(ViewHolder holder, final Order order, final int position) {

            boolean check = false;
            boolean receive = false;
            boolean refuse = false;
            boolean cancel = false;
            boolean dispatch = false;

            if (order.getStatus().equals(Dicts.STATUS_5500)) {//已派发待接单
                check = true;
                receive = true;
                refuse = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5510)) {//车队拒绝接单

            }else if (order.getStatus().equals(Dicts.STATUS_5520)) {//已接单待派车
                check = true;
                cancel = true;
                dispatch = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5525)) {//货代取消派发

            }else if (order.getStatus().equals(Dicts.STATUS_5530)) {//车队撤销接单

            }else if (order.getStatus().equals(Dicts.STATUS_5550)) {//派车中
                check = true;
                dispatch = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5600)) {//已派车
                check = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5650)) {//提箱中
                check = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5700)) {//已提箱
                check = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5750)) {//还箱中
                check = true;
            }else if (order.getStatus().equals(Dicts.STATUS_5800)) {//已还箱
                check = true;
            }else {

            }

            holder.check.setVisibility(check ? View.VISIBLE : View.GONE);
            holder.receive.setVisibility(receive ? View.VISIBLE : View.GONE);
            holder.refuse.setVisibility(refuse ? View.VISIBLE : View.GONE);
            holder.cancel.setVisibility(cancel ? View.VISIBLE : View.GONE);
            holder.dispatch.setVisibility(dispatch ? View.VISIBLE : View.GONE);

            if (check) {
                holder.check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order, position);
                    }
                });
            }

            if (receive) {
                holder.receive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order, position);
                    }
                });
            }

            if (refuse) {
                holder.refuse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order, position);
                    }
                });
            }

            if (cancel) {
                holder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order, position);
                    }
                });
            }

            if (dispatch) {
                holder.dispatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, order, position);
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
            @BindView(R.id.buyer)
            protected TextView buyer;
            @BindView(R.id.delegate)
            protected TextView delegate;
            @BindView(R.id.address)
            protected TextView addr;
            @BindView(R.id.container)
            protected TextView container;
            @BindView(R.id.status)
            protected TextView status;
            @BindView(R.id.order_check)
            protected TextView check;
            @BindView(R.id.order_receive)
            protected TextView receive;
            @BindView(R.id.order_refuse)
            protected TextView refuse;
            @BindView(R.id.order_cancel)
            protected TextView cancel;
            @BindView(R.id.order_dispatch)
            protected TextView dispatch;
        }
    }
}
