package com.sdeport.logistics.common.widgets;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.sdeport.logistics.common.R;
import com.sdeport.logistics.common.utils.LocalDisplay;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yinpeng on 2018/5/16.
 *
 * Show ads like a gallery
 *
 */

public class Banner extends RelativeLayout{

    private static final int CODE_HANDLE_NEXT = 0;
    private static final int CODE_HANDLE_PREVIOUS = 1;

    private int mCount;
    private int offScreenLimit = 2;
    private static final int cacheSize = Integer.MAX_VALUE;
    private int pageMargin = 0;
    private int scrollSpeed = 400;
    private ViewPager mPager;
    private SparseArray<ImageView> mViews;
    private long autoDuration = 4000;
    private PlayType mType = PlayType.NEXT;

    private boolean isRunning;
    private boolean canAutoNext = true;
    private boolean autoPlay = true;

    private Context mContext;

    private GlideImageLoader imageLoader;
    private ArrayList<String> mData;

    private onItemClickListener onItemClickListener;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    private Timer timer;

    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_HANDLE_NEXT:
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1 > cacheSize ? cacheSize : mPager.getCurrentItem() + 1, true);
                    break;
                case CODE_HANDLE_PREVIOUS:
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1 < 0 ? cacheSize / 2 : mPager.getCurrentItem() - 1, true);
                    break;
            }
            return false;
        }
    });

    public enum PlayType {
        NEXT,//下一个
        PREVIOUS//上一个
    }

    public Banner(Context context) {
        super(context);
        initAttrs(context);
    }

    public Banner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context);
    }

    public Banner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Banner(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context);
    }

    private Banner initAttrs(Context context) {
        this.mContext = context;
//        mViews = new ArrayList<>();
        mViews = new SparseArray<>();
        imageLoader = new GlideImageLoader();
        LayoutInflater.from(context).inflate(R.layout.layout_banner, this);
        mPager = (ViewPager) findViewById(R.id.banner_pager);
        mPager.setPageMargin(LocalDisplay.dp2px(pageMargin));
        mPager.setOffscreenPageLimit(offScreenLimit);
        mPager.setPageTransformer(true, new PageFormer(true, true));
        mPager.addOnPageChangeListener(changeListener);
        setScrollSpeed(scrollSpeed);
        return this;
    }

    private void setScrollSpeed(int duration) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mContext, new AccelerateDecelerateInterpolator());
            field.set(mPager, scroller);
            scroller.setmDuration(duration);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Banner setData(ArrayList<String> data) {
        this.mData = data;
        mCount = data == null ? 0 : mData.size();
        return this;
    }

    public Banner setMargin(int margin) {
        mPager.setPageMargin(LocalDisplay.dp2px(pageMargin));
        return this;
    }

    public void startPlay(boolean auto) {

        autoPlay = auto;

        if (isRunning) {
            refreshList();
            return;
        }
        isRunning = true;

        if (mPager.getAdapter() == null) {
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(mCount == 0 ? cacheSize / 2 : cacheSize / 2 - cacheSize / 2 % mCount, false);
        }else {
            mAdapter.notifyDataSetChanged();
        }

        if (auto) {
            timer = new Timer(true);
            timer.schedule(new AutoPlayTask(), autoDuration, autoDuration);
        }
    }

    public Banner setDuration(long time) {
        this.autoDuration = time;
        return this;
    }

    public void setPlayType(PlayType type) {
        this.mType = type;
    }

    public void pause() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    public void resume() {
        if (timer == null && autoPlay) {
            timer = new Timer(true);
            timer.schedule(new AutoPlayTask(), autoDuration, autoDuration);
        }
    }

    public void stopPlay() {
        Log.e("banner", "stopPlay");
        if (timer != null) {
            timer.cancel();
        }
        timer = null;

        isRunning = false;
    }

    private class AutoPlayTask extends TimerTask {

        @Override
        public void run() {
            if (canAutoNext) handler.sendEmptyMessage(mType.ordinal());
        }
    }

    private OnClickListener mOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, mPager.getCurrentItem() % mCount);
            }
        }
    };

    private ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageScrollStateChanged(state);
            }

            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE:
                    canAutoNext = true;
                    break;
                default:
                    canAutoNext = false;
                    break;
            }
        }
    };

    public Banner refreshList() {
        mAdapter.notifyDataSetChanged();
        return this;
    }

    public Banner setOnItemClickListener (onItemClickListener listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public Banner addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.onPageChangeListener = listener;
        return this;
    }

    private class PageFormer implements ViewPager.PageTransformer {

        public static final float MAX_SCALE = 1.0f;
        public static final float MIN_SCALE = 0.9f;
        public static final float MAX_ALPHA = 1.0f;
        public static final float MIN_ALPHA = 0.5f;

        private boolean alpha = true;
        private boolean scale = true;

        public PageFormer(boolean alpha, boolean scale) {
            this.alpha = alpha;
            this.scale = scale;
        }

        @Override
        public void transformPage(View page, float position) {

            if (position < -1) {
                position = -1;
            } else if (position > 1) {
                position = 1;
            }

            float tempScale = position < 0 ? 1 + position : 1 - position;

            if(scale){
                float slope = (MAX_SCALE - MIN_SCALE) / 1;
                //一个公式
                float scaleValue = MIN_SCALE + tempScale * slope;
//                page.setScaleX(scaleValue);
                page.setScaleY(scaleValue);
            }
            if(alpha){
                //模糊
                float alope = (MAX_ALPHA - MIN_ALPHA) / 1;
                float alphaValue = MIN_ALPHA + tempScale * alope;
                page.setAlpha(alphaValue);
            }
        }
    }

    private PagerAdapter mAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return cacheSize;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            int index = position % (offScreenLimit * 2 + 2);

            if (mViews.get(index) == null) {
                ImageView view = new ImageView(mContext);
                view.setOnClickListener(mOnClick);
                mViews.put(index, view);

                container.addView(mViews.get(index));
            }

            imageLoader.displayImage(mContext, mData.get(position % mCount), mViews.get(index));

            return mViews.get(index);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            int index = position % (offScreenLimit * 2 + 2);
//            Log.e("banner", "destroyItem: position = "+position+"; index = "+index+"; list = "+mViews+"; container.size "+container.getChildCount());
//            container.removeView(mViews.get(index));
        }
    };

    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 控制viewpager切换速度
     * */
    public class FixedSpeedScroller extends Scroller {
        private int mDuration = 1500;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public int getmDuration() {
            return mDuration;
        }

        public void setmDuration(int time) {
            mDuration = time;
        }
    }
}
