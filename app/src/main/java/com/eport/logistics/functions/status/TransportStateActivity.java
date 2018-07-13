package com.eport.logistics.functions.status;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.BaseActivity;
import com.eport.logistics.R;
import com.eport.logistics.bean.Dicts;
import com.eport.logistics.bean.TransStatus;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 物流状态跟踪
 * */
public class TransportStateActivity extends BaseActivity {

    private static final String TAG = "TransportStateActivity";

    private Unbinder unbinder;
    private LayoutInflater mInflater;
    private Adapter mAdapter;

    @BindView(R.id.lister)
    protected ListView lister;
    @BindView(R.id.empty)
    protected TextView mEmpty;
    @BindView(R.id.status_order)
    protected EditText mOrderEt;
    @BindView(R.id.status_check)
    protected Button mCommit;

    private ArrayList<TransStatus> statusList;

    private boolean requestDataFinish = true;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_logistics_status_activity);
        unbinder = ButterKnife.bind(this);
        mInflater = LayoutInflater.from(this);
        setTopBar(R.drawable.icon_back, getIntent().getStringExtra("menuName"), 0);
        mAdapter = new Adapter();
        lister.setAdapter(mAdapter);

        mCommit.setOnClickListener(this);
    }

    /**
     * 加载数据
     * */
    private void getDataList(String order) {

        if (!requestDataFinish) {
            return;
        }

        WebRequest.getInstance().getStatusRecord(order, new Observer<JSONObject>() {
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

                parseDataList(o);
            }

            @Override
            public void onError(Throwable e) {
                requestDataFinish = true;
                requestFinished();
                MyToast.show(TransportStateActivity.this,
                        e == null ? getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 解析数据
     * */
    private void parseDataList(JSONObject rootJson) {
        requestDataFinish = true;
        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("statusReocrds");

        statusList = (ArrayList<TransStatus>) JSON.parseArray(dataArray.toJSONString(), TransStatus.class);

        requestFinished();
        Log.e(TAG, "parseDataList: "+ statusList);
    }

    private boolean requestFinished() {
        if (requestDataFinish) {
            mAdapter.notifyDataSetChanged();
            dismissDialog();

            mEmpty.setVisibility(statusList == null || statusList.isEmpty() ? View.VISIBLE : View.GONE);
            return true;
        }

        return false;
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
            case R.id.status_check:
                String order = mOrderEt.getText().toString().trim();
                if (TextUtils.isEmpty(order)) {
                    break;
                }
                getDataList(order);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return statusList == null ? 0 : statusList.size();
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
                convertView = mInflater.inflate(R.layout.item_logistics_status, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == 0) {
                holder.dot.setImageResource(R.drawable.fg_dot);
                holder.lineShort.setVisibility(View.VISIBLE);
                holder.line.setVisibility(View.GONE);
            }else {
                holder.dot.setImageResource(R.drawable.fg_dot_normal);
                holder.line.setVisibility(View.VISIBLE);
                holder.lineShort.setVisibility(View.GONE);
            }

            setStatus(holder.status, position);

            setDate(holder.time, position);

            return convertView;
        }

        private void setStatus(TextView view, int position) {
            TransStatus trans = statusList.get(position);
            if (TextUtils.isEmpty(trans.getStatus())) {
                view.setText("");
                return;
            }

            if (position == 0) {
                view.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else {
                view.setTextColor(getResources().getColor(R.color.text_main));
            }

            String transContent = "";

            if (trans.getStatus().equals(Dicts.STATUS_5200)) {
                transContent = getString(R.string.transport_status_5200);
            }else if (trans.getStatus().equals(Dicts.STATUS_5300)) {
                transContent = getString(R.string.transport_status_5300);
            }else if (trans.getStatus().equals(Dicts.STATUS_5400)) {
                transContent = getString(R.string.transport_status_5400);
            }else if (trans.getStatus().equals(Dicts.STATUS_5700)) {
                transContent = getString(R.string.transport_status_5700);
            }else if (trans.getStatus().equals(Dicts.STATUS_5800)) {
                transContent = getString(R.string.transport_status_5800);
            }else if (trans.getStatus().equals(Dicts.STATUS_5900)) {
                transContent = getString(R.string.transport_status_5900);
            }

            view.setText(transContent);
        }

        private void setDate(TextView view, int position) {

            TransStatus trans = statusList.get(position);

            if (trans.getDate() == null) {
                view.setText("");
                return;
            }

            String date = "";
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                        .format(new Date(trans.getDate()));
            }catch (Exception e) {
                e.printStackTrace();
            }

            if (position == 0) {
                view.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else {
                view.setTextColor(getResources().getColor(R.color.text_main));
            }
            view.setText(date);
        }

        public class ViewHolder {
            @BindView(R.id.progress_line)
            protected ImageView line;
            @BindView(R.id.progress_line_short)
            protected ImageView lineShort;
            @BindView(R.id.progress_dot)
            protected ImageView dot;
            @BindView(R.id.order_status)
            protected TextView status;
            @BindView(R.id.order_time)
            protected TextView time;
            @BindView(R.id.order_remark)
            protected TextView remark;
        }
    }
}
