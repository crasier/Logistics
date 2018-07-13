package com.inspur.eport.logistics.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 派车单类
 * */
public class DispatchOrder implements Serializable{
    @JSONField(name = "ID")
    private String id;
    @JSONField(name = "CONSIGNEE_C_NAME")
    private String buyerCN;
    @JSONField(name = "CONSIGNEE_E_NAME")
    private String buyerEN;
    @JSONField(name = "CNTR_NO")
    private String containerNo;
    @JSONField(name = "CNTR_TYPE")
    private String containerType;
    @JSONField(name = "FORWARDER_NAME")
    private String forwarder;
    @JSONField(name = "TRUCK_NO")
    private String truckNo;
    @JSONField(name = "FLOW_STATUS")
    private String status;
    @JSONField(name = "RTN_TRUCK_NO")
    private String rtnTruckNo;
    @JSONField(name = "FK_FORWARDING_ID")
    private String forwardingId;
    @JSONField(name = "DELIV_PLACE_NAME")
    private String delivPlace;
    @JSONField(name = "RTN_PLACE_NAME")
    private String rtnPlace;
    @JSONField(name = "DRIVER_NAME")
    private String driver;
    @JSONField(name = "RTN_DRIVER_NAME")
    private String rtnDriver;
    @JSONField(name = "CNTR_SIZE")
    private String containerSize;
    @JSONField(name = "DELIV_TIME")
    private Long delivTime;
    @JSONField(name = "ORI_BACK")
    private String oriBack;
    @JSONField(name = "RTN_DRIVER_ID")
    private String rtnDriverId;
    @JSONField(name = "BILL_NO")
    private String billNo;
    @JSONField(name = "ADDR")
    private String address;
    @JSONField(name = "FO_FLOW_STATUS")
    private String foStatus;
    @JSONField(name = "ROW_ID")
    private String rowId;
    @JSONField(name = "FK_RECEIPT_ID")
    private String fkReceiptId;
    @JSONField(name = "RTN_APPOINT_TIME")
    private String appointTimeRtn;

    private String appointTimeGet;

    @JSONField(name = "TRANS_STATUS")//预约提箱
    private String transStatus;
    @JSONField(name = "TRANS_TIME")//预约提箱发送时间
    private Long transTime;
    @JSONField(name = "RTN_TRANS_STATUS")//预约还箱
    private String transStatusRtn;
    @JSONField(name = "RTN_TRANS_TIME")//预约还箱发送时间
    private Long transTimeRtn;

    private boolean spread;
    private boolean selected;

    @Override
    public String toString() {
        return "DispatchOrder{" +
                "id='" + id + '\'' +
                ", buyerCN='" + buyerCN + '\'' +
                ", buyerEN='" + buyerEN + '\'' +
                ", containerNo='" + containerNo + '\'' +
                ", containerType='" + containerType + '\'' +
                ", forwarder='" + forwarder + '\'' +
                ", truckNo='" + truckNo + '\'' +
                ", status='" + status + '\'' +
                ", rtnTruckNo='" + rtnTruckNo + '\'' +
                ", forwardingId='" + forwardingId + '\'' +
                ", delivPlace='" + delivPlace + '\'' +
                ", rtnPlace='" + rtnPlace + '\'' +
                ", driver='" + driver + '\'' +
                ", rtnDriver='" + rtnDriver + '\'' +
                ", containerSize='" + containerSize + '\'' +
                ", delivTime='" + delivTime + '\'' +
                ", oriBack='" + oriBack + '\'' +
                ", rtnDriverId='" + rtnDriverId + '\'' +
                ", billNo='" + billNo + '\'' +
                ", address='" + address + '\'' +
                ", foStatus='" + foStatus + '\'' +
                ", rowId='" + rowId + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerCN() {
        return buyerCN;
    }

    public void setBuyerCN(String buyerCN) {
        this.buyerCN = buyerCN;
    }

    public String getBuyerEN() {
        return buyerEN;
    }

    public void setBuyerEN(String buyerEN) {
        this.buyerEN = buyerEN;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getForwarder() {
        return forwarder;
    }

    public void setForwarder(String forwarder) {
        this.forwarder = forwarder;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRtnTruckNo() {
        return rtnTruckNo;
    }

    public void setRtnTruckNo(String rtnTruckNo) {
        this.rtnTruckNo = rtnTruckNo;
    }

    public String getForwardingId() {
        return forwardingId;
    }

    public void setForwardingId(String forwardingId) {
        this.forwardingId = forwardingId;
    }

    public String getDelivPlace() {
        return delivPlace;
    }

    public void setDelivPlace(String delivPlace) {
        this.delivPlace = delivPlace;
    }

    public String getRtnPlace() {
        return rtnPlace;
    }

    public void setRtnPlace(String rtnPlace) {
        this.rtnPlace = rtnPlace;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getRtnDriver() {
        return rtnDriver;
    }

    public void setRtnDriver(String rtnDriver) {
        this.rtnDriver = rtnDriver;
    }

    public String getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(String containerSize) {
        this.containerSize = containerSize;
    }

    public Long getDelivTime() {
        return delivTime;
    }

    public void setDelivTime(Long delivTime) {
        this.delivTime = delivTime;
    }

    public String getOriBack() {
        return oriBack;
    }

    public void setOriBack(String oriBack) {
        this.oriBack = oriBack;
    }

    public String getRtnDriverId() {
        return rtnDriverId;
    }

    public void setRtnDriverId(String rtnDriverId) {
        this.rtnDriverId = rtnDriverId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFoStatus() {
        return foStatus;
    }

    public void setFoStatus(String foStatus) {
        this.foStatus = foStatus;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public boolean isSpread() {
        return spread;
    }

    public String getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(String transStatus) {
        this.transStatus = transStatus;
    }

    public Long getTransTime() {
        return transTime;
    }

    public void setTransTime(Long transTime) {
        this.transTime = transTime;
    }

    public String getTransStatusRtn() {
        return transStatusRtn;
    }

    public void setTransStatusRtn(String transStatusRtn) {
        this.transStatusRtn = transStatusRtn;
    }

    public Long getTransTimeRtn() {
        return transTimeRtn;
    }

    public void setTransTimeRtn(Long transTimeRtn) {
        this.transTimeRtn = transTimeRtn;
    }

    public String getFkReceiptId() {
        return fkReceiptId;
    }

    public void setFkReceiptId(String fkReceiptId) {
        this.fkReceiptId = fkReceiptId;
    }

    public String getAppointTimeGet() {
        return appointTimeGet;
    }

    public void setAppointTimeGet(String appointTimeGet) {
        this.appointTimeGet = appointTimeGet;
    }

    public String getAppointTimeRtn() {
        return appointTimeRtn;
    }

    public void setAppointTimeRtn(String appointTimeRtn) {
        this.appointTimeRtn = appointTimeRtn;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * 是否可以派车
     * */
    public boolean isCanDispatch() {
        if (TextUtils.isEmpty(getStatus())) {
            return false;
        }
        return getStatus().equals(Dicts.STATUS_5520) || getStatus().equals(Dicts.STATUS_5550);
    }

    /**
     * 根据状态判断箱子是否可以还箱改派
     * */
    public boolean isCanDispatchReturn() {
        if (TextUtils.isEmpty(getStatus())) {
            return false;
        }

        return getStatus().equals(Dicts.STATUS_5600) || getStatus().equals(Dicts.STATUS_5650) ||
                getStatus().equals(Dicts.STATUS_5700)|| getStatus().equals(Dicts.STATUS_5700);
    }
}
