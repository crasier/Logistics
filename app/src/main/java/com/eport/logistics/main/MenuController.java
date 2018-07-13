package com.eport.logistics.main;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eport.logistics.R;
import com.eport.logistics.bean.LogMenu;
import com.eport.logistics.functions.order.OrderContainerActivity;
import com.eport.logistics.functions.dispatch.DispatchOrderManageActivity;
import com.eport.logistics.functions.driver.DriverManageActivity;
import com.eport.logistics.functions.status.TransportStateActivity;
import com.eport.logistics.functions.team.TruckTeamManageActivity;
import com.eport.logistics.functions.transport.TransportOrderManageActivity;
import com.eport.logistics.functions.truck.TruckManageActivity;
import com.eport.logistics.utils.ViewHolder;
import com.eport.logistics.widgets.ChildGridView;

import java.util.ArrayList;

public class MenuController {


    private static final String TAG = "MenuController";

    private Context mContext;
    private View mRootView;
    private ArrayList<LogMenu> mMenus;
    private LayoutInflater mInflater;

    private MenuAdapter mAdapter;

    private ChildGridView mGridView;


    public MenuController(Context context, View rootView, ArrayList<LogMenu> menus) {
        this.mContext = context;
        this.mRootView = rootView;
        this.mMenus = menus;
        mInflater = LayoutInflater.from(mContext);
        mGridView = rootView.findViewById(R.id.parent_gridview);
        mAdapter = new MenuAdapter();
        mGridView.setAdapter(mAdapter);
    }

    public void setData(ArrayList<LogMenu> menus) {
        mMenus = menus;
        Log.e(TAG, "setData: data = "+menus);
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private void onItemClick(String title, Intent intent) {
        if (intent == null) {
            return;
        }
        intent.putExtra("menuName", title);
        mContext.startActivity(intent);
    }

    private class MenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mMenus == null) {
                return 0;
            }

            if (mMenus.size() % mGridView.getNumColumns() == 0) {
                return mMenus.size();
            }else {
                return (mMenus.size() / mGridView.getNumColumns() + 1) * mGridView.getNumColumns();
            }
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
                convertView = mInflater.inflate(R.layout.item_menu_child, null);
            }

            if (position >= mMenus.size()) {
                setTitle(ViewHolder.<TextView> get(convertView, R.id.menu_name), null);
                setMenuIcon(ViewHolder.<ImageView> get(convertView, R.id.menu_icon), null);

                return convertView;
            }

            setTitle(ViewHolder.<TextView> get(convertView, R.id.menu_name), mMenus.get(position));
            setMenuIcon(ViewHolder.<ImageView> get(convertView, R.id.menu_icon), mMenus.get(position));
            return convertView;
        }

        private void setTitle(TextView view, LogMenu menu) {
            if (menu == null) {
                view.setText("");
                return;
            }

            view.setText(menu.getMenuName());
        }

        private void setMenuIcon(ImageView view, final LogMenu menu) {
            if (menu == null) {
                view.setImageDrawable(null);
                view.setBackgroundResource(0);
                view.setOnClickListener(null);
                return;
            }
            view.setBackgroundResource(R.drawable.bg_menu_item);
            final Intent intent;
            switch (menu.getMenuTag()) {
                case "logistics_yswtd":
                    view.setImageResource(R.drawable.icon_menu_wtd);
                    intent = new Intent(mContext, TransportOrderManageActivity.class);
                    break;
                case "logistics_trucker_dispatch":
                    view.setImageResource(R.drawable.icon_menu_pcd);
                    intent = new Intent(mContext, DispatchOrderManageActivity.class);
                    break;
                case "logistics_moto_user":
                    view.setImageResource(R.drawable.icon_menu_cd);
                    intent = new Intent(mContext, TruckTeamManageActivity.class);
                    break;
                case "logistics_moto_truck":
                    view.setImageResource(R.drawable.icon_menu_cl);
                    intent = new Intent(mContext, TruckManageActivity.class);
                    break;
                case "logistcis_driver":
                    view.setImageResource(R.drawable.icon_menu_sj);
                    intent = new Intent(mContext, DriverManageActivity.class);
                    break;
                case "logistics_moto_DisApp":
                    view.setImageResource(R.drawable.icon_menu_yy);
                    intent = new Intent(mContext, OrderContainerActivity.class);
                    break;
                case "logistics_com_trace":
                    view.setImageResource(R.drawable.icon_menu_wl);
                    intent = new Intent(mContext, TransportStateActivity.class);
                    break;
                default:
                    view.setImageResource(R.drawable.icon_menu_wtd);
                    intent = null;
                    break;
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(menu.getMenuName(), intent);
                }
            });
        }
    }
}
