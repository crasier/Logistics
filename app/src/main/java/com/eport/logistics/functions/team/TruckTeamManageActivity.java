package com.eport.logistics.functions.team;

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
import com.eport.logistics.bean.Team;
import com.eport.logistics.server.WebRequest;
import com.eport.logistics.utils.MyToast;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 车队管理
 * */
public class TruckTeamManageActivity extends BaseActivity{


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

    private ArrayList<Team> teamsList;

    private int pageSize;//每页数据条数
    private int pageNum;//当前所在页
    private int pageTotal;//总页数
    private int itemTotal;//总条数

    private Team operatingTeam;

    private final int itemPerPage = 10;
    private String teamName = "";
    private String teamCode = "";
    private String contacter = "";
    private String chiAccount = "";
    private String accountName = "";
    private String cardID = "";
    private String nick = "";

    private TeamManageFloatingWindow floatingWindow;


    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.layout_team_manage_activity);
        unbinder = ButterKnife.bind(this);
        setTopBar(R.drawable.icon_back, getIntent().getStringExtra("menuName"), R.drawable.icon_add);

        mInflater = LayoutInflater.from(this);
        mAdapter = new Adapter();
        mLister.setAdapter(mAdapter);

        footView = mInflater.inflate(R.layout.view_text, null).findViewById(R.id.text);
        mLister.addFooterView(footView);

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

    private void refresh() {

        createDialog(false);

        if (teamsList == null) {
            teamsList = new ArrayList<>();
        }

        requestDataListFinish = false;

        getDataList(false);
    }

    /**
     * 加载更多
     * */
    private void loadMore() {
        if (!requestDataListFinish) {
            return;
        }
        requestDataListFinish = false;
        getDataList(true);
    }

    private boolean requestDataListFinish = false;

    private void refreshFinished() {
        if (isFinishing()) {
            return;
        }
        if (requestDataListFinish) {
            mRefresher.finishRefresh(true);
            dismissDialog();
            mAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(teamsList != null && teamsList.size() > 0 ? View.GONE : View.VISIBLE);
            if (teamsList == null || teamsList.size() == 0) {
                footView.setText("");
            }
        }
    }

    /**
     * 加载数据
     * @param add 获取后是更新还是添加
     * */
    private void getDataList(final boolean add) {

        WebRequest.getInstance().getTeamList(
                add ? pageNum + 1 : 1,
                itemPerPage,
                teamName,
                teamCode,
                contacter,
                chiAccount,
                accountName,
                cardID,
                nick,
                new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        createDialog(false);
                        requestDataListFinish = false;
                    }

                    @Override
                    public void onNext(JSONObject o) {
                        if (o == null) {
                            onError(new Throwable(getString(R.string.operation_failed)));
                            return;
                        }

                        if (!o.getBoolean("success")) {
                            onError(new Throwable(o.getString("failReason")));
                            return;
                        }

                        parseDataList(o, add);
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestDataListFinish = true;
                        refreshFinished();
                        MyToast.show(TruckTeamManageActivity.this,
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

        JSONArray dataArray = rootJson.getJSONObject("data").getJSONArray("list");

        pageTotal = rootJson.getJSONObject("data").getInteger("pages");
        pageNum = rootJson.getJSONObject("data").getInteger("pageNum");
        pageSize = rootJson.getJSONObject("data").getInteger("size");
        itemTotal = rootJson.getJSONObject("data").getInteger("total");

        if (add) {
            teamsList.addAll(JSON.parseArray(dataArray.toJSONString(), Team.class));
        }else {
            teamsList = (ArrayList<Team>) JSON.parseArray(dataArray.toJSONString(), Team.class);
        }

        for (int i = 0; i < teamsList.size(); i++) {
            if (operatingTeam != null && operatingTeam.getId().equals(teamsList.get(i).getId())) {
                teamsList.get(i).setSpread(operatingTeam.isSpread());
                break;
            }
        }

        Log.e(TAG, "parseDataList: "+ teamsList);
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
                addTeam();
                break;
        }
    }

    /**
     * 打开检索信息悬浮窗
     * */
    private void showCheckAction() {
        if (floatingWindow == null) {
            floatingWindow = new TeamManageFloatingWindow(this, mFloatingWindow);
            floatingWindow.setButtonClickListener(new TeamManageFloatingWindow.OnButtonClickListener() {
                @Override
                public void onCancelClick() {
                    floatingWindow.dismiss();
                    if (mFloatingBtn.getVisibility() != View.VISIBLE) {
                        mFloatingBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCheckClick(String name, String code, String contact, String phone, String account, String accountName, String card, String nick) {
                    teamName = name;
                    teamCode = code;
                    contacter = contact;
                    chiAccount = account;
                    TruckTeamManageActivity.this.accountName = accountName;
                    cardID = card;
                    TruckTeamManageActivity.this.nick = nick;

                    getDataList(false);
                }


                @Override
                public void onResetClick() {
                    teamName = "";
                    teamCode = "";
                    contacter = "";
                    chiAccount = "";
                    TruckTeamManageActivity.this.accountName = "";
                    cardID = "";
                    TruckTeamManageActivity.this.nick = "";

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
    private void onOperationClick(View view, final Team team, final int position) {

        operatingTeam = team;

        switch (view.getId()) {
            case R.id.team_modify:
                Log.e(TAG, "onOperationClick modify: "+ team);
                Intent modifyIntent = new Intent(this, TeamAddActivity.class);
                modifyIntent.putExtra("team", team);
                startActivityForResult(modifyIntent, Codes.CODE_REQUEST_TEAM);
                break;
            case R.id.team_operation:
                Log.e(TAG, "onOperationClick operation : "+ team);

                WebRequest.getInstance().modifyTeamUsable(team.getStaffId(),
                        team.getInuse().equals("1") ? 0 : 1,
                        new Observer<JSONObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                createDialog(false);
                            }

                            @Override
                            public void onNext(JSONObject o) {

                                dismissDialog();

                                if (o == null) {
                                    onError(new Throwable(getString(R.string.operation_failed)));
                                    return;
                                }

                                if (o.getBoolean("success")) {
                                    team.setInuse(team.getInuse().equals("1") ? "0" : "1");
                                    teamsList.set(position, team);
                                    mAdapter.notifyDataSetChanged();
                                }else {
                                    onError(new Throwable(o.getString("failReason")));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                dismissDialog();
                                MyToast.show(TruckTeamManageActivity.this,
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
    private void addTeam() {
        startActivityForResult(new Intent(this, TeamAddActivity.class), Codes.CODE_REQUEST_TEAM);
    }

    private void onItemClick(int position) {
        Team team = teamsList.get(position);
        team.setSpread(!team.isSpread());
        operatingTeam = team;
        teamsList.set(position, team);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Codes.CODE_REQUEST_TEAM) {
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
            return teamsList == null ? 0 : teamsList.size();
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
                convertView = mInflater.inflate(R.layout.item_team_manage_parent, null);
                holder = new ViewHolder();
                ButterKnife.bind(holder, convertView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position >= teamsList.size() - 1) {
                if (teamsList.size() < itemTotal) {
                    loadMore();
                    footView.setText(R.string.foot_load_more);
                }else {
                    footView.setText(R.string.foot_no_more);
                }
            }

            setTitle(holder, position);
            if (!teamsList.get(position).isSpread()) {
                holder.arrow.setRotation(0);
                holder.child.setVisibility(View.GONE);
                return convertView;
            }


            holder.arrow.setRotation(180);
            holder.child.setVisibility(View.VISIBLE);

            Team team = teamsList.get(position);
            holder.name.setText(TextUtils.isEmpty(team.getTypeName()) ? "" : team.getTypeName());
            holder.code.setText(TextUtils.isEmpty(team.getCode()) ? "" : team.getCode());
            holder.contact.setText(TextUtils.isEmpty(team.getContacter()) ? "" : team.getContacter());
            holder.label.setText(TextUtils.isEmpty(team.getTruckNo()) ? "" : team.getTruckNo());
            holder.account.setText(TextUtils.isEmpty(team.getChiAccount()) ? "" : team.getChiAccount());
            holder.nick.setText(TextUtils.isEmpty(team.getNick()) ? "" : team.getNick());
            holder.accName.setText(TextUtils.isEmpty(team.getAccount()) ? "" : team.getAccount());
            holder.phone.setText(TextUtils.isEmpty(team.getPhone()) ? "" : team.getPhone());
            holder.card.setText(TextUtils.isEmpty(team.getCardId()) ? "" : team.getCardId());

            setOperation(holder, team, position);

            return convertView;
        }

        private void setTitle(ViewHolder holder, final int position) {
            Team team = teamsList.get(position);
            holder.title.setText(String.format(Locale.CHINA,
                    "%s - %s - %s",
                    team.getTypeName() == null ? "" : team.getTypeName(),
                    team.getContacter() == null ? "" : team.getContacter(),
                    TextUtils.isEmpty(team.getInuse()) ? "" :
                team.getInuse().equals("1") ? getString(R.string.team_inuse) : getString(R.string.team_not_inuse)));
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
        private void setOperation(ViewHolder holder, final Team team, final int position) {

            boolean modify = false;
            boolean operation = false;

            if (team.getType().equals("1")) {
                modify = true;
            }else if (team.getType().equals("2")) {
                modify = true;
                operation = true;
            }else {

            }

            holder.modify.setVisibility(modify ? View.VISIBLE : View.GONE);
            holder.operation.setVisibility(operation ? View.VISIBLE : View.GONE);

            holder.operation.setText(team.getInuse() != null && team.getInuse().equals("1") ? R.string.stop : R.string.start);


            if (modify) {
                holder.modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, team, position);
                    }
                });
            }

            if (operation) {
                holder.operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onOperationClick(v, team, position);
                    }
                });
            }
        }

        public class ViewHolder {
            @BindView(R.id.order_top)
            protected View top;
            @BindView(R.id.order_main_name)
            protected TextView title;
            @BindView(R.id.order_main_arrow)
            protected ImageView arrow;
            @BindView(R.id.order_main_child)
            protected View child;

            @BindView(R.id.team_name)
            protected TextView name;
            @BindView(R.id.team_code)
            protected TextView code;
            @BindView(R.id.team_contact)
            protected TextView contact;
            @BindView(R.id.team_label_normal)
            protected TextView label;
            @BindView(R.id.team_account)
            protected TextView account;
            @BindView(R.id.team_nick)
            protected TextView nick;
            @BindView(R.id.team_account_name)
            protected TextView accName;
            @BindView(R.id.team_phone)
            protected TextView phone;
            @BindView(R.id.team_card)
            protected TextView card;
            @BindView(R.id.team_modify)
            protected TextView modify;
            @BindView(R.id.team_operation)
            protected TextView operation;
        }
    }
}
