package com.sdeport.logistics.driver;

import android.app.Application;
import android.content.Context;

import com.sdeport.logistics.common.utils.LocalDisplay;
import com.sdeport.logistics.common.utils.Prefer;
import com.sdeport.logistics.driver.constant.Constants;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initUtils();
    }

    private void initUtils() {
        Prefer.getInstance().init(this, Constants.LOCAL_PREFER_NAME);
        LocalDisplay.init(this);
    }
}
