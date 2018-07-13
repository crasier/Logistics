package com.eport.logistics;

import android.app.Application;
import android.content.Context;

import com.eport.logistics.utils.Prefer;

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
        Prefer.getInstance().init(this);
    }
}
