package com.sdeport.logistics.driver.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
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
import com.sdeport.logistics.common.widgets.CustomDialog;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Event;
import com.sdeport.logistics.driver.bean.Order;
import com.sdeport.logistics.driver.bean.OrderPage;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.constant.Constants;
import com.sdeport.logistics.driver.server.WebRequest;

import org.greenrobot.eventbus.EventBus;

import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class OrdersFragment extends BaseFragment {

    private static final String TAG = "OrdersFragment";

    private static final int ORDER_TYPE_ALL = 0;
    private static final int ORDER_TYPE_NEW = 1;
    private static final int ORDER_TYPE_ING = 2;
    private static final int ORDER_TYPE_FINISHED = 3;

    private Unbinder unbinder;

    private LayoutInflater mInflater;

    @BindView(R.id.refresher)
    protected SmartRefreshLayout refresher;
    @BindView(R.id.header)
    protected MaterialHeader header;
    @BindView(R.id.list_emptier)
    protected TextView empty;
    @BindView(R.id.title_all)
    protected TextView all;
    @BindView(R.id.title_new)
    protected TextView unReceived;
    @BindView(R.id.title_received)
    protected TextView received;
    @BindView(R.id.title_finished)
    protected TextView finished;
//    @BindView(R.id.line)
//    protected ImageView line;

    @BindView(R.id.pager)
    protected ViewPager pager;

//    private SparseArray<ArrayList<Order>> dataList;

    private TextView tabTitles[];
    private ArrayList<ListView> tabViews;

    private PageAdapter adapter;
    private ListerAdapter listerAdapter;

    private int currentPageIndex = 0;//注意：此index与获取派车单状态码是一一对应的 页0-码0,页1-码1,页2-码2,页3-码3

    private SparseArray<OrderPage> dataLists;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_orders, container, false);

        dataLists = new SparseArray<>();
        mInflater = inflater;
        unbinder = ButterKnife.bind(this, contentView);

        empty.setText(R.string.order_list_empty);
        pager.setOffscreenPageLimit(3);
        pager.addOnPageChangeListener(pageChangeListener);

        refresher.setOnRefreshListener(refreshListener);
        all.setOnClickListener(onClickListener);
        unReceived.setOnClickListener(onClickListener);
        received.setOnClickListener(onClickListener);
        finished.setOnClickListener(onClickListener);

        tabTitles = new TextView[]{all, unReceived, received, finished};
        tabViews = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            ListView view = (ListView) inflater.inflate(R.layout.listview, null);
            view.setAdapter(new ListerAdapter(i));
            tabViews.add(view);
        }

        adapter = new PageAdapter();
        pager.setAdapter(adapter);

        getDataList(false, ORDER_TYPE_ALL);
        getDataList(false, ORDER_TYPE_NEW);
        getDataList(false, ORDER_TYPE_ING);
        getDataList(false, ORDER_TYPE_FINISHED);
        return contentView;
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            getDataList(false, currentPageIndex);
        }
    };

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            if (currentPageIndex == position) {
                return;
            }

            tabTitles[currentPageIndex].setTextColor(getResources().getColor(R.color.text_dark));
            tabTitles[currentPageIndex].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            tabTitles[position].setTextColor(getResources().getColor(R.color.orange));
            tabTitles[position].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.drawable_line_orange);

            currentPageIndex = position;

            refreshList(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = currentPageIndex;
            switch (v.getId()) {
                case R.id.title_all:
                    index = 0;
                    break;
                case R.id.title_new:
                    index = 1;
                    break;
                case R.id.title_received:
                    index = 2;
                    break;
                case R.id.title_finished:
                    index = 3;
                    break;
            }

            if (currentPageIndex == index) {
                return;
            }
            pager.setCurrentItem(index, false);
        }
    };

    private void getDataList(final boolean add, final int target) {

        if (dataLists.get(target) == null) {
            dataLists.put(target, new OrderPage());
        }

        if (dataLists.get(target).isLoading()) {
            return;
        }

        dataLists.get(target).setLoading(true);
        WebRequest.getInstance().getOrderList(
                add ? dataLists.get(target).getDatas().size() / dataLists.get(target).getCountPerPage() + 1 : 1,
                dataLists.get(target).getCountPerPage(),
                String.valueOf(target), new Observer<JSONObject>() {
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

    private void refreshList(int pageIndex) {

        listerAdapter = (ListerAdapter) tabViews.get(pageIndex).getAdapter();
        empty.setVisibility(dataLists.get(pageIndex).getDatas().isEmpty() ? View.VISIBLE : View.GONE);
        listerAdapter.setData(dataLists.get(pageIndex).getDatas());
        listerAdapter.notifyDataSetChanged();
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
        refreshList(currentPageIndex);
    }

    private void onViewClick(View v, int position) {
        Order order = dataLists.get(currentPageIndex).getDatas().get(position);
        switch (v.getId()) {
            case R.id.accept_t:
                accept(order.getId(), "T");
                break;
            case R.id.accept_r:
                accept(order.getId(), "R");
                break;
            case R.id.refuse_t:
                refuseOrCancel(true, order.getId(), "T");
                break;
            case R.id.refuse_r:
                refuseOrCancel(true, order.getId(), "R");
                break;
            case R.id.cancel_t:
                refuseOrCancel(false, order.getId(), "T");
                break;
            case R.id.cancel_r:
                refuseOrCancel(false, order.getId(), "R");
                break;
            case R.id.confirm_t:
                confirm(true, order.getId(), "T");
                break;
            case R.id.confirm_r:
                confirm(false, order.getId(), "R");
                break;
            case R.id.order_simple:
                dataLists.get(currentPageIndex).getDatas().get(position).setSpread(!dataLists.get(currentPageIndex).getDatas().get(position).isSpread());
                break;
            case R.id.collect:
                setCollectStatus(order);
                break;
        }
        ((ListerAdapter) tabViews.get(currentPageIndex).getAdapter()).notifyDataSetChanged();
    }

    /**
     * 弹出对话框，确定接单
     * */
    private void accept(final String id, final String type) {
        CustomDialog acceptDialog = new CustomDialog.Builder(mActivity)
                .setMessage(getString(R.string.order_accept_confirm))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        acceptOrder(id, type);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        acceptDialog.setCanceledOnTouchOutside(false);
        acceptDialog.show();
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
                getDataList(false, ORDER_TYPE_ALL);
                getDataList(false, ORDER_TYPE_NEW);
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
     * 弹出对话框，拒绝/撤销派车单
     * */
    private void refuseOrCancel(boolean refuse, final String id, final String type) {
        CustomDialog acceptDialog = new CustomDialog.Builder(mActivity)
                .setMessage(getString(refuse ? R.string.order_refuse_confirm : R.string.order_cancel_confirm))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        refuseOrCancelOrder(id, type);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        acceptDialog.setCanceledOnTouchOutside(false);
        acceptDialog.show();
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
                getDataList(false, ORDER_TYPE_NEW);
                getDataList(false, ORDER_TYPE_ALL);
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
     * 弹出对话框,确认提箱/还箱
     * */
    private void confirm(boolean t, final String id, final String type) {
        CustomDialog acceptDialog = new CustomDialog.Builder(mActivity)
                .setMessage(getString(t ? R.string.order_confirm_t : R.string.order_confirm_r))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        confirmOrder(id, type);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        acceptDialog.setCanceledOnTouchOutside(false);
        acceptDialog.show();
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
                getDataList(false, ORDER_TYPE_ALL);
                getDataList(false, ORDER_TYPE_ING);
                getDataList(false, ORDER_TYPE_FINISHED);
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
     * 设置收藏状态
     * */
    private void setCollectStatus(final Order order) {
        if (User.getUser().getCollectedOrder() != null) {
            final boolean remove = order.getId().equals(User.getUser().getCollectedOrder().getId());
            String msg = "";
            if (remove) {
                msg = getString(R.string.order_collect_remove);
            }else {
                msg = getString(R.string.order_collect_exchange,
                        User.getUser().getCollectedOrder().getBillNo(),
                        order.getBillNo());
            }
            CustomDialog colDialog = new CustomDialog.Builder(mActivity)
                    .setMessage(msg)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            saveCollection(remove, order);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            colDialog.setCanceledOnTouchOutside(false);
            colDialog.show();
        }else {
            saveCollection(true, order);
        }
    }

    private void saveCollection(boolean remove, Order order) {
        if (remove) {
            Prefer.getInstance().putString(Constants.KEY_ORDER_ING, "");
            User.getUser().setCollectedOrder(null);
            MyToast.show(mActivity, R.string.order_remove_success);
        }else {
            Prefer.getInstance().putString(Constants.KEY_ORDER_ING, JSON.toJSONString(order));
            User.getUser().setCollectedOrder(order);
            MyToast.show(mActivity, R.string.order_collect_success);
        }
        EventBus.getDefault().post(new Event(Event.TAG_REFRESH_ING));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(tabViews.get(position));
            return tabViews.get(position);
//            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }

    private class ListerAdapter extends BaseAdapter {

        private ArrayList<Order> orders;
        private int type;

        public ListerAdapter(int type) {
            this.type = type;
        }

        public void setData(ArrayList<Order> orders) {
            this.orders = orders;
        }

        @Override
        public int getCount() {
            return orders == null ? 0 : orders.size();
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

            if (dataLists.get(type).getDatas().size() < dataLists.get(type).getTotal()
                    && position >= dataLists.get(type).getDatas().size()) {
                getDataList(true, type);
            }

            Order order = dataLists.get(type).getDatas().get(position);
            holder.bill.setText(
                    String.format(Locale.CHINA, "%s:%s",
                            getString(R.string.order_ing_bill_no),
                            order.getBillNo()));
            holder.simple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });
            holder.owner.setText(order.getOwner() == null ? "" : order.getOwner());
            holder.address.setText(order.getAddress() == null ? "" : order.getAddress());

            setCollectStatus(holder, position);

            if (!dataLists.get(type).getDatas().get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.spreader.setVisibility(View.GONE);
                return convertView;
            }else {
                holder.arrow.setRotation(180);
                holder.spreader.setVisibility(View.VISIBLE);
            }
            setStatus(holder, position);

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

        private void setCollectStatus(ViewHolder holder, final int position) {
            final Order order = orders.get(position);
            if (!TextUtils.isEmpty(order.gettStatus()) || !TextUtils.isEmpty(order.getrStatus())) {
                holder.collect.setVisibility(View.VISIBLE);
                if (User.getUser().getCollectedOrder() != null &&
                        User.getUser().getCollectedOrder().getId().equals(order.getId())) {
                    holder.collect.setImageResource(R.drawable.icon_collected);
                }else {
                    holder.collect.setImageResource(R.drawable.icon_collect);
                }
                holder.collect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onViewClick(view, position);
                    }
                });
            }else {
                holder.collect.setVisibility(View.INVISIBLE);
            }
        }

        /**
         * 设置状态
         * */
        private void setStatus(ViewHolder holder, final int position) {
            Order order = dataLists.get(type).getDatas().get(position);

            if (!TextUtils.isEmpty(order.gettStatus())) {
                holder.tBtns.setVisibility(View.VISIBLE);
                boolean accept = false;
                boolean refuse = false;
                boolean cancel = false;
                boolean confirm = false;

                switch (order.getrStatus()) {
                    case "0":
                        holder.tStatus.setText(R.string.order_status_t_0);
                        break;
                    case "1":
                        accept = true;
                        refuse = true;
                        holder.tStatus.setText(R.string.order_status_t_1);
                        break;
                    case "2":
                        cancel = true;
                        confirm = true;
                        holder.tStatus.setText(R.string.order_status_t_2);
                        break;
                    case "3":
                        holder.tStatus.setText(R.string.order_status_t_3);
                        break;
                    case "4":
                        holder.tStatus.setText(R.string.order_status_t_4);
                        break;
                    default:
                        holder.tStatus.setText(R.string.order_status_unknown);
                        break;
                }

                holder.tAccept.setVisibility(accept ? View.VISIBLE : View.GONE);
                holder.tRefuse.setVisibility(refuse ? View.VISIBLE : View.GONE);
                holder.tCancel.setVisibility(cancel ? View.VISIBLE : View.GONE);
                holder.tConfirm.setVisibility(confirm ? View.VISIBLE : View.GONE);

                if (accept) {
                    holder.tAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }

                if (refuse) {
                    holder.tRefuse.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
                if (cancel) {
                    holder.tCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
                if (confirm) {
                    holder.tConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
            }else {
                holder.tBtns.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(order.getrStatus())) {
                holder.rBtns.setVisibility(View.VISIBLE);
                boolean accept = false;
                boolean refuse = false;
                boolean cancel = false;
                boolean confirm = false;

                switch (order.getrStatus()) {
                    case "0":
                        holder.rStatus.setText(R.string.order_status_r_0);
                        break;
                    case "1":
                        accept = true;
                        refuse = true;
                        holder.rStatus.setText(R.string.order_status_r_1);
                        break;
                    case "2":
                        cancel = true;
                        confirm = true;
                        holder.rStatus.setText(R.string.order_status_r_2);
                        break;
                    case "3":
                        holder.rStatus.setText(R.string.order_status_r_3);
                        break;
                    case "4":
                        holder.rStatus.setText(R.string.order_status_r_4);
                        break;
                    case "5":
                        holder.rStatus.setText(R.string.order_status_r_5);
                        break;
                    default:
                        holder.rStatus.setText(R.string.order_status_unknown);
                        break;
                }

                holder.rAccept.setVisibility(accept ? View.VISIBLE : View.GONE);
                holder.rRefuse.setVisibility(refuse ? View.VISIBLE : View.GONE);
                holder.rCancel.setVisibility(cancel ? View.VISIBLE : View.GONE);
                holder.rConfirm.setVisibility(confirm ? View.VISIBLE : View.GONE);

                if (accept) {
                    holder.rAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }

                if (refuse) {
                    holder.rRefuse.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
                if (cancel) {
                    holder.rCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
                if (confirm) {
                    holder.rConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onViewClick(view, position);
                        }
                    });
                }
            }else {
                holder.rBtns.setVisibility(View.GONE);
            }
        }
    }

    protected class ViewHolder{
        @BindView(R.id.arrow)
        protected ImageView arrow;
        @BindView(R.id.bill_no)
        protected TextView bill;
        @BindView(R.id.collect)
        protected ImageView collect;
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
        @BindView(R.id.status_t)
        protected TextView tStatus;
        @BindView(R.id.btns_get)
        protected View tBtns;
        @BindView(R.id.accept_t)
        protected TextView tAccept;
        @BindView(R.id.refuse_t)
        protected TextView tRefuse;
        @BindView(R.id.cancel_t)
        protected TextView tCancel;
        @BindView(R.id.confirm_t)
        protected TextView tConfirm;
        @BindView(R.id.info_rtn)
        protected View infoRtn;
        @BindView(R.id.driver_rtn)
        protected TextView driverRtn;
        @BindView(R.id.address_rtn)
        protected TextView addrRtn;
        @BindView(R.id.status_r)
        protected TextView rStatus;
        @BindView(R.id.btns_rtn)
        protected View rBtns;
        @BindView(R.id.accept_r)
        protected TextView rAccept;
        @BindView(R.id.refuse_r)
        protected TextView rRefuse;
        @BindView(R.id.cancel_r)
        protected TextView rCancel;
        @BindView(R.id.confirm_r)
        protected TextView rConfirm;
    }
}
