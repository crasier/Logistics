package com.sdeport.logistics.driver.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdeport.logistics.common.widgets.Banner;
import com.sdeport.logistics.driver.BaseFragment;
import com.sdeport.logistics.driver.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @description 只保留一级菜单的主界面
 * @author Winfred Yin
 * @date 2018-08-15
 * */
public class HomeFragment extends BaseFragment {

    private final String TAG = "HomeFragment";

    @BindView(R.id.home_refresh)
    protected SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.home_header)
    protected MaterialHeader mHeader;
    @BindView(R.id.home_empty)
    protected TextView mEmpty;

    private Unbinder unbinder;
    private LayoutInflater mInflater;
    private MenuAdapter mAdapter;

    private static HomeFragment instance;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        instance = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_fragment_home, container, false);

        unbinder = ButterKnife.bind(this, contentView);
        mInflater = inflater;

        mRefreshLayout.setOnRefreshListener(onRefreshListener);
        mAdapter = new MenuAdapter();

        return contentView;
    }

    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
        }
    };

    private AbsListView.OnScrollListener onScrollListener =
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    switch (i) {
                        case SCROLL_STATE_IDLE:
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            };


    @Override
    public void onStart() {
        try {
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        try {
        }catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    public static HomeFragment getInstance() {
        return instance;
    }

    private void onItemClick(String title, Intent intent) {
        if (intent == null) {
            return;
        }
        intent.putExtra("menuName", title);
        startActivity(intent);
    }

    private class MenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
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
            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
