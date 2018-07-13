package com.eport.logistics.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eport.logistics.BaseFragment;
import com.eport.logistics.R;
import com.eport.logistics.bean.LogMenu;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HomeFragment extends BaseFragment {

    private final String TAG = "HomeFragment";

    @BindView(R.id.home_refresh)
    protected SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.home_header)
    protected MaterialHeader mHeader;
    @BindView(R.id.home_list)
    protected ListView mListView;
    @BindView(R.id.home_empty)
    protected TextView mEmpty;

    private Unbinder unbinder;
    private LayoutInflater mInflator;
    private HomeAdapter mAdapter;

    private static HomeFragment instance;

    private ArrayList<LogMenu> pItems;//一级菜单
    private LinkedHashMap<String, ArrayList<LogMenu>> cItems;

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
        mInflator = inflater;

        mRefreshLayout.setOnRefreshListener(onRefreshListener);
        mAdapter = new HomeAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(onScrollListener);

        getMenu();
        return contentView;
    }

    private OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh(RefreshLayout refreshLayout) {
            getMenu();
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


    /**
     * 获取菜单列表信息
     * */
    private void getMenu() {
        mActivity.createDialog(true);
        WebRequest.getInstance().getMenu(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: ");
            }

            @Override
            public void onNext(String data) {
                Log.e(TAG, "onNext: " + data);
                if (data == null) {
                    onError(new Throwable(getString(R.string.operation_failed)));
                    return;
                }
                mActivity.dismissDialog();
                mRefreshLayout.finishRefresh(true);
                parseData(data);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e);
                mActivity.dismissDialog();
                mRefreshLayout.finishRefresh(false);
                MyToast.show(mActivity, e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: ");
            }
        });
    }

    private void parseData(String data) {


        JSONObject rootData = null;
        try {
            rootData = JSON.parseObject(data);
        }catch (Exception e) {

        }

        if (rootData == null || rootData.getString("IsOk") == null || !rootData.getString("IsOk").equals("1")) {
            MyToast.show(mActivity, R.string.operation_failed);
            return;
        }
        JSONArray menus = rootData.getJSONObject("ReturnData").getJSONArray("menu");
        LinkedHashMap<String, LogMenu> filterMap = new LinkedHashMap<>();
        for (int i = 0; i < menus.size(); i++) {
            LogMenu item = JSON.parseObject(menus.get(i).toString(), LogMenu.class);
            filterMap.put(item.getIndex(), item);
        }

        if (pItems == null) {
            pItems = new ArrayList<>();
        }else {
            pItems.clear();
        }
        if (cItems == null) {
            cItems = new LinkedHashMap<>();
        }else {
            cItems.clear();
        }

        for (String key : filterMap.keySet()) {
            if (filterMap.containsKey(filterMap.get(key).getPid())) {//子菜单
                ArrayList<LogMenu> list;
                String pKey = filterMap.get(key).getPid();//对应的上级菜单id
                if (cItems.get(pKey) == null) {
                    list = new ArrayList<>();
                    list.add(filterMap.get(key));
                }else {
                    list = cItems.get(pKey);
                    if (list != null) {
                        list.add(filterMap.get(key));
                    }
                }
                cItems.put(pKey, list);
            }else {//主菜单
                pItems.add(filterMap.get(key));
            }
        }

        Log.e(TAG, "parseData: parent = "+pItems+"; child = "+cItems);
        mAdapter.notifyDataSetChanged();
    }

    private void onItemTitleClickListener(View view) {
        mAdapter.notifyDataSetChanged();
    }

    public static HomeFragment getInstance() {
        return instance;
    }

    private class HomeAdapter extends BaseAdapter {

        public HomeAdapter() {
            mInflator = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return pItems == null ? 0 : pItems.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            ViewHolder holder;

            if (view == null) {
                view = mInflator.inflate(R.layout.item_menu_parent, null);
                holder = new ViewHolder();
                holder.controller = new MenuController(mActivity, view, cItems.get(pItems.get(i).getIndex()));
                holder.title = view.findViewById(R.id.parent_title);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            holder.title.setText(pItems.get(i).getMenuName());
            holder.controller.setData(cItems.get(pItems.get(i).getIndex()));
            holder.controller.notifyDataSetChanged();
            return view;
        }

        private class ViewHolder {
            MenuController controller;
            TextView title;
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
