package com.eport.logistics.functions.driver;

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
import com.eport.logistics.bean.Driver;
import com.eport.logistics.bean.Team;
import com.eport.logistics.functions.truck.TruckManageActivity;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.observable.ObservableSwitchIfEmpty;

/**
 * 驾驶员管理
 * */
public class DriverManageActivity extends BaseActivity {
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

    private ArrayList<Driver> driversList;

    private int pageSize;//每页数据条数
    private int pageNum;//当前所在页
    private int pageTotal;//总页数
    private int itemTotal;//总条数

    private Driver operatingDriver;

    private final int itemPerPage = 10;

    private String chiAccount = "";
    private String accountCName = "";
    private String idCardNo = "";
    private String phone = "";
    private String nickName = "";
    private String truckNo = "";
    private String fkMotorcadeId = "c8f6ff81-841a-4f08-beff-5fd5a1ac39ce";
    private String staffType = "1";

    private DriverManageFloatingWindow floatingWindow;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_driver_manage_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, getIntent().getStringExtra("menuName"), R.drawable.icon_add);

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

        if (driversList == null) {
            driversList = new ArrayList<>();
        }else {
            driversList.clear();
        }

        getDataList(false);
    }

    /**
     * 加载更多
     * */
    private void loadMore() {
        getDataList(true);
    }

    private boolean requestDataListFinish = true;

    private void refreshFinished() {
        if (isFinishing()) {
            return;
        }
        if (requestDataListFinish) {
            mRefresher.finishRefresh(true);
            dismissDialog();
            mAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(driversList != null && driversList.size() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 加载数据
     * @param add 获取后是更新还是添加
     * */
    private void getDataList(final boolean add) {
        if (!requestDataListFinish) {
            return;
        }

        requestDataListFinish = false;

        WebRequest.getInstance().getDriverList(
                add ? pageNum + 1 : 1,
                pageSize == 0 ? itemPerPage : pageSize,
                chiAccount,
                accountCName,
                idCardNo,
                phone,
                nickName,
                truckNo,
                fkMotorcadeId,
                staffType,
                new Observer<JSONObject>() {
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

                        parseDataList(o, add);
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestDataListFinish = true;
                        refreshFinished();
                        MyToast.show(DriverManageActivity.this,
                                e == null ? getString(R.string.operation_failed) : e.getMessage());
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

        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("rows");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");
        if (itemTotal == 0) {
            pageNum = 1;
        }else {
            pageNum = driversList.size() / itemPerPage + 1;
        }

        Log.e(TAG, "parseDataList: itemTotal = "+itemTotal+"; pageNum = "+pageNum);

        if (add) {
            driversList.addAll(JSON.parseArray(dataArray.toJSONString(), Driver.class));
        }else {
            driversList = (ArrayList<Driver>) JSON.parseArray(dataArray.toJSONString(), Driver.class);
        }

        for (int i = 0; i < driversList.size(); i++) {
            if (operatingDriver != null && operatingDriver.getId().equals(driversList.get(i).getId())) {
                driversList.get(i).setSpread(operatingDriver.isSpread());
                break;
            }
        }

        Log.e(TAG, "parseDataList: "+ driversList);
        requestDataListFinish = true;

        refreshFinished();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Codes.CODE_REQUEST_DRIVER) {
                getDataList(false);
            }
        }
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
            case R.id.base_top_right:
                addTeam();
                break;
        }
    }

    /**
     * 打开检索信息悬浮窗
     * */
    private void showCheckAction() {
        if (floatingWindow == null) {
            floatingWindow = new DriverManageFloatingWindow(this, mFloatingWindow);
            floatingWindow.setButtonClickListener(new DriverManageFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindow.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String account, String name, String cardId, String phone, String nick, String truckNo) {
                    chiAccount = account;
                    accountCName = name;
                    idCardNo = cardId;
                    DriverManageActivity.this.phone = phone;
                    nickName = nick;
                    DriverManageActivity.this.truckNo = truckNo;

                    getDataList(false);
                }


                @Override
                public void onResetClick() {
                    chiAccount = "";
                    accountCName = "";
                    idCardNo = "";
                    phone = "";
                    nickName = "";
                    truckNo = "";

                    getDataList(false);
                }
            });
        }
        mFloatingBtn.setVisibility(View.GONE);
        floatingWindow.show();
    }

    /**
     * 点击展开项中的功能按钮操作
     * */
    private void onOperationClick(View view, final Driver driver, final int position) {

        operatingDriver = driver;

        switch (view.getId()) {
            case R.id.driver_modify:
                Log.e(TAG, "onOperationClick modify: "+ driver);
                Intent operIntent = new Intent(DriverManageActivity.this, DriverAddActivity.class);
                operIntent.putExtra("driver", driver);
                startActivityForResult(operIntent, Codes.CODE_REQUEST_DRIVER);
                break;
            case R.id.driver_operation:
                Log.e(TAG, "onOperationClick operation : "+ driver);
                WebRequest.getInstance().modifyDriverUsable(
                        driver.getId(),
                        driver.getFkUserId(),
                        driver.getInUse() == null ? "0"  : driver.getInUse(),
                        new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                createDialog(false);
                            }

                            @Override
                            public void onNext(String o) {
                                if (o == null) {
                                    onError(new Throwable(getString(R.string.operation_failed)));
                                    return;
                                }

                                Log.e(TAG, "onNext: result = "+o);

                                if (!o.equals("1")) {
                                    onError(new Throwable(getString(R.string.operation_failed)));
                                    return;
                                }
                                driver.setInUse(driver.getInUse() != null && driver.getInUse().equals("1") ? "0" : "1");
                                driversList.set(position, driver);
                                mAdapter.notifyDataSetChanged();
                                dismissDialog();
                                MyToast.show(DriverManageActivity.this, R.string.operation_success);
                            }

                            @Override
                            public void onError(Throwable e) {
                                dismissDialog();
                                MyToast.show(DriverManageActivity.this,
                                        e == null ? getString(R.string.operation_failed) : e.getMessage());
                            }

                            @Override
                            public void onComplete() {

                            }
                        }
                );
                break;
        }
    }

    /**
     * 新增一个车队信息
     * */
    private void addTeam() {
        startActivity(new Intent(this, DriverAddActivity.class));
    }

    private void onItemClick(int position) {
        Driver driver = driversList.get(position);
        driver.setSpread(!driver.isSpread());
        operatingDriver = driver;
        driversList.set(position, driver);
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_driver_manage_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= driversList.size() - 1) {
                if (driversList.size() < itemTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                }else {
                    footView.setText(R.string.foot_no_more);
                }
            }

            setTitle(holder, position);
            if (!driversList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.child.setVisibility(View.GONE);
                return convertView;
            }


            holder.arrow.setRotation(180);
            holder.child.setVisibility(View.VISIBLE);

            Driver driver = driversList.get(position);
            holder.account.setText(TextUtils.isEmpty(driver.getChiAccount()) ? "" : driver.getChiAccount());
            holder.name.setText(TextUtils.isEmpty(driver.getAccountName()) ? "" : driver.getAccountName());
            holder.cardId.setText(TextUtils.isEmpty(driver.getIdCardNo()) ? "" : driver.getIdCardNo());
            holder.phone.setText(TextUtils.isEmpty(driver.getPhone()) ? "" : driver.getPhone());
            holder.nick.setText(TextUtils.isEmpty(driver.getNick()) ? "" : driver.getNick());
            holder.truck.setText(TextUtils.isEmpty(driver.getTruckNo()) ? "" : driver.getTruckNo());

            setOperation(holder, driver, position);

            return convertView;
        }

        private void setTitle(ViewHolder holder, final int position) {
            Driver driver = driversList.get(position);
            holder.title.setText(String.format(Locale.CHINA,
                    "%s - %s - %s",
                    driver.getAccountName() == null ? "" : driver.getAccountName(),
                    driver.getTruckNo() == null ? "" : driver.getTruckNo(),
                    TextUtils.isEmpty(driver.getInUse()) ? "" :
                            driver.getInUse().equals("1") ? getString(R.string.team_inuse) : getString(R.string.team_not_inuse)));
            holder.top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }

        /**
         * 根据状态，设置哪些操作功能按钮可以显示
         * */
        private void setOperation(ViewHolder holder, final Driver driver, final int position) {

            if (driver.getInUse() != null && driver.getInUse().equals("1")) {
                holder.operation.setText(R.string.stop);
                holder.modify.setVisibility(View.VISIBLE);
            }else {
                holder.operation.setText(R.string.start);
                holder.modify.setVisibility(View.GONE);
            }

            holder.modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperationClick(v, driver, position);
                }
            });

            holder.operation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperationClick(v, driver, position);
                }
            });
        }

        public class ViewHolder {
            @BindView(R.id.top)
            protected View top;
            @BindView(R.id.info_simple)
            protected TextView title;
            @BindView(R.id.info_arrow)
            protected ImageView arrow;
            @BindView(R.id.order_main_child)
            protected View child;

            @BindView(R.id.driver_account)
            protected TextView account;
            @BindView(R.id.driver_name)
            protected TextView name;
            @BindView(R.id.driver_nick)
            protected TextView nick;
            @BindView(R.id.driver_phone)
            protected TextView phone;
            @BindView(R.id.driver_card_id)
            protected TextView cardId;
            @BindView(R.id.driver_truck)
            protected TextView truck;

            @BindView(R.id.driver_modify)
            protected TextView modify;
            @BindView(R.id.driver_operation)
            protected TextView operation;
        }
    }
}
