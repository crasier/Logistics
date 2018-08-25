package com.sdeport.logistics.driver.constant;

public class Constants {

    public static boolean DEBUG = true;

    public static final String LOCAL_PREFER_NAME = "SDEPORT_LOGISTICS_DRIVER";//本地持久化文件名称

    public static final String KEY_PREFER_ACCOUNT = "key_prefer_account";
    public static final String KEY_PREFER_PASSWORD = "key_prefer_password";

    public static final String KEY_PREFER_FUTURE_REGISTER = "prefer_future_register";
    public static final String KEY_PREFER_FUTURE_RETRIEVE = "prefer_future_retrieve";

    public static final String KEY_ORDER_ING = "key_order_ing";//加入正在进行中的派车单
    public static final String KEY_ORDER_ID = "key_order_id";//派车单ID


    public static final String TAG_ORDER_OPERATION_ALL = "A";//同时操作提、还箱
    public static final String TAG_ORDER_OPERATION_T = "T";//操作提箱
    public static final String TAG_ORDER_OPERATION_R = "R";//操作还箱
}
