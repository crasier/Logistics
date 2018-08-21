package com.sdeport.logistics.driver.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Order;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OrdersFragment extends BaseFragment {

    private Unbinder unbinder;

    private LayoutInflater mInflater;

    @BindView(R.id.refresher)
    protected SmartRefreshLayout refresher;
    @BindView(R.id.header)
    protected MaterialHeader header;
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

    private ArrayList<Order> orderList;

    private TextView tabTitles[];
    private ArrayList<ListView> tabViews;

    private PageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_orders, container, false);

        mInflater = inflater;
        unbinder = ButterKnife.bind(this, contentView);
        pager.setOffscreenPageLimit(2);
        pager.addOnPageChangeListener(pageChangeListener);
        tabTitles = new TextView[]{all, unReceived, received, finished};
        tabViews = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ListView view = (ListView) inflater.inflate(R.layout.listview, null);
            view.setAdapter(new ListerAdapter());
            tabViews.add(view);
            tabViews.get(i).getAdapter();
        }

        adapter = new PageAdapter();
        pager.setAdapter(adapter);
        return contentView;
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < tabTitles.length; i++) {
                if (i == position) {
                    tabTitles[i].setTextColor(getResources().getColor(R.color.orange));
                    tabTitles[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.drawable_line_orange);
                }else {
                    tabTitles[i].setTextColor(getResources().getColor(R.color.text_dark));
                    tabTitles[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void getDataList() {

    }

    private void onViewClick(View v, int position) {

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
            return tabViews.get(position);
//            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }

    private class ListerAdapter extends BaseAdapter {

        private ArrayList<Order> orders;

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

            holder.bill.setText(getString(R.string.order_ing_bill_no)+":"+"CNTU2030132345");
            holder.bill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });
            holder.status.setText("已提箱");
            holder.buyer.setText("山东华中股份有限公司");
            holder.address.setText("山东省青岛市即墨区寂寞路");

            setClicker(holder, position);

            if (!orders.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.spreader.setVisibility(View.GONE);
                return convertView;
            }else {
                holder.arrow.setRotation(180);
                holder.spreader.setVisibility(View.VISIBLE);
            }

            holder.contact.setText("15622220000");
            holder.timeDelivery.setText("2018-08-22 12:30:00");
            holder.cntr.setText("20GP");
            holder.driverGet.setText("张三,  鲁A12345,  2018-08-23 12:33:00");
            holder.addrGet.setText("提箱地："+"青岛港");
            holder.driverRtn.setText("李四,  鲁B54321,  2018-08-23 12:00:33");
            holder.addrRtn.setText("还箱地："+"青岛港2");

            return convertView;
        }

        private void setClicker(ViewHolder holder, final int position) {
            holder.receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewClick(v, position);
                }
            });
        }
    }

    protected class ViewHolder{
        @BindView(R.id.arrow)
        protected ImageView arrow;
        @BindView(R.id.bill_no)
        protected TextView bill;
        @BindView(R.id.status)
        protected TextView status;
        @BindView(R.id.buyer)
        protected TextView buyer;
        @BindView(R.id.address)
        protected TextView address;

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
        @BindView(R.id.info_rtn)
        protected View infoRtn;
        @BindView(R.id.driver_rtn)
        protected TextView driverRtn;
        @BindView(R.id.address_rtn)
        protected TextView addrRtn;

        @BindView(R.id.functions)
        protected View funs;
        @BindView(R.id.receive)
        protected TextView receive;
        @BindView(R.id.refuse)
        protected TextView refuse;
        @BindView(R.id.cancel)
        protected TextView cancel;
        @BindView(R.id.check)
        protected TextView check;
    }
}
