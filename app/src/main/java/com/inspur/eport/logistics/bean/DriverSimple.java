package com.inspur.eport.logistics.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 简版司机信息
 * */
public class DriverSimple implements Serializable {
    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "ACCOUNT_C_NAME")
    private String name;
    @JSONField(name = "TRUCK_NO")
    private String truck;

    @Override
    public String toString() {
        return "DriverSimple{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", truck='" + truck + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }
}
