package com.sdeport.logistics.common.utils;

import android.content.Context;
import android.util.Log;

import com.mic.etoast2.Toast;

public class MyToast {

    private static Toast mToast;

    public static void show(Context ctx, int resId) {
        show(ctx, ctx.getString(resId));
    }

    public static void show(Context ctx, String resStr) {
        try {
            mToast = Toast.makeText(ctx, resStr, android.widget.Toast.LENGTH_SHORT);
            mToast.show();
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("toast", "show failed reason = " + e);
        }
    }

    public static void cancel() {
        if (mToast != null) {
            try {
                mToast.cancel();
            }catch (Exception e) {
                Log.e("toast", "cancel failed reason = " + e);
            }
        }
    }
}
