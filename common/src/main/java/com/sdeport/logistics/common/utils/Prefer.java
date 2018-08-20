package com.sdeport.logistics.common.utils;

import android.content.Context;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.preference.Preference;

public class Prefer {

    private static final String NAME = "LOGISTICS";

    private Context mContext;
    private SharedPreferences prefer;
    private SharedPreferences.Editor editor;


    private static class LazyHolder {
        public static final Prefer INSTANCE = new Prefer();
    }

    private Prefer() {

    }

    public static Prefer getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(Context context) {
        this.mContext = context;
        prefer = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key, String defaultValue) {
        return prefer.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        editor = prefer.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
