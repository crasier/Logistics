package com.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 简版卡车信息
 * */
public class TruckSimple implements Serializable {
    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "TRUCK_NO")
    private String truck;

    @Override
    public String toString() {
        return "TruckSimple{" +
                "id='" + id + '\'' +
                ", truck='" + truck + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }
}
