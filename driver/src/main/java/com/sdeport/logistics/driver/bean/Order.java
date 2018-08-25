package com.sdeport.logistics.driver.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class Order implements Serializable {

    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "BILL_NO")//提单号
    private String billNo;
    @JSONField(name = "CONSIGNEE_C_NAME")//收货人
    private String owner;
    @JSONField(name = "FORWARDER_NAME")//货代
    private String forwarder;
    @JSONField(name = "ADDR")//送货地址
    private String address;
    @JSONField(name = "DELIV_TIME")//送货时间
    private String delivTime;
    @JSONField(name = "CNTR_NO")//箱号
    private String cntrNo;
    @JSONField(name = "CNTR_SIZE_CODE")//集装箱尺寸
    private String cntrSize;
    @JSONField(name = "CNTR_TYPE_CODE")//箱型
    private String cntrType;
    @JSONField(name = "DRIVER_NAME")//提箱司机
    private String driver;
    @JSONField(name = "TRUCK_NO")//提箱车牌号
    private String truck;
    @JSONField(name = "DELIV_PLACE_NAME")//提箱地点
    private String delivPlace;
    @JSONField(name = "T_STATUS")//提箱状态
    private String tStatus;
    @JSONField(name = "RTN_DRIVER_NAME")//还箱司机
    private String driverRtn;
    @JSONField(name = "RTN_TRUCK_NO")//还箱车牌号
    private String truckRtn;
    @JSONField(name = "RTN_PLACE_NAME")//还箱地点
    private String delivPlaceRtn;
    @JSONField(name = "R_STATUS")//还箱状态
    private String rStatus;

    private boolean spread;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getForwarder() {
        return forwarder;
    }

    public void setForwarder(String forwarder) {
        this.forwarder = forwarder;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDelivTime() {
        return delivTime;
    }

    public void setDelivTime(String delivTime) {
        this.delivTime = delivTime;
    }

    public String getCntrNo() {
        return cntrNo;
    }

    public void setCntrNo(String cntrNo) {
        this.cntrNo = cntrNo;
    }

    public String getCntrSize() {
        return cntrSize;
    }

    public void setCntrSize(String cntrSize) {
        this.cntrSize = cntrSize;
    }

    public String getCntrType() {
        return cntrType;
    }

    public void setCntrType(String cntrType) {
        this.cntrType = cntrType;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getTruck() {
        return truck;
    }

    public void setTruck(String truck) {
        this.truck = truck;
    }

    public String getDelivPlace() {
        return delivPlace;
    }

    public void setDelivPlace(String delivPlace) {
        this.delivPlace = delivPlace;
    }

    public String gettStatus() {
        return tStatus;
    }

    public void settStatus(String tStatus) {
        this.tStatus = tStatus;
    }

    public String getDriverRtn() {
        return driverRtn;
    }

    public void setDriverRtn(String driverRtn) {
        this.driverRtn = driverRtn;
    }

    public String getTruckRtn() {
        return truckRtn;
    }

    public void setTruckRtn(String truckRtn) {
        this.truckRtn = truckRtn;
    }

    public String getDelivPlaceRtn() {
        return delivPlaceRtn;
    }

    public void setDelivPlaceRtn(String delivPlaceRtn) {
        this.delivPlaceRtn = delivPlaceRtn;
    }

    public String getrStatus() {
        return rStatus;
    }

    public void setrStatus(String rStatus) {
        this.rStatus = rStatus;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", billNo='" + billNo + '\'' +
                ", owner='" + owner + '\'' +
                ", forwarder='" + forwarder + '\'' +
                ", address='" + address + '\'' +
                ", delivTime='" + delivTime + '\'' +
                ", cntrNo='" + cntrNo + '\'' +
                ", cntrSize='" + cntrSize + '\'' +
                ", cntrType='" + cntrType + '\'' +
                ", driver='" + driver + '\'' +
                ", truck='" + truck + '\'' +
                ", delivPlace='" + delivPlace + '\'' +
                ", tStatus='" + tStatus + '\'' +
                ", driverRtn='" + driverRtn + '\'' +
                ", truckRtn='" + truckRtn + '\'' +
                ", delivPlaceRtn='" + delivPlaceRtn + '\'' +
                ", rStatus='" + rStatus + '\'' +
                ", spread=" + spread +
                '}';
    }
}

