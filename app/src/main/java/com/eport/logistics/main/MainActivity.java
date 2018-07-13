package com.eport.logistics.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eport.logistics.BaseActivity;
import com.eport.logistics.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends BaseActivity {

    private Unbinder unbinder;
    private HomeAdapter mAdapter;
    private FragmentManager fragmentManager;

    @BindView(R.id.main_info)
    protected TextView mInfo;
    @BindView(R.id.main_community)
    protected TextView mCommunity;
    @BindView(R.id.main_mine)
    protected TextView mMine;
    @BindView(R.id.main_pager)
    protected ViewPager mPager;

    private int iconIdArray[] = new int[] {
        R.drawable.icon_home_enable,
        R.drawable.icon_home_diable,
        R.drawable.icon_community_enable,
        R.drawable.icon_community_disable,
        R.drawable.icon_mine_enable,
        R.drawable.icon_mine_disable
    };

    private String tabNameArray[];
    private TextView tabs[];

    private HomeFragment homeFragment;
    private NewsFragment communityFragment;
    private MineFragment mineFragment;

    private int currentIndex;

    @Override
    protected void initUI(Bundle savedInstanceState) {

        tabNameArray = getResources().getStringArray(R.array.main_tab_name);
        homeFragment = new HomeFragment();
        communityFragment = new NewsFragment();
        mineFragment = new MineFragment();

        addContentView(R.layout.layout_activity_main);

        unbinder = ButterKnife.bind(this);

        tabs = new TextView[]{mInfo, mCommunity, mMine};

        fragmentManager = getSupportFragmentManager();

        mAdapter = new HomeAdapter(fragmentManager);
        mPager.setOffscreenPageLimit(2);

        mInfo.setOnClickListener(this);
        mCommunity.setOnClickListener(this);
        mMine.setOnClickListener(this);

        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(pageChangeListener);

        setTopBar(0, R.string.main_info, 0);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentIndex = position;
            mTitle.setText(tabNameArray[currentIndex]);
            changeUI();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE:
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_info:
                currentIndex = 0;
                mPager.setCurrentItem(currentIndex);
                changeUI();
                break;
            case R.id.main_community:
                currentIndex = 1;
                mPager.setCurrentItem(currentIndex);
                changeUI();
                break;
            case R.id.main_mine:
                currentIndex = 2;
                mPager.setCurrentItem(currentIndex);
                changeUI();
                break;
            case R.id.home_empty:
                currentIndex = 2;
                mPager.setCurrentItem(currentIndex);
                changeUI();
                break;
        }
    }

    private void changeUI() {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    iconIdArray[currentIndex == i ? i * 2 : i * 2 + 1],
                    0,
                    0);
            tabs[i].setTextColor(getResources().getColor(currentIndex == i ? R.color.colorPrimary : R.color.text_hint));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private class HomeAdapter extends FragmentPagerAdapter {

        public HomeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("pageAdapter", "getItem: position = "+position);
            switch (position) {
                case 0:
                    return homeFragment;
                case 1:
                    return communityFragment;
                case 2:
                    return mineFragment;
            }
            return null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0) {

        }
    }
}
