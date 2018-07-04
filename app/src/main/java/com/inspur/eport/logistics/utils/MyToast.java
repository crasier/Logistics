package com.inspur.eport.logistics.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class MyToast {

    private static Toast mToast;

    public static void show(Context ctx, int resId) {
        show(ctx, ctx.getString(resId));
    }

    public static void show(Context ctx, String resStr) {
        if (mToast == null) {
            mToast = Toast.makeText(ctx, resStr, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);//设置toast显示的位置，这是居中
            mToast.setDuration(Toast.LENGTH_SHORT);//设置toast显示的时长
        }else {
            mToast.setText(resStr);//设置文本
        }
        mToast.show();//展示toast
    }
}
