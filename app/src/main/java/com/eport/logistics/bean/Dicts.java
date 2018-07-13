package com.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Locale;

/**
 * 订单状态码
 * */
public class Dicts implements Serializable{

    public static final String STATUS_5200 = "5200";//已委托
    public static final String STATUS_5300 = "5300";//已换单
    public static final String STATUS_5400 = "5400";//已押箱
    public static final String STATUS_5500 = "5500";//已派发待接单
    public static final String STATUS_5510 = "5510";//车队拒绝接单
    public static final String STATUS_5520 = "5520";//已接单待派车
    public static final String STATUS_5525 = "5525";//货代取消派发
    public static final String STATUS_5530 = "5530";//车队撤销接单
    public static final String STATUS_5550 = "5550";//派车中
    public static final String STATUS_5600 = "5600";//已派车
    public static final String STATUS_5650 = "5650";//提箱中
    public static final String STATUS_5700 = "5700";//已提箱
    public static final String STATUS_5750 = "5750";//还箱中
    public static final String STATUS_5800 = "5800";//已还箱
    public static final String STATUS_5900 = "5900";//已退押

    @JSONField(name = "value")
    private String value;
    @JSONField(name = "label")
    private String label;
    @JSONField(name = "add1")
    private String add1;
    @JSONField(name = "add2")
    private String add2;
    @JSONField(name = "add3")
    private String add3;

    @Override
    public String toString() {
        return String.format(Locale.CHINA,
                "value:%s,label:%s,add1:%s,add2:%s,add3:%s",
                value, label, add1, add2, add3);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAdd1() {
        return add1;
    }

    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    public String getAdd2() {
        return add2;
    }

    public void setAdd2(String add2) {
        this.add2 = add2;
    }

    public String getAdd3() {
        return add3;
    }

    public void setAdd3(String add3) {
        this.add3 = add3;
    }
}
