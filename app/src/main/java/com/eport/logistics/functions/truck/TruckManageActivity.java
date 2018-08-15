package com.eport.logistics.functions.truck;

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
import com.eport.logistics.bean.Truck;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Locale;

import javax.xml.transform.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 车辆管理
 * */
public class TruckManageActivity extends BaseActivity {
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

    private ArrayList<Truck> trucksList;

    private int pageSize;//每页数据条数
    private int pageNum;//当前所在页
    private int pageTotal;//总页数
    private int itemTotal;//总条数

    private Truck operatingTruck;

    private final int itemPerPage = 10;

    private String truckNo = "";
    private String truckType = "";
    private String drivingLicNo = "";
    private String carryCap = "";

    private TruckManageFloatingWindow floatingWindow;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_truck_manage_activity);
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

        if (trucksList == null) {
            trucksList = new ArrayList<>();
        }

        pageNum = 1;

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
            mEmpty.setVisibility(trucksList != null && trucksList.size() > 0 ? View.GONE : View.VISIBLE);
            if (trucksList == null || trucksList.size() == 0) {
                footView.setText("");
            }
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

        WebRequest.getInstance().getTruckList(
                add ? pageNum + 1 : 1,
                pageSize == 0 ? itemPerPage : pageSize,
                truckNo,
                truckType,
                drivingLicNo,
                carryCap,
                new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        requestDataListFinish = false;
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
                        MyToast.show(TruckManageActivity.this,
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

        if (requestDataListFinish) {
            return;
        }

        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("rows");

//        pageTotal = rootJson.getJSONObject("data").getInteger("pages");
//        pageNum = rootJson.getJSONObject("data").getInteger("pageNum");
//        pageSize = rootJson.getJSONObject("data").getInteger("size");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");

        Log.e(TAG, "parseDataList: pageNum = "+pageNum);


        if (add) {
            trucksList.addAll(JSON.parseArray(dataArray.toJSONString(), Truck.class));
        }else {
            trucksList = (ArrayList<Truck>) JSON.parseArray(dataArray.toJSONString(), Truck.class);
        }

        pageNum = trucksList.size() / itemPerPage;

        for (int i = 0; i < trucksList.size(); i++) {
            if (operatingTruck != null && operatingTruck.getId().equals(trucksList.get(i).getId())) {
                trucksList.get(i).setSpread(operatingTruck.isSpread());
                break;
            }
        }

        Log.e(TAG, "parseDataList: "+ trucksList);
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
            case R.id.base_top_right:
                addTruck();
                break;
        }
    }

    /**
     * 打开检索信息悬浮窗
     * */
    private void showCheckAction() {
        if (floatingWindow == null) {
            floatingWindow = new TruckManageFloatingWindow(this, mFloatingWindow);
            floatingWindow.setButtonClickListener(new TruckManageFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindow.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String no, String type, String license, String capacity) {
                    truckNo = no;
                    truckType = type;
                    drivingLicNo = license;
                    carryCap = capacity;

                    getDataList(false);
                }

                @Override
                public void onResetClick() {
                    truckNo = "";
                    truckType = "";
                    drivingLicNo = "";
                    carryCap = "";

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
    private void onOperationClick(View view, final Truck truck, final int position) {

        operatingTruck = truck;

        switch (view.getId()) {
            case R.id.truck_modify:
                Log.e(TAG, "onOperationClick modify: "+ truck);
                Intent operIntent = new Intent(TruckManageActivity.this, TruckAddActivity.class);
                operIntent.putExtra("truck", truck);
                startActivityForResult(operIntent, Codes.CODE_REQUEST_TRUCK);
                break;
            case R.id.truck_operation:
                Log.e(TAG, "onOperationClick operation : "+ truck);
                WebRequest.getInstance().modifyTruckUsable(truck.getId(), truck.getTruckStatus().equals("1") ? "1" : "0",
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
                                truck.setTruckStatus(truck.getTruckStatus().equals("1") ? "0" : "1");
                                trucksList.set(position, truck);
                                mAdapter.notifyDataSetChanged();
                                dismissDialog();
                                MyToast.show(TruckManageActivity.this, R.string.operation_success);
                            }

                            @Override
                            public void onError(Throwable e) {
                                dismissDialog();
                                MyToast.show(TruckManageActivity.this,
                                        e == null ? getString(R.string.operation_failed) : e.getMessage());
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                break;
        }
    }

    /**
     * 新增一个车队信息
     * */
    private void addTruck() {
        startActivityForResult(new Intent(this, TruckAddActivity.class), Codes.CODE_REQUEST_TRUCK);
    }

    private void onItemClick(int position) {
        Truck truck = trucksList.get(position);
        truck.setSpread(!truck.isSpread());
        operatingTruck = truck;
        trucksList.set(position, truck);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Codes.CODE_REQUEST_TRUCK) {
                getDataList(false);
            }
        }
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_truck_manage_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= trucksList.size() - 1) {
                if (trucksList.size() < itemTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                }else {
                    footView.setText(R.string.foot_no_more);
                }
            }

            setTitle(holder, position);
            if (!trucksList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.child.setVisibility(View.GONE);
                return convertView;
            }


            holder.arrow.setRotation(180);
            holder.child.setVisibility(View.VISIBLE);

            Truck truck = trucksList.get(position);

            holder.teamName.setText(TextUtils.isEmpty(truck.getMotocadeName()) ? "" : truck.getMotocadeName());
            holder.teamCode.setText(TextUtils.isEmpty(truck.getMotocadeCode()) ? "" : truck.getMotocadeCode());
            holder.truckNo.setText(TextUtils.isEmpty(truck.getTruckNo()) ? "" : truck.getTruckNo());
            holder.license.setText(TextUtils.isEmpty(truck.getLicenseId()) ? "" : truck.getLicenseId());
            holder.type.setText(TextUtils.isEmpty(truck.getTruckType()) ? "" : truck.getTruckType());
            holder.capacity.setText(TextUtils.isEmpty(truck.getCarryCap()) ? "" : truck.getCarryCap());

            setOperation(holder, truck, position);

            return convertView;
        }

        private void setTitle(ViewHolder holder, final int position) {
            Truck truck = trucksList.get(position);
            holder.title.setText(String.format(Locale.CHINA,
                    "%s - %s - %s",
                    truck.getMotocadeName() == null ? "" : truck.getMotocadeName(),
                    truck.getTruckNo() == null ? "" : truck.getTruckNo(),
                    TextUtils.isEmpty(truck.getTruckStatus()) ? "" :
                            truck.getTruckStatus().equals("1") ? getString(R.string.team_inuse) : getString(R.string.team_not_inuse)));
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
        private void setOperation(ViewHolder holder, final Truck truck, final int position) {

            holder.operation.setText(truck.getTruckStatus() != null && truck.getTruckStatus().equals("1") ? R.string.stop : R.string.start);
            if (truck.getTruckStatus() != null && truck.getTruckStatus().equals("1")) {
                holder.modify.setVisibility(View.VISIBLE);
                holder.modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, truck, position);
                    }
                });
            }else {
                holder.modify.setVisibility(View.GONE);
            }

            holder.operation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOperationClick(v, truck, position);
                }
            });
        }

        public class ViewHolder {
            @BindView(R.id.parent_top)
            protected View top;
            @BindView(R.id.parent_title)
            protected TextView title;
            @BindView(R.id.parent_arrow)
            protected ImageView arrow;
            @BindView(R.id.child)
            protected View child;

            @BindView(R.id.team_name)
            protected TextView teamName;
            @BindView(R.id.team_code)
            protected TextView teamCode;
            @BindView(R.id.truck_no)
            protected TextView truckNo;
            @BindView(R.id.truck_license)
            protected TextView license;
            @BindView(R.id.truck_type)
            protected TextView type;
            @BindView(R.id.truck_capacity)
            protected TextView capacity;
            @BindView(R.id.truck_modify)
            protected TextView modify;
            @BindView(R.id.truck_operation)
            protected TextView operation;
        }
    }
}
