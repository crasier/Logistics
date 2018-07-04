package com.inspur.eport.logistics.functions.dispatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Dicts;
import com.inspur.eport.logistics.bean.DispatchOrder;
import com.inspur.eport.logistics.bean.DriverSimple;
import com.inspur.eport.logistics.bean.TruckSimple;
import com.inspur.eport.logistics.functions.transport.TransportFloatingWindow;
import com.inspur.eport.logistics.server.TestData;
import com.inspur.eport.logistics.utils.MyToast;
import com.inspur.eport.logistics.utils.ViewHolder;
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

/**
 * 提箱派车、提箱改派
 */
public class TransportOrderDispatchActivity extends BaseActivity {

    public static final String TAG = "TransportOrderDispatch";

    Unbinder unbinder;
    private Adapter mAdapter;
    private LayoutInflater mInflater;
    private DriverAdapter driverAdapter;
    private TruckAdapter truckAdapter;

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

    @BindView(R.id.dispatch_driver)
    protected EditText driverInput;
    @BindView(R.id.dispatch_truck)
    protected EditText truckInput;
    @BindView(R.id.dispatch_driver_spinner)
    protected Spinner driverSpinner;
    @BindView(R.id.dispatch_truck_spinner)
    protected Spinner truckSpinner;


    private TextView footView;

    private LinkedHashMap<String, Dicts> dictsMap;
    private ArrayList<DispatchOrder> ordersList;
    private ArrayList<DriverSimple> driversList;
    private ArrayList<TruckSimple> trucksList;

    private DriverSimple driverTemp;
    private TruckSimple truckTemp;

    private int itemTotal;//总条数
    private int pageTotal;//总页数
    private int pageCurrent;//当前所在页

    private DispatchOrder operatingOrder;

    private final int itemPerPage = 10;

    private TransportFloatingWindow floatingWindow;
    private DispatchDialog dispatchDialog;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_order_dispatch_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, getIntent().getStringExtra("menuName"), R.drawable.icon_send);

        mInflater = LayoutInflater.from(this);
        mAdapter = new Adapter();
        mLister.setAdapter(mAdapter);

        footView = mInflater.inflate(R.layout.view_text, null).findViewById(R.id.text);
        mLister.addFooterView(footView);
        mLister.setOnScrollListener(onScrollListener);

        mRefresher.setOnRefreshListener(refreshListener);
        mFloatingBtn.setOnClickListener(this);

        driverAdapter = new DriverAdapter();
        truckAdapter = new TruckAdapter();

        driverSpinner.setOnItemSelectedListener(onItemSelectedListener);
        truckSpinner.setOnItemSelectedListener(onItemSelectedListener);

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

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            switch (parent.getId()) {
                case R.id.dispatch_driver_spinner:
                    driverTemp = driversList.get(position);
                    driverInput.setText(driversList.get(position).getName());
                    if (!TextUtils.isEmpty(driversList.get(position).getTruck())) {
                        truckInput.setText(driversList.get(position).getTruck());
                    }
                    break;
                case R.id.dispatch_truck_spinner:
                    truckTemp = trucksList.get(position);
                    truckInput.setText(trucksList.get(position).getTruck());
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

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
        getDrivers();
        getTrucks();
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

    private void getDrivers() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseDrivers(TestData.driverList);
            }
        }, new Random().nextInt(3) * 1000);
    }

    private void parseDrivers(String data) {
        driversList = (ArrayList<DriverSimple>) JSON.parseArray(data, DriverSimple.class);
        driverSpinner.setAdapter(driverAdapter);
    }

    private void getTrucks() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseTrucks(TestData.truckList);
            }
        }, new Random().nextInt(3) * 1000);
    }

    private void parseTrucks(String data) {
        trucksList = (ArrayList<TruckSimple>) JSON.parseArray(data, TruckSimple.class);
        truckSpinner.setAdapter(truckAdapter);
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
                parseDataList(TestData.dispatchList, add);
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

            if (ordersList.get(i).isCanDispatch()) {
                ordersList.get(i).setSpread(true);
            }

            if (operatingOrder != null && operatingOrder.getId().equals(ordersList.get(i).getId())) {
                ordersList.get(i).setSpread(operatingOrder.isSpread());
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
            case R.id.base_top_right://确定派车订单
                preDispatch();
                break;
        }
    }

    /**
     * 确认选择了派车单，且该填写的内容都填写完毕
     */
    private void preDispatch() {
        ArrayList<DispatchOrder> orders = new ArrayList<>();
        for (DispatchOrder order : ordersList) {
            if (order.isSelected()) {
                if (TextUtils.isEmpty(order.getAppointTimeGet())) {
                    MyToast.show(this, getString(R.string.dispatch_appoint_get_empty, order.getContainerNo()));
                    return;
                }

                if (TextUtils.isEmpty(order.getAppointTimeRtn())) {
                    MyToast.show(this, getString(R.string.dispatch_appoint_rtn_empty, order.getContainerNo()));
                    return;
                }

                if (TextUtils.isEmpty(order.getDelivTime())) {
                    MyToast.show(this, getString(R.string.dispatch_deliver_time_empty, order.getContainerNo()));
                    return;
                }
                orders.add(order);
            }
        }

        if (orders.isEmpty()) {
            MyToast.show(this, R.string.dispatch_list_empty);
            return;
        }

        String driver = driverInput.getText().toString();
        String truck = truckInput.getText().toString();

        if (TextUtils.isEmpty(driver)) {
            MyToast.show(this, R.string.dispatch_driver_empty);
            return;
        }

        if (TextUtils.isEmpty(truck)) {
            MyToast.show(this, R.string.dispatch_truck_empty);
            return;
        }

        dispatch(orders);
    }

    /**
     * 弹出派车单确认框，选择司机和货车，点击确认派车
     */
    private void dispatch(final ArrayList<DispatchOrder> orders) {
        if (dispatchDialog == null) {
            dispatchDialog = new DispatchDialog(this, R.style.MyDialogStyle);
        }
        dispatchDialog.setOrders(orders)
                .setOnSureClickListener(new DispatchDialog.onSureClickListener() {
                    @Override
                    public void onSureClick() {
                        //TODO dispatch orders
                        Log.e(TAG, "onSureClick: orders = " + orders);
                    }
                })
                .setDriver(driverTemp == null ? "" : driverTemp.getName())
                .setTruck(truckTemp == null ? "" : truckTemp.getTruck())
                .setOnCancelClickListener(new DispatchDialog.onCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        dispatchDialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 打开检索信息悬浮窗
     */
    private void showCheckAction() {
        if (floatingWindow == null) {
            floatingWindow = new TransportFloatingWindow(this, mFloatingWindow);
            floatingWindow.setDictsList(new ArrayList<>(dictsMap.values()));
            floatingWindow.setButtonClickListener(new TransportFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindow.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String no, String delegate, String buyer, String dateStart, String dateEnd, String status) {
                    //TODO get data list within limits
                    Log.e(TAG, "onCheckClick: no = " + no + ";delegate=" + delegate + ";buyer=" + buyer + ";dateStart=" + dateStart + ";dateEnd=" + dateEnd + ";status=" + status);
                }

                @Override
                public void onResetClick() {
                    //TODO get data list without limits
                }
            });
        }
        mFloatingBtn.setVisibility(View.GONE);
        floatingWindow.show();
    }

    private void onItemClick(int position) {
        DispatchOrder order = ordersList.get(position);
        order.setSpread(!order.isSpread());
        operatingOrder = order;
        ordersList.set(position, order);
        mAdapter.notifyDataSetChanged();
    }

    private void onCheckedChangeListener(boolean isChecked, int position) {
        Log.e(TAG, "onCheckedChangeListener: " + isChecked + "; position = " + position);
        ordersList.get(position).setSelected(isChecked);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (floatingWindow != null && floatingWindow.isShowing()) {
            floatingWindow.dismiss();
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
                convertView = mInflater.inflate(R.layout.item_transport_dispatch_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                holder.backOrigin.setAdapter(new SpinnerAdapter());
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= ordersList.size() - 1) {
                if (ordersList.size() < itemTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                } else {
                    footView.setText("");
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

            holder.no.setText(order.getContainerNo());
            holder.type.setText(TextUtils.isEmpty(order.getContainerType()) ? "" : order.getContainerType());
            holder.placeGet.setText(order.getDelivPlace());
            holder.address.setText(order.getAddress());
            holder.placeRtn.setText(order.getRtnPlace());
            setAppointTimeGet(holder.appointTimeGet, position);
            setAppointTimeRtn(holder.appointTimeRtn, position);
            setDeliverTime(holder.timeDeliver, position);
            setBackOrigin(holder.backOrigin, position);
            return convertView;
        }

        private void setTitle(ViewHolder holder, final int position) {

            DispatchOrder order = ordersList.get(position);

            holder.title.setText(getString(R.string.dispatch_name, order.getContainerNo() == null ? "" : order.getContainerNo())
                    + "  " + (dictsMap.containsKey(order.getStatus()) ? dictsMap.get(order.getStatus()).getLabel() : ""));
            holder.top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });


            if (order.isCanDispatch()) {
                holder.checked.setEnabled(true);
                holder.checked.setChecked(order.isSelected());
                holder.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onCheckedChangeListener(isChecked, position);
                    }
                });
            } else {
                holder.checked.setEnabled(false);
            }
        }

        /**
         * 设置预约提箱时间
         */
        private void setAppointTimeGet(final TextView view, final int position) {
            DispatchOrder order = ordersList.get(position);

            if (order.isCanDispatch()) {
                view.setText(order.getAppointTimeGet() == null ? "" : order.getAppointTimeGet());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDate(view, position);
                    }
                });
            } else {
                view.setText("");
            }
        }

        /**
         * 设置预约还箱时间
         */
        private void setAppointTimeRtn(final TextView view, final int position) {
            DispatchOrder order = ordersList.get(position);

            if (order.isCanDispatch()) {
                view.setText(order.getAppointTimeRtn() == null ? "" : order.getAppointTimeRtn());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDate(view, position);
                    }
                });
            } else {
                view.setText(order.getAppointTimeRtn() == null ? "" : order.getAppointTimeRtn());
            }
        }

        /**
         * 设置送货时间
         */
        private void setDeliverTime(final TextView view, final int position) {
            DispatchOrder order = ordersList.get(position);

            if (order.isCanDispatch()) {
                view.setText(order.getDelivTime() == null ? "" : order.getDelivTime());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDate(view, position);
                    }
                });
            } else {
                view.setText(order.getDelivTime() == null ? "" : order.getDelivTime());
            }
        }


        /**
         * 选择日期
         */
        private void selectDate(final TextView targetView, final int position) {
            Log.e(TAG, "selectDate: position = "+position);
            View rootView = mInflater.inflate(R.layout.view_datepicker, null);
            final DatePicker picker = rootView.findViewById(R.id.picker);
            AlertDialog.Builder builder = new AlertDialog.Builder(TransportOrderDispatchActivity.this, R.style.MyDialogStyle);
            builder.setTitle(R.string.order_check_picker);
            builder.setView(rootView);
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int year = picker.getYear();
                    int month = picker.getMonth() + 1;
                    int day = picker.getDayOfMonth();

                    String date = String.format(Locale.CHINA,
                            "%d-%d-%d",
                            year, month, day);

                    switch (targetView.getId()) {
                        case R.id.appoint_time_get:
                            ordersList.get(position).setAppointTimeGet(date);
                            if (!ordersList.get(position).isSelected()) {
                                break;
                            }
                            for (int i = 0; i < ordersList.size(); i++) {
                                if (ordersList.get(i).isCanDispatch() && ordersList.get(i).isSelected()) {
                                    if (TextUtils.isEmpty(ordersList.get(i).getAppointTimeGet())) {
                                        ordersList.get(i).setAppointTimeGet(date);
                                    }
                                }
                            }
                            break;
                        case R.id.time_deliver:
                            ordersList.get(position).setDelivTime(date);
                            if (!ordersList.get(position).isSelected()) {
                                break;
                            }
                            for (int i = 0; i < ordersList.size(); i++) {
                                if (ordersList.get(i).isCanDispatch() && ordersList.get(i).isSelected()) {
                                    if (TextUtils.isEmpty(ordersList.get(i).getDelivTime())) {
                                        ordersList.get(i).setDelivTime(date);
                                    }
                                }
                            }
                            break;
                        case R.id.appoint_time_rtn:
                            ordersList.get(position).setAppointTimeRtn(date);
                            if (!ordersList.get(position).isSelected()) {
                                break;
                            }
                            for (int i = 0; i < ordersList.size(); i++) {
                                if (ordersList.get(i).isCanDispatch() && ordersList.get(i).isSelected()) {
                                    if (TextUtils.isEmpty(ordersList.get(i).getAppointTimeRtn())) {
                                        ordersList.get(i).setAppointTimeRtn(date);
                                    }
                                }
                            }
                            break;
                    }

                    notifyDataSetChanged();
                }
            });
            builder.create().show();
        }

        /**
         * 设置原车返回与否
         */
        private void setBackOrigin(Spinner view, final int position) {
            DispatchOrder order = ordersList.get(position);
            if (order.isCanDispatch()) {
                view.setSelection(0);//默认原车返回
            } else {
                view.setSelection(order.getOriBack() != null && order.getOriBack().equals("1") ? 0 : 1);
            }
        }

        public class ViewHolder {
            @BindView(R.id.dispatch_top)
            protected View top;
            @BindView(R.id.dispatch_checked)
            protected CheckBox checked;
            @BindView(R.id.dispatch_title)
            protected TextView title;
            @BindView(R.id.dispatch_title_arrow)
            protected ImageView arrow;
            @BindView(R.id.dispatch_child)
            protected View child;

            @BindView(R.id.number)
            protected TextView no;
            @BindView(R.id.type)
            protected TextView type;
            @BindView(R.id.place_get)
            protected TextView placeGet;
            @BindView(R.id.address)
            protected TextView address;
            @BindView(R.id.place_rtn)
            protected TextView placeRtn;
            @BindView(R.id.appoint_time_get)
            protected TextView appointTimeGet;
            @BindView(R.id.time_deliver)
            protected TextView timeDeliver;
            @BindView(R.id.appoint_time_rtn)
            protected TextView appointTimeRtn;
            @BindView(R.id.back_origin)
            protected Spinner backOrigin;
        }
    }

    private class SpinnerAdapter extends BaseAdapter {

        private String[] backStatus;

        public SpinnerAdapter() {
            backStatus = getResources().getStringArray(R.array.yes_or_no);
        }

        @Override
        public int getCount() {
            return backStatus.length;
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

            ViewHolder.<TextView>get(convertView, R.id.text)
                    .setText(backStatus[position]);
            return convertView;
        }
    }

    private class DriverAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return driversList == null ? 0 : driversList.size();
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
                    .setText(driversList.get(position).getName() == null ? "" : driversList.get(position).getName());
            return convertView;
        }
    }

    private class TruckAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return trucksList == null ? 0 : trucksList.size();
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
                    .setText(trucksList.get(position).getTruck() == null ? "" : trucksList.get(position).getTruck());
            return convertView;
        }
    }
}
