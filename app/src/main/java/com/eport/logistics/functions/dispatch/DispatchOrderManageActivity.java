package com.eport.logistics.functions.dispatch;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import com.eport.logistics.BaseActivity;
import com.eport.logistics.Codes;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 派车单管理
 */
public class DispatchOrderManageActivity extends BaseActivity {
    public static final String TAG = "DispatchOrderManage";

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
    private ArrayList<DispatchOrder> ordersList;

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

    private DispatchOrder operatingOrder;

    private final int itemPerPage = 10;

    private DispatchFloatingWindowManager floatingWindowManager;

    private boolean requestDictsFinish = true;
    private boolean requestDataListFinish = true;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_dispatch_order_activity);
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
            if (ordersList == null || ordersList.size() == 0) {
                mEmpty.setVisibility(View.VISIBLE);
                footView.setText("");
            }else {
                mEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void getDicts() {
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

        if (requestDictsFinish) {
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
                        MyToast.show(DispatchOrderManageActivity.this,
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

        if (requestDataListFinish || isFinishing()) {
            return;
        }

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
            floatingWindowManager = new DispatchFloatingWindowManager(this, mFloatingWindow);
            floatingWindowManager.setDictsList(new ArrayList<>(dictsMap.values()));
            floatingWindowManager.setButtonClickListener(new DispatchFloatingWindowManager.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindowManager.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String no, String delegate, String buyer, String dateStart, String dateEnd, String status, String back) {
                    //TODO get data list within limits
                    Log.e(TAG, "onCheckClick: no = " + no + ";delegate=" + delegate + ";buyer=" + buyer
                            + ";dateStart=" + dateStart + ";dateEnd=" + dateEnd + ";status=" + status+";back="+back);
                    billNo = no;
                    forwarderName = delegate;
                    consigneeCName = buyer;
                    delivTimeStart = dateStart;
                    delivTimeEnd = dateEnd;
                    flowStatus = status;
                    oriBack = back;

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
    private void onOperationClick(View view, DispatchOrder order) {

        operatingOrder = order;

        switch (view.getId()) {
            case R.id.order_get_change:
                Log.e(TAG, "onOperationClick check : " + order);
//                MyToast.show(DispatchOrderManageActivity.this, "提箱改派");
                Intent dispatchIntent = new Intent(this, TransportOrderDispatchActivity.class);
                dispatchIntent.putExtra("fkForwardingId", operatingOrder.getForwardingId());
                dispatchIntent.putExtra("containerNo", operatingOrder.getContainerNo());
                dispatchIntent.putExtra("menuName", getString(R.string.dispatch_redispatch_title));
                startActivityForResult(dispatchIntent, Codes.CODE_REQUEST_DISPATCH);
                break;
            case R.id.order_return_change:
                Log.e(TAG, "onOperationClick receive : " + order);
//                MyToast.show(DispatchOrderManageActivity.this, "还箱改派");
                Intent returnIntent = new Intent(this, TransportOrderReturnActivity.class);
                returnIntent.putExtra("fkForwardingId", operatingOrder.getForwardingId());
                returnIntent.putExtra("containerNo", operatingOrder.getContainerNo());
                returnIntent.putExtra("menuName", getString(R.string.dispatch_return_title));
                startActivityForResult(returnIntent, Codes.CODE_REQUEST_DISPATCH);
                break;
        }
    }

    /**
     * 标题栏点击
     * */
    private void onItemClick(int position) {
        DispatchOrder order = ordersList.get(position);
        order.setSpread(!order.isSpread());
        operatingOrder = order;
        ordersList.set(position, order);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 标题栏长按
     * */
    private void onItemLongClick(int position) {
        try {
            DispatchOrder order = ordersList.get(position);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("billNo", order.getBillNo()));
            MyToast.show(DispatchOrderManageActivity.this, getString(R.string.bill_copy_clipboard)+" "+order.getBillNo());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Codes.CODE_REQUEST_DISPATCH) {
                getDataList(false);
            }
        }
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
                convertView = mInflater.inflate(R.layout.item_dispatch_order_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= ordersList.size() - 1) {
                if (ordersList.size() < itemTotal && pageNum < pageTotal) {
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
            setOperation(holder, order);

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
            if (TextUtils.isEmpty(order.getOriBack())) {
                holder.originRtn.setText("");
            } else {
                holder.originRtn.setText(order.getOriBack().equals("1") ? R.string.yes : R.string.no);
            }

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

            holder.top.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClick(position);
                    return true;
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
            if (TextUtils.isEmpty(order.getStatus())) {
                view.setText("");
            }else if (dictsMap.containsKey(order.getStatus())) {
                view.setText(dictsMap.get(order.getStatus()).getLabel());
            }else {
                switch (order.getStatus()) {
                    case Dicts.STATUS_5200:
                        view.setText(R.string.transport_status_5200);
                        break;
                    case Dicts.STATUS_5300:
                        view.setText(R.string.transport_status_5300);
                        break;
                    case Dicts.STATUS_5400:
                        view.setText(R.string.transport_status_5400);
                        break;
                    case Dicts.STATUS_5500:
                        view.setText(R.string.transport_status_5500);
                        break;
                    case Dicts.STATUS_5510:
                        view.setText(R.string.transport_status_5510);
                        break;
                    case Dicts.STATUS_5520:
                        view.setText(R.string.transport_status_5520);
                        break;
                    case Dicts.STATUS_5525:
                        view.setText(R.string.transport_status_5525);
                        break;
                    case Dicts.STATUS_5530:
                        view.setText(R.string.transport_status_5530);
                        break;
                    case Dicts.STATUS_5550:
                        view.setText(R.string.transport_status_5550);
                        break;
                    case Dicts.STATUS_5600:
                        view.setText(R.string.transport_status_5600);
                        break;
                    case Dicts.STATUS_5700:
                        view.setText(R.string.transport_status_5700);
                        break;
                    case Dicts.STATUS_5750:
                        view.setText(R.string.transport_status_5750);
                        break;
                    case Dicts.STATUS_5800:
                        view.setText(R.string.transport_status_5800);
                        break;
                    case Dicts.STATUS_5900:
                        view.setText(R.string.transport_status_5900);
                        break;
                    default:
                        view.setText("");
                }
            }
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
                get = true;
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
                if (TextUtils.isEmpty(order.getRtnTruckNo()) || TextUtils.isEmpty(order.getRtnDriver())) {
                    holder.changeRtn.setText(R.string.dispatch_title_rtn);
                }else {
                    holder.changeRtn.setText(R.string.dispatch_return_title);
                }
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
            @BindView(R.id.dispatch_return_origin)
            protected TextView originRtn;
            @BindView(R.id.order_get_change)
            protected TextView changeGet;
            @BindView(R.id.order_return_change)
            protected TextView changeRtn;
        }
    }
}
