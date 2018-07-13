package com.inspur.eport.logistics.functions.transport;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.eport.logistics.BaseActivity;
import com.inspur.eport.logistics.R;
import com.inspur.eport.logistics.bean.Order;
import com.inspur.eport.logistics.server.WebRequest;
import com.inspur.eport.logistics.utils.MyToast;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TransportOrderDetailActivity extends BaseActivity {

    private static final String TAG = "TransportOrderDetail";

    private Unbinder unbinder;
    private Order order;
    private LayoutInflater mInflater;
    private Adapter mAdapter;

    @BindView(R.id.detail_no)
    protected TextView mNo;
    @BindView(R.id.detail_delegate)
    protected TextView mDelegate;
    @BindView(R.id.detail_contact)
    protected TextView mContact;
    @BindView(R.id.detail_address)
    protected TextView mAddress;
    @BindView(R.id.detail_remark)
    protected TextView mRemark;
    @BindView(R.id.lister)
    protected ListView mLister;

    private WebView mContent;


    @Override
    protected void initUI(Bundle savedInstanceState) {

        addContentView(R.layout.view_webview);
        mContent = findViewById(R.id.webview);
        mContent.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        WebSettings webSettings = mContent.getSettings();
//        addContentView(R.layout.layout_order_transport_detail);
        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(true);
        // 设置缓存模式,一共有四种模式
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 设置缓存路径
//        webSettings.setAppCachePath("");
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true);
        // 将图片调整到合适的大小
        webSettings.setUseWideViewPort(true);
        // 支持内容重新布局,一共有四种方式
        // 默认的是NARROW_COLUMNS
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置可以被显示的屏幕控制
        webSettings.setDisplayZoomControls(true);
        // 设置默认字体大小
        webSettings.setDefaultFontSize(12);


        order = (Order) getIntent().getSerializableExtra("order");
//        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, R.string.order_detail_title, 0);
//        mInflater = LayoutInflater.from(this);
//        mAdapter = new Adapter();
//        mLister.setAdapter(mAdapter);

//        mNo.setText(order.getBillNo());
//        mDelegate.setText(order.getDelegate());
//        mContact.setText(order.getBuyerCN());
//        mAddress.setText(order.getAddress());

        getOrderDetail();
    }

    private void getOrderDetail() {
        WebRequest.getInstance().getOrderDetail(order.getId(), new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(false);
            }

            @Override
            public void onNext(String o) {
                Log.e(TAG, "onNext: "+o);
                if (TextUtils.isEmpty(o)) {
                    onError(new Throwable(getString(R.string.operation_failed)));
                    return;
                }

                dismissDialog();
                mContent.loadData(o, "text/html; charset=UTF-8", null);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: "+e);
                dismissDialog();
                MyToast.show(TransportOrderDetailActivity.this,
                        e == null ? getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void freeMe() {

    }

    @Override
    public void onBackPressed() {
        if (unbinder != null) {
            unbinder.unbind();
        }

        mContent.destroy();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                onBackPressed();
                break;
        }
    }

    private Order.Container getTitleContainer() {
        Order.Container cont = new Order.Container();
        cont.setNo(getString(R.string.order_container_no));
        cont.setType(getString(R.string.order_container_type));
        cont.setSize(getString(R.string.order_container_size));
        cont.setDate(getString(R.string.order_container_date));
        cont.setPlace(getString(R.string.order_container_place));
        return cont;
    }

    public class Adapter extends BaseAdapter{

        private ArrayList<Order.Container> containersList;

        public Adapter() {
            if (order == null || order.getContainers() == null) {
                containersList = new ArrayList<>();
                containersList.add(getTitleContainer());
            }else {
                containersList = order.getContainers();
                containersList.add(0, getTitleContainer());
            }
        }

        @Override
        public int getCount() {
            return containersList.size();
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
                convertView = mInflater.inflate(R.layout.item_transport_order_detail, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == 0) {
                holder.no.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.type.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.size.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.date.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.place.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else {
                holder.no.setTextColor(getResources().getColor(R.color.text_main));
                holder.type.setTextColor(getResources().getColor(R.color.text_main));
                holder.size.setTextColor(getResources().getColor(R.color.text_main));
                holder.date.setTextColor(getResources().getColor(R.color.text_main));
                holder.place.setTextColor(getResources().getColor(R.color.text_main));
            }

            holder.no.setText(containersList.get(position).getNo());
            holder.type.setText(containersList.get(position).getType());
            holder.size.setText(containersList.get(position).getSize());
            holder.date.setText(containersList.get(position).getDate());
            holder.place.setText(containersList.get(position).getPlace());

            return convertView;
        }

        public class ViewHolder {
            @BindView(R.id.detail_container_no)
            TextView no;
            @BindView(R.id.detail_container_type)
            TextView type;
            @BindView(R.id.detail_container_size)
            TextView size;
            @BindView(R.id.detail_container_date)
            TextView date;
            @BindView(R.id.detail_container_place)
            TextView place;
        }
    }
}
